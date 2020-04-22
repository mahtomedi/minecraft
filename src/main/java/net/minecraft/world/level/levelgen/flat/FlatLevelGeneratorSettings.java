package net.minecraft.world.level.levelgen.flat;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.placement.ChanceDecoratorConfiguration;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlatLevelGeneratorSettings extends ChunkGeneratorSettings {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> MINESHAFT_COMPOSITE_FEATURE = Feature.MINESHAFT
        .configured(new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL));
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> VILLAGE_COMPOSITE_FEATURE = Feature.VILLAGE
        .configured(new JigsawConfiguration("village/plains/town_centers", 6));
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> STRONGHOLD_COMPOSITE_FEATURE = Feature.STRONGHOLD
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> SWAMPHUT_COMPOSITE_FEATURE = Feature.SWAMP_HUT
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> DESERT_PYRAMID_COMPOSITE_FEATURE = Feature.DESERT_PYRAMID
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> JUNGLE_PYRAMID_COMPOSITE_FEATURE = Feature.JUNGLE_TEMPLE
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> IGLOO_COMPOSITE_FEATURE = Feature.IGLOO.configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> SHIPWRECK_COMPOSITE_FEATURE = Feature.SHIPWRECK
        .configured(new ShipwreckConfiguration(false));
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> OCEAN_MONUMENT_COMPOSITE_FEATURE = Feature.OCEAN_MONUMENT
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> ENDCITY_COMPOSITE_FEATURE = Feature.END_CITY.configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> WOOLAND_MANSION_COMPOSITE_FEATURE = Feature.WOODLAND_MANSION
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> FORTRESS_COMPOSITE_FEATURE = Feature.NETHER_BRIDGE
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> RUINED_PORTAL_COMPOSITE_FEATURE = Feature.RUINED_PORTAL
        .configured(new RuinedPortalConfiguration());
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> OCEAN_RUIN_COMPOSITE_FEATURE = Feature.OCEAN_RUIN
        .configured(new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.1F));
    private static final ConfiguredFeature<?, ? extends StructureFeature<?>> PILLAGER_OUTPOST_COMPOSITE_FEATURE = Feature.PILLAGER_OUTPOST
        .configured(FeatureConfiguration.NONE);
    private static final ConfiguredFeature<?, ?> WATER_LAKE_COMPOSITE_FEATURE = Feature.LAKE
        .configured(new BlockStateConfiguration(Blocks.WATER.defaultBlockState()))
        .decorated(FeatureDecorator.WATER_LAKE.configured(new ChanceDecoratorConfiguration(4)));
    private static final ConfiguredFeature<?, ?> LAVA_LAKE_COMPOSITE_FEATURE = Feature.LAKE
        .configured(new BlockStateConfiguration(Blocks.LAVA.defaultBlockState()))
        .decorated(FeatureDecorator.LAVA_LAKE.configured(new ChanceDecoratorConfiguration(80)));
    public static final Map<ConfiguredFeature<?, ?>, GenerationStep.Decoration> STRUCTURE_FEATURES_STEP = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(MINESHAFT_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
        param0.put(VILLAGE_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(STRONGHOLD_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
        param0.put(SWAMPHUT_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(DESERT_PYRAMID_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(JUNGLE_PYRAMID_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(IGLOO_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(RUINED_PORTAL_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(SHIPWRECK_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(OCEAN_RUIN_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(WATER_LAKE_COMPOSITE_FEATURE, GenerationStep.Decoration.LOCAL_MODIFICATIONS);
        param0.put(LAVA_LAKE_COMPOSITE_FEATURE, GenerationStep.Decoration.LOCAL_MODIFICATIONS);
        param0.put(ENDCITY_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(WOOLAND_MANSION_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(FORTRESS_COMPOSITE_FEATURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES);
        param0.put(OCEAN_MONUMENT_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
        param0.put(PILLAGER_OUTPOST_COMPOSITE_FEATURE, GenerationStep.Decoration.SURFACE_STRUCTURES);
    });
    public static final Map<String, ConfiguredFeature<?, ?>[]> STRUCTURE_FEATURES = Util.make(
        Maps.newHashMap(),
        param0 -> {
            param0.put("mineshaft", new ConfiguredFeature[]{MINESHAFT_COMPOSITE_FEATURE});
            param0.put("village", new ConfiguredFeature[]{VILLAGE_COMPOSITE_FEATURE});
            param0.put("stronghold", new ConfiguredFeature[]{STRONGHOLD_COMPOSITE_FEATURE});
            param0.put(
                "biome_1",
                new ConfiguredFeature[]{
                    SWAMPHUT_COMPOSITE_FEATURE,
                    DESERT_PYRAMID_COMPOSITE_FEATURE,
                    JUNGLE_PYRAMID_COMPOSITE_FEATURE,
                    IGLOO_COMPOSITE_FEATURE,
                    OCEAN_RUIN_COMPOSITE_FEATURE,
                    SHIPWRECK_COMPOSITE_FEATURE
                }
            );
            param0.put("oceanmonument", new ConfiguredFeature[]{OCEAN_MONUMENT_COMPOSITE_FEATURE});
            param0.put("lake", new ConfiguredFeature[]{WATER_LAKE_COMPOSITE_FEATURE});
            param0.put("lava_lake", new ConfiguredFeature[]{LAVA_LAKE_COMPOSITE_FEATURE});
            param0.put("endcity", new ConfiguredFeature[]{ENDCITY_COMPOSITE_FEATURE});
            param0.put("mansion", new ConfiguredFeature[]{WOOLAND_MANSION_COMPOSITE_FEATURE});
            param0.put("fortress", new ConfiguredFeature[]{FORTRESS_COMPOSITE_FEATURE});
            param0.put("pillager_outpost", new ConfiguredFeature[]{PILLAGER_OUTPOST_COMPOSITE_FEATURE});
            param0.put("ruined_portal", new ConfiguredFeature[]{RUINED_PORTAL_COMPOSITE_FEATURE});
        }
    );
    public static final Map<ConfiguredFeature<?, ? extends StructureFeature<?>>, FeatureConfiguration> STRUCTURE_FEATURES_DEFAULT = Util.make(
        Maps.newHashMap(), param0 -> {
            param0.put(MINESHAFT_COMPOSITE_FEATURE, new MineshaftConfiguration(0.004, MineshaftFeature.Type.NORMAL));
            param0.put(VILLAGE_COMPOSITE_FEATURE, new JigsawConfiguration("village/plains/town_centers", 6));
            param0.put(STRONGHOLD_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(SWAMPHUT_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(DESERT_PYRAMID_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(JUNGLE_PYRAMID_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(IGLOO_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(OCEAN_RUIN_COMPOSITE_FEATURE, new OceanRuinConfiguration(OceanRuinFeature.Type.COLD, 0.3F, 0.9F));
            param0.put(SHIPWRECK_COMPOSITE_FEATURE, new ShipwreckConfiguration(false));
            param0.put(OCEAN_MONUMENT_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(ENDCITY_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(WOOLAND_MANSION_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(FORTRESS_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
            param0.put(PILLAGER_OUTPOST_COMPOSITE_FEATURE, FeatureConfiguration.NONE);
        }
    );
    private final List<FlatLayerInfo> layersInfo = Lists.newArrayList();
    private final Map<String, Map<String, String>> structuresOptions = Maps.newHashMap();
    private Biome biome;
    private final BlockState[] layers = new BlockState[256];
    private boolean voidGen;
    private int seaLevel;

    @Nullable
    public static Block byString(String param0) {
        try {
            ResourceLocation var0 = new ResourceLocation(param0);
            return Registry.BLOCK.getOptional(var0).orElse(null);
        } catch (IllegalArgumentException var2) {
            LOGGER.warn("Invalid blockstate: {}", param0, var2);
            return null;
        }
    }

    public Biome getBiome() {
        return this.biome;
    }

    public void setBiome(Biome param0) {
        this.biome = param0;
    }

    public Map<String, Map<String, String>> getStructuresOptions() {
        return this.structuresOptions;
    }

    public List<FlatLayerInfo> getLayersInfo() {
        return this.layersInfo;
    }

    public void updateLayers() {
        int var0 = 0;

        for(FlatLayerInfo var1 : this.layersInfo) {
            var1.setStart(var0);
            var0 += var1.getHeight();
        }

        this.seaLevel = 0;
        this.voidGen = true;
        var0 = 0;

        for(FlatLayerInfo var3 : this.layersInfo) {
            for(int var4 = var3.getStart(); var4 < var3.getStart() + var3.getHeight(); ++var4) {
                BlockState var5 = var3.getBlockState();
                if (var5.getBlock() != Blocks.AIR) {
                    this.voidGen = false;
                    this.layers[var4] = var5;
                }
            }

            if (var3.getBlockState().getBlock() == Blocks.AIR) {
                var0 += var3.getHeight();
            } else {
                this.seaLevel += var3.getHeight() + var0;
                var0 = 0;
            }
        }

    }

    @Override
    public String toString() {
        StringBuilder var0 = new StringBuilder();

        for(int var1 = 0; var1 < this.layersInfo.size(); ++var1) {
            if (var1 > 0) {
                var0.append(",");
            }

            var0.append(this.layersInfo.get(var1));
        }

        var0.append(";");
        var0.append(Registry.BIOME.getKey(this.biome));
        var0.append(";");
        if (!this.structuresOptions.isEmpty()) {
            int var2 = 0;

            for(Entry<String, Map<String, String>> var3 : this.structuresOptions.entrySet()) {
                if (var2++ > 0) {
                    var0.append(",");
                }

                var0.append(var3.getKey().toLowerCase(Locale.ROOT));
                Map<String, String> var4 = var3.getValue();
                if (!var4.isEmpty()) {
                    var0.append("(");
                    int var5 = 0;

                    for(Entry<String, String> var6 : var4.entrySet()) {
                        if (var5++ > 0) {
                            var0.append(" ");
                        }

                        var0.append(var6.getKey());
                        var0.append("=");
                        var0.append(var6.getValue());
                    }

                    var0.append(")");
                }
            }
        }

        return var0.toString();
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    private static FlatLayerInfo getLayerInfoFromString(String param0, int param1) {
        String[] var0 = param0.split("\\*", 2);
        int var1;
        if (var0.length == 2) {
            try {
                var1 = Math.max(Integer.parseInt(var0[0]), 0);
            } catch (NumberFormatException var91) {
                LOGGER.error("Error while parsing flat world string => {}", var91.getMessage());
                return null;
            }
        } else {
            var1 = 1;
        }

        int var4 = Math.min(param1 + var1, 256);
        int var5 = var4 - param1;

        Block var6;
        try {
            var6 = byString(var0[var0.length - 1]);
        } catch (Exception var81) {
            LOGGER.error("Error while parsing flat world string => {}", var81.getMessage());
            return null;
        }

        if (var6 == null) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", var0[var0.length - 1]);
            return null;
        } else {
            FlatLayerInfo var9 = new FlatLayerInfo(var5, var6);
            var9.setStart(param1);
            return var9;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private static List<FlatLayerInfo> getLayersInfoFromString(String param0) {
        List<FlatLayerInfo> var0 = Lists.newArrayList();
        String[] var1 = param0.split(",");
        int var2 = 0;

        for(String var3 : var1) {
            FlatLayerInfo var4 = getLayerInfoFromString(var3, var2);
            if (var4 == null) {
                return Collections.emptyList();
            }

            var0.add(var4);
            var2 += var4.getHeight();
        }

        return var0;
    }

    @OnlyIn(Dist.CLIENT)
    public <T> Dynamic<T> toObject(DynamicOps<T> param0) {
        T var0 = param0.createList(
            this.layersInfo
                .stream()
                .map(
                    param1 -> param0.createMap(
                            ImmutableMap.of(
                                param0.createString("height"),
                                param0.createInt(param1.getHeight()),
                                param0.createString("block"),
                                param0.createString(Registry.BLOCK.getKey(param1.getBlockState().getBlock()).toString())
                            )
                        )
                )
        );
        T var1 = param0.createMap(
            this.structuresOptions
                .entrySet()
                .stream()
                .map(
                    param1 -> Pair.of(
                            param0.createString(param1.getKey().toLowerCase(Locale.ROOT)),
                            param0.createMap(
                                param1.getValue()
                                    .entrySet()
                                    .stream()
                                    .map(param1x -> Pair.of(param0.createString(param1x.getKey()), param0.createString(param1x.getValue())))
                                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                            )
                        )
                )
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
        );
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("layers"),
                    var0,
                    param0.createString("biome"),
                    param0.createString(Registry.BIOME.getKey(this.biome).toString()),
                    param0.createString("structures"),
                    var1
                )
            )
        );
    }

    public static FlatLevelGeneratorSettings fromObject(Dynamic<?> param0) {
        FlatLevelGeneratorSettings var0 = ChunkGeneratorType.FLAT.createSettings();
        List<Pair<Integer, Block>> var1 = param0.get("layers")
            .asList(param0x -> Pair.of(param0x.get("height").asInt(1), byString(param0x.get("block").asString(""))));
        if (var1.stream().anyMatch(param0x -> param0x.getSecond() == null)) {
            return getDefault();
        } else {
            List<FlatLayerInfo> var2 = var1.stream().map(param0x -> new FlatLayerInfo(param0x.getFirst(), param0x.getSecond())).collect(Collectors.toList());
            if (var2.isEmpty()) {
                return getDefault();
            } else {
                var0.getLayersInfo().addAll(var2);
                var0.updateLayers();
                var0.setBiome(Registry.BIOME.get(new ResourceLocation(param0.get("biome").asString(""))));
                param0.get("structures")
                    .flatMap(Dynamic::getMapValues)
                    .ifPresent(
                        param1 -> param1.keySet()
                                .forEach(param1x -> param1x.asString().map(param1xx -> var0.getStructuresOptions().put(param1xx, Maps.newHashMap())))
                    );
                return var0;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static FlatLevelGeneratorSettings fromString(String param0) {
        Iterator<String> var0 = Splitter.on(';').split(param0).iterator();
        if (!var0.hasNext()) {
            return getDefault();
        } else {
            FlatLevelGeneratorSettings var1 = ChunkGeneratorType.FLAT.createSettings();
            List<FlatLayerInfo> var2 = getLayersInfoFromString(var0.next());
            if (var2.isEmpty()) {
                return getDefault();
            } else {
                var1.getLayersInfo().addAll(var2);
                var1.updateLayers();
                Biome var3 = Biomes.PLAINS;
                if (var0.hasNext()) {
                    try {
                        ResourceLocation var4 = new ResourceLocation(var0.next());
                        var3 = Registry.BIOME.getOptional(var4).orElseThrow(() -> new IllegalArgumentException("Invalid Biome: " + var4));
                    } catch (Exception var17) {
                        LOGGER.error("Error while parsing flat world string => {}", var17.getMessage());
                    }
                }

                var1.setBiome(var3);
                if (var0.hasNext()) {
                    String[] var6 = var0.next().toLowerCase(Locale.ROOT).split(",");

                    for(String var7 : var6) {
                        String[] var8 = var7.split("\\(", 2);
                        if (!var8[0].isEmpty()) {
                            var1.addStructure(var8[0]);
                            if (var8.length > 1 && var8[1].endsWith(")") && var8[1].length() > 1) {
                                String[] var9 = var8[1].substring(0, var8[1].length() - 1).split(" ");

                                for(String var10 : var9) {
                                    String[] var11 = var10.split("=", 2);
                                    if (var11.length == 2) {
                                        var1.addStructureOption(var8[0], var11[0], var11[1]);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    var1.getStructuresOptions().put("village", Maps.newHashMap());
                }

                return var1;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void addStructure(String param0) {
        Map<String, String> var0 = Maps.newHashMap();
        this.structuresOptions.put(param0, var0);
    }

    @OnlyIn(Dist.CLIENT)
    private void addStructureOption(String param0, String param1, String param2) {
        this.structuresOptions.get(param0).put(param1, param2);
        if ("village".equals(param0) && "distance".equals(param1)) {
            this.villagesSpacing = Mth.getInt(param2, this.villagesSpacing, 9);
        }

        if ("biome_1".equals(param0) && "distance".equals(param1)) {
            this.templesSpacing = Mth.getInt(param2, this.templesSpacing, 9);
        }

        if ("stronghold".equals(param0)) {
            if ("distance".equals(param1)) {
                this.strongholdsDistance = Mth.getInt(param2, this.strongholdsDistance, 1);
            } else if ("count".equals(param1)) {
                this.strongholdsCount = Mth.getInt(param2, this.strongholdsCount, 1);
            } else if ("spread".equals(param1)) {
                this.strongholdsSpread = Mth.getInt(param2, this.strongholdsSpread, 1);
            }
        }

        if ("oceanmonument".equals(param0)) {
            if ("separation".equals(param1)) {
                this.monumentsSeparation = Mth.getInt(param2, this.monumentsSeparation, 1);
            } else if ("spacing".equals(param1)) {
                this.monumentsSpacing = Mth.getInt(param2, this.monumentsSpacing, 1);
            }
        }

        if ("endcity".equals(param0) && "distance".equals(param1)) {
            this.endCitySpacing = Mth.getInt(param2, this.endCitySpacing, 1);
        }

        if ("mansion".equals(param0) && "distance".equals(param1)) {
            this.woodlandMansionSpacing = Mth.getInt(param2, this.woodlandMansionSpacing, 1);
        }

    }

    public static FlatLevelGeneratorSettings getDefault() {
        FlatLevelGeneratorSettings var0 = ChunkGeneratorType.FLAT.createSettings();
        var0.setBiome(Biomes.PLAINS);
        var0.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BEDROCK));
        var0.getLayersInfo().add(new FlatLayerInfo(2, Blocks.DIRT));
        var0.getLayersInfo().add(new FlatLayerInfo(1, Blocks.GRASS_BLOCK));
        var0.updateLayers();
        var0.getStructuresOptions().put("village", Maps.newHashMap());
        return var0;
    }

    public boolean isVoidGen() {
        return this.voidGen;
    }

    public BlockState[] getLayers() {
        return this.layers;
    }

    public void deleteLayer(int param0) {
        this.layers[param0] = null;
    }
}
