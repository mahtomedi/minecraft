package net.minecraft.world.level.levelgen;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.entity.npc.CatSpawner;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FlatLevelSource extends ChunkGenerator {
    private final Biome biomeWrapper;
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final CatSpawner catSpawner = new CatSpawner();
    private final FlatLevelGeneratorSettings settings;

    public FlatLevelSource(FlatLevelGeneratorSettings param0) {
        super(new FixedBiomeSource(param0.getBiome()), param0.structureSettings());
        this.settings = param0;
        this.biomeWrapper = this.getBiomeFromSettings();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ChunkGenerator withSeed(long param0) {
        return this;
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
            var0.getSpecialEffects(),
            var0.getParent()
        );
        Map<String, Map<String, String>> var2 = this.settings.getStructuresOptions();

        for(String var3 : var2.keySet()) {
            ConfiguredFeature<?, ?>[] var4 = FlatLevelGeneratorSettings.STRUCTURE_FEATURES.get(var3);
            if (var4 != null) {
                for(ConfiguredFeature<?, ?> var5 : var4) {
                    var1.addFeature(FlatLevelGeneratorSettings.STRUCTURE_FEATURES_STEP.get(var5), var5);
                    if (var5.feature instanceof StructureFeature) {
                        StructureFeature<FeatureConfiguration> var6 = (StructureFeature)var5.feature;
                        FeatureConfiguration var7 = var0.getStructureConfiguration(var6);
                        FeatureConfiguration var8 = var7 != null ? var7 : FlatLevelGeneratorSettings.STRUCTURE_FEATURES_DEFAULT.get(var5);
                        var1.addStructureStart(var6.configured(var8));
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
                    for(ConfiguredFeature<?, ?> var12 : var0.getFeaturesForStep(var11)) {
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
                var1.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(var14, var15)));
            }
        }

        return var1;
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion param0, ChunkAccess param1) {
    }

    @Override
    public int getSpawnHeight() {
        BlockState[] var0 = this.settings.getLayers();

        for(int var1 = 0; var1 < var0.length; ++var1) {
            BlockState var2 = var0[var1] == null ? Blocks.AIR.defaultBlockState() : var0[var1];
            if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(var2)) {
                return var1 - 1;
            }
        }

        return var0.length;
    }

    @Override
    protected Biome getCarvingOrDecorationBiome(BiomeManager param0, BlockPos param1) {
        return this.biomeWrapper;
    }

    @Override
    public boolean canGenerateStructure(StructureFeature<?> param0) {
        return this.biomeWrapper.isValidStart(param0);
    }

    @Override
    public void fillFromNoise(LevelAccessor param0, StructureFeatureManager param1, ChunkAccess param2) {
        BlockState[] var0 = this.settings.getLayers();
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
        Heightmap var2 = param2.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap var3 = param2.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for(int var4 = 0; var4 < var0.length; ++var4) {
            BlockState var5 = var0[var4];
            if (var5 != null) {
                for(int var6 = 0; var6 < 16; ++var6) {
                    for(int var7 = 0; var7 < 16; ++var7) {
                        param2.setBlockState(var1.set(var6, var4, var7), var5, false);
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
    public BlockGetter getBaseColumn(int param0, int param1) {
        return new NoiseColumn(
            Arrays.stream(this.settings.getLayers())
                .map(param0x -> param0x == null ? Blocks.AIR.defaultBlockState() : param0x)
                .toArray(param0x -> new BlockState[param0x])
        );
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
    public BlockPos findNearestMapFeature(ServerLevel param0, String param1, BlockPos param2, int param3, boolean param4) {
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
            BiomeSpecialEffects param7,
            @Nullable String param8
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
                    .specialEffects(param7)
                    .parent(param8)
            );
        }
    }
}
