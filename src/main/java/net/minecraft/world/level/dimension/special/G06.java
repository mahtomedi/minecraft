package net.minecraft.world.level.dimension.special;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.NormalDimension;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.ChanceRangeDecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShapeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.RainbowBlockProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class G06 extends SpecialDimensionBase {
    public G06(Level param0, DimensionType param1) {
        super(param0, param1, 1.0F);
    }

    @Override
    public ChunkGenerator<?> createRandomLevelGenerator() {
        return new G06.Generator(this.level, fixedBiome(Biomes.SHAPES), NoneGeneratorSettings.INSTANCE);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return NormalDimension.getTimeOfDayI(param0, 3000.0);
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
        public int getSpawnHeight() {
            return 30;
        }

        @Override
        public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
        }

        @Override
        public void applyCarvers(BiomeManager param0, ChunkAccess param1, GenerationStep.Carving param2) {
        }

        @Override
        public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
            return 0;
        }

        @Override
        public BlockGetter getBaseColumn(int param0, int param1) {
            return EmptyBlockGetter.INSTANCE;
        }

        @Override
        public ChunkGeneratorType<?, ?> getType() {
            return ChunkGeneratorType.T06;
        }
    }

    public static class ShapesBiome extends Biome {
        public ShapesBiome() {
            super(
                new Biome.BiomeBuilder()
                    .surfaceBuilder(SurfaceBuilder.NOPE, SurfaceBuilder.CONFIG_STONE)
                    .precipitation(Biome.Precipitation.NONE)
                    .biomeCategory(Biome.BiomeCategory.NONE)
                    .depth(0.1F)
                    .scale(0.2F)
                    .temperature(0.5F)
                    .downfall(0.5F)
                    .specialEffects(
                        new BiomeSpecialEffects.Builder()
                            .waterColor(52713007)
                            .waterFogColor(1876255554)
                            .fogColor(12638463)
                            .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                            .build()
                    )
                    .parent(null)
            );
            Random var0 = new Random(12461L);
            List<ConfiguredFeature<?, ?>> var1 = Lists.newArrayList();
            Stream.of(ColoredBlocks.COLORED_BLOCKS)
                .flatMap(Stream::of)
                .forEach(
                    param2 -> {
                        float var0x = 1.0F + var0.nextFloat() * 5.0F;
                        float var1x = Math.min(var0x + var0.nextFloat() * 10.0F, 15.0F);
                        var1.add(
                            Feature.SHAPE
                                .configured(
                                    new ShapeConfiguration(
                                        new SimpleStateProvider(param2.defaultBlockState()),
                                        Util.randomObject(var0, ShapeConfiguration.Metric.values()),
                                        var0x,
                                        var1x
                                    )
                                )
                        );
                    }
                );

            for(Block[] var2 : ColoredBlocks.COLORED_BLOCKS) {
                ImmutableList<BlockState> var3 = Stream.of(var2).map(Block::defaultBlockState).collect(ImmutableList.toImmutableList());

                for(ShapeConfiguration.Metric var4 : ShapeConfiguration.Metric.values()) {
                    float var5 = 1.0F + var0.nextFloat() * 5.0F;
                    float var6 = Math.min(var5 + var0.nextFloat() * 10.0F, 15.0F);
                    var1.add(Feature.SHAPE.configured(new ShapeConfiguration(new RainbowBlockProvider(var3), var4, var5, var6)));
                }
            }

            float var7 = 1.0F / (float)var1.size();
            this.addFeature(
                GenerationStep.Decoration.SURFACE_STRUCTURES,
                Feature.RANDOM_SELECTOR
                    .configured(
                        new RandomFeatureConfiguration(
                            var1.stream().map(param1 -> new WeightedConfiguredFeature<>(param1, var7)).collect(Collectors.toList()),
                            Util.randomObject(var0, var1)
                        )
                    )
                    .decorated(FeatureDecorator.CHANCE_RANGE.configured(new ChanceRangeDecoratorConfiguration(1.0F, 16, 16, 128)))
            );
        }
    }
}
