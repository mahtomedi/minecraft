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
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.List;
import java.util.Map;
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
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
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
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderDispatcher {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_WORKERS_32_BIT = 4;
    private static final VertexFormat VERTEX_FORMAT = DefaultVertexFormat.BLOCK;
    private static final int MAX_HIGH_PRIORITY_QUOTA = 2;
    private final PriorityBlockingQueue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> toBatchHighPriority = Queues.newPriorityBlockingQueue();
    private final Queue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> toBatchLowPriority = Queues.newLinkedBlockingDeque();
    private int highPriorityQuota = 2;
    private final Queue<ChunkBufferBuilderPack> freeBuffers;
    private final Queue<Runnable> toUpload = Queues.newConcurrentLinkedQueue();
    private volatile int toBatchCount;
    private volatile int freeBufferCount;
    final ChunkBufferBuilderPack fixedBuffers;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    ClientLevel level;
    final LevelRenderer renderer;
    private Vec3 camera = Vec3.ZERO;

    public ChunkRenderDispatcher(ClientLevel param0, LevelRenderer param1, Executor param2, boolean param3, ChunkBufferBuilderPack param4) {
        this.level = param0;
        this.renderer = param1;
        int var0 = Math.max(
            1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1
        );
        int var1 = Runtime.getRuntime().availableProcessors();
        int var2 = param3 ? var1 : Math.min(var1, 4);
        int var3 = Math.max(1, Math.min(var2, var0));
        this.fixedBuffers = param4;
        List<ChunkBufferBuilderPack> var4 = Lists.newArrayListWithExpectedSize(var3);

        try {
            for(int var5 = 0; var5 < var3; ++var5) {
                var4.add(new ChunkBufferBuilderPack());
            }
        } catch (OutOfMemoryError var14) {
            LOGGER.warn("Allocated only {}/{} buffers", var4.size(), var3);
            int var7 = Math.min(var4.size() * 2 / 3, var4.size() - 1);

            for(int var8 = 0; var8 < var7; ++var8) {
                var4.remove(var4.size() - 1);
            }

            System.gc();
        }

        this.freeBuffers = Queues.newArrayDeque(var4);
        this.freeBufferCount = this.freeBuffers.size();
        this.executor = param2;
        this.mailbox = ProcessorMailbox.create(param2, "Chunk Renderer");
        this.mailbox.tell(this::runTask);
    }

    public void setLevel(ClientLevel param0) {
        this.level = param0;
    }

    private void runTask() {
        if (!this.freeBuffers.isEmpty()) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0x = this.pollTask();
            if (var0x != null) {
                ChunkBufferBuilderPack var1x = this.freeBuffers.poll();
                this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
                this.freeBufferCount = this.freeBuffers.size();
                CompletableFuture.supplyAsync(Util.wrapThreadWithTaskName(var0x.name(), () -> var0x.doTask(var1x)), this.executor)
                    .thenCompose(param0x -> param0x)
                    .whenComplete((param1x, param2x) -> {
                        if (param2x != null) {
                            CrashReport var0xx = CrashReport.forThrowable(param2x, "Batching chunks");
                            Minecraft.getInstance().delayCrash(() -> Minecraft.getInstance().fillReport(var0xx));
                        } else {
                            this.mailbox.tell(() -> {
                                if (param1x == ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL) {
                                    var1x.clearAll();
                                } else {
                                    var1x.discardAll();
                                }
    
                                this.freeBuffers.add(var1x);
                                this.freeBufferCount = this.freeBuffers.size();
                                this.runTask();
                            });
                        }
                    });
            }
        }
    }

    @Nullable
    private ChunkRenderDispatcher.RenderChunk.ChunkCompileTask pollTask() {
        if (this.highPriorityQuota <= 0) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0 = this.toBatchLowPriority.poll();
            if (var0 != null) {
                this.highPriorityQuota = 2;
                return var0;
            }
        }

        ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var1 = this.toBatchHighPriority.poll();
        if (var1 != null) {
            --this.highPriorityQuota;
            return var1;
        } else {
            this.highPriorityQuota = 2;
            return this.toBatchLowPriority.poll();
        }
    }

    public String getStats() {
        return String.format("pC: %03d, pU: %02d, aB: %02d", this.toBatchCount, this.toUpload.size(), this.freeBufferCount);
    }

    public int getToBatchCount() {
        return this.toBatchCount;
    }

    public int getToUpload() {
        return this.toUpload.size();
    }

    public int getFreeBufferCount() {
        return this.freeBufferCount;
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

    public void rebuildChunkSync(ChunkRenderDispatcher.RenderChunk param0, RenderRegionCache param1) {
        param0.compileSync(param1);
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
    }

    public void schedule(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask param0) {
        this.mailbox.tell(() -> {
            if (param0.isHighPriority) {
                this.toBatchHighPriority.offer(param0);
            } else {
                this.toBatchLowPriority.offer(param0);
            }

            this.toBatchCount = this.toBatchHighPriority.size() + this.toBatchLowPriority.size();
            this.runTask();
        });
    }

    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder param0, VertexBuffer param1) {
        return CompletableFuture.runAsync(() -> {
        }, this.toUpload::add).thenCompose(param2 -> this.doUploadChunkLayer(param0, param1));
    }

    private CompletableFuture<Void> doUploadChunkLayer(BufferBuilder param0, VertexBuffer param1) {
        return param1.uploadLater(param0);
    }

    private void clearBatchQueue() {
        while(!this.toBatchHighPriority.isEmpty()) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0 = this.toBatchHighPriority.poll();
            if (var0 != null) {
                var0.cancel();
            }
        }

        while(!this.toBatchLowPriority.isEmpty()) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var1 = this.toBatchLowPriority.poll();
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
        this.clearBatchQueue();
        this.mailbox.close();
        this.freeBuffers.clear();
    }

    @OnlyIn(Dist.CLIENT)
    static enum ChunkTaskResult {
        SUCCESSFUL,
        CANCELLED;
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompiledChunk {
        public static final ChunkRenderDispatcher.CompiledChunk UNCOMPILED = new ChunkRenderDispatcher.CompiledChunk() {
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
    public class RenderChunk {
        public static final int SIZE = 16;
        public final int index;
        public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> compiled = new AtomicReference<>(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
        final AtomicInteger initialCompilationCancelCount = new AtomicInteger(0);
        @Nullable
        private ChunkRenderDispatcher.RenderChunk.RebuildTask lastRebuildTask;
        @Nullable
        private ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask lastResortTransparencyTask;
        private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
        private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers()
            .stream()
            .collect(Collectors.toMap(param0x -> param0x, param0x -> new VertexBuffer()));
        private AABB bb;
        private boolean dirty = true;
        final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
        private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], param0x -> {
            for(int var0 = 0; var0 < param0x.length; ++var0) {
                param0x[var0] = new BlockPos.MutableBlockPos();
            }

        });
        private boolean playerChanged;

        public RenderChunk(int param1, int param2, int param3, int param4) {
            this.index = param1;
            this.setOrigin(param2, param3, param4);
        }

        private boolean doesChunkExistAt(BlockPos param0) {
            return ChunkRenderDispatcher.this.level
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

        public ChunkRenderDispatcher.CompiledChunk getCompiledChunk() {
            return this.compiled.get();
        }

        private void reset() {
            this.cancelTasks();
            this.compiled.set(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
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

        public boolean resortTransparency(RenderType param0, ChunkRenderDispatcher param1) {
            ChunkRenderDispatcher.CompiledChunk var0 = this.getCompiledChunk();
            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
            }

            if (!var0.hasBlocks.contains(param0)) {
                return false;
            } else {
                this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(this.getDistToPlayerSqr(), var0);
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

        public ChunkRenderDispatcher.RenderChunk.ChunkCompileTask createCompileTask(RenderRegionCache param0) {
            boolean var0 = this.cancelTasks();
            BlockPos var1 = this.origin.immutable();
            int var2 = 1;
            RenderChunkRegion var3 = param0.createRegion(ChunkRenderDispatcher.this.level, var1.offset(-1, -1, -1), var1.offset(16, 16, 16), 1);
            boolean var4 = this.compiled.get() == ChunkRenderDispatcher.CompiledChunk.UNCOMPILED;
            if (var4 && var0) {
                this.initialCompilationCancelCount.incrementAndGet();
            }

            this.lastRebuildTask = new ChunkRenderDispatcher.RenderChunk.RebuildTask(
                this.getDistToPlayerSqr(), var3, !var4 || this.initialCompilationCancelCount.get() > 2
            );
            return this.lastRebuildTask;
        }

        public void rebuildChunkAsync(ChunkRenderDispatcher param0, RenderRegionCache param1) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0 = this.createCompileTask(param1);
            param0.schedule(var0);
        }

        void updateGlobalBlockEntities(Set<BlockEntity> param0) {
            Set<BlockEntity> var0 = Sets.newHashSet(param0);
            Set<BlockEntity> var1;
            synchronized(this.globalBlockEntities) {
                var1 = Sets.newHashSet(this.globalBlockEntities);
                var0.removeAll(this.globalBlockEntities);
                var1.removeAll(param0);
                this.globalBlockEntities.clear();
                this.globalBlockEntities.addAll(param0);
            }

            ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(var1, var0);
        }

        public void compileSync(RenderRegionCache param0) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0 = this.createCompileTask(param0);
            var0.doTask(ChunkRenderDispatcher.this.fixedBuffers);
        }

        @OnlyIn(Dist.CLIENT)
        abstract class ChunkCompileTask implements Comparable<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
            protected final boolean isHighPriority;

            public ChunkCompileTask(double param0, boolean param1) {
                this.distAtCreation = param0;
                this.isHighPriority = param1;
            }

            public abstract CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack var1);

            public abstract void cancel();

            protected abstract String name();

            public int compareTo(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask param0) {
                return Doubles.compare(this.distAtCreation, param0.distAtCreation);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RebuildTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
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
            public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack param0) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                } else if (!RenderChunk.this.hasAllNeighbors()) {
                    this.region = null;
                    RenderChunk.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                } else {
                    Vec3 var0 = ChunkRenderDispatcher.this.getCameraPosition();
                    float var1 = (float)var0.x;
                    float var2 = (float)var0.y;
                    float var3 = (float)var0.z;
                    ChunkRenderDispatcher.CompiledChunk var4 = new ChunkRenderDispatcher.CompiledChunk();
                    Set<BlockEntity> var5 = this.compile(var1, var2, var3, var4, param0);
                    RenderChunk.this.updateGlobalBlockEntities(var5);
                    if (this.isCancelled.get()) {
                        return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                    } else {
                        List<CompletableFuture<Void>> var6 = Lists.newArrayList();
                        var4.hasBlocks
                            .forEach(
                                param2 -> var6.add(ChunkRenderDispatcher.this.uploadChunkLayer(param0.builder(param2), RenderChunk.this.getBuffer(param2)))
                            );
                        return Util.sequenceFailFast(var6).handle((param1, param2) -> {
                            if (param2 != null && !(param2 instanceof CancellationException) && !(param2 instanceof InterruptedException)) {
                                CrashReport var0x = CrashReport.forThrowable(param2, "Rendering chunk");
                                Minecraft.getInstance().delayCrash(() -> var0x);
                            }

                            if (this.isCancelled.get()) {
                                return ChunkRenderDispatcher.ChunkTaskResult.CANCELLED;
                            } else {
                                RenderChunk.this.compiled.set(var4);
                                RenderChunk.this.initialCompilationCancelCount.set(0);
                                ChunkRenderDispatcher.this.renderer.addRecentlyCompiledChunk(RenderChunk.this);
                                return ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                            }
                        });
                    }
                }
            }

            private Set<BlockEntity> compile(
                float param0, float param1, float param2, ChunkRenderDispatcher.CompiledChunk param3, ChunkBufferBuilderPack param4
            ) {
                int var0 = 1;
                BlockPos var1 = RenderChunk.this.origin.immutable();
                BlockPos var2 = var1.offset(15, 15, 15);
                VisGraph var3 = new VisGraph();
                Set<BlockEntity> var4 = Sets.newHashSet();
                RenderChunkRegion var5 = this.region;
                this.region = null;
                PoseStack var6 = new PoseStack();
                if (var5 != null) {
                    ModelBlockRenderer.enableCaching();
                    Set<RenderType> var7 = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());
                    RandomSource var8 = RandomSource.create();
                    BlockRenderDispatcher var9 = Minecraft.getInstance().getBlockRenderer();

                    for(BlockPos var10 : BlockPos.betweenClosed(var1, var2)) {
                        BlockState var11 = var5.getBlockState(var10);
                        if (var11.isSolidRender(var5, var10)) {
                            var3.setOpaque(var10);
                        }

                        if (var11.hasBlockEntity()) {
                            BlockEntity var12 = var5.getBlockEntity(var10);
                            if (var12 != null) {
                                this.handleBlockEntity(param3, var4, var12);
                            }
                        }

                        BlockState var13 = var5.getBlockState(var10);
                        FluidState var14 = var13.getFluidState();
                        if (!var14.isEmpty()) {
                            RenderType var15 = ItemBlockRenderTypes.getRenderLayer(var14);
                            BufferBuilder var16 = param4.builder(var15);
                            if (var7.add(var15)) {
                                RenderChunk.this.beginLayer(var16);
                            }

                            if (var9.renderLiquid(var10, var5, var16, var13, var14)) {
                                param3.hasBlocks.add(var15);
                            }
                        }

                        if (var11.getRenderShape() != RenderShape.INVISIBLE) {
                            RenderType var17 = ItemBlockRenderTypes.getChunkRenderType(var11);
                            BufferBuilder var18 = param4.builder(var17);
                            if (var7.add(var17)) {
                                RenderChunk.this.beginLayer(var18);
                            }

                            var6.pushPose();
                            var6.translate((double)(var10.getX() & 15), (double)(var10.getY() & 15), (double)(var10.getZ() & 15));
                            if (var9.renderBatched(var11, var10, var5, var6, var18, true, var8)) {
                                param3.hasBlocks.add(var17);
                            }

                            var6.popPose();
                        }
                    }

                    if (param3.hasBlocks.contains(RenderType.translucent())) {
                        BufferBuilder var19 = param4.builder(RenderType.translucent());
                        var19.setQuadSortOrigin(param0 - (float)var1.getX(), param1 - (float)var1.getY(), param2 - (float)var1.getZ());
                        param3.transparencyState = var19.getSortState();
                    }

                    for(RenderType var20 : var7) {
                        param4.builder(var20).end();
                    }

                    ModelBlockRenderer.clearCache();
                }

                param3.visibilitySet = var3.resolve();
                return var4;
            }

            private <E extends BlockEntity> void handleBlockEntity(ChunkRenderDispatcher.CompiledChunk param0, Set<BlockEntity> param1, E param2) {
                BlockEntityRenderer<E> var0 = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(param2);
                if (var0 != null) {
                    param0.renderableBlockEntities.add(param2);
                    if (var0.shouldRenderOffScreen(param2)) {
                        param1.add(param2);
                    }
                }

            }

            @Override
            public void cancel() {
                this.region = null;
                if (this.isCancelled.compareAndSet(false, true)) {
                    RenderChunk.this.setDirty(false);
                }

            }
        }

        @OnlyIn(Dist.CLIENT)
        class ResortTransparencyTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
            private final ChunkRenderDispatcher.CompiledChunk compiledChunk;

            public ResortTransparencyTask(double param0, ChunkRenderDispatcher.CompiledChunk param1) {
                super(param0, true);
                this.compiledChunk = param1;
            }

            @Override
            protected String name() {
                return "rend_chk_sort";
            }

            @Override
            public CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> doTask(ChunkBufferBuilderPack param0) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                } else if (!RenderChunk.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                } else {
                    Vec3 var0 = ChunkRenderDispatcher.this.getCameraPosition();
                    float var1 = (float)var0.x;
                    float var2 = (float)var0.y;
                    float var3 = (float)var0.z;
                    BufferBuilder.SortState var4 = this.compiledChunk.transparencyState;
                    if (var4 != null && !this.compiledChunk.isEmpty(RenderType.translucent())) {
                        BufferBuilder var5 = param0.builder(RenderType.translucent());
                        RenderChunk.this.beginLayer(var5);
                        var5.restoreSortState(var4);
                        var5.setQuadSortOrigin(
                            var1 - (float)RenderChunk.this.origin.getX(),
                            var2 - (float)RenderChunk.this.origin.getY(),
                            var3 - (float)RenderChunk.this.origin.getZ()
                        );
                        this.compiledChunk.transparencyState = var5.getSortState();
                        var5.end();
                        if (this.isCancelled.get()) {
                            return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                        } else {
                            CompletableFuture<ChunkRenderDispatcher.ChunkTaskResult> var6 = ChunkRenderDispatcher.this.uploadChunkLayer(
                                    param0.builder(RenderType.translucent()), RenderChunk.this.getBuffer(RenderType.translucent())
                                )
                                .thenApply(param0x -> ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                            return var6.handle(
                                (param0x, param1) -> {
                                    if (param1 != null && !(param1 instanceof CancellationException) && !(param1 instanceof InterruptedException)) {
                                        CrashReport var0x = CrashReport.forThrowable(param1, "Rendering chunk");
                                        Minecraft.getInstance().delayCrash(() -> var0x);
                                    }
    
                                    return this.isCancelled.get()
                                        ? ChunkRenderDispatcher.ChunkTaskResult.CANCELLED
                                        : ChunkRenderDispatcher.ChunkTaskResult.SUCCESSFUL;
                                }
                            );
                        }
                    } else {
                        return CompletableFuture.completedFuture(ChunkRenderDispatcher.ChunkTaskResult.CANCELLED);
                    }
                }
            }

            @Override
            public void cancel() {
                this.isCancelled.set(true);
            }
        }
    }
}
