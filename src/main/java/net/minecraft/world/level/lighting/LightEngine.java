package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>> implements LayerLightEventListener {
    public static final int MAX_LEVEL = 15;
    protected static final int MIN_OPACITY = 1;
    protected static final long PULL_LIGHT_IN_ENTRY = LightEngine.QueueEntry.decreaseAllDirections(1);
    private static final int MIN_QUEUE_SIZE = 512;
    protected static final Direction[] PROPAGATION_DIRECTIONS = Direction.values();
    protected final LightChunkGetter chunkSource;
    protected final S storage;
    private final LongOpenHashSet blockNodesToCheck = new LongOpenHashSet(512, 0.5F);
    private final LongArrayFIFOQueue decreaseQueue = new LongArrayFIFOQueue();
    private final LongArrayFIFOQueue increaseQueue = new LongArrayFIFOQueue();
    private final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
    private static final int CACHE_SIZE = 2;
    private final long[] lastChunkPos = new long[2];
    private final LightChunk[] lastChunk = new LightChunk[2];

    protected LightEngine(LightChunkGetter param0, S param1) {
        this.chunkSource = param0;
        this.storage = param1;
        this.clearChunkCache();
    }

    public static boolean hasDifferentLightProperties(BlockGetter param0, BlockPos param1, BlockState param2, BlockState param3) {
        if (param3 == param2) {
            return false;
        } else {
            return param3.getLightBlock(param0, param1) != param2.getLightBlock(param0, param1)
                || param3.getLightEmission() != param2.getLightEmission()
                || param3.useShapeForLightOcclusion()
                || param2.useShapeForLightOcclusion();
        }
    }

    public static int getLightBlockInto(
        BlockGetter param0, BlockState param1, BlockPos param2, BlockState param3, BlockPos param4, Direction param5, int param6
    ) {
        boolean var0 = isEmptyShape(param1);
        boolean var1 = isEmptyShape(param3);
        if (var0 && var1) {
            return param6;
        } else {
            VoxelShape var2 = var0 ? Shapes.empty() : param1.getOcclusionShape(param0, param2);
            VoxelShape var3 = var1 ? Shapes.empty() : param3.getOcclusionShape(param0, param4);
            return Shapes.mergedFaceOccludes(var2, var3, param5) ? 16 : param6;
        }
    }

    public static VoxelShape getOcclusionShape(BlockGetter param0, BlockPos param1, BlockState param2, Direction param3) {
        return isEmptyShape(param2) ? Shapes.empty() : param2.getFaceOcclusionShape(param0, param1, param3);
    }

    protected static boolean isEmptyShape(BlockState param0) {
        return !param0.canOcclude() || !param0.useShapeForLightOcclusion();
    }

    protected BlockState getState(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX());
        int var1 = SectionPos.blockToSectionCoord(param0.getZ());
        LightChunk var2 = this.getChunk(var0, var1);
        return var2 == null ? Blocks.BEDROCK.defaultBlockState() : var2.getBlockState(param0);
    }

    protected int getOpacity(BlockState param0, BlockPos param1) {
        return Math.max(1, param0.getLightBlock(this.chunkSource.getLevel(), param1));
    }

    protected boolean shapeOccludes(long param0, BlockState param1, long param2, BlockState param3, Direction param4) {
        VoxelShape var0 = this.getOcclusionShape(param1, param0, param4);
        VoxelShape var1 = this.getOcclusionShape(param3, param2, param4.getOpposite());
        return Shapes.faceShapeOccludes(var0, var1);
    }

    protected VoxelShape getOcclusionShape(BlockState param0, long param1, Direction param2) {
        return getOcclusionShape(this.chunkSource.getLevel(), this.mutablePos.set(param1), param0, param2);
    }

    @Nullable
    protected LightChunk getChunk(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);

        for(int var1 = 0; var1 < 2; ++var1) {
            if (var0 == this.lastChunkPos[var1]) {
                return this.lastChunk[var1];
            }
        }

        LightChunk var2 = this.chunkSource.getChunkForLighting(param0, param1);

        for(int var3 = 1; var3 > 0; --var3) {
            this.lastChunkPos[var3] = this.lastChunkPos[var3 - 1];
            this.lastChunk[var3] = this.lastChunk[var3 - 1];
        }

        this.lastChunkPos[0] = var0;
        this.lastChunk[0] = var2;
        return var2;
    }

    private void clearChunkCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunk, null);
    }

    @Override
    public void checkBlock(BlockPos param0) {
        this.blockNodesToCheck.add(param0.asLong());
    }

    public void queueSectionData(long param0, @Nullable DataLayer param1) {
        this.storage.queueSectionData(param0, param1);
    }

    public void retainData(ChunkPos param0, boolean param1) {
        this.storage.retainData(SectionPos.getZeroNode(param0.x, param0.z), param1);
    }

    @Override
    public void updateSectionStatus(SectionPos param0, boolean param1) {
        this.storage.updateSectionStatus(param0.asLong(), param1);
    }

    @Override
    public void setLightEnabled(ChunkPos param0, boolean param1) {
        this.storage.setLightEnabled(SectionPos.getZeroNode(param0.x, param0.z), param1);
    }

    @Override
    public int runLightUpdates() {
        LongIterator var0 = this.blockNodesToCheck.iterator();

        while(var0.hasNext()) {
            this.checkNode(var0.nextLong());
        }

        this.blockNodesToCheck.clear();
        this.blockNodesToCheck.trim(512);
        int var1 = 0;
        var1 += this.propagateDecreases();
        var1 += this.propagateIncreases();
        this.clearChunkCache();
        this.storage.markNewInconsistencies(this);
        this.storage.swapSectionMap();
        return var1;
    }

    private int propagateIncreases() {
        int var0;
        for(var0 = 0; !this.increaseQueue.isEmpty(); ++var0) {
            long var1 = this.increaseQueue.dequeueLong();
            long var2 = this.increaseQueue.dequeueLong();
            int var3 = this.storage.getStoredLevel(var1);
            int var4 = LightEngine.QueueEntry.getFromLevel(var2);
            if (LightEngine.QueueEntry.isIncreaseFromEmission(var2) && var3 < var4) {
                this.storage.setStoredLevel(var1, var4);
                var3 = var4;
            }

            if (var3 == var4) {
                this.propagateIncrease(var1, var2, var3);
            }
        }

        return var0;
    }

    private int propagateDecreases() {
        int var0;
        for(var0 = 0; !this.decreaseQueue.isEmpty(); ++var0) {
            long var1 = this.decreaseQueue.dequeueLong();
            long var2 = this.decreaseQueue.dequeueLong();
            this.propagateDecrease(var1, var2);
        }

        return var0;
    }

    protected void enqueueDecrease(long param0, long param1) {
        this.decreaseQueue.enqueue(param0);
        this.decreaseQueue.enqueue(param1);
    }

    protected void enqueueIncrease(long param0, long param1) {
        this.increaseQueue.enqueue(param0);
        this.increaseQueue.enqueue(param1);
    }

    @Override
    public boolean hasLightWork() {
        return this.storage.hasInconsistencies() || !this.blockNodesToCheck.isEmpty() || !this.decreaseQueue.isEmpty() || !this.increaseQueue.isEmpty();
    }

    @Nullable
    @Override
    public DataLayer getDataLayerData(SectionPos param0) {
        return this.storage.getDataLayerData(param0.asLong());
    }

    @Override
    public int getLightValue(BlockPos param0) {
        return this.storage.getLightValue(param0.asLong());
    }

    public String getDebugData(long param0) {
        return this.getDebugSectionType(param0).display();
    }

    public LayerLightSectionStorage.SectionType getDebugSectionType(long param0) {
        return this.storage.getDebugSectionType(param0);
    }

    protected abstract void checkNode(long var1);

    protected abstract void propagateIncrease(long var1, long var3, int var5);

    protected abstract void propagateDecrease(long var1, long var3);

    public static class QueueEntry {
        private static final int FROM_LEVEL_BITS = 4;
        private static final int DIRECTION_BITS = 6;
        private static final long LEVEL_MASK = 15L;
        private static final long DIRECTIONS_MASK = 1008L;
        private static final long FLAG_FROM_EMPTY_SHAPE = 1024L;
        private static final long FLAG_INCREASE_FROM_EMISSION = 2048L;

        public static long decreaseSkipOneDirection(int param0, Direction param1) {
            long var0 = withoutDirection(1008L, param1);
            return withLevel(var0, param0);
        }

        public static long decreaseAllDirections(int param0) {
            return withLevel(1008L, param0);
        }

        public static long increaseLightFromEmission(int param0, boolean param1) {
            long var0 = 1008L;
            var0 |= 2048L;
            if (param1) {
                var0 |= 1024L;
            }

            return withLevel(var0, param0);
        }

        public static long increaseSkipOneDirection(int param0, boolean param1, Direction param2) {
            long var0 = withoutDirection(1008L, param2);
            if (param1) {
                var0 |= 1024L;
            }

            return withLevel(var0, param0);
        }

        public static long increaseOnlyOneDirection(int param0, boolean param1, Direction param2) {
            long var0 = 0L;
            if (param1) {
                var0 |= 1024L;
            }

            var0 = withDirection(var0, param2);
            return withLevel(var0, param0);
        }

        public static long increaseSkySourceInDirections(boolean param0, boolean param1, boolean param2, boolean param3, boolean param4) {
            long var0 = withLevel(0L, 15);
            if (param0) {
                var0 = withDirection(var0, Direction.DOWN);
            }

            if (param1) {
                var0 = withDirection(var0, Direction.NORTH);
            }

            if (param2) {
                var0 = withDirection(var0, Direction.SOUTH);
            }

            if (param3) {
                var0 = withDirection(var0, Direction.WEST);
            }

            if (param4) {
                var0 = withDirection(var0, Direction.EAST);
            }

            return var0;
        }

        public static int getFromLevel(long param0) {
            return (int)(param0 & 15L);
        }

        public static boolean isFromEmptyShape(long param0) {
            return (param0 & 1024L) != 0L;
        }

        public static boolean isIncreaseFromEmission(long param0) {
            return (param0 & 2048L) != 0L;
        }

        public static boolean shouldPropagateInDirection(long param0, Direction param1) {
            return (param0 & 1L << param1.ordinal() + 4) != 0L;
        }

        private static long withLevel(long param0, int param1) {
            return param0 & -16L | (long)param1 & 15L;
        }

        private static long withDirection(long param0, Direction param1) {
            return param0 | 1L << param1.ordinal() + 4;
        }

        private static long withoutDirection(long param0, Direction param1) {
            return param0 & ~(1L << param1.ordinal() + 4);
        }
    }
}
