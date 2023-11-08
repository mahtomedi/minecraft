package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.stream.Stream;
import net.minecraft.Util;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class WorldGenSettingsHeightAndBiomeFix extends DataFix {
    private static final String NAME = "WorldGenSettingsHeightAndBiomeFix";
    public static final String WAS_PREVIOUSLY_INCREASED_KEY = "has_increased_height_already";

    public WorldGenSettingsHeightAndBiomeFix(Schema param0) {
        super(param0, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.WORLD_GEN_SETTINGS);
        OpticFinder<?> var1 = var0.findField("dimensions");
        Type<?> var2 = this.getOutputSchema().getType(References.WORLD_GEN_SETTINGS);
        Type<?> var3 = var2.findFieldType("dimensions");
        return this.fixTypeEverywhereTyped(
            "WorldGenSettingsHeightAndBiomeFix",
            var0,
            var2,
            param2 -> {
                OptionalDynamic<?> var0x = param2.get(DSL.remainderFinder()).get("has_increased_height_already");
                boolean var1x = var0x.result().isEmpty();
                boolean var2x = var0x.asBoolean(true);
                return param2.update(DSL.remainderFinder(), param0x -> param0x.remove("has_increased_height_already"))
                    .updateTyped(
                        var1,
                        var3,
                        param3 -> Util.writeAndReadTypedOrThrow(
                                param3,
                                var3,
                                param2x -> param2x.update(
                                        "minecraft:overworld",
                                        param2xx -> param2xx.update(
                                                "generator",
                                                param2xxx -> {
                                                    String var0xx = param2xxx.get("type").asString("");
                                                    if ("minecraft:noise".equals(var0xx)) {
                                                        MutableBoolean var1xx = new MutableBoolean();
                                                        param2xxx = param2xxx.update(
                                                            "biome_source",
                                                            param2xxxx -> {
                                                                String var0xxx = param2xxxx.get("type").asString("");
                                                                if ("minecraft:vanilla_layered".equals(var0xxx)
                                                                    || var1x && "minecraft:multi_noise".equals(var0xxx)) {
                                                                    if (param2xxxx.get("large_biomes").asBoolean(false)) {
                                                                        var1xx.setTrue();
                                                                    }
                            
                                                                    return param2xxxx.createMap(
                                                                        ImmutableMap.of(
                                                                            param2xxxx.createString("preset"),
                                                                            param2xxxx.createString("minecraft:overworld"),
                                                                            param2xxxx.createString("type"),
                                                                            param2xxxx.createString("minecraft:multi_noise")
                                                                        )
                                                                    );
                                                                } else {
                                                                    return param2xxxx;
                                                                }
                                                            }
                                                        );
                                                        return var1xx.booleanValue()
                                                            ? param2xxx.update(
                                                                "settings",
                                                                param0x -> "minecraft:overworld".equals(param0x.asString(""))
                                                                        ? param0x.createString("minecraft:large_biomes")
                                                                        : param0x
                                                            )
                                                            : param2xxx;
                                                    } else if ("minecraft:flat".equals(var0xx)) {
                                                        return var2x
                                                            ? param2xxx
                                                            : param2xxx.update(
                                                                "settings",
                                                                param0x -> param0x.update("layers", WorldGenSettingsHeightAndBiomeFix::updateLayers)
                                                            );
                                                    } else {
                                                        return param2xxx;
                                                    }
                                                }
                                            )
                                    )
                            )
                    );
            }
        );
    }

    private static Dynamic<?> updateLayers(Dynamic<?> param0) {
        Dynamic<?> var0 = param0.createMap(
            ImmutableMap.of(param0.createString("height"), param0.createInt(64), param0.createString("block"), param0.createString("minecraft:air"))
        );
        return param0.createList(Stream.concat(Stream.of(var0), param0.asStream()));
    }
}
