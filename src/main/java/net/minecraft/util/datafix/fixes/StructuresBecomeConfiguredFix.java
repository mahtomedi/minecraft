package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;

public class StructuresBecomeConfiguredFix extends DataFix {
    private static final Map<String, StructuresBecomeConfiguredFix.Conversion> CONVERSION_MAP = ImmutableMap.<String, StructuresBecomeConfiguredFix.Conversion>builder(
            
        )
        .put(
            "mineshaft",
            StructuresBecomeConfiguredFix.Conversion.biomeMapped(
                Map.of(List.of("minecraft:badlands", "minecraft:eroded_badlands", "minecraft:wooded_badlands"), "minecraft:mineshaft_mesa"),
                "minecraft:mineshaft"
            )
        )
        .put(
            "shipwreck",
            StructuresBecomeConfiguredFix.Conversion.biomeMapped(
                Map.of(List.of("minecraft:beach", "minecraft:snowy_beach"), "minecraft:shipwreck_beached"), "minecraft:shipwreck"
            )
        )
        .put(
            "ocean_ruin",
            StructuresBecomeConfiguredFix.Conversion.biomeMapped(
                Map.of(List.of("minecraft:warm_ocean", "minecraft:lukewarm_ocean", "minecraft:deep_lukewarm_ocean"), "minecraft:ocean_ruin_warm"),
                "minecraft:ocean_ruin_cold"
            )
        )
        .put(
            "village",
            StructuresBecomeConfiguredFix.Conversion.biomeMapped(
                Map.of(
                    List.of("minecraft:desert"),
                    "minecraft:village_desert",
                    List.of("minecraft:savanna"),
                    "minecraft:village_savanna",
                    List.of("minecraft:snowy_plains"),
                    "minecraft:village_snowy",
                    List.of("minecraft:taiga"),
                    "minecraft:village_taiga"
                ),
                "minecraft:village_plains"
            )
        )
        .put(
            "ruined_portal",
            StructuresBecomeConfiguredFix.Conversion.biomeMapped(
                Map.of(
                    List.of("minecraft:desert"),
                    "minecraft:ruined_portal_desert",
                    List.of(
                        "minecraft:badlands",
                        "minecraft:eroded_badlands",
                        "minecraft:wooded_badlands",
                        "minecraft:windswept_hills",
                        "minecraft:windswept_forest",
                        "minecraft:windswept_gravelly_hills",
                        "minecraft:savanna_plateau",
                        "minecraft:windswept_savanna",
                        "minecraft:stony_shore",
                        "minecraft:meadow",
                        "minecraft:frozen_peaks",
                        "minecraft:jagged_peaks",
                        "minecraft:stony_peaks",
                        "minecraft:snowy_slopes"
                    ),
                    "minecraft:ruined_portal_mountain",
                    List.of("minecraft:bamboo_jungle", "minecraft:jungle", "minecraft:sparse_jungle"),
                    "minecraft:ruined_portal_jungle",
                    List.of(
                        "minecraft:deep_frozen_ocean",
                        "minecraft:deep_cold_ocean",
                        "minecraft:deep_ocean",
                        "minecraft:deep_lukewarm_ocean",
                        "minecraft:frozen_ocean",
                        "minecraft:ocean",
                        "minecraft:cold_ocean",
                        "minecraft:lukewarm_ocean",
                        "minecraft:warm_ocean"
                    ),
                    "minecraft:ruined_portal_ocean"
                ),
                "minecraft:ruined_portal_standard"
            )
        )
        .put("pillager_outpost", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:pillager_outpost"))
        .put("mansion", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:mansion"))
        .put("jungle_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:jungle_pyramid"))
        .put("desert_pyramid", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:desert_pyramid"))
        .put("igloo", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:igloo"))
        .put("swamp_hut", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:swamp_hut"))
        .put("stronghold", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:stronghold"))
        .put("monument", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:monument"))
        .put("fortress", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:fortress"))
        .put("endcity", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:end_city"))
        .put("buried_treasure", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:buried_treasure"))
        .put("nether_fossil", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:nether_fossil"))
        .put("bastion_remnant", StructuresBecomeConfiguredFix.Conversion.trivial("minecraft:bastion_remnant"))
        .build();

    public StructuresBecomeConfiguredFix(Schema param0) {
        super(param0, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.CHUNK);
        Type<?> var1 = this.getInputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("StucturesToConfiguredStructures", var0, var1, this::fix);
    }

    private Dynamic<?> fix(Dynamic<?> param0) {
        return param0.update(
            "structures",
            param1 -> param1.update("starts", param1x -> this.updateStarts(param1x, param0))
                    .update("References", param1x -> this.updateReferences(param1x, param0))
        );
    }

    private Dynamic<?> updateStarts(Dynamic<?> param0, Dynamic<?> param1) {
        Map<? extends Dynamic<?>, ? extends Dynamic<?>> var0 = param0.getMapValues().result().get();
        List<Dynamic<?>> var1 = new ArrayList<>();
        var0.forEach((param1x, param2) -> {
            if (param2.get("id").asString("INVALID").equals("INVALID")) {
                var1.add(param1x);
            }

        });

        for(Dynamic<?> var2 : var1) {
            param0 = param0.remove(var2.asString(""));
        }

        return param0.updateMapValues(param1x -> this.updateStart(param1x, param1));
    }

    private Pair<Dynamic<?>, Dynamic<?>> updateStart(Pair<Dynamic<?>, Dynamic<?>> param0, Dynamic<?> param1) {
        Dynamic<?> var0 = this.findUpdatedStructureType(param0, param1);
        return new Pair<>(var0, param0.getSecond().set("id", var0));
    }

    private Dynamic<?> updateReferences(Dynamic<?> param0, Dynamic<?> param1) {
        Map<? extends Dynamic<?>, ? extends Dynamic<?>> var0 = param0.getMapValues().result().get();
        List<Dynamic<?>> var1 = new ArrayList<>();
        var0.forEach((param1x, param2) -> {
            if (param2.asLongStream().count() == 0L) {
                var1.add(param1x);
            }

        });

        for(Dynamic<?> var2 : var1) {
            param0 = param0.remove(var2.asString(""));
        }

        return param0.updateMapValues(param1x -> this.updateReference(param1x, param1));
    }

    private Pair<Dynamic<?>, Dynamic<?>> updateReference(Pair<Dynamic<?>, Dynamic<?>> param0, Dynamic<?> param1) {
        return param0.mapFirst(param2 -> this.findUpdatedStructureType(param0, param1));
    }

    private Dynamic<?> findUpdatedStructureType(Pair<Dynamic<?>, Dynamic<?>> param0, Dynamic<?> param1) {
        String var0 = param0.getFirst().asString("UNKNOWN").toLowerCase(Locale.ROOT);
        StructuresBecomeConfiguredFix.Conversion var1 = CONVERSION_MAP.get(var0);
        if (var1 == null) {
            throw new IllegalStateException("Found unknown structure: " + var0);
        } else {
            Dynamic<?> var2 = param0.getSecond();
            String var3 = var1.fallback;
            if (!var1.biomeMapping().isEmpty()) {
                Optional<String> var4 = this.guessConfiguration(param1, var1);
                if (var4.isPresent()) {
                    var3 = var4.get();
                }
            }

            Dynamic<?> var5 = var2.createString(var3);
            return var5;
        }
    }

    private Optional<String> guessConfiguration(Dynamic<?> param0, StructuresBecomeConfiguredFix.Conversion param1) {
        Object2IntArrayMap<String> var0 = new Object2IntArrayMap<>();
        param0.get("sections")
            .asList(Function.identity())
            .forEach(param2 -> param2.get("biomes").get("palette").asList(Function.identity()).forEach(param2x -> {
                    String var0x = param1.biomeMapping().get(param2x.asString(""));
                    if (var0x != null) {
                        var0.mergeInt(var0x, 1, Integer::sum);
                    }
    
                }));
        return var0.object2IntEntrySet()
            .stream()
            .max(Comparator.comparingInt(it.unimi.dsi.fastutil.objects.Object2IntMap.Entry::getIntValue))
            .map(Entry::getKey);
    }

    static record Conversion(Map<String, String> biomeMapping, String fallback) {
        public static StructuresBecomeConfiguredFix.Conversion trivial(String param0) {
            return new StructuresBecomeConfiguredFix.Conversion(Map.of(), param0);
        }

        public static StructuresBecomeConfiguredFix.Conversion biomeMapped(Map<List<String>, String> param0, String param1) {
            return new StructuresBecomeConfiguredFix.Conversion(unpack(param0), param1);
        }

        private static Map<String, String> unpack(Map<List<String>, String> param0) {
            Builder<String, String> var0 = ImmutableMap.builder();

            for(Entry<List<String>, String> var1 : param0.entrySet()) {
                var1.getKey().forEach(param2 -> var0.put(param2, var1.getValue()));
            }

            return var0.build();
        }
    }
}
