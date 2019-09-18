package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Doubles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.ProcessorMailbox;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ChunkRenderDispatcher {
    private static final Logger LOGGER = LogManager.getLogger();
    private final PriorityBlockingQueue<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> chunksToBatch = Queues.newPriorityBlockingQueue();
    private final Queue<ChunkBufferBuilderPack> availableChunkBuffers;
    private final Queue<Runnable> pendingUploads = Queues.newConcurrentLinkedQueue();
    private final ChunkBufferBuilderPack fixedBuffers;
    private final ProcessorMailbox<Runnable> mailbox;
    private final Executor executor;
    private Level level;
    private final LevelRenderer renderer;
    private Vec3 camera = Vec3.ZERO;

    public ChunkRenderDispatcher(Level param0, LevelRenderer param1, Executor param2, boolean param3) {
        this.level = param0;
        this.renderer = param1;
        int var0 = Math.max(
            1, (int)((double)Runtime.getRuntime().maxMemory() * 0.3) / (RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum() * 4) - 1
        );
        int var1 = Runtime.getRuntime().availableProcessors();
        int var2 = param3 ? var1 : Math.min(var1, 4);
        int var3 = Math.max(1, Math.min(var2, var0));
        this.fixedBuffers = new ChunkBufferBuilderPack();
        List<ChunkBufferBuilderPack> var4 = Lists.newArrayListWithExpectedSize(var3);

        try {
            for(int var5 = 0; var5 < var3; ++var5) {
                var4.add(new ChunkBufferBuilderPack());
            }
        } catch (OutOfMemoryError var13) {
            LOGGER.warn("Allocated only {}/{} buffers", var4.size(), var3);
            int var7 = Math.min(var4.size() * 2 / 3, var4.size() - 1);

            for(int var8 = 0; var8 < var7; ++var8) {
                var4.remove(var4.size() - 1);
            }

            System.gc();
        }

        this.availableChunkBuffers = Queues.newArrayDeque(var4);
        this.executor = param2;
        this.mailbox = ProcessorMailbox.create(param2, "Chunk Renderer");
        this.mailbox.tell(this::runTask);
    }

    public void setLevel(Level param0) {
        this.level = param0;
    }

    private void runTask() {
        if (!this.availableChunkBuffers.isEmpty()) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0x = this.chunksToBatch.poll();
            if (var0x != null) {
                ChunkBufferBuilderPack var1x = this.availableChunkBuffers.poll();
                CompletableFuture.runAsync(() -> {
                }, this.executor).thenCompose(param2x -> var0x.doTask(var1x)).whenComplete((param1x, param2x) -> {
                    this.mailbox.tell(() -> {
                        var1x.clearAll();
                        this.availableChunkBuffers.add(var1x);
                    });
                    this.mailbox.tell(this::runTask);
                    if (param2x != null) {
                        CrashReport var0xx = CrashReport.forThrowable(param2x, "Batching chunks");
                        Minecraft.getInstance().delayCrash(Minecraft.getInstance().fillReport(var0xx));
                    }

                });
            }
        }
    }

    public String getStats() {
        return String.format("pC: %03d, pU: %02d, aB: %02d", this.chunksToBatch.size(), this.pendingUploads.size(), this.availableChunkBuffers.size());
    }

    public void setCamera(Vec3 param0) {
        this.camera = param0;
    }

    public Vec3 getCameraPosition() {
        return this.camera;
    }

    public boolean uploadAllPendingUploads() {
        boolean var0;
        Runnable var1;
        for(var0 = false; (var1 = this.pendingUploads.poll()) != null; var0 = true) {
            var1.run();
        }

        return var0;
    }

    public void rebuildChunkSync(ChunkRenderDispatcher.RenderChunk param0) {
        param0.compileSync();
    }

    public void blockUntilClear() {
        this.clearBatchQueue();
    }

    public void schedule(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask param0) {
        this.chunksToBatch.offer(param0);
        this.mailbox.tell(this::runTask);
    }

    public CompletableFuture<Void> uploadChunkLayer(BufferBuilder param0, VertexBuffer param1) {
        return Minecraft.getInstance().submit(() -> {
        }).thenCompose(param2 -> this.doUploadChunkLayer(param0, param1));
    }

    private CompletableFuture<Void> doUploadChunkLayer(BufferBuilder param0, VertexBuffer param1) {
        return param1.uploadLater(param0);
    }

    private void clearBatchQueue() {
        while(!this.chunksToBatch.isEmpty()) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0 = this.chunksToBatch.poll();
            if (var0 != null) {
                var0.cancel();
            }
        }

    }

    public boolean isQueueEmpty() {
        return this.chunksToBatch.isEmpty() && this.pendingUploads.isEmpty();
    }

    public void dispose() {
        this.clearBatchQueue();
        this.mailbox.close();
        this.availableChunkBuffers.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public static class CompiledChunk {
        public static final ChunkRenderDispatcher.CompiledChunk UNCOMPILED = new ChunkRenderDispatcher.CompiledChunk() {
            @Override
            public boolean facesCanSeeEachother(Direction param0, Direction param1) {
                return false;
            }
        };
        private final Set<RenderType> hasBlocks = Sets.newHashSet();
        private final Set<RenderType> hasLayer = Sets.newHashSet();
        private boolean isCompletelyEmpty = true;
        private final List<BlockEntity> renderableBlockEntities = Lists.newArrayList();
        private VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        private BufferBuilder.State transparencyState;

        public boolean hasNoRenderableLayers() {
            return this.isCompletelyEmpty;
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
        public final AtomicReference<ChunkRenderDispatcher.CompiledChunk> compiled = new AtomicReference<>(ChunkRenderDispatcher.CompiledChunk.UNCOMPILED);
        @Nullable
        private ChunkRenderDispatcher.RenderChunk.RebuildTask lastRebuildTask;
        @Nullable
        private ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask lastResortTransparencyTask;
        private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
        private final Map<RenderType, VertexBuffer> buffers = RenderType.chunkBufferLayers()
            .stream()
            .collect(Collectors.toMap(param0x -> param0x, param0x -> new VertexBuffer(DefaultVertexFormat.BLOCK)));
        public AABB bb;
        private int lastFrame = -1;
        private boolean dirty = true;
        private final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
        private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], param0x -> {
            for(int var0 = 0; var0 < param0x.length; ++var0) {
                param0x[var0] = new BlockPos.MutableBlockPos();
            }

        });
        private boolean playerChanged;

        private boolean doesChunkExistAt(BlockPos param0) {
            return !ChunkRenderDispatcher.this.level.getChunk(param0.getX() >> 4, param0.getZ() >> 4).isEmpty();
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

        public boolean setFrame(int param0) {
            if (this.lastFrame == param0) {
                return false;
            } else {
                this.lastFrame = param0;
                return true;
            }
        }

        public VertexBuffer getBuffer(RenderType param0) {
            return this.buffers.get(param0);
        }

        public void setOrigin(int param0, int param1, int param2) {
            if (param0 != this.origin.getX() || param1 != this.origin.getY() || param2 != this.origin.getZ()) {
                this.reset();
                this.origin.set(param0, param1, param2);
                this.bb = new AABB((double)param0, (double)param1, (double)param2, (double)(param0 + 16), (double)(param1 + 16), (double)(param2 + 16));

                for(Direction var0 : Direction.values()) {
                    this.relativeOrigins[var0.ordinal()].set(this.origin).move(var0, 16);
                }

            }
        }

        protected double getDistToPlayerSqr() {
            Camera var0 = Minecraft.getInstance().gameRenderer.getMainCamera();
            double var1 = this.bb.minX + 8.0 - var0.getPosition().x;
            double var2 = this.bb.minY + 8.0 - var0.getPosition().y;
            double var3 = this.bb.minZ + 8.0 - var0.getPosition().z;
            return var1 * var1 + var2 * var2 + var3 * var3;
        }

        private void beginLayer(BufferBuilder param0, BlockPos param1) {
            param0.begin(7, DefaultVertexFormat.BLOCK);
            param0.offset((double)(-param1.getX()), (double)(-param1.getY()), (double)(-param1.getZ()));
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
            this.buffers.values().forEach(VertexBuffer::delete);
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

            if (!var0.hasLayer.contains(param0)) {
                return false;
            } else {
                this.lastResortTransparencyTask = new ChunkRenderDispatcher.RenderChunk.ResortTransparencyTask(this.getDistToPlayerSqr(), var0);
                param1.schedule(this.lastResortTransparencyTask);
                return true;
            }
        }

        protected void cancelTasks() {
            if (this.lastRebuildTask != null) {
                this.lastRebuildTask.cancel();
                this.lastRebuildTask = null;
            }

            if (this.lastResortTransparencyTask != null) {
                this.lastResortTransparencyTask.cancel();
                this.lastResortTransparencyTask = null;
            }

        }

        public ChunkRenderDispatcher.RenderChunk.ChunkCompileTask createCompileTask() {
            this.cancelTasks();
            BlockPos var0 = this.origin.immutable();
            int var1 = 1;
            RenderChunkRegion var2 = RenderChunkRegion.createIfNotEmpty(ChunkRenderDispatcher.this.level, var0.offset(-1, -1, -1), var0.offset(16, 16, 16), 1);
            this.lastRebuildTask = new ChunkRenderDispatcher.RenderChunk.RebuildTask(this.getDistToPlayerSqr(), var2);
            return this.lastRebuildTask;
        }

        public void rebuildChunkAsync(ChunkRenderDispatcher param0) {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0 = this.createCompileTask();
            param0.schedule(var0);
        }

        private void updateGlobalBlockEntities(Set<BlockEntity> param0) {
            Set<BlockEntity> var0 = Sets.newHashSet(param0);
            Set<BlockEntity> var1 = Sets.newHashSet(this.globalBlockEntities);
            var0.removeAll(this.globalBlockEntities);
            var1.removeAll(param0);
            this.globalBlockEntities.clear();
            this.globalBlockEntities.addAll(param0);
            ChunkRenderDispatcher.this.renderer.updateGlobalBlockEntities(var1, var0);
        }

        public void compileSync() {
            ChunkRenderDispatcher.RenderChunk.ChunkCompileTask var0 = this.createCompileTask();
            var0.doTask(ChunkRenderDispatcher.this.fixedBuffers);
        }

        @OnlyIn(Dist.CLIENT)
        abstract class ChunkCompileTask implements Comparable<ChunkRenderDispatcher.RenderChunk.ChunkCompileTask> {
            protected final double distAtCreation;
            protected final AtomicBoolean isCancelled = new AtomicBoolean(false);

            public ChunkCompileTask(double param0) {
                this.distAtCreation = param0;
            }

            public abstract CompletableFuture<Unit> doTask(ChunkBufferBuilderPack var1);

            public abstract void cancel();

            public int compareTo(ChunkRenderDispatcher.RenderChunk.ChunkCompileTask param0) {
                return Doubles.compare(this.distAtCreation, param0.distAtCreation);
            }
        }

        @OnlyIn(Dist.CLIENT)
        class RebuildTask extends ChunkRenderDispatcher.RenderChunk.ChunkCompileTask {
            @Nullable
            protected RenderChunkRegion region;

            public RebuildTask(double param0, @Nullable RenderChunkRegion param1) {
                super(param0);
                this.region = param1;
            }

            @Override
            public CompletableFuture<Unit> doTask(ChunkBufferBuilderPack param0) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                } else if (!RenderChunk.this.hasAllNeighbors()) {
                    this.region = null;
                    RenderChunk.this.setDirty(false);
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                } else {
                    Vec3 var0 = ChunkRenderDispatcher.this.getCameraPosition();
                    float var1 = (float)var0.x;
                    float var2 = (float)var0.y;
                    float var3 = (float)var0.z;
                    ChunkRenderDispatcher.CompiledChunk var4 = new ChunkRenderDispatcher.CompiledChunk();
                    Set<BlockEntity> var5 = this.compile(var1, var2, var3, var4, param0);
                    RenderChunk.this.updateGlobalBlockEntities(var5);
                    if (this.isCancelled.get()) {
                        return CompletableFuture.completedFuture(Unit.INSTANCE);
                    } else {
                        List<CompletableFuture<Void>> var6 = Lists.newArrayList();
                        var4.hasLayer
                            .forEach(
                                param2 -> var6.add(ChunkRenderDispatcher.this.uploadChunkLayer(param0.builder(param2), RenderChunk.this.getBuffer(param2)))
                            );
                        CompletableFuture<Unit> var7 = Util.sequence(var6).thenApply(param0x -> Unit.INSTANCE);
                        return var7.whenComplete((param1, param2) -> {
                            if (param2 != null && !(param2 instanceof CancellationException) && !(param2 instanceof InterruptedException)) {
                                Minecraft.getInstance().delayCrash(CrashReport.forThrowable(param2, "Rendering chunk"));
                            }

                            if (!this.isCancelled.get()) {
                                RenderChunk.this.compiled.set(var4);
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
                if (var5 != null) {
                    ModelBlockRenderer.enableCaching();
                    Random var6 = new Random();
                    BlockRenderDispatcher var7 = Minecraft.getInstance().getBlockRenderer();

                    for(BlockPos var8 : BlockPos.betweenClosed(var1, var2)) {
                        BlockState var9 = var5.getBlockState(var8);
                        Block var10 = var9.getBlock();
                        if (var9.isSolidRender(var5, var8)) {
                            var3.setOpaque(var8);
                        }

                        if (var10.isEntityBlock()) {
                            BlockEntity var11 = var5.getBlockEntity(var8, LevelChunk.EntityCreationType.CHECK);
                            if (var11 != null) {
                                BlockEntityRenderer<BlockEntity> var12 = BlockEntityRenderDispatcher.instance.getRenderer(var11);
                                if (var12 != null) {
                                    param3.renderableBlockEntities.add(var11);
                                    if (var12.shouldRenderOffScreen(var11)) {
                                        var4.add(var11);
                                    }
                                }
                            }
                        }

                        FluidState var13 = var5.getFluidState(var8);
                        if (!var13.isEmpty()) {
                            RenderType var14 = RenderType.getRenderLayer(var13);
                            BufferBuilder var15 = param4.builder(var14);
                            if (param3.hasLayer.add(var14)) {
                                RenderChunk.this.beginLayer(var15, var1);
                            }

                            if (var7.renderLiquid(var8, var5, var15, var13)) {
                                param3.isCompletelyEmpty = false;
                                param3.hasBlocks.add(var14);
                            }
                        }

                        if (var9.getRenderShape() != RenderShape.INVISIBLE) {
                            RenderType var16 = RenderType.getRenderLayer(var9);
                            BufferBuilder var17 = param4.builder(var16);
                            if (param3.hasLayer.add(var16)) {
                                RenderChunk.this.beginLayer(var17, var1);
                            }

                            if (var7.renderBatched(var9, var8, var5, var17, var6)) {
                                param3.isCompletelyEmpty = false;
                                param3.hasBlocks.add(var16);
                            }
                        }
                    }

                    if (param3.hasBlocks.contains(RenderType.TRANSLUCENT)) {
                        BufferBuilder var18 = param4.builder(RenderType.TRANSLUCENT);
                        var18.sortQuads(param0, param1, param2);
                        param3.transparencyState = var18.getState();
                    }

                    param3.hasLayer.stream().map(param4::builder).forEach(BufferBuilder::end);
                    ModelBlockRenderer.clearCache();
                }

                param3.visibilitySet = var3.resolve();
                return var4;
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
                super(param0);
                this.compiledChunk = param1;
            }

            @Override
            public CompletableFuture<Unit> doTask(ChunkBufferBuilderPack param0) {
                if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                } else if (!RenderChunk.this.hasAllNeighbors()) {
                    this.isCancelled.set(true);
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                } else if (this.isCancelled.get()) {
                    return CompletableFuture.completedFuture(Unit.INSTANCE);
                } else {
                    Vec3 var0 = ChunkRenderDispatcher.this.getCameraPosition();
                    float var1 = (float)var0.x;
                    float var2 = (float)var0.y;
                    float var3 = (float)var0.z;
                    BufferBuilder.State var4 = this.compiledChunk.transparencyState;
                    if (var4 != null && this.compiledChunk.hasBlocks.contains(RenderType.TRANSLUCENT)) {
                        BufferBuilder var5 = param0.builder(RenderType.TRANSLUCENT);
                        RenderChunk.this.beginLayer(var5, RenderChunk.this.origin);
                        var5.restoreState(var4);
                        var5.sortQuads(var1, var2, var3);
                        this.compiledChunk.transparencyState = var5.getState();
                        var5.end();
                        if (this.isCancelled.get()) {
                            return CompletableFuture.completedFuture(Unit.INSTANCE);
                        } else {
                            CompletableFuture<Unit> var6 = ChunkRenderDispatcher.this.uploadChunkLayer(
                                    param0.builder(RenderType.TRANSLUCENT), RenderChunk.this.getBuffer(RenderType.TRANSLUCENT)
                                )
                                .thenApply(param0x -> Unit.INSTANCE);
                            return var6.whenComplete((param0x, param1) -> {
                                if (param1 != null && !(param1 instanceof CancellationException) && !(param1 instanceof InterruptedException)) {
                                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(param1, "Rendering chunk"));
                                }

                            });
                        }
                    } else {
                        return CompletableFuture.completedFuture(Unit.INSTANCE);
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
