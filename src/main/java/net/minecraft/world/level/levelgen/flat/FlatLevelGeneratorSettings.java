package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.<FlatLevelGeneratorSettings>create(
            param0 -> param0.group(
                        StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings),
                        FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
                        Registry.BIOME.fieldOf("biome").withDefault(() -> {
                            LOGGER.error("Unknown biome, defaulting to plains");
                            return Biomes.PLAINS;
                        }).forGetter(param0x -> param0x.biome)
                    )
                    .apply(param0, FlatLevelGeneratorSettings::new)
        )
        .stable();
    private static final ConfiguredFeature<?, ?> WATER_LAKE_COMPOSITE_FEATURE = Feature.LAKE
        .configured(new BlockStateConfiguration(Blocks.WATER.defaultBlockState()))
        .decorated(FeatureDecorator.WATER_LAKE.configured(new ChanceDecoratorConfiguration(4)));
    private static final ConfiguredFeature<?, ?> LAVA_LAKE_COMPOSITE_FEATURE = Feature.LAKE
        .configured(new BlockStateConfiguration(Blocks.LAVA.defaultBlockState()))
        .decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(80)));
    private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURES = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(StructureFeature.MINESHAFT, BiomeDefaultFeatures.MINESHAFT);
        param0.put(StructureFeature.VILLAGE, BiomeDefaultFeatures.VILLAGE_PLAINS);
        param0.put(StructureFeature.STRONGHOLD, BiomeDefaultFeatures.STRONGHOLD);
        param0.put(StructureFeature.SWAMP_HUT, BiomeDefaultFeatures.SWAMP_HUT);
        param0.put(StructureFeature.DESERT_PYRAMID, BiomeDefaultFeatures.DESERT_PYRAMID);
        param0.put(StructureFeature.JUNGLE_TEMPLE, BiomeDefaultFeatures.JUNGLE_TEMPLE);
        param0.put(StructureFeature.IGLOO, BiomeDefaultFeatures.IGLOO);
        param0.put(StructureFeature.OCEAN_RUIN, BiomeDefaultFeatures.OCEAN_RUIN_COLD);
        param0.put(StructureFeature.SHIPWRECK, BiomeDefaultFeatures.SHIPWRECK);
        param0.put(StructureFeature.OCEAN_MONUMENT, BiomeDefaultFeatures.OCEAN_MONUMENT);
        param0.put(StructureFeature.END_CITY, BiomeDefaultFeatures.END_CITY);
        param0.put(StructureFeature.WOODLAND_MANSION, BiomeDefaultFeatures.WOODLAND_MANSION);
        param0.put(StructureFeature.NETHER_BRIDGE, BiomeDefaultFeatures.NETHER_BRIDGE);
        param0.put(StructureFeature.PILLAGER_OUTPOST, BiomeDefaultFeatures.PILLAGER_OUTPOST);
        param0.put(StructureFeature.RUINED_PORTAL, BiomeDefaultFeatures.RUINED_PORTAL_STANDARD);
        param0.put(StructureFeature.BASTION_REMNANT, BiomeDefaultFeatures.BASTION_REMNANT);
    });
    private final StructureSettings structureSettings;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private Biome biome;
    private final BlockState[] layers = new BlockState[256];
    private boolean voidGen;
    private boolean decoration = false;
    private boolean addLakes = false;

    public FlatLevelGeneratorSettings(StructureSettings param0, List<FlatLayerInfo> param1, Biome param2) {
        this(param0);
        this.layersInfo.addAll(param1);
        this.updateLayers();
        this.biome = param2;
    }

    public FlatLevelGeneratorSettings(StructureSettings param0) {
        this.structureSettings = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public FlatLevelGeneratorSettings withStructureSettings(StructureSettings param0) {
        FlatLevelGeneratorSettings var0 = new FlatLevelGeneratorSettings(param0);

        for(FlatLayerInfo var1 : this.getLayersInfo()) {
            var0.getLayersInfo().add(new FlatLayerInfo(var1.getHeight(), var1.getBlockState().getBlock()));
            var0.updateLayers();
        }

        var0.setBiome(this.biome);
        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public void setDecoration() {
        this.decoration = true;
    }

    @OnlyIn(Dist.CLIENT)
    public void setAddLakes() {
        this.addLakes = true;
    }

    public Biome getBiomeFromSettings() {
        Biome var0 = this.getBiome();
        Biome var1 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(var0.getSurfaceBuilder())
                .precipitation(var0.getPrecipitation())
                .biomeCategory(var0.getBiomeCategory())
                .depth(var0.getDepth())
                .scale(var0.getScale())
                .temperature(var0.getTemperature())
                .downfall(var0.getDownfall())
                .specialEffects(var0.getSpecialEffects())
                .parent(var0.getParent())
        ) {
        };
        if (this.addLakes) {
            var1.addFeature(GenerationStep.Decoration.LAKES, WATER_LAKE_COMPOSITE_FEATURE);
            var1.addFeature(GenerationStep.Decoration.LAKES, LAVA_LAKE_COMPOSITE_FEATURE);
        }

        for(Entry<StructureFeature<?>, StructureFeatureConfiguration> var2 : this.structureSettings.structureConfig().entrySet()) {
            var1.addStructureStart(var0.withBiomeConfig(STRUCTURE_FEATURES.get(var2.getKey())));
        }

        boolean var3 = (!this.voidGen || var0 == Biomes.THE_VOID) && this.decoration;
        if (var3) {
            List<GenerationStep.Decoration> var4 = Lists.newArrayList();
            var4.add(GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
            var4.add(GenerationStep.Decoration.SURFACE_STRUCTURES);

            for(GenerationStep.Decoration var5 : GenerationStep.Decoration.values()) {
                if (!var4.contains(var5)) {
                    for(ConfiguredFeature<?, ?> var6 : var0.getFeaturesForStep(var5)) {
                        var1.addFeature(var5, var6);
                    }
                }
            }
        }

        BlockState[] var7 = this.getLayers();

        for(int var8 = 0; var8 < var7.length; ++var8) {
            BlockState var9 = var7[var8];
            if (var9 != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(var9)) {
                this.layers[var8] = null;
                var1.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(var8, var9)));
            }
        }

        return var1;
    }

    public StructureSettings structureSettings() {
        return this.structureSettings;
    }

    public Biome getBiome() {
        return this.biome;
    }

    public void setBiome(Biome param0) {
        this.biome = param0;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public BlockState[] getLayers() {
        return this.layers;
    }

    public void updateLayers() {
        int var0 = 0;

        for(FlatLayerInfo var1 : this.layersInfo) {
            var1.setStart(var0);
            var0 += var1.getHeight();
        }

        this.voidGen = true;

        for(FlatLayerInfo var2 : this.layersInfo) {
            for(int var3 = var2.getStart(); var3 < var2.getStart() + var2.getHeight(); ++var3) {
                BlockState var4 = var2.getBlockState();
                if (!var4.is(Blocks.AIR)) {
                    this.voidGen = false;
                    this.layers[var3] = var4;
                }
            }
        }

    }

    public static FlatLevelGeneratorSettings getDefault() {
        StructureSettings var0 = new StructureSettings(
            Optional.of(StructureSettings.DEFAULT_STRONGHOLD),
            Maps.newHashMap(ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE)))
        );
        FlatLevelGeneratorSettings var1 = new FlatLevelGeneratorSettings(var0);
        var1.setBiome(Biomes.PLAINS);
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        var1.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        var1.updateLayers();
        return var1;
    }
}
