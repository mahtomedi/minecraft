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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G27 extends SpecialDimensionBase {
    public G27(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G27.Generator(this.level, fixedBiome(Biomes.THE_VOID), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.0F;
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
            this.fillPattern(param1, var0, 0, 0);
            this.fillPattern(param1, var0, 1, 0);
            this.fillPattern(param1, var0, 0, 1);
            this.fillPattern(param1, var0, 1, 1);
        }

        private void fillPattern(ChunkAccess param0, ChunkPos param1, int param2, int param3) {
            int var0 = 8388607;
            long var1 = ((long)param1.x * 2L + (long)param2 & 8388607L) << 31 | ((long)param1.z * 2L + (long)param3 & 8388607L) << 8;
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
            BlockState var3 = Blocks.WHITE_CONCRETE.defaultBlockState();
            BlockState var4 = Blocks.BLACK_CONCRETE.defaultBlockState();

            for(int var5 = 0; var5 < 256; ++var5) {
                long var6 = var1 | (long)var5;

                for(int var7 = 0; var7 < 7; ++var7) {
                    for(int var8 = 0; var8 < 7; ++var8) {
                        boolean var9 = (var6 & 1L) != 0L;
                        var6 >>= 1;
                        param0.setBlockState(var2.set(var7 + param2 * 8, var5, var8 + param3 * 8), var9 ? var3 : var4, false);
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
            return ChunkGeneratorType.T22;
        }
    }
}
