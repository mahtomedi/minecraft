package net.minecraft.world.level.dimension.special;

import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.OverworldGeneratorSettings;
import net.minecraft.world.level.levelgen.OverworldLevelSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ShapeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;

public class G40 extends NormalDimension {
    public G40(Level param0, DimensionType param1) {
        super(param0, param1);
    }

    @Override
    public ChunkGenerator<? extends ChunkGeneratorSettings> createRandomLevelGenerator() {
        return new G40.Generator(this.level, SpecialDimensionBase.normalBiomes(this.level.getSeed()), ChunkGeneratorType.SURFACE.createSettings());
    }

    public static class Generator extends OverworldLevelSource {
        public static final SimpleStateProvider STATE_PROVIDER = new SimpleStateProvider(Blocks.AIR.defaultBlockState());
        private final PerlinSimplexNoise noise;

        public Generator(LevelAccessor param0, BiomeSource param1, OverworldGeneratorSettings param2) {
            super(param0, param1, param2);
            WorldgenRandom var0 = new WorldgenRandom(param0.getSeed());
            this.noise = new PerlinSimplexNoise(var0, IntStream.rangeClosed(-4, 1));
        }

        @Override
        public void applyBiomeDecoration(WorldGenRegion param0) {
            super.applyBiomeDecoration(param0);
            WorldgenRandom var0 = new WorldgenRandom();
            int var1 = param0.getCenterX();
            int var2 = param0.getCenterZ();
            var0.setBaseChunkSeed(var1, var2);
            int var3 = var0.nextInt(4);

            for(int var4 = 0; var4 < var3; ++var4) {
                int var5 = 16 * var1 + var0.nextInt(16);
                int var6 = 16 * var2 + var0.nextInt(16);
                int var7 = param0.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, var5, var6);
                int var8 = Mth.ceil((double)var7 * (1.0 + var0.nextGaussian() / 2.0));
                ShapeConfiguration.Metric var9 = Util.randomObject(var0, ShapeConfiguration.Metric.values());
                float var10 = 2.0F + var0.nextFloat() * 5.0F;
                float var11 = Math.min(var10 + var0.nextFloat() * 10.0F, 15.0F);
                Feature.SHAPE.place(param0, this, var0, new BlockPos(var5, var8, var6), new ShapeConfiguration(STATE_PROVIDER, var9, var10, var11));
            }

        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T35;
        }
    }
}
