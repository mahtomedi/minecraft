package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.GsonHelper;

public class LevelDataGeneratorOptionsFix extends DataFix {
    static final Map<String, String> MAP = Util.make(Maps.newHashMap(), param0 -> {
        param0.put("0", "minecraft:ocean");
        param0.put("1", "minecraft:plains");
        param0.put("2", "minecraft:desert");
        param0.put("3", "minecraft:mountains");
        param0.put("4", "minecraft:forest");
        param0.put("5", "minecraft:taiga");
        param0.put("6", "minecraft:swamp");
        param0.put("7", "minecraft:river");
        param0.put("8", "minecraft:nether");
        param0.put("9", "minecraft:the_end");
        param0.put("10", "minecraft:frozen_ocean");
        param0.put("11", "minecraft:frozen_river");
        param0.put("12", "minecraft:snowy_tundra");
        param0.put("13", "minecraft:snowy_mountains");
        param0.put("14", "minecraft:mushroom_fields");
        param0.put("15", "minecraft:mushroom_field_shore");
        param0.put("16", "minecraft:beach");
        param0.put("17", "minecraft:desert_hills");
        param0.put("18", "minecraft:wooded_hills");
        param0.put("19", "minecraft:taiga_hills");
        param0.put("20", "minecraft:mountain_edge");
        param0.put("21", "minecraft:jungle");
        param0.put("22", "minecraft:jungle_hills");
        param0.put("23", "minecraft:jungle_edge");
        param0.put("24", "minecraft:deep_ocean");
        param0.put("25", "minecraft:stone_shore");
        param0.put("26", "minecraft:snowy_beach");
        param0.put("27", "minecraft:birch_forest");
        param0.put("28", "minecraft:birch_forest_hills");
        param0.put("29", "minecraft:dark_forest");
        param0.put("30", "minecraft:snowy_taiga");
        param0.put("31", "minecraft:snowy_taiga_hills");
        param0.put("32", "minecraft:giant_tree_taiga");
        param0.put("33", "minecraft:giant_tree_taiga_hills");
        param0.put("34", "minecraft:wooded_mountains");
        param0.put("35", "minecraft:savanna");
        param0.put("36", "minecraft:savanna_plateau");
        param0.put("37", "minecraft:badlands");
        param0.put("38", "minecraft:wooded_badlands_plateau");
        param0.put("39", "minecraft:badlands_plateau");
        param0.put("40", "minecraft:small_end_islands");
        param0.put("41", "minecraft:end_midlands");
        param0.put("42", "minecraft:end_highlands");
        param0.put("43", "minecraft:end_barrens");
        param0.put("44", "minecraft:warm_ocean");
        param0.put("45", "minecraft:lukewarm_ocean");
        param0.put("46", "minecraft:cold_ocean");
        param0.put("47", "minecraft:deep_warm_ocean");
        param0.put("48", "minecraft:deep_lukewarm_ocean");
        param0.put("49", "minecraft:deep_cold_ocean");
        param0.put("50", "minecraft:deep_frozen_ocean");
        param0.put("127", "minecraft:the_void");
        param0.put("129", "minecraft:sunflower_plains");
        param0.put("130", "minecraft:desert_lakes");
        param0.put("131", "minecraft:gravelly_mountains");
        param0.put("132", "minecraft:flower_forest");
        param0.put("133", "minecraft:taiga_mountains");
        param0.put("134", "minecraft:swamp_hills");
        param0.put("140", "minecraft:ice_spikes");
        param0.put("149", "minecraft:modified_jungle");
        param0.put("151", "minecraft:modified_jungle_edge");
        param0.put("155", "minecraft:tall_birch_forest");
        param0.put("156", "minecraft:tall_birch_hills");
        param0.put("157", "minecraft:dark_forest_hills");
        param0.put("158", "minecraft:snowy_taiga_mountains");
        param0.put("160", "minecraft:giant_spruce_taiga");
        param0.put("161", "minecraft:giant_spruce_taiga_hills");
        param0.put("162", "minecraft:modified_gravelly_mountains");
        param0.put("163", "minecraft:shattered_savanna");
        param0.put("164", "minecraft:shattered_savanna_plateau");
        param0.put("165", "minecraft:eroded_badlands");
        param0.put("166", "minecraft:modified_wooded_badlands_plateau");
        param0.put("167", "minecraft:modified_badlands_plateau");
    });
    public static final String GENERATOR_OPTIONS = "generatorOptions";

    public LevelDataGeneratorOptionsFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getOutputSchema().getType(References.LEVEL);
        return this.fixTypeEverywhereTyped(
            "LevelDataGeneratorOptionsFix", this.getInputSchema().getType(References.LEVEL), var0, param1 -> param1.write().flatMap(param1x -> {
                    Optional<String> var0x = param1x.get("generatorOptions").asString().result();
                    Dynamic<?> var2;
                    if ("flat".equalsIgnoreCase(param1x.get("generatorName").asString(""))) {
                        String var1 = var0x.orElse("");
                        var2 = param1x.set("generatorOptions", convert(var1, param1x.getOps()));
                    } else if ("buffet".equalsIgnoreCase(param1x.get("generatorName").asString("")) && var0x.isPresent()) {
                        Dynamic<JsonElement> var3 = new Dynamic<>(JsonOps.INSTANCE, GsonHelper.parse(var0x.get(), true));
                        var2 = param1x.set("generatorOptions", var3.convert(param1x.getOps()));
                    } else {
                        var2 = param1x;
                    }
    
                    return var0.readTyped(var2);
                }).map(Pair::getFirst).result().orElseThrow(() -> new IllegalStateException("Could not read new level type."))
        );
    }

    private static <T> Dynamic<T> convert(String param0, DynamicOps<T> param1) {
        Iterator<String> var0 = Splitter.on(';').split(param0).iterator();
        String var1 = "minecraft:plains";
        Map<String, Map<String, String>> var2 = Maps.newHashMap();
        List<Pair<Integer, String>> var3;
        if (!param0.isEmpty() && var0.hasNext()) {
            var3 = getLayersInfoFromString(var0.next());
            if (!var3.isEmpty()) {
                if (var0.hasNext()) {
                    var1 = MAP.getOrDefault(var0.next(), "minecraft:plains");
                }

                if (var0.hasNext()) {
                    String[] var4 = var0.next().toLowerCase(Locale.ROOT).split(",");

                    for(String var5 : var4) {
                        String[] var6 = var5.split("\\(", 2);
                        if (!var6[0].isEmpty()) {
                            var2.put(var6[0], Maps.newHashMap());
                            if (var6.length > 1 && var6[1].endsWith(")") && var6[1].length() > 1) {
                                String[] var7 = var6[1].substring(0, var6[1].length() - 1).split(" ");

                                for(String var8 : var7) {
                                    String[] var9 = var8.split("=", 2);
                                    if (var9.length == 2) {
                                        var2.get(var6[0]).put(var9[0], var9[1]);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    var2.put("village", Maps.newHashMap());
                }
            }
        } else {
            var3 = Lists.newArrayList();
            var3.add(Pair.of(1, "minecraft:bedrock"));
            var3.add(Pair.of(2, "minecraft:dirt"));
            var3.add(Pair.of(1, "minecraft:grass_block"));
            var2.put("village", Maps.newHashMap());
        }

        T var11 = param1.createList(
            var3.stream()
                .map(
                    param1x -> param1.createMap(
                            ImmutableMap.of(
                                param1.createString("height"),
                                param1.createInt(param1x.getFirst()),
                                param1.createString("block"),
                                param1.createString(param1x.getSecond())
                            )
                        )
                )
        );
        T var12 = param1.createMap(
            var2.entrySet()
                .stream()
                .map(
                    param1x -> Pair.of(
                            param1.createString(param1x.getKey().toLowerCase(Locale.ROOT)),
                            param1.createMap(
                                param1x.getValue()
                                    .entrySet()
                                    .stream()
                                    .map(param1xx -> Pair.of(param1.createString(param1xx.getKey()), param1.createString(param1xx.getValue())))
                                    .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
                            )
                        )
                )
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
        );
        return new Dynamic<>(
            param1,
            param1.createMap(
                ImmutableMap.of(
                    param1.createString("layers"), var11, param1.createString("biome"), param1.createString(var1), param1.createString("structures"), var12
                )
            )
        );
    }

    @Nullable
    private static Pair<Integer, String> getLayerInfoFromString(String param0) {
        String[] var0 = param0.split("\\*", 2);
        int var1;
        if (var0.length == 2) {
            try {
                var1 = Integer.parseInt(var0[0]);
            } catch (NumberFormatException var41) {
                return null;
            }
        } else {
            var1 = 1;
        }

        String var4 = var0[var0.length - 1];
        return Pair.of(var1, var4);
    }

    private static List<Pair<Integer, String>> getLayersInfoFromString(String param0) {
        List<Pair<Integer, String>> var0 = Lists.newArrayList();
        String[] var1 = param0.split(",");

        for(String var2 : var1) {
            Pair<Integer, String> var3 = getLayerInfoFromString(var2);
            if (var3 == null) {
                return Collections.emptyList();
            }

            var0.add(var3);
        }

        return var0;
    }
}
