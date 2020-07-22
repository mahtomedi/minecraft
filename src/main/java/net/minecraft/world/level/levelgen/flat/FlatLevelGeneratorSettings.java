package net.minecraft.world.level.levelgen.flat;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.data.worldgen.Features;
import net.minecraft.data.worldgen.StructureFeatures;
import net.minecraft.world.level.biome.Biome;
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
                        StructureSettings.CODEC.fieldOf("structures").forGetter(FlatLevelGeneratorSettings::structureSettings),
                        FlatLayerInfo.CODEC.listOf().fieldOf("layers").forGetter(FlatLevelGeneratorSettings::getLayersInfo),
                        Codec.BOOL.fieldOf("lakes").orElse(false).forGetter(param0x -> param0x.addLakes),
                        Codec.BOOL.fieldOf("features").orElse(false).forGetter(param0x -> param0x.decoration),
                        Biome.CODEC
                            .fieldOf("biome")
                            .orElseGet(Util.prefix("Unknown biome, defaulting to plains", LOGGER::error), () -> () -> Biomes.PLAINS)
                            .forGetter(param0x -> param0x.biome)
                    )
                    .apply(param0, FlatLevelGeneratorSettings::new)
        )
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
    private final StructureSettings structureSettings;
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private Supplier<Biome> biome = () -> Biomes.PLAINS;
    private final BlockState[] layers = new BlockState[256];
    private boolean voidGen;
    private boolean decoration = false;
    private boolean addLakes = false;

    public FlatLevelGeneratorSettings(StructureSettings param0, List<FlatLayerInfo> param1, boolean param2, boolean param3, Supplier<Biome> param4) {
        this(param0);
        if (param2) {
            this.setAddLakes();
        }

        if (param3) {
            this.setDecoration();
        }

        this.layersInfo.addAll(param1);
        this.updateLayers();
        this.biome = param4;
    }

    public FlatLevelGeneratorSettings(StructureSettings param0) {
        this.structureSettings = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public FlatLevelGeneratorSettings withStructureSettings(StructureSettings param0) {
        return this.withLayers(this.layersInfo, param0);
    }

    @OnlyIn(Dist.CLIENT)
    public FlatLevelGeneratorSettings withLayers(List<FlatLayerInfo> param0, StructureSettings param1) {
        FlatLevelGeneratorSettings var0 = new FlatLevelGeneratorSettings(param1);

        for(FlatLayerInfo var1 : param0) {
            var0.layersInfo.add(new FlatLayerInfo(var1.getHeight(), var1.getBlockState().getBlock()));
            var0.updateLayers();
        }

        var0.setBiome(this.biome.get());
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
        Biome var1 = new Biome(
            new Biome.BiomeBuilder()
                .surfaceBuilder(var0.getSurfaceBuilder())
                .precipitation(var0.getPrecipitation())
                .biomeCategory(var0.getBiomeCategory())
                .depth(var0.getDepth())
                .scale(var0.getScale())
                .temperature(var0.getBaseTemperature())
                .downfall(var0.getDownfall())
                .specialEffects(var0.getSpecialEffects())
                .parent(var0.getParent())
        );
        if (this.addLakes) {
            var1.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_WATER);
            var1.addFeature(GenerationStep.Decoration.LAKES, Features.LAKE_LAVA);
        }

        for(Entry<StructureFeature<?>, StructureFeatureConfiguration> var2 : this.structureSettings.structureConfig().entrySet()) {
            var1.addStructureStart(var0.withBiomeConfig(STRUCTURE_FEATURES.get(var2.getKey())));
        }

        boolean var3 = (!this.voidGen || var0 == Biomes.THE_VOID) && this.decoration;
        if (var3) {
            List<List<Supplier<ConfiguredFeature<?, ?>>>> var4 = var0.features();

            for(int var5 = 0; var5 < var4.size(); ++var5) {
                if (var5 != GenerationStep.Decoration.UNDERGROUND_STRUCTURES.ordinal() && var5 != GenerationStep.Decoration.SURFACE_STRUCTURES.ordinal()) {
                    for(Supplier<ConfiguredFeature<?, ?>> var7 : var4.get(var5)) {
                        var1.addFeature(var5, var7);
                    }
                }
            }
        }

        BlockState[] var8 = this.getLayers();

        for(int var9 = 0; var9 < var8.length; ++var9) {
            BlockState var10 = var8[var9];
            if (var10 != null && !Heightmap.Types.MOTION_BLOCKING.isOpaque().test(var10)) {
                this.layers[var9] = null;
                var1.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, Feature.FILL_LAYER.configured(new LayerConfiguration(var9, var10)));
            }
        }

        return var1;
    }

    public StructureSettings structureSettings() {
        return this.structureSettings;
    }

    public Biome getBiome() {
        return this.biome.get();
    }

    public void setBiome(Biome param0) {
        this.biome = () -> param0;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public BlockState[] getLayers() {
        return this.layers;
    }

    public void updateLayers() {
        Arrays.fill(this.layers, 0, this.layers.length, null);
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
