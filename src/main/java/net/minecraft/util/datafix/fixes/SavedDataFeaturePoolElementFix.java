package net.minecraft.util.datafix.fixes;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SavedDataFeaturePoolElementFix extends DataFix {
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d+)\\]");
    private static final Set<String> PIECE_TYPE = Sets.newHashSet(
        "minecraft:jigsaw", "minecraft:nvi", "minecraft:pcp", "minecraft:bastionremnant", "minecraft:runtime"
    );
    private static final Set<String> FEATURES = Sets.newHashSet("minecraft:tree", "minecraft:flower", "minecraft:block_pile", "minecraft:random_patch");

    public SavedDataFeaturePoolElementFix(Schema param0) {
        super(param0, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.writeFixAndRead(
            "SavedDataFeaturePoolElementFix",
            this.getInputSchema().getType(References.STRUCTURE_FEATURE),
            this.getOutputSchema().getType(References.STRUCTURE_FEATURE),
            SavedDataFeaturePoolElementFix::fixTag
        );
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> param0) {
        return param0.update("Children", SavedDataFeaturePoolElementFix::updateChildren);
    }

    private static <T> Dynamic<T> updateChildren(Dynamic<T> param0x) {
        return param0x.asStreamOpt().map(SavedDataFeaturePoolElementFix::updateChildren).map(param0x::createList).result().orElse(param0x);
    }

    private static Stream<? extends Dynamic<?>> updateChildren(Stream<? extends Dynamic<?>> param0x) {
        return param0x.map(
            (Function<? super Dynamic<?>, ? extends Dynamic<?>>)(param0xx -> {
                String var0x = param0xx.get("id").asString("");
                if (!PIECE_TYPE.contains(var0x)) {
                    return param0xx;
                } else {
                    OptionalDynamic<?> var1 = param0xx.get("pool_element");
                    return !var1.get("element_type").asString("").equals("minecraft:feature_pool_element")
                        ? param0xx
                        : param0xx.update("pool_element", param0xxx -> param0xxx.update("feature", SavedDataFeaturePoolElementFix::fixFeature));
                }
            })
        );
    }

    private static <T> OptionalDynamic<T> get(Dynamic<T> param0, String... param1) {
        if (param1.length == 0) {
            throw new IllegalArgumentException("Missing path");
        } else {
            OptionalDynamic<T> var0 = param0.get(param1[0]);

            for(int var1 = 1; var1 < param1.length; ++var1) {
                String var2 = param1[var1];
                Matcher var3 = INDEX_PATTERN.matcher(var2);
                if (var3.matches()) {
                    int var4 = Integer.parseInt(var3.group(1));
                    List<? extends Dynamic<T>> var5 = var0.asList(Function.identity());
                    if (var4 >= 0 && var4 < var5.size()) {
                        var0 = new OptionalDynamic<>(param0.getOps(), DataResult.success(var5.get(var4)));
                    } else {
                        var0 = new OptionalDynamic<>(param0.getOps(), DataResult.error(() -> "Missing id:" + var4));
                    }
                } else {
                    var0 = var0.get(var2);
                }
            }

            return var0;
        }
    }

    @VisibleForTesting
    protected static Dynamic<?> fixFeature(Dynamic<?> param0) {
        Optional<String> var0 = getReplacement(
            get(param0, "type").asString(""),
            get(param0, "name").asString(""),
            get(param0, "config", "state_provider", "type").asString(""),
            get(param0, "config", "state_provider", "state", "Name").asString(""),
            get(param0, "config", "state_provider", "entries", "[0]", "data", "Name").asString(""),
            get(param0, "config", "foliage_placer", "type").asString(""),
            get(param0, "config", "leaves_provider", "state", "Name").asString("")
        );
        return var0.isPresent() ? param0.createString(var0.get()) : param0;
    }

    private static Optional<String> getReplacement(String param0, String param1, String param2, String param3, String param4, String param5, String param6) {
        String var0;
        if (!param0.isEmpty()) {
            var0 = param0;
        } else {
            if (param1.isEmpty()) {
                return Optional.empty();
            }

            if ("minecraft:normal_tree".equals(param1)) {
                var0 = "minecraft:tree";
            } else {
                var0 = param1;
            }
        }

        if (FEATURES.contains(var0)) {
            if ("minecraft:random_patch".equals(var0)) {
                if ("minecraft:simple_state_provider".equals(param2)) {
                    if ("minecraft:sweet_berry_bush".equals(param3)) {
                        return Optional.of("minecraft:patch_berry_bush");
                    }

                    if ("minecraft:cactus".equals(param3)) {
                        return Optional.of("minecraft:patch_cactus");
                    }
                } else if ("minecraft:weighted_state_provider".equals(param2) && ("minecraft:grass".equals(param4) || "minecraft:fern".equals(param4))) {
                    return Optional.of("minecraft:patch_taiga_grass");
                }
            } else if ("minecraft:block_pile".equals(var0)) {
                if (!"minecraft:simple_state_provider".equals(param2) && !"minecraft:rotated_block_provider".equals(param2)) {
                    if ("minecraft:weighted_state_provider".equals(param2)) {
                        if ("minecraft:packed_ice".equals(param4) || "minecraft:blue_ice".equals(param4)) {
                            return Optional.of("minecraft:pile_ice");
                        }

                        if ("minecraft:jack_o_lantern".equals(param4) || "minecraft:pumpkin".equals(param4)) {
                            return Optional.of("minecraft:pile_pumpkin");
                        }
                    }
                } else {
                    if ("minecraft:hay_block".equals(param3)) {
                        return Optional.of("minecraft:pile_hay");
                    }

                    if ("minecraft:melon".equals(param3)) {
                        return Optional.of("minecraft:pile_melon");
                    }

                    if ("minecraft:snow".equals(param3)) {
                        return Optional.of("minecraft:pile_snow");
                    }
                }
            } else {
                if ("minecraft:flower".equals(var0)) {
                    return Optional.of("minecraft:flower_plain");
                }

                if ("minecraft:tree".equals(var0)) {
                    if ("minecraft:acacia_foliage_placer".equals(param5)) {
                        return Optional.of("minecraft:acacia");
                    }

                    if ("minecraft:blob_foliage_placer".equals(param5) && "minecraft:oak_leaves".equals(param6)) {
                        return Optional.of("minecraft:oak");
                    }

                    if ("minecraft:pine_foliage_placer".equals(param5)) {
                        return Optional.of("minecraft:pine");
                    }

                    if ("minecraft:spruce_foliage_placer".equals(param5)) {
                        return Optional.of("minecraft:spruce");
                    }
                }
            }
        }

        return Optional.empty();
    }
}
