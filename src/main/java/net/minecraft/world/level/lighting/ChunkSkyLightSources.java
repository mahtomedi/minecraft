package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ChunkSkyLightSources {
    private static final int SIZE = 16;
    public static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
    private final int minY;
    private final BitStorage heightmap;
    private final BlockPos.MutableBlockPos mutablePos1 = new BlockPos.MutableBlockPos();
    private final BlockPos.MutableBlockPos mutablePos2 = new BlockPos.MutableBlockPos();

    public ChunkSkyLightSources(LevelHeightAccessor param0) {
        this.minY = param0.getMinBuildHeight() - 1;
        int var0 = param0.getMaxBuildHeight();
        int var1 = Mth.ceillog2(var0 - this.minY + 1);
        this.heightmap = new SimpleBitStorage(var1, 256);
    }

    public void fillFrom(ChunkAccess param0) {
        int var0 = param0.getHighestFilledSectionIndex();
        if (var0 == -1) {
            this.fill(this.minY);
        } else {
            for(int var1 = 0; var1 < 16; ++var1) {
                for(int var2 = 0; var2 < 16; ++var2) {
                    int var3 = Math.max(this.findLowestSourceY(param0, var0, var2, var1), this.minY);
                    this.set(index(var2, var1), var3);
                }
            }

        }
    }

    private int findLowestSourceY(ChunkAccess param0, int param1, int param2, int param3) {
        int var0 = SectionPos.sectionToBlockCoord(param0.getSectionYFromSectionIndex(param1) + 1);
        BlockPos.MutableBlockPos var1 = this.mutablePos1.set(param2, var0, param3);
        BlockPos.MutableBlockPos var2 = this.mutablePos2.setWithOffset(var1, Direction.DOWN);
        BlockState var3 = Blocks.AIR.defaultBlockState();

        for(int var4 = param1; var4 >= 0; --var4) {
            LevelChunkSection var5 = param0.getSection(var4);
            if (var5.hasOnlyAir()) {
                var3 = Blocks.AIR.defaultBlockState();
                int var6 = param0.getSectionYFromSectionIndex(var4);
                var1.setY(SectionPos.sectionToBlockCoord(var6));
                var2.setY(var1.getY() - 1);
            } else {
                for(int var7 = 15; var7 >= 0; --var7) {
                    BlockState var8 = var5.getBlockState(param2, var7, param3);
                    if (isEdgeOccluded(param0, var1, var3, var2, var8)) {
                        return var1.getY();
                    }

                    var3 = var8;
                    var1.set(var2);
                    var2.move(Direction.DOWN);
                }
            }
        }

        return this.minY;
    }

    public boolean update(BlockGetter param0, int param1, int param2, int param3) {
        int var0 = param2 + 1;
        int var1 = index(param1, param3);
        int var2 = this.get(var1);
        if (var0 < var2) {
            return false;
        } else {
            BlockPos var3 = this.mutablePos1.set(param1, param2 + 1, param3);
            BlockState var4 = param0.getBlockState(var3);
            BlockPos var5 = this.mutablePos2.set(param1, param2, param3);
            BlockState var6 = param0.getBlockState(var5);
            if (this.updateEdge(param0, var1, var2, var3, var4, var5, var6)) {
                return true;
            } else {
                BlockPos var7 = this.mutablePos1.set(param1, param2 - 1, param3);
                BlockState var8 = param0.getBlockState(var7);
                return this.updateEdge(param0, var1, var2, var5, var6, var7, var8);
            }
        }
    }

    private boolean updateEdge(BlockGetter param0, int param1, int param2, BlockPos param3, BlockState param4, BlockPos param5, BlockState param6) {
        int var0 = param3.getY();
        if (isEdgeOccluded(param0, param3, param4, param5, param6)) {
            if (var0 > param2) {
                this.set(param1, var0);
                return true;
            }
        } else if (var0 == param2) {
            this.set(param1, this.findLowestSourceBelow(param0, param5, param6));
            return true;
        }

        return false;
    }

    private int findLowestSourceBelow(BlockGetter param0, BlockPos param1, BlockState param2) {
        BlockPos.MutableBlockPos var0 = this.mutablePos1.set(param1);
        BlockPos.MutableBlockPos var1 = this.mutablePos2.setWithOffset(param1, Direction.DOWN);
        BlockState var2 = param2;

        while(var1.getY() >= this.minY) {
            BlockState var3 = param0.getBlockState(var1);
            if (isEdgeOccluded(param0, var0, var2, var1, var3)) {
                return var0.getY();
            }

            var2 = var3;
            var0.set(var1);
            var1.move(Direction.DOWN);
        }

        return this.minY;
    }

    private static boolean isEdgeOccluded(BlockGetter param0, BlockPos param1, BlockState param2, BlockPos param3, BlockState param4) {
        if (param4.getLightBlock(param0, param3) != 0) {
            return true;
        } else {
            VoxelShape var0 = LightEngine.getOcclusionShape(param0, param1, param2, Direction.DOWN);
            VoxelShape var1 = LightEngine.getOcclusionShape(param0, param3, param4, Direction.UP);
            return Shapes.faceShapeOccludes(var0, var1);
        }
    }

    public int getLowestSourceY(int param0, int param1) {
        int var0 = this.get(index(param0, param1));
        return this.extendSourcesBelowWorld(var0);
    }

    public int getHighestLowestSourceY() {
        int var0 = Integer.MIN_VALUE;

        for(int var1 = 0; var1 < this.heightmap.getSize(); ++var1) {
            int var2 = this.heightmap.get(var1);
            if (var2 > var0) {
                var0 = var2;
            }
        }

        return this.extendSourcesBelowWorld(var0 + this.minY);
    }

    private void fill(int param0) {
        int var0 = param0 - this.minY;

        for(int var1 = 0; var1 < this.heightmap.getSize(); ++var1) {
            this.heightmap.set(var1, var0);
        }

    }

    private void set(int param0, int param1) {
        this.heightmap.set(param0, param1 - this.minY);
    }

    private int get(int param0) {
        return this.heightmap.get(param0) + this.minY;
    }

    private int extendSourcesBelowWorld(int param0) {
        return param0 == this.minY ? Integer.MIN_VALUE : param0;
    }

    private static int index(int param0, int param1) {
        return param0 + param1 * 16;
    }
}
