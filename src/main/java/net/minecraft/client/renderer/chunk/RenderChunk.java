package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderChunk {
    private volatile Level level;
    private final LevelRenderer renderer;
    public static int updateCounter;
    public CompiledChunk compiled = CompiledChunk.UNCOMPILED;
    private final ReentrantLock taskLock = new ReentrantLock();
    private final ReentrantLock compileLock = new ReentrantLock();
    private ChunkCompileTask pendingTask;
    private final Set<BlockEntity> globalBlockEntities = Sets.newHashSet();
    private final VertexBuffer[] buffers = new VertexBuffer[BlockLayer.values().length];
    public AABB bb;
    private int lastFrame = -1;
    private boolean dirty = true;
    private final BlockPos.MutableBlockPos origin = new BlockPos.MutableBlockPos(-1, -1, -1);
    private final BlockPos.MutableBlockPos[] relativeOrigins = Util.make(new BlockPos.MutableBlockPos[6], param0x -> {
        for(int var0x = 0; var0x < param0x.length; ++var0x) {
            param0x[var0x] = new BlockPos.MutableBlockPos();
        }

    });
    private boolean playerChanged;

    public RenderChunk(Level param0, LevelRenderer param1) {
        this.level = param0;
        this.renderer = param1;

        for(int var0 = 0; var0 < BlockLayer.values().length; ++var0) {
            this.buffers[var0] = new VertexBuffer(DefaultVertexFormat.BLOCK);
        }

    }

    private static boolean doesChunkExistAt(BlockPos param0, Level param1) {
        return !param1.getChunk(param0.getX() >> 4, param0.getZ() >> 4).isEmpty();
    }

    public boolean hasAllNeighbors() {
        int var0 = 24;
        if (!(this.getDistToPlayerSqr() > 576.0)) {
            return true;
        } else {
            Level var1 = this.getLevel();
            return doesChunkExistAt(this.relativeOrigins[Direction.WEST.ordinal()], var1)
                && doesChunkExistAt(this.relativeOrigins[Direction.NORTH.ordinal()], var1)
                && doesChunkExistAt(this.relativeOrigins[Direction.EAST.ordinal()], var1)
                && doesChunkExistAt(this.relativeOrigins[Direction.SOUTH.ordinal()], var1);
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

    public VertexBuffer getBuffer(int param0) {
        return this.buffers[param0];
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

    public void rebuildTransparent(float param0, float param1, float param2, ChunkCompileTask param3) {
        CompiledChunk var0 = param3.getCompiledChunk();
        if (var0.getTransparencyState() != null && !var0.isEmpty(BlockLayer.TRANSLUCENT)) {
            this.beginLayer(param3.getBuilders().builder(BlockLayer.TRANSLUCENT), this.origin);
            param3.getBuilders().builder(BlockLayer.TRANSLUCENT).restoreState(var0.getTransparencyState());
            this.preEndLayer(BlockLayer.TRANSLUCENT, param0, param1, param2, param3.getBuilders().builder(BlockLayer.TRANSLUCENT), var0);
        }
    }

    public void compile(float param0, float param1, float param2, ChunkCompileTask param3) {
        CompiledChunk var0 = new CompiledChunk();
        int var1 = 1;
        BlockPos var2 = this.origin.immutable();
        BlockPos var3 = var2.offset(15, 15, 15);
        Level var4 = this.level;
        if (var4 != null) {
            param3.getStatusLock().lock();

            try {
                if (param3.getStatus() != ChunkCompileTask.Status.COMPILING) {
                    return;
                }

                param3.setCompiledChunk(var0);
            } finally {
                param3.getStatusLock().unlock();
            }

            VisGraph var5 = new VisGraph();
            HashSet var6 = Sets.newHashSet();
            RenderChunkRegion var7 = param3.takeRegion();
            if (var7 != null) {
                ++updateCounter;
                boolean[] var8 = new boolean[BlockLayer.values().length];
                ModelBlockRenderer.enableCaching();
                Random var9 = new Random();
                BlockRenderDispatcher var10x = Minecraft.getInstance().getBlockRenderer();

                for(BlockPos var11 : BlockPos.betweenClosed(var2, var3)) {
                    BlockState var12 = var7.getBlockState(var11);
                    Block var13 = var12.getBlock();
                    if (var12.isSolidRender(var7, var11)) {
                        var5.setOpaque(var11);
                    }

                    if (var13.isEntityBlock()) {
                        BlockEntity var14 = var7.getBlockEntity(var11, LevelChunk.EntityCreationType.CHECK);
                        if (var14 != null) {
                            BlockEntityRenderer<BlockEntity> var15 = BlockEntityRenderDispatcher.instance.getRenderer(var14);
                            if (var15 != null) {
                                var0.addRenderableBlockEntity(var14);
                                if (var15.shouldRenderOffScreen(var14)) {
                                    var6.add(var14);
                                }
                            }
                        }
                    }

                    FluidState var16 = var7.getFluidState(var11);
                    if (!var16.isEmpty()) {
                        BlockLayer var17 = var16.getRenderLayer();
                        int var18 = var17.ordinal();
                        BufferBuilder var19 = param3.getBuilders().builder(var18);
                        if (!var0.hasLayer(var17)) {
                            var0.layerIsPresent(var17);
                            this.beginLayer(var19, var2);
                        }

                        var8[var18] |= var10x.renderLiquid(var11, var7, var19, var16);
                    }

                    if (var12.getRenderShape() != RenderShape.INVISIBLE) {
                        BlockLayer var20 = var13.getRenderLayer();
                        int var21 = var20.ordinal();
                        BufferBuilder var22 = param3.getBuilders().builder(var21);
                        if (!var0.hasLayer(var20)) {
                            var0.layerIsPresent(var20);
                            this.beginLayer(var22, var2);
                        }

                        var8[var21] |= var10x.renderBatched(var12, var11, var7, var22, var9);
                    }
                }

                for(BlockLayer var23 : BlockLayer.values()) {
                    if (var8[var23.ordinal()]) {
                        var0.setChanged(var23);
                    }

                    if (var0.hasLayer(var23)) {
                        this.preEndLayer(var23, param0, param1, param2, param3.getBuilders().builder(var23), var0);
                    }
                }

                ModelBlockRenderer.clearCache();
            }

            var0.setVisibilitySet(var5.resolve());
            this.taskLock.lock();

            try {
                Set<BlockEntity> var24 = Sets.newHashSet(var6);
                Set<BlockEntity> var25 = Sets.newHashSet(this.globalBlockEntities);
                var24.removeAll(this.globalBlockEntities);
                var25.removeAll(var6);
                this.globalBlockEntities.clear();
                this.globalBlockEntities.addAll(var6);
                this.renderer.updateGlobalBlockEntities(var25, var24);
            } finally {
                this.taskLock.unlock();
            }

        }
    }

    protected void cancelCompile() {
        this.taskLock.lock();

        try {
            if (this.pendingTask != null && this.pendingTask.getStatus() != ChunkCompileTask.Status.DONE) {
                this.pendingTask.cancel();
                this.pendingTask = null;
            }
        } finally {
            this.taskLock.unlock();
        }

    }

    public ReentrantLock getTaskLock() {
        return this.taskLock;
    }

    public ChunkCompileTask createCompileTask() {
        this.taskLock.lock();

        ChunkCompileTask var4;
        try {
            this.cancelCompile();
            BlockPos var0 = this.origin.immutable();
            int var1 = 1;
            RenderChunkRegion var2 = RenderChunkRegion.createIfNotEmpty(this.level, var0.offset(-1, -1, -1), var0.offset(16, 16, 16), 1);
            this.pendingTask = new ChunkCompileTask(this, ChunkCompileTask.Type.REBUILD_CHUNK, this.getDistToPlayerSqr(), var2);
            var4 = this.pendingTask;
        } finally {
            this.taskLock.unlock();
        }

        return var4;
    }

    @Nullable
    public ChunkCompileTask createTransparencySortTask() {
        this.taskLock.lock();

        Object var1;
        try {
            if (this.pendingTask == null || this.pendingTask.getStatus() != ChunkCompileTask.Status.PENDING) {
                if (this.pendingTask != null && this.pendingTask.getStatus() != ChunkCompileTask.Status.DONE) {
                    this.pendingTask.cancel();
                    this.pendingTask = null;
                }

                this.pendingTask = new ChunkCompileTask(this, ChunkCompileTask.Type.RESORT_TRANSPARENCY, this.getDistToPlayerSqr(), null);
                this.pendingTask.setCompiledChunk(this.compiled);
                return this.pendingTask;
            }

            var1 = null;
        } finally {
            this.taskLock.unlock();
        }

        return (ChunkCompileTask)var1;
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

    private void preEndLayer(BlockLayer param0, float param1, float param2, float param3, BufferBuilder param4, CompiledChunk param5) {
        if (param0 == BlockLayer.TRANSLUCENT && !param5.isEmpty(param0)) {
            param4.sortQuads(param1, param2, param3);
            param5.setTransparencyState(param4.getState());
        }

        param4.end();
    }

    public CompiledChunk getCompiledChunk() {
        return this.compiled;
    }

    public void setCompiledChunk(CompiledChunk param0) {
        this.compileLock.lock();

        try {
            this.compiled = param0;
        } finally {
            this.compileLock.unlock();
        }

    }

    public void reset() {
        this.cancelCompile();
        this.compiled = CompiledChunk.UNCOMPILED;
        this.dirty = true;
    }

    public void releaseBuffers() {
        this.reset();
        this.level = null;

        for(int var0 = 0; var0 < BlockLayer.values().length; ++var0) {
            if (this.buffers[var0] != null) {
                this.buffers[var0].delete();
            }
        }

    }

    public BlockPos getOrigin() {
        return this.origin;
    }

    public void setDirty(boolean param0) {
        if (this.dirty) {
            param0 |= this.playerChanged;
        }

        this.dirty = true;
        this.playerChanged = param0;
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

    public Level getLevel() {
        return this.level;
    }
}
