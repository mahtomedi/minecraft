package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;

public class G35 extends NormalDimension {
    public G35(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return new G35.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
    }

    public static class Generator extends OverworldLevelSource {
        public Generator(LevelAccessor param0, BiomeSource param1, OverworldGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            super.applyBiomeDecoration(param0);
            int var0 = param0.getCenterX();
            int var1 = param0.getCenterZ();
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
            BlockState var3 = Blocks.BRICKS.defaultBlockState();
            BlockState var4 = Blocks.WALL_TORCH.defaultBlockState();
            boolean var5 = Math.floorMod(var0, 4) == 2;
            boolean var6 = Math.floorMod(var1, 4) == 2;

            for(int var7 = 0; var7 < 16; ++var7) {
                for(int var8 = 0; var8 < 16; ++var8) {
                    if (var8 != 0 || var7 != 0 || !var5 || !var6) {
                        param0.setBlock(var2.set(16 * var0 + var8, 255, 16 * var1 + var7), var3, 4);
                    }
                }
            }

            if (var0 % 4 == 0) {
                for(int var9 = 0; var9 < 16; ++var9) {
                    for(int var10 = 0; var10 < 256; ++var10) {
                        param0.setBlock(var2.set(16 * var0, var10, 16 * var1 + var9), var3, 4);
                    }
                }

                if (var6) {
                    BlockPos var11 = param0.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(16 * var0, 0, 16 * var1));
                    BlockState var12 = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.EAST);
                    param0.setBlock(var11, var12.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 4);
                    BlockPos var13 = var11.above();
                    param0.setBlock(var13, var12.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 4);
                    BlockPos var14 = var13.above();
                    param0.setBlock(var14.east(), var4.setValue(WallTorchBlock.FACING, Direction.EAST), 4);
                    param0.setBlock(var14.west(), var4.setValue(WallTorchBlock.FACING, Direction.WEST), 4);
                }
            }

            if (var1 % 4 == 0) {
                for(int var15 = 0; var15 < 16; ++var15) {
                    for(int var16 = 0; var16 < 256; ++var16) {
                        param0.setBlock(var2.set(16 * var0 + var15, var16, 16 * var1), var3, 4);
                    }
                }

                if (var5) {
                    BlockPos var17 = param0.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, new BlockPos(16 * var0, 0, 16 * var1));
                    BlockState var18 = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
                    param0.setBlock(var17, var18.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER), 4);
                    BlockPos var19 = var17.above();
                    param0.setBlock(var19, var18.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 4);
                    BlockPos var20 = var19.above();
                    param0.setBlock(var20.north(), var4.setValue(WallTorchBlock.FACING, Direction.NORTH), 4);
                    param0.setBlock(var20.south(), var4.setValue(WallTorchBlock.FACING, Direction.SOUTH), 4);
                }
            }

        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T30;
        }
    }
}
