package net.minecraft.world.level.dimension.special;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G01 extends SpecialDimensionBase {
    private static int clearSign(int param0) {
        return param0 & 2147483647;
    }

    public G01(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G01.Generator(this.level, fixedBiome(Biomes.DESERT), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 12000.0F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return param0;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return false;
    }

    public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
        public Generator(LevelAccessor param0, BiomeSource param1, NoneGeneratorSettings param2) {
            super(param0, param1, param2);
        }

        @Override
        public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public int getSpawnHeight() {
            return 100;
        }

        @Override
        public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
            ChunkPos var0 = param1.getPos();

            for(int var1 = 0; var1 < 8; ++var1) {
                int var2 = G01.clearSign(var0.x) ^ var1 ^ G01.clearSign(var0.z);
                Block[] var3 = ColoredBlocks.COLORED_BLOCKS[var2 % ColoredBlocks.COLORED_BLOCKS.length];
                BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();

                for(int var5 = 0; var5 < 16; ++var5) {
                    for(int var6 = 0; var6 < 16; ++var6) {
                        for(int var7 = 0; var7 < 16; ++var7) {
                            int var8 = 16 * var1 + var6;
                            int var9 = var5 ^ var8 ^ var7;
                            param1.setBlockState(var4.set(var5, var8, var7), var3[var9 % var3.length].defaultBlockState(), false);
                        }
                    }
                }
            }

        }

        @Override
        public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
            return 100;
        }

        @Override
        public BlockGetter getBaseColumn(int param0, int param1) {
            return EmptyBlockGetter.INSTANCE;
        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T01;
        }
    }
}
