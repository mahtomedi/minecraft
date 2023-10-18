package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.SectionBufferBuilderPool;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SectionRenderDispatcher {
    private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
    private final PriorityBlockingQueue<SectionRenderDispatcher.RenderSection.CompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
    private final Queue<SectionRenderDispatcher.RenderSection.CompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
    private int highPriorityQuota = 2;
    private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
    final SectionBufferBuilderPack fixedBuffers;
    private final SectionBufferBuilderPool bufferPool;
    private volatile int toBatchCount;
    private volatile boolean closed;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    ClientLevel level;
    final LevelRenderer renderer;
    private Vec3 camera = Vec3.ZERO;

    public SectionRenderDispatcher(ClientLevel param0, LevelRenderer param1, Executor param2, RenderBuffers param3) {
        this.level = param0;
        this.renderer = param1;
        this.fixedBuffers = param3.fixedBufferPack();
        this.bufferPool = param3.sectionBufferPool();
        this.executor = param2;
        this.mailbox = ProcessorMailbox.create(param2, "Section Renderer");
        this.mailbox.tell(this::runTask);
    }

    public void setLevel(ClientLevel param0) {
        this.level = param0;
    }

    private void runTask() {
        if (!this.closed && !this.bufferPool.isEmpty()) {
            SectionRenderDispatcher.RenderSection.CompileTask var0 = this.pollTask();
            if (var0 != null) {
                SectionBufferBuilderPack var1 = Objects.requireNonNull(this.bufferPool.acquire());
                this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(var0.name(), () -> var0.doTask(var1)), this.executor)
                    .thenCompose(param0x -> param0x)
                    .whenComplete((param1x, param2x) -> {
                        if (param2x != null) {
                            Minecraft.getInstance().delayCrash(CrashReport.forThrowable(param2x, "Batching sections"));
                        } else {
                            this.mailbox.tell(() -> {
                                if (param1x == SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL) {
                                    var1.clearAll();
                                } else {
                                    var1.discardAll();
                                }
    
                                this.bufferPool.release(var1);
                                this.runTask();
                            });
                        }
                    });
            }
        }
    }

    @Nullable
    private SectionRenderDispatcher.RenderSection.CompileTask pollTask() {
        if (this.highPriorityQuota <= 0) {
            SectionRenderDispatcher.RenderSection.CompileTask var0 = this.toBatchLowPriority.poll();
            if (var0 != null) {
                this.highPriorityQuota = 2;
                return var0;
            }
        }

        SectionRenderDispatcher.RenderSection.CompileTask var1 = this.toBatchHighPriority.poll();
        if (var1 != null) {
            --this.highPriorityQuota;
            return var1;
        } else {
            this.highPriorityQuota = 2;
            return this.toBatchLowPriority.poll();
        }
    }

    public String getStats() {
        return String.format(Locale.ROOT, "pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.bufferPool.getFreeBufferCount());
    }

    public int getToBatchCount() {
        return this.toBatchCount;
    }

    public int getToUpload() {
        return this.toUpload.size();
    }

    public int getFreeBufferCount() {
        return this.bufferPool.getFreeBufferCount();
    }

    public void setCamera(Vec3 param0) {
        this.camera = param0;
    }

    public Vec3 getCameraPosition() {
        return this.camera;
    }

    public void uploadAllPendingUploads() {
        Runnable var0;
        while((var0 = this.toUpload.poll()) != null) {
            var0.run();
        }

    }

    public void rebuildSectionSync(SectionRenderDispatcher.RenderSection param0, RenderRegionCache param1) {
        param0.compileSync(param1);
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
    }

    public void schedule(SectionRenderDispatcher.RenderSection.CompileTask param0) {
        if (!this.closed) {
            this.mailbox.tell(() -> {
                if (!this.closed) {
                    if (param0.isHighPriority) {
                        this.toBatchHighPriority.offer(param0);
                    } else {
                        this.toBatchLowPriority.offer(param0);
                    }

                    this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                    this.runTask();
                }
            });
        }
    }

    public CompletableFuture<Void> uploadSectionLayer(BufferBuilder.RenderedBuffer param0, VertexBuffer param1) {
        return this.closed ? CompletableFuture.completedFuture(null) : CompletableFuture.runAsync(() -> {
            if (param1.isInvalid()) {
                param0.release();
            } else {
                param1.bind();
                param1.upload(param0);
                VertexBuffer.unbind();
            }
        }, this.toUpload::add);
    }

    private void clearBatchQueue() {
        while(!this.toBatchHighPriority.isEmpty()) {
            SectionRenderDispatcher.RenderSection.CompileTask var0 = this.toBatchHighPriority.poll();
            if (var0 != null) {
                var0.cancel();
            }
        }

        while(!this.toBatchLowPriority.isEmpty()) {
            SectionRenderDispatcher.RenderSection.CompileTask var1 = this.toBatchLowPriority.poll();
            if (var1 != null) {
                var1.cancel();
            }
        }

        this.toBatchCount = 0;
    }

    public boolean isQueueEmpty() {
        return this.toBatchCount == 0 && this.toUpload.isEmpty();
    }

    public void dispose() {
        this.closed = true;
        this.clearBatchQueue();
        this.uploadAllPendingUploads();
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompiledSection {
        public static final SectionRenderDispatcher.CompiledSection UNCOMPILED = new SectionRenderDispatcher.CompiledSection() {
            @Override
            public boolean facesCanSeeEachother(Direction param0, Direction param1) {
                return false;
            }
        };
        final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
        final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
        VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        BufferBuilder.SortState transparencyState;

        public boolean hasNoRenderableLayers() {
            return this.hasBlocks.isEmpty();
        }

        public boolean isEmpty(RenderType param0) {
            return !this.hasBlocks.contains(param0);
        }

        public List<BlockEntity> getRenderableBlockEntities() {
            return this.renderableBlockEntities;
        }

        public boolean facesCanSeeEachother(Direction param0, Direction param1) {
            return this.visibilitySet.visibilityBetween(param0, param1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public class RenderSection {
        public static final int SIZE = 16;
        public final int index;
        public final AtomicReference<SectionRenderDispatcher.CompiledSection> compiled = new AtomicReference<>(
            SectionRenderDispatcher.CompiledSection.UNCOMPILED
        );
        final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
        @Nullable
        private SectionRenderDispatcher.RenderSection.RebuildTask lastRebuildTask;
        @Nullable
        private SectionRenderDispatcher.RenderSection.ResortTransparencyTask lastResortTransparencyTask;
        private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
        private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers()
            .stream()
            .collect(Collectors.toMap(param0x -> param0x, param0x -> new VertexBuffer(VertexBuffer.Usage.STATIC)));
        private AABB bb;
        private boolean dirty = true;
        final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
        private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], param0x -> {
            for(int var0 = 0; var0 < param0x.length; ++var0) {
                param0x[var0] = new BlockPos.MutableBlockPos();
            }

        });
        private boolean playerChanged;

        public RenderSection(int param1, int param2, int param3, int param4) {
            this.index = param1;
            this.setOrigin(param2, param3, param4);
        }

        private boolean doesChunkExistAt(BlockPos param0) {
            return SectionRenderDispatcher.this.level
                    .getChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()), ChunkStatus.FULL, false)
                != null;
        }

        public boolean hasAllNeighbors() {
            int var0 = 24;
            if (!(this.getDistToPlayerSqr() > 576.0)) {
                return true;
            } else {
                return this.doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()])
                    && this.doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()]);
            }
        }

        public AABB getBoundingBox() {
            return this.bb;
        }

        public VertexBuffer getBuffer(RenderType param0) {
            return this.buffers.get(param0);
        }

        public void setOrigin(int param0, int param1, int param2) {
            this.reset();
            this.origin.set(param0, param1, param2);
            this.bb = new AABB((double)param0, (double)param1, (double)param2, (double)(param0 + 16), (double)(param1 + 16), (double)(param2 + 16));

            for(Direction var0 : Direction.values()) {
                this.relativeOrigins[var0.ordinal()].set(this.origin).move(var0, 16);
            }

        }

        protected double getDistToPlayerSqr() {
            Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
            double var1 = this.bb.minX + 8.0 - var0.getPosition().x;
            double var2 = this.bb.minY + 8.0 - var0.getPosition().y;
            double var3 = this.bb.minZ + 8.0 - var0.getPosition().z;
            return var1 * var1 + var2 * var2 + var3 * var3;
        }

        void beginLayer(BufferBuilder param0) {
            param0.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        }

        public SectionRenderDispatcher.CompiledSection getCompiled() {
            return this.compiled.get();
        }

        private void reset() {
            this.cancelTasks();
            this.compiled.set(SectionRenderDispatcher.CompiledSection.UNCOMPILED);
            this.dirty = true;
        }

        public void releaseBuffers() {
            this.reset();
            this.buffers.values().forEach(VertexBuffer::close);
        }

        public BlockPos getOrigin() {
            return this.origin;
        }

        public void setDirty(boolean param0) {
            boolean var0 = this.dirty;
            this.dirty = true;
            this.playerChanged = param0 | (var0 && this.playerChanged);
        }

        public void setNotDirty() {
            this.dirty = false;
            this.playerChanged = false;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public boolean isDirtyFromPlayer() {
            return this.dirty && this.playerChanged;
        }

        public BlockPos getRelativeOrigin(Direction param0) {
            return this.relativeOrigins[param0.ordinal()];
        }

        public boolean resortTransparency(RenderType param0, SectionRenderDispatcher param1) {
            SectionRenderDispatcher.CompiledSection var0 = this.getCompiled();
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
            }

            if (!var0.hasBlocks.contains(param0)) {
                return false;
            } else {
                this.lastResortTransparencyTask = new SectionRenderDispatcher.RenderSection.ResortTransparencyTask(this.getDistToPlayerSqr(), var0);
                param1.schedule(this.lastResortTransparencyTask);
                return true;
            }
        }

        protected boolean cancelTasks() {
            boolean var0 = false;
            if (this.lastRebuildTask != null) {
                this.lastRebuildTask.cancel();
                this.lastRebuildTask = null;
                var0 = true;
            }

            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
                this.lastResortTransparencyTask = null;
            }

            return var0;
        }

        public SectionRenderDispatcher.RenderSection.CompileTask createCompileTask(RenderRegionCache param0) {
            boolean var0 = this.cancelTasks();
            BlockPos var1 = this.origin.immutable();
            int var2 = 1;
            RenderChunkRegion var3 = param0.createRegion(SectionRenderDispatcher.this.level, var1.offset(-1, -1, -1), var1.offset(16, 16, 16), 1);
            boolean var4 = this.compiled.get() == SectionRenderDispatcher.CompiledSection.UNCOMPILED;
            if (var4 && var0) {
                this.initialCompilationCancelCount.incrementAndGet();
            }

            this.lastRebuildTask = new SectionRenderDispatcher.RenderSection.RebuildTask(
                this.getDistToPlayerSqr(), var3, !var4 || this.initialCompilationCancelCount.get() > 2
            );
            return this.lastRebuildTask;
        }

        public void rebuildSectionAsync(SectionRenderDispatcher param0, RenderRegionCache param1) {
            SectionRenderDispatcher.RenderSection.CompileTask var0 = this.createCompileTask(param1);
            param0.schedule(var0);
        }

        void updateGlobalBlockEntities(Collection<BlockEntity> param0) {
            Set<BlockEntity> var0 = Sets.newHashSet(param0);
            Set<BlockEntity> var1;
            synchronized(this.globalBlockEntities) {
                var1 = Sets.newHashSet(this.globalBlockEntities);
                var0.removeAll(this.globalBlockEntities);
                var1.removeAll(param0);
                this.globalBlockEntities.clear();
                this.globalBlockEntities.addAll(param0);
            }

            SectionRenderDispatcher.this.renderer.updateGlobalBlockEntities(var1, var0);
        }

        public void compileSync(RenderRegionCache param0) {
            SectionRenderDispatcher.RenderSection.CompileTask var0 = this.createCompileTask(param0);
            var0.doTask(SectionRenderDispatcher.this.fixedBuffers);
        }

        public boolean isAxisAlignedWith(int param0, int param1, int param2) {
            BlockPos var0 = this.getOrigin();
            return param0 == SectionPos.blockToSectionCoord(var0.getX())
                || param2 == SectionPos.blockToSectionCoord(var0.getZ())
                || param1 == SectionPos.blockToSectionCoord(var0.getY());
        }

        @OnlyIn(Dist.CLIENT)
        abstract class CompileTask implements Comparable<SectionRenderDispatcher.RenderSection.CompileTask> {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
            protected final boolean isHighPriority;

            public CompileTask(double param0, boolean param1) {
                this.distAtCreation = param0;
                this.isHighPriority = param1;
            }

            public abstract CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack var1);

            public abstract void cancel();

            protected abstract String name();

            public int compareTo(SectionRenderDispatcher.RenderSection.CompileTask param0) {
                return Doubles.compare(this.distAtCreation, param0.distAtCreation);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RebuildTask extends SectionRenderDispatcher.RenderSection.CompileTask {
            @Nullable
            protected RenderChunkRegion region;

            public RebuildTask(@Nullable double param0, RenderChunkRegion param1, boolean param2) {
                super(param0, param2);
                this.region = param1;
            }

            @Override
            protected String name() {
                return "rend_chk_rebuild";
            }

            @Override
            public CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack param0) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (!RenderSection.this.hasAllNeighbors()) {
                    this.region = null;
                    RenderSection.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else {
                    Vec3 var0 = SectionRenderDispatcher.this.getCameraPosition();
                    float var1 = (float)var0.x;
                    float var2 = (float)var0.y;
                    float var3 = (float)var0.z;
                    SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults var4 = this.compile(var1, var2, var3, param0);
                    RenderSection.this.updateGlobalBlockEntities(var4.globalBlockEntities);
                    if (this.isCancelled.get()) {
                        var4.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                        return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                    } else {
                        SectionRenderDispatcher.CompiledSection var5 = new SectionRenderDispatcher.CompiledSection();
                        var5.visibilitySet = var4.visibilitySet;
                        var5.renderableBlockEntities.addAll(var4.blockEntities);
                        var5.transparencyState = var4.transparencyState;
                        List<CompletableFuture<Void>> var6 = Lists.newArrayList();
                        var4.renderedLayers.forEach((param2, param3) -> {
                            var6.add(SectionRenderDispatcher.this.uploadSectionLayer(param3, RenderSection.this.getBuffer(param2)));
                            var5.hasBlocks.add(param2);
                        });
                        return Util.sequenceFailFast(var6).handle((param1, param2) -> {
                            if (param2 != null && !(param2 instanceof CancellationException) && !(param2 instanceof InterruptedException)) {
                                Minecraft.getInstance().delayCrash(CrashReport.forThrowable(param2, "Rendering section"));
                            }

                            if (this.isCancelled.get()) {
                                return SectionRenderDispatcher.SectionTaskResult.CANCELLED;
                            } else {
                                RenderSection.this.compiled.set(var5);
                                RenderSection.this.initialCompilationCancelCount.set(0);
                                SectionRenderDispatcher.this.renderer.addRecentlyCompiledSection(RenderSection.this);
                                return SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
                            }
                        });
                    }
                }
            }

            private SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults compile(
                float param0, float param1, float param2, SectionBufferBuilderPack param3
            ) {
                SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults var0 = new SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults();
                int var1 = 1;
                BlockPos var2 = RenderSection.this.origin.immutable();
                BlockPos var3 = var2.offset(15, 15, 15);
                VisGraph var4 = new VisGraph();
                RenderChunkRegion var5 = this.region;
                this.region = null;
                PoseStack var6 = new PoseStack();
                if (var5 != null) {
                    ModelBlockRenderer.enableCaching();
                    Set<RenderType> var7 = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
                    RandomSource var8 = RandomSource.create();
                    BlockRenderDispatcher var9 = Minecraft.getInstance().getBlockRenderer();

                    for(BlockPos var10 : BlockPos.betweenClosed(var2, var3)) {
                        BlockState var11 = var5.getBlockState(var10);
                        if (var11.isSolidRender(var5, var10)) {
                            var4.setOpaque(var10);
                        }

                        if (var11.hasBlockEntity()) {
                            BlockEntity var12 = var5.getBlockEntity(var10);
                            if (var12 != null) {
                                this.handleBlockEntity(var0, var12);
                            }
                        }

                        FluidState var13 = var11.getFluidState();
                        if (!var13.isEmpty()) {
                            RenderType var14 = ItemBlockRenderTypes.getRenderLayer(var13);
                            BufferBuilder var15 = param3.builder(var14);
                            if (var7.add(var14)) {
                                RenderSection.this.beginLayer(var15);
                            }

                            var9.renderLiquid(var10, var5, var15, var11, var13);
                        }

                        if (var11.getRenderShape() != RenderShape.INVISIBLE) {
                            RenderType var16 = ItemBlockRenderTypes.getChunkRenderType(var11);
                            BufferBuilder var17 = param3.builder(var16);
                            if (var7.add(var16)) {
                                RenderSection.this.beginLayer(var17);
                            }

                            var6.pushPose();
                            var6.translate((float)(var10.getX() & 15), (float)(var10.getY() & 15), (float)(var10.getZ() & 15));
                            var9.renderBatched(var11, var10, var5, var6, var17, true, var8);
                            var6.popPose();
                        }
                    }

                    if (var7.contains(RenderType.translucent())) {
                        BufferBuilder var18 = param3.builder(RenderType.translucent());
                        if (!var18.isCurrentBatchEmpty()) {
                            var18.setQuadSorting(
                                VertexSorting.byDistance(param0 - (float)var2.getX(), param1 - (float)var2.getY(), param2 - (float)var2.getZ())
                            );
                            var0.transparencyState = var18.getSortState();
                        }
                    }

                    for(RenderType var19 : var7) {
                        BufferBuilder.RenderedBuffer var20 = param3.builder(var19).endOrDiscardIfEmpty();
                        if (var20 != null) {
                            var0.renderedLayers.put(var19, var20);
                        }
                    }

                    ModelBlockRenderer.clearCache();
                }

                var0.visibilitySet = var4.resolve();
                return var0;
            }

            private <E extends BlockEntity> void handleBlockEntity(SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults param0, E param1) {
                BlockEntityRenderer<E> var0 = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(param1);
                if (var0 != null) {
                    param0.blockEntities.add(param1);
                    if (var0.shouldRenderOffScreen(param1)) {
                        param0.globalBlockEntities.add(param1);
                    }
                }

            }

            @Override
            public void cancel() {
                this.region = null;
                if (this.isCancelled.compareAndSet(false, true)) {
                    RenderSection.this.setDirty(false);
                }

            }

            @OnlyIn(Dist.CLIENT)
            static final class CompileResults {
                public final List<BlockEntity> globalBlockEntities = new ArrayList<>();
                public final List<BlockEntity> blockEntities = new ArrayList<>();
                public final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
                public VisibilitySet visibilitySet = new VisibilitySet();
                @Nullable
                public BufferBuilder.SortState transparencyState;
            }
        }

        @OnlyIn(Dist.CLIENT)
        class ResortTransparencyTask extends SectionRenderDispatcher.RenderSection.CompileTask {
            private final SectionRenderDispatcher.CompiledSection compiledSection;

            public ResortTransparencyTask(double param0, SectionRenderDispatcher.CompiledSection param1) {
                super(param0, true);
                this.compiledSection = param1;
            }

            @Override
            protected String name() {
                return "rend_chk_sort";
            }

            @Override
            public CompletableFuture<SectionRenderDispatcher.SectionTaskResult> doTask(SectionBufferBuilderPack param0) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (!RenderSection.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                } else {
                    Vec3 var0 = SectionRenderDispatcher.this.getCameraPosition();
                    float var1 = (float)var0.x;
                    float var2 = (float)var0.y;
                    float var3 = (float)var0.z;
                    BufferBuilder.SortState var4 = this.compiledSection.transparencyState;
                    if (var4 != null && !this.compiledSection.isEmpty(RenderType.translucent())) {
                        BufferBuilder var5 = param0.builder(RenderType.translucent());
                        RenderSection.this.beginLayer(var5);
                        var5.restoreSortState(var4);
                        var5.setQuadSorting(
                            VertexSorting.byDistance(
                                var1 - (float)RenderSection.this.origin.getX(),
                                var2 - (float)RenderSection.this.origin.getY(),
                                var3 - (float)RenderSection.this.origin.getZ()
                            )
                        );
                        this.compiledSection.transparencyState = var5.getSortState();
                        BufferBuilder.RenderedBuffer var6 = var5.end();
                        if (this.isCancelled.get()) {
                            var6.release();
                            return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                        } else {
                            CompletableFuture<SectionRenderDispatcher.SectionTaskResult> var7 = SectionRenderDispatcher.this.uploadSectionLayer(
                                    var6, RenderSection.this.getBuffer(RenderType.translucent())
                                )
                                .thenApply(param0x -> SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                            return var7.handle(
                                (param0x, param1) -> {
                                    if (param1 != null && !(param1 instanceof CancellationException) && !(param1 instanceof InterruptedException)) {
                                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(param1, "Rendering section"));
                                    }
    
                                    return this.isCancelled.get()
                                        ? SectionRenderDispatcher.SectionTaskResult.CANCELLED
                                        : SectionRenderDispatcher.SectionTaskResult.SUCCESSFUL;
                                }
                            );
                        }
                    } else {
                        return CompletableFuture.completedFuture(SectionRenderDispatcher.SectionTaskResult.CANCELLED);
                    }
                }
            }

            @Override
            public void cancel() {
                this.isCancelled.set(true);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum SectionTaskResult {
        SUCCESSFUL,
        CANCELLED;
    }
}
