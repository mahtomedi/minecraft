package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
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
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G20 extends SpecialDimensionBase {
    private static final Vec3 PURPLISH = new Vec3((double)s(207.0F), (double)s(104.0F), (double)s(255.0F));

    private static float s(float param0) {
        return param0 / 255.0F;
    }

    public G20(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G20.Generator(this.level, fixedBiome(Biomes.JUNGLE), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public boolean isNaturalDimension() {
        return true;
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.75F;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return PURPLISH;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return true;
    }

    public static class Generator extends ChunkGenerator<NoneGeneratorSettings> {
        private final PerlinSimplexNoise noise = new PerlinSimplexNoise(new WorldgenRandom(1234L), IntStream.rangeClosed(-5, 0));

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
            BlockState var0 = Blocks.BLACK_CONCRETE.defaultBlockState();
            BlockState var1 = Blocks.LIME_CONCRETE.defaultBlockState();
            ChunkPos var2 = param1.getPos();
            BlockPos.MutableBlockPos var3 = new BlockPos.MutableBlockPos();

            for(int var4 = 0; var4 < 16; ++var4) {
                for(int var5 = 0; var5 < 16; ++var5) {
                    double var6 = 64.0
                        + (this.noise.getValue((double)((float)var2.x + (float)var4 / 16.0F), (double)((float)var2.z + (float)var5 / 16.0F), false) + 1.0)
                            * 20.0;

                    for(int var7 = 0; (double)var7 < var6; ++var7) {
                        param1.setBlockState(var3.set(var4, var7, var5), var4 != 0 && var7 % 16 != 0 && var5 != 0 ? var0 : var1, false);
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
            return ChunkGeneratorType.T15;
        }
    }
}
