package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicLike;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

public class WorldGenSettingsFix extends DataFix {
    private static final String VILLAGE = "minecraft:village";
    private static final String DESERT_PYRAMID = "minecraft:desert_pyramid";
    private static final String IGLOO = "minecraft:igloo";
    private static final String JUNGLE_TEMPLE = "minecraft:jungle_pyramid";
    private static final String SWAMP_HUT = "minecraft:swamp_hut";
    private static final String PILLAGER_OUTPOST = "minecraft:pillager_outpost";
    private static final String END_CITY = "minecraft:endcity";
    private static final String WOODLAND_MANSION = "minecraft:mansion";
    private static final String OCEAN_MONUMENT = "minecraft:monument";
    private static final ImmutableMap<String, WorldGenSettingsFix.StructureFeatureConfiguration> DEFAULTS = ImmutableMap.<String, WorldGenSettingsFix.StructureFeatureConfiguration>builder(
            
        )
        .put("minecraft:village", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 10387312))
        .put("minecraft:desert_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357617))
        .put("minecraft:igloo", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357618))
        .put("minecraft:jungle_pyramid", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357619))
        .put("minecraft:swamp_hut", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 14357620))
        .put("minecraft:pillager_outpost", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 8, 165745296))
        .put("minecraft:monument", new WorldGenSettingsFix.StructureFeatureConfiguration(32, 5, 10387313))
        .put("minecraft:endcity", new WorldGenSettingsFix.StructureFeatureConfiguration(20, 11, 10387313))
        .put("minecraft:mansion", new WorldGenSettingsFix.StructureFeatureConfiguration(80, 20, 10387319))
        .build();

    public WorldGenSettingsFix(Schema param0) {
        super(param0, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(
            "WorldGenSettings building",
            this.getInputSchema().getType(References.WORLD_GEN_SETTINGS),
            param0 -> param0.update(DSL.remainderFinder(), WorldGenSettingsFix::fix)
        );
    }

    private static <T> Dynamic<T> noise(long param0, DynamicLike<T> param1, Dynamic<T> param2, Dynamic<T> param3) {
        return param1.createMap(
            ImmutableMap.of(
                param1.createString("type"),
                param1.createString("minecraft:noise"),
                param1.createString("biome_source"),
                param3,
                param1.createString("seed"),
                param1.createLong(param0),
                param1.createString("settings"),
                param2
            )
        );
    }

    private static <T> Dynamic<T> vanillaBiomeSource(Dynamic<T> param0, long param1, boolean param2, boolean param3) {
        Builder<Dynamic<T>, Dynamic<T>> var0 = ImmutableMap.<Dynamic<T>, Dynamic<T>>builder()
            .put(param0.createString("type"), param0.createString("minecraft:vanilla_layered"))
            .put(param0.createString("seed"), param0.createLong(param1))
            .put(param0.createString("large_biomes"), param0.createBoolean(param3));
        if (param2) {
            var0.put(param0.createString("legacy_biome_init_layer"), param0.createBoolean(param2));
        }

        return param0.createMap(var0.build());
    }

    private static <T> Dynamic<T> fix(Dynamic<T> param0) {
        DynamicOps<T> var0 = param0.getOps();
        long var1 = param0.get("RandomSeed").asLong(0L);
        Optional<String> var2 = param0.get("generatorName").asString().map(param0x -> param0x.toLowerCase(Locale.ROOT)).result();
        Optional<String> var3 = param0.get("legacy_custom_options")
            .asString()
            .result()
            .map(Optional::of)
            .orElseGet(() -> var2.equals(Optional.of("customized")) ? param0.get("generatorOptions").asString().result() : Optional.empty());
        boolean var4 = false;
        Dynamic<T> var5;
        if (var2.equals(Optional.of("customized"))) {
            var5 = defaultOverworld(param0, var1);
        } else if (!var2.isPresent()) {
            var5 = defaultOverworld(param0, var1);
        } else {
            String var27 = var2.get();
            switch(var27) {
                case "flat":
                    OptionalDynamic<T> var7 = param0.get("generatorOptions");
                    Map<Dynamic<T>, Dynamic<T>> var8x = fixFlatStructures(var0, var7);
                    var5 = param0.createMap(
                        ImmutableMap.of(
                            param0.createString("type"),
                            param0.createString("minecraft:flat"),
                            param0.createString("settings"),
                            param0.createMap(
                                ImmutableMap.of(
                                    param0.createString("structures"),
                                    param0.createMap(var8x),
                                    param0.createString("layers"),
                                    var7.get("layers")
                                        .result()
                                        .orElseGet(
                                            () -> param0.createList(
                                                    Stream.of(
                                                        param0.createMap(
                                                            ImmutableMap.of(
                                                                param0.createString("height"),
                                                                param0.createInt(1),
                                                                param0.createString("block"),
                                                                param0.createString("minecraft:bedrock")
                                                            )
                                                        ),
                                                        param0.createMap(
                                                            ImmutableMap.of(
                                                                param0.createString("height"),
                                                                param0.createInt(2),
                                                                param0.createString("block"),
                                                                param0.createString("minecraft:dirt")
                                                            )
                                                        ),
                                                        param0.createMap(
                                                            ImmutableMap.of(
                                                                param0.createString("height"),
                                                                param0.createInt(1),
                                                                param0.createString("block"),
                                                                param0.createString("minecraft:grass_block")
                                                            )
                                                        )
                                                    )
                                                )
                                        ),
                                    param0.createString("biome"),
                                    param0.createString(var7.get("biome").asString("minecraft:plains"))
                                )
                            )
                        )
                    );
                    break;
                case "debug_all_block_states":
                    var5 = param0.createMap(ImmutableMap.of(param0.createString("type"), param0.createString("minecraft:debug")));
                    break;
                case "buffet":
                    OptionalDynamic<T> var11 = param0.get("generatorOptions");
                    OptionalDynamic<?> var12 = var11.get("chunk_generator");
                    Optional<String> var13 = var12.get("type").asString().result();
                    Dynamic<T> var14;
                    if (Objects.equals(var13, Optional.of("minecraft:caves"))) {
                        var14 = param0.createString("minecraft:caves");
                        var4 = true;
                    } else if (Objects.equals(var13, Optional.of("minecraft:floating_islands"))) {
                        var14 = param0.createString("minecraft:floating_islands");
                    } else {
                        var14 = param0.createString("minecraft:overworld");
                    }

                    Dynamic<T> var17 = var11.get("biome_source")
                        .result()
                        .orElseGet(() -> param0.createMap(ImmutableMap.of(param0.createString("type"), param0.createString("minecraft:fixed"))));
                    Dynamic<T> var19;
                    if (var17.get("type").asString().result().equals(Optional.of("minecraft:fixed"))) {
                        String var18 = var17.get("options")
                            .get("biomes")
                            .asStream()
                            .findFirst()
                            .flatMap(param0x -> param0x.asString().result())
                            .orElse("minecraft:ocean");
                        var19 = var17.remove("options").set("biome", param0.createString(var18));
                    } else {
                        var19 = var17;
                    }

                    var5 = noise(var1, param0, var14, var19);
                    break;
                default:
                    boolean var22 = var2.get().equals("default");
                    boolean var23 = var2.get().equals("default_1_1") || var22 && param0.get("generatorVersion").asInt(0) == 0;
                    boolean var24 = var2.get().equals("amplified");
                    boolean var25 = var2.get().equals("largebiomes");
                    var5 = noise(
                        var1,
                        param0,
                        param0.createString(var24 ? "minecraft:amplified" : "minecraft:overworld"),
                        vanillaBiomeSource(param0, var1, var23, var25)
                    );
            }
        }

        boolean var27 = param0.get("MapFeatures").asBoolean(true);
        boolean var28 = param0.get("BonusChest").asBoolean(false);
        Builder<T, T> var29 = ImmutableMap.builder();
        var29.put(var0.createString("seed"), var0.createLong(var1));
        var29.put(var0.createString("generate_features"), var0.createBoolean(var27));
        var29.put(var0.createString("bonus_chest"), var0.createBoolean(var28));
        var29.put(var0.createString("dimensions"), vanillaLevels(param0, var1, var5, var4));
        var3.ifPresent(param2 -> var29.put(var0.createString("legacy_custom_options"), var0.createString(param2)));
        return new Dynamic<>(var0, var0.createMap(var29.build()));
    }

    protected static <T> Dynamic<T> defaultOverworld(Dynamic<T> param0, long param1) {
        return noise(param1, param0, param0.createString("minecraft:overworld"), vanillaBiomeSource(param0, param1, false, false));
    }

    protected static <T> T vanillaLevels(Dynamic<T> param0, long param1, Dynamic<T> param2, boolean param3) {
        DynamicOps<T> var0 = param0.getOps();
        return var0.createMap(
            ImmutableMap.of(
                var0.createString("minecraft:overworld"),
                var0.createMap(
                    ImmutableMap.of(
                        var0.createString("type"),
                        var0.createString("minecraft:overworld" + (param3 ? "_caves" : "")),
                        var0.createString("generator"),
                        param2.getValue()
                    )
                ),
                var0.createString("minecraft:the_nether"),
                var0.createMap(
                    ImmutableMap.of(
                        var0.createString("type"),
                        var0.createString("minecraft:the_nether"),
                        var0.createString("generator"),
                        noise(
                                param1,
                                param0,
                                param0.createString("minecraft:nether"),
                                param0.createMap(
                                    ImmutableMap.of(
                                        param0.createString("type"),
                                        param0.createString("minecraft:multi_noise"),
                                        param0.createString("seed"),
                                        param0.createLong(param1),
                                        param0.createString("preset"),
                                        param0.createString("minecraft:nether")
                                    )
                                )
                            )
                            .getValue()
                    )
                ),
                var0.createString("minecraft:the_end"),
                var0.createMap(
                    ImmutableMap.of(
                        var0.createString("type"),
                        var0.createString("minecraft:the_end"),
                        var0.createString("generator"),
                        noise(
                                param1,
                                param0,
                                param0.createString("minecraft:end"),
                                param0.createMap(
                                    ImmutableMap.of(
                                        param0.createString("type"),
                                        param0.createString("minecraft:the_end"),
                                        param0.createString("seed"),
                                        param0.createLong(param1)
                                    )
                                )
                            )
                            .getValue()
                    )
                )
            )
        );
    }

    private static <T> Map<Dynamic<T>, Dynamic<T>> fixFlatStructures(DynamicOps<T> param0, OptionalDynamic<T> param1) {
        MutableInt var0 = new MutableInt(32);
        MutableInt var1 = new MutableInt(3);
        MutableInt var2 = new MutableInt(128);
        MutableBoolean var3 = new MutableBoolean(false);
        Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> var4 = Maps.newHashMap();
        if (!param1.result().isPresent()) {
            var3.setTrue();
            var4.put("minecraft:village", DEFAULTS.get("minecraft:village"));
        }

        param1.get("structures")
            .flatMap(Dynamic::getMapValues)
            .result()
            .ifPresent(
                param5 -> param5.forEach(
                        (param5x, param6) -> param6.getMapValues()
                                .result()
                                .ifPresent(
                                    param6x -> param6x.forEach(
                                            (param6xx, param7) -> {
                                                String var0x = param5x.asString("");
                                                String var1x = param6xx.asString("");
                                                String var2x = param7.asString("");
                                                if ("stronghold".equals(var0x)) {
                                                    var3.setTrue();
                                                    switch(var1x) {
                                                        case "distance":
                                                            var0.setValue(getInt(var2x, var0.getValue(), 1));
                                                            return;
                                                        case "spread":
                                                            var1.setValue(getInt(var2x, var1.getValue(), 1));
                                                            return;
                                                        case "count":
                                                            var2.setValue(getInt(var2x, var2.getValue(), 1));
                                                            return;
                                                    }
                                                } else {
                                                    switch(var1x) {
                                                        case "distance":
                                                            switch(var0x) {
                                                                case "village":
                                                                    setSpacing(var4, "minecraft:village", var2x, 9);
                                                                    return;
                                                                case "biome_1":
                                                                    setSpacing(var4, "minecraft:desert_pyramid", var2x, 9);
                                                                    setSpacing(var4, "minecraft:igloo", var2x, 9);
                                                                    setSpacing(var4, "minecraft:jungle_pyramid", var2x, 9);
                                                                    setSpacing(var4, "minecraft:swamp_hut", var2x, 9);
                                                                    setSpacing(var4, "minecraft:pillager_outpost", var2x, 9);
                                                                    return;
                                                                case "endcity":
                                                                    setSpacing(var4, "minecraft:endcity", var2x, 1);
                                                                    return;
                                                                case "mansion":
                                                                    setSpacing(var4, "minecraft:mansion", var2x, 1);
                                                                    return;
                                                                default:
                                                                    return;
                                                            }
                                                        case "separation":
                                                            if ("oceanmonument".equals(var0x)) {
                                                                WorldGenSettingsFix.StructureFeatureConfiguration var3x = var4.getOrDefault(
                                                                    "minecraft:monument", DEFAULTS.get("minecraft:monument")
                                                                );
                                                                int var4x = getInt(var2x, var3x.separation, 1);
                                                                var4.put(
                                                                    "minecraft:monument",
                                                                    new WorldGenSettingsFix.StructureFeatureConfiguration(var4x, var3x.separation, var3x.salt)
                                                                );
                                                            }
                        
                                                            return;
                                                        case "spacing":
                                                            if ("oceanmonument".equals(var0x)) {
                                                                setSpacing(var4, "minecraft:monument", var2x, 1);
                                                            }
                        
                                                            return;
                                                    }
                                                }
                                            }
                                        )
                                )
                    )
            );
        Builder<Dynamic<T>, Dynamic<T>> var5 = ImmutableMap.builder();
        var5.put(
            param1.createString("structures"),
            param1.createMap(
                var4.entrySet()
                    .stream()
                    .collect(Collectors.toMap(param1x -> param1.createString(param1x.getKey()), param1x -> param1x.getValue().serialize(param0)))
            )
        );
        if (var3.isTrue()) {
            var5.put(
                param1.createString("stronghold"),
                param1.createMap(
                    ImmutableMap.of(
                        param1.createString("distance"),
                        param1.createInt(var0.getValue()),
                        param1.createString("spread"),
                        param1.createInt(var1.getValue()),
                        param1.createString("count"),
                        param1.createInt(var2.getValue())
                    )
                )
            );
        }

        return var5.build();
    }

    private static int getInt(String param0, int param1) {
        return NumberUtils.toInt(param0, param1);
    }

    private static int getInt(String param0, int param1, int param2) {
        return Math.max(param2, getInt(param0, param1));
    }

    private static void setSpacing(Map<String, WorldGenSettingsFix.StructureFeatureConfiguration> param0, String param1, String param2, int param3) {
        WorldGenSettingsFix.StructureFeatureConfiguration var0 = param0.getOrDefault(param1, DEFAULTS.get(param1));
        int var1 = getInt(param2, var0.spacing, param3);
        param0.put(param1, new WorldGenSettingsFix.StructureFeatureConfiguration(var1, var0.separation, var0.salt));
    }

    static final class StructureFeatureConfiguration {
        public static final Codec<WorldGenSettingsFix.StructureFeatureConfiguration> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.INT.fieldOf("spacing").forGetter(param0x -> param0x.spacing),
                        Codec.INT.fieldOf("separation").forGetter(param0x -> param0x.separation),
                        Codec.INT.fieldOf("salt").forGetter(param0x -> param0x.salt)
                    )
                    .apply(param0, WorldGenSettingsFix.StructureFeatureConfiguration::new)
        );
        final int spacing;
        final int separation;
        final int salt;

        public StructureFeatureConfiguration(int param0, int param1, int param2) {
            this.spacing = param0;
            this.separation = param1;
            this.salt = param2;
        }

        public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
            return new Dynamic<>(param0, CODEC.encodeStart(param0, this).result().orElse(param0.emptyMap()));
        }
    }
}
