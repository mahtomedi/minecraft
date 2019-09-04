package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;

public class FlatLevelSource extends ChunkGenerator<FlatLevelGeneratorSettings> {
    private final Biome biomeWrapper;
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final CatSpawner catSpawner = new CatSpawner();

    public FlatLevelSource(LevelAccessor param0, BiomeSource param1, FlatLevelGeneratorSettings param2) {
        super(param0, param1, param2);
        this.biomeWrapper = this.getBiomeFromSettings();
    }

    private Biome getBiomeFromSettings() {
        Biome var0 = this.settings.getBiome();
        FlatLevelSource.FlatLevelBiomeWrapper var1 = new FlatLevelSource.FlatLevelBiomeWrapper(
            var0.getSurfaceBuilder(),
            var0.getPrecipitation(),
            var0.getBiomeCategory(),
            var0.getDepth(),
            var0.getScale(),
            var0.getTemperature(),
            var0.getDownfall(),
            var0.getWaterColor(),
            var0.getWaterFogColor(),
            var0.getParent()
        );
        Map<String, Map<String, String>> var2 = this.settings.getStructuresOptions();

        for(String var3 : var2.keySet()) {
            ConfiguredFeature<?>[] var4 = FlatLevelGeneratorSettings.STRUCTURE_FEATURES.get(var3);
            if (var4 != null) {
                for(ConfiguredFeature<?> var5 : var4) {
                    var1.addFeature(FlatLevelGeneratorSettings.STRUCTURE_FEATURES_STEP.get(var5), var5);
                    ConfiguredFeature<?> var6 = ((DecoratedFeatureConfiguration)var5.config).feature;
                    if (var6.feature instanceof StructureFeature) {
                        StructureFeature<FeatureConfiguration> var7 = (StructureFeature)var6.feature;
                        FeatureConfiguration var8 = var0.getStructureConfiguration(var7);
                        var1.addStructureStart(var7, var8 != null ? var8 : FlatLevelGeneratorSettings.STRUCTURE_FEATURES_DEFAULT.get(var5));
                    }
                }
            }
        }

        boolean var9 = (!this.settings.isVoidGen() || var0 == Biomes.THE_VOID) && var2.containsKey("decoration");
        if (var9) {
            List<GenerationStep.Decoration> var10 = Lists.newArrayList();
            var10.add(GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
            var10.add(GenerationStep.Decoration.SURFACE_STRUCTURES);

            for(GenerationStep.Decoration var11 : GenerationStep.Decoration.values()) {
                if (!var10.contains(var11)) {
                    for(ConfiguredFeature<?> var12 : var0.getFeaturesForStep(var11)) {
                        var1.addFeature(var11, var12);
                    }
                }
            }
        }

        BlockState[] var13 = this.settings.getLayers();

        for(int var14 = 0; var14 < var13.length; ++var14) {
            BlockState var15 = var13[var14];
            if (var15 != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(var15)) {
                this.settings.deleteLayer(var14);
                var1.addFeature(
                    GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                    Biome.makeComposite(Feature.FILL_LAYER, new LayerConfiguration(var14, var15), FeatureDecorator.NOPE, DecoratorConfiguration.NONE)
                );
            }
        }

        return var1;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
    }

    @Override
    public int getSpawnHeight() {
        ChunkAccess var0 = this.level.getChunk(0, 0);
        return var0.getHeight(Heightmap.Types.MOTION_BLOCKING, 8, 8);
    }

    @Override
    protected Biome getCarvingOrDecorationBiome(BiomeManager param0, BlockPos param1) {
        return this.biomeWrapper;
    }

    @Override
    public void fillFromNoise(LevelAccessor param0, ChunkAccess param1) {
        BlockState[] var0 = this.settings.getLayers();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        Heightmap var2 = param1.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var3 = param1.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for(int var4 = 0; var4 < var0.length; ++var4) {
            BlockState var5 = var0[var4];
            if (var5 != null) {
                for(int var6 = 0; var6 < 16; ++var6) {
                    for(int var7 = 0; var7 < 16; ++var7) {
                        param1.setBlockState(var1.set(var6, var4, var7), var5, false);
                        var2.update(var6, var4, var7, var5);
                        var3.update(var6, var4, var7, var5);
                    }
                }
            }
        }

    }

    @Override
    public int getBaseHeight(int param0, int param1, Heightmap.Types param2) {
        BlockState[] var0 = this.settings.getLayers();

        for(int var1 = var0.length - 1; var1 >= 0; --var1) {
            BlockState var2 = var0[var1];
            if (var2 != null && param2.isOpaque().test(var2)) {
                return var1 + 1;
            }
        }

        return 0;
    }

    @Override
    public void tickCustomSpawners(ServerLevel param0, boolean param1, boolean param2) {
        this.phantomSpawner.tick(param0, param1, param2);
        this.catSpawner.tick(param0, param1, param2);
    }

    @Override
    public boolean isBiomeValidStartForStructure(Biome param0, StructureFeature<? extends FeatureConfiguration> param1) {
        return this.biomeWrapper.isValidStart(param1);
    }

    @Nullable
    @Override
    public <C extends FeatureConfiguration> C getStructureConfiguration(Biome param0, StructureFeature<C> param1) {
        return this.biomeWrapper.getStructureConfiguration(param1);
    }

    @Nullable
    @Override
    public BlockPos findNearestMapFeature(Level param0, String param1, BlockPos param2, int param3, boolean param4) {
        return !this.settings.getStructuresOptions().keySet().contains(param1.toLowerCase(Locale.ROOT))
            ? null
            : super.findNearestMapFeature(param0, param1, param2, param3, param4);
    }

    class FlatLevelBiomeWrapper extends Biome {
        protected FlatLevelBiomeWrapper(
            ConfiguredSurfaceBuilder<?> param0,
            Biome.Precipitation param1,
            Biome.BiomeCategory param2,
            float param3,
            float param4,
            float param5,
            float param6,
            int param7,
            int param8,
            @Nullable String param9
        ) {
            super(
                new Biome.BiomeBuilder()
                    .surfaceBuilder(param0)
                    .precipitation(param1)
                    .biomeCategory(param2)
                    .depth(param3)
                    .scale(param4)
                    .temperature(param5)
                    .downfall(param6)
                    .waterColor(param7)
                    .waterFogColor(param8)
                    .parent(param9)
            );
        }
    }
}
