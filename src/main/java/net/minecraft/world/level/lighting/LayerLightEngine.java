package net.minecraft.world.level.lighting;

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
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class LayerLightEngine<M extends DataLayerStorageMap<M>, S extends LayerLightSectionStorage<M>>
    extends DynamicGraphMinFixedPoint
    implements LayerLightEventListener {
    private static final Direction[] DIRECTIONS = Direction.values();
    protected final LightChunkGetter chunkSource;
    protected final S storage;
    private boolean runningLightUpdates;
    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    private static final int CACHE_SIZE = 2;
    private final long[] lastChunkPos = new long[2];
    private final BlockGetter[] lastChunk = new BlockGetter[2];

    public LayerLightEngine(LightChunkGetter param0, S param1) {
        super(16, 256, 8192);
        this.chunkSource = param0;
        this.storage = param1;
        this.clearCache();
    }

    @Override
    protected void checkNode(long param0) {
        this.storage.runAllUpdates();
        if (this.storage.storingLightForSection(SectionPos.blockToSection(param0))) {
            super.checkNode(param0);
        }

    }

    @Nullable
    private BlockGetter getChunk(int param0, int param1) {
        long var0 = ChunkPos.asLong(param0, param1);

        for(int var1 = 0; var1 < 2; ++var1) {
            if (var0 == this.lastChunkPos[var1]) {
                return this.lastChunk[var1];
            }
        }

        BlockGetter var2 = this.chunkSource.getChunkForLighting(param0, param1);

        for(int var3 = 1; var3 > 0; --var3) {
            this.lastChunkPos[var3] = this.lastChunkPos[var3 - 1];
            this.lastChunk[var3] = this.lastChunk[var3 - 1];
        }

        this.lastChunkPos[0] = var0;
        this.lastChunk[0] = var2;
        return var2;
    }

    private void clearCache() {
        Arrays.fill(this.lastChunkPos, ChunkPos.INVALID_CHUNK_POS);
        Arrays.fill(this.lastChunk, null);
    }

    protected BlockState getState(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX());
        int var1 = SectionPos.blockToSectionCoord(param0.getZ());
        BlockGetter var2 = this.getChunk(var0, var1);
        return var2 == null ? Blocks.BEDROCK.defaultBlockState() : var2.getBlockState(param0);
    }

    protected int getOpacity(BlockState param0, BlockPos param1) {
        return param0.getLightBlock(this.chunkSource.getLevel(), param1);
    }

    protected boolean shapeOccludes(long param0, BlockState param1, long param2, BlockState param3, Direction param4) {
        VoxelShape var0 = this.getShape(param1, param0, param4);
        VoxelShape var1 = this.getShape(param3, param2, param4.getOpposite());
        return Shapes.faceShapeOccludes(var0, var1);
    }

    private VoxelShape getShape(BlockState param0, long param1, Direction param2) {
        return param0.canOcclude() && param0.useShapeForLightOcclusion()
            ? param0.getFaceOcclusionShape(this.chunkSource.getLevel(), this.pos.set(param1), param2)
            : Shapes.empty();
    }

    public static int getLightBlockInto(
        BlockGetter param0, BlockState param1, BlockPos param2, BlockState param3, BlockPos param4, Direction param5, int param6
    ) {
        boolean var0 = param1.canOcclude() && param1.useShapeForLightOcclusion();
        boolean var1 = param3.canOcclude() && param3.useShapeForLightOcclusion();
        if (!var0 && !var1) {
            return param6;
        } else {
            VoxelShape var2 = var0 ? param1.getOcclusionShape(param0, param2) : Shapes.empty();
            VoxelShape var3 = var1 ? param3.getOcclusionShape(param0, param4) : Shapes.empty();
            return Shapes.mergedFaceOccludes(var2, var3, param5) ? 16 : param6;
        }
    }

    @Nullable
    protected static Direction getDirection(long param0, long param1) {
        int var0 = BlockPos.getX(param1) - BlockPos.getX(param0);
        int var1 = BlockPos.getY(param1) - BlockPos.getY(param0);
        int var2 = BlockPos.getZ(param1) - BlockPos.getZ(param0);
        return Direction.fromDelta(var0, var1, var2);
    }

    @Override
    protected int getComputedLevel(long param0, long param1, int param2) {
        return 0;
    }

    @Override
    protected int getLevel(long param0) {
        return this.isSource(param0) ? 0 : 15 - this.storage.getStoredLevel(param0);
    }

    protected int getLevel(DataLayer param0, long param1) {
        return 15
            - param0.get(
                SectionPos.sectionRelative(BlockPos.getX(param1)),
                SectionPos.sectionRelative(BlockPos.getY(param1)),
                SectionPos.sectionRelative(BlockPos.getZ(param1))
            );
    }

    @Override
    protected void setLevel(long param0, int param1) {
        this.storage.setStoredLevel(param0, Math.min(15, 15 - param1));
    }

    @Override
    protected int computeLevelFromNeighbor(long param0, long param1, int param2) {
        return 0;
    }

    @Override
    public boolean hasLightWork() {
        return this.hasWork() || this.storage.hasWork() || this.storage.hasInconsistencies();
    }

    @Override
    public int runUpdates(int param0, boolean param1, boolean param2) {
        if (!this.runningLightUpdates) {
            if (this.storage.hasWork()) {
                param0 = this.storage.runUpdates(param0);
                if (param0 == 0) {
                    return param0;
                }
            }

            this.storage.markNewInconsistencies(this, param1, param2);
        }

        this.runningLightUpdates = true;
        if (this.hasWork()) {
            param0 = this.runUpdates(param0);
            this.clearCache();
            if (param0 == 0) {
                return param0;
            }
        }

        this.runningLightUpdates = false;
        this.storage.swapSectionMap();
        return param0;
    }

    protected void queueSectionData(long param0, @Nullable DataLayer param1, boolean param2) {
        this.storage.queueSectionData(param0, param1, param2);
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
        return this.storage.getLevel(param0) + "";
    }

    public int getDebugSectionLevel(long param0) {
        return this.storage.getLevel(param0);
    }

    @Override
    public void checkBlock(BlockPos param0) {
        long var0 = param0.asLong();
        this.checkNode(var0);

        for(Direction var1 : DIRECTIONS) {
            this.checkNode(BlockPos.offset(var0, var1));
        }

    }

    @Override
    public void onBlockEmissionIncrease(BlockPos param0, int param1) {
    }

    @Override
    public void updateSectionStatus(SectionPos param0, boolean param1) {
        this.storage.updateSectionStatus(param0.asLong(), param1);
    }

    @Override
    public void enableLightSources(ChunkPos param0, boolean param1) {
        long var0 = SectionPos.getZeroNode(SectionPos.asLong(param0.x, 0, param0.z));
        this.storage.enableLightSources(var0, param1);
    }

    public void retainData(ChunkPos param0, boolean param1) {
        long var0 = SectionPos.getZeroNode(SectionPos.asLong(param0.x, 0, param0.z));
        this.storage.retainData(var0, param1);
    }
}
