package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<FlatLevelGeneratorSettings> CODEC = RecordCodecBuilder.<FlatLevelGeneratorSettings>create(
            param0 -> param0.group(
                        RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(param0x -> param0x.biomes),
                        StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings),
                        FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
                        Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(param0x -> param0x.addLakes),
                        Codec.BOOL.fieldOf("features").orElse(false).forGetter(param0x -> param0x.decoration),
                        Biome.CODEC.optionalFieldOf("biome").orElseGet(Optional::empty).forGetter(param0x -> Optional.of(param0x.biome))
                    )
                    .apply(param0, FlatLevelGeneratorSettings::new)
        )
        .comapFlatMap(FlatLevelGeneratorSettings::validateHeight, Function.identity())
        .stable();
    private static final Map<StructureFeature<?>, ConfiguredStructureFeature<?, ?>> STRUCTURE_FEATURES = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(StructureFeature.MINESHAFT, StructureFeatures.MINESHAFT);
        param0.put(StructureFeature.VILLAGE, StructureFeatures.VILLAGE_PLAINS);
        param0.put(StructureFeature.STRONGHOLD, StructureFeatures.STRONGHOLD);
        param0.put(StructureFeature.SWAMP_HUT, StructureFeatures.SWAMP_HUT);
        param0.put(StructureFeature.DESERT_PYRAMID, StructureFeatures.DESERT_PYRAMID);
        param0.put(StructureFeature.JUNGLE_TEMPLE, StructureFeatures.JUNGLE_TEMPLE);
        param0.put(StructureFeature.IGLOO, StructureFeatures.IGLOO);
        param0.put(StructureFeature.OCEAN_RUIN, StructureFeatures.OCEAN_RUIN_COLD);
        param0.put(StructureFeature.SHIPWRECK, StructureFeatures.SHIPWRECK);
        param0.put(StructureFeature.OCEAN_MONUMENT, StructureFeatures.OCEAN_MONUMENT);
        param0.put(StructureFeature.END_CITY, StructureFeatures.END_CITY);
        param0.put(StructureFeature.WOODLAND_MANSION, StructureFeatures.WOODLAND_MANSION);
        param0.put(StructureFeature.NETHER_BRIDGE, StructureFeatures.NETHER_BRIDGE);
        param0.put(StructureFeature.PILLAGER_OUTPOST, StructureFeatures.PILLAGER_OUTPOST);
        param0.put(StructureFeature.RUINED_PORTAL, StructureFeatures.RUINED_PORTAL_STANDARD);
        param0.put(StructureFeature.BASTION_REMNANT, StructureFeatures.BASTION_REMNANT);
    });
    private final Registry<Biome> biomes;
    private final StructureSettings structureSettings;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private Supplier<Biome> biome;
    private final List<BlockState> layers;
    private boolean voidGen;
    private boolean decoration;
    private boolean addLakes;

    private static DataResult<FlatLevelGeneratorSettings> validateHeight(FlatLevelGeneratorSettings param0) {
        int var0 = param0.layersInfo.stream().mapToInt(FlatLayerInfo::getHeight).sum();
        return var0 > DimensionType.Y_SIZE ? DataResult.error("Sum of layer heights is > " + DimensionType.Y_SIZE, param0) : DataResult.success(param0);
    }

    private FlatLevelGeneratorSettings(
        Registry<Biome> param0, StructureSettings param1, List<FlatLayerInfo> param2, boolean param3, boolean param4, Optional<Supplier<Biome>> param5
    ) {
        this(param1, param0);
        if (param3) {
            this.setAddLakes();
        }

        if (param4) {
            this.setDecoration();
        }

        this.layersInfo.addAll(param2);
        this.updateLayers();
        if (!param5.isPresent()) {
            LOGGER.error("Unknown biome, defaulting to plains");
            this.biome = () -> param0.getOrThrow(Biomes.PLAINS);
        } else {
            this.biome = param5.get();
        }

    }

    public FlatLevelGeneratorSettings(StructureSettings param0, Registry<Biome> param1) {
        this.biomes = param1;
        this.structureSettings = param0;
        this.biome = () -> param1.getOrThrow(Biomes.PLAINS);
        this.layers = Lists.newArrayList();
    }

    @OnlyIn(Dist.CLIENT)
    public FlatLevelGeneratorSettings withStructureSettings(StructureSettings param0) {
        return this.withLayers(this.layersInfo, param0);
    }

    @OnlyIn(Dist.CLIENT)
    public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> param0, StructureSettings param1) {
        FlatLevelGeneratorSettings var0 = new FlatLevelGeneratorSettings(param1, this.biomes);

        for(FlatLayerInfo var1 : param0) {
            var0.layersInfo.add(new FlatLayerInfo(var1.getHeight(), var1.getBlockState().getBlock()));
            var0.updateLayers();
        }

        var0.setBiome(this.biome);
        if (this.decoration) {
            var0.setDecoration();
        }

        if (this.addLakes) {
            var0.setAddLakes();
        }

        return var0;
    }

    public void setDecoration() {
        this.decoration = true;
    }

    public void setAddLakes() {
        this.addLakes = true;
    }

    public Biome getBiomeFromSettings() {
        Biome var0 = this.getBiome();
        BiomeGenerationSettings var1 = var0.getGenerationSettings();
        BiomeGenerationSettings.Builder var2 = new BiomeGenerationSettings.Builder().surfaceBuilder(var1.getSurfaceBuilder());
        if (this.addLakes) {
            var2.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
            var2.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
        }

        for(Entry<StructureFeature<?>, StructureFeatureConfiguration> var3 : this.structureSettings.structureConfig().entrySet()) {
            var2.addStructureStart(var1.withBiomeConfig(STRUCTURE_FEATURES.get(var3.getKey())));
        }

        boolean var4 = (!this.voidGen || this.biomes.getResourceKey(var0).equals(Optional.of(Biomes.THE_VOID))) && this.decoration;
        if (var4) {
            List<List<Supplier<ConfiguredFeature<?, ?>>>> var5 = var1.features();

            for(int var6 = 0; var6 < var5.size(); ++var6) {
                if (var6 != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && var6 != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) {
                    for(Supplier<ConfiguredFeature<?, ?>> var8 : var5.get(var6)) {
                        var2.addFeature(var6, var8);
                    }
                }
            }
        }

        List<BlockState> var9 = this.getLayers();

        for(int var10 = 0; var10 < var9.size(); ++var10) {
            BlockState var11 = var9.get(var10);
            if (!Heightmap.Types.MOTION_BLOCKING.isOpaque().test(var11)) {
                var9.set(var10, null);
                var2.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(var10, var11)));
            }
        }

        return new Biome.BiomeBuilder()
            .precipitation(var0.getPrecipitation())
            .biomeCategory(var0.getBiomeCategory())
            .depth(var0.getDepth())
            .scale(var0.getScale())
            .temperature(var0.getBaseTemperature())
            .downfall(var0.getDownfall())
            .specialEffects(var0.getSpecialEffects())
            .generationSettings(var2.build())
            .mobSpawnSettings(var0.getMobSettings())
            .build();
    }

    public StructureSettings structureSettings() {
        return this.structureSettings;
    }

    public Biome getBiome() {
        return this.biome.get();
    }

    @OnlyIn(Dist.CLIENT)
    public void setBiome(Supplier<Biome> param0) {
        this.biome = param0;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public List<BlockState> getLayers() {
        return this.layers;
    }

    public void updateLayers() {
        this.layers.clear();

        for(FlatLayerInfo var0 : this.layersInfo) {
            for(int var1 = 0; var1 < var0.getHeight(); ++var1) {
                this.layers.add(var0.getBlockState());
            }
        }

        this.voidGen = this.layers.stream().anyMatch(param0 -> !param0.is(Blocks.AIR));
    }

    public static FlatLevelGeneratorSettings getDefault(Registry<Biome> param0) {
        StructureSettings var0 = new StructureSettings(
            Optional.of(StructureSettings.DEFAULT_STRONGHOLD),
            Maps.newHashMap(ImmutableMap.of(StructureFeature.VILLAGE, StructureSettings.DEFAULTS.get(StructureFeature.VILLAGE)))
        );
        FlatLevelGeneratorSettings var1 = new FlatLevelGeneratorSettings(var0, param0);
        var1.biome = () -> param0.getOrThrow(Biomes.PLAINS);
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        var1.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        var1.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        var1.updateLayers();
        return var1;
    }
}
