package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class G37 extends NormalDimension {
    public G37(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return new G37.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
    }

    public static class Generator extends OverworldLevelSource {
        public Generator(LevelAccessor param0, BiomeSource param1, OverworldGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            super.applyBiomeDecoration(param0);
            WorldgenRandom var0 = new WorldgenRandom();
            int var1 = param0.getCenterX();
            int var2 = param0.getCenterZ();
            var0.setBaseChunkSeed(var1, var2);
            if (var0.nextInt(10) == 0) {
                BlockState var3 = Blocks.AIR.defaultBlockState();
                BlockState var4 = Blocks.OBSIDIAN.defaultBlockState();
                BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();
                int var6 = 1 + var0.nextInt(10);
                int var7 = var6 * var6 + 1;
                int var8 = var6 + 3;
                int var9 = var8 * var8 + 1;

                for(int var10 = -var8; var10 <= var8; ++var10) {
                    for(int var11 = -var8; var11 <= var8; ++var11) {
                        int var12 = var10 * var10 + var11 * var11;
                        if (var12 <= var7) {
                            for(int var13 = 0; var13 < 256; ++var13) {
                                param0.setBlock(var5.set(16 * var1 + var10, var13, 16 * var2 + var11), var4, 4);
                            }
                        } else if (var12 <= var9) {
                            for(int var14 = 0; var14 < 256; ++var14) {
                                param0.setBlock(var5.set(16 * var1 + var10, var14, 16 * var2 + var11), var3, 4);
                            }
                        }
                    }
                }
            }

        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T32;
        }
    }
}
