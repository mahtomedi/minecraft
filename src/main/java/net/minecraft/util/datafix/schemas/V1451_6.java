package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;

public class V1451_6 extends NamespacedSchema {
    public static final String SPECIAL_OBJECTIVE_MARKER = "_special";
    protected static final HookFunction UNPACK_OBJECTIVE_ID = new HookFunction() {
        @Override
        public <T> T apply(DynamicOps<T> param0, T param1) {
            Dynamic<T> var0 = new Dynamic<>(param0, param1);
            return DataFixUtils.orElse(
                    var0.get("CriteriaName")
                        .asString()
                        .get()
                        .left()
                        .map(param0x -> {
                            int var0x = param0x.indexOf(58);
                            if (var0x < 0) {
                                return Pair.of("_special", param0x);
                            } else {
                                try {
                                    ResourceLocation var3x = ResourceLocation.of(param0x.substring(0, var0x), '.');
                                    ResourceLocation var2x = ResourceLocation.of(param0x.substring(var0x + 1), '.');
                                    return Pair.of(var3x.toString(), var2x.toString());
                                } catch (Exception var4) {
                                    return Pair.of("_special", param0x);
                                }
                            }
                        })
                        .map(
                            param1x -> var0.set(
                                    "CriteriaType",
                                    var0.createMap(
                                        ImmutableMap.of(
                                            var0.createString("type"),
                                            var0.createString(param1x.getFirst()),
                                            var0.createString("id"),
                                            var0.createString(param1x.getSecond())
                                        )
                                    )
                                )
                        ),
                    var0
                )
                .getValue();
        }
    };
    protected static final HookFunction REPACK_OBJECTIVE_ID = new HookFunction() {
        @Override
        public <T> T apply(DynamicOps<T> param0, T param1) {
            Dynamic<T> var0 = new Dynamic<>(param0, param1);
            Optional<Dynamic<T>> var1 = var0.get("CriteriaType")
                .get()
                .get()
                .left()
                .flatMap(
                    param1x -> {
                        Optional<String> var0x = param1x.get("type").asString().get().left();
                        Optional<String> var1x = param1x.get("id").asString().get().left();
                        if (var0x.isPresent() && var1x.isPresent()) {
                            String var2x = var0x.get();
                            return var2x.equals("_special")
                                ? Optional.of(var0.createString(var1x.get()))
                                : Optional.of(param1x.createString(V1451_6.packNamespacedWithDot(var2x) + ":" + V1451_6.packNamespacedWithDot(var1x.get())));
                        } else {
                            return Optional.empty();
                        }
                    }
                );
            return DataFixUtils.orElse(var1.map(param1x -> var0.set("CriteriaName", param1x).remove("CriteriaType")), var0).getValue();
        }
    };

    public V1451_6(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        Supplier<TypeTemplate> var0 = () -> DSL.compoundList(References.ITEM_NAME.in(param0), DSL.constType(DSL.intType()));
        param0.registerType(
            false,
            References.STATS,
            () -> DSL.optionalFields(
                    "stats",
                    DSL.optionalFields(
                        "minecraft:mined",
                        DSL.compoundList(References.BLOCK_NAME.in(param0), DSL.constType(DSL.intType())),
                        "minecraft:crafted",
                        var0.get(),
                        "minecraft:used",
                        var0.get(),
                        "minecraft:broken",
                        var0.get(),
                        "minecraft:picked_up",
                        var0.get(),
                        DSL.optionalFields(
                            "minecraft:dropped",
                            var0.get(),
                            "minecraft:killed",
                            DSL.compoundList(References.ENTITY_NAME.in(param0), DSL.constType(DSL.intType())),
                            "minecraft:killed_by",
                            DSL.compoundList(References.ENTITY_NAME.in(param0), DSL.constType(DSL.intType())),
                            "minecraft:custom",
                            DSL.compoundList(DSL.constType(namespacedString()), DSL.constType(DSL.intType()))
                        )
                    )
                )
        );
        Map<String, Supplier<TypeTemplate>> var1 = createCriterionTypes(param0);
        param0.registerType(
            false,
            References.OBJECTIVE,
            () -> DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), var1)), UNPACK_OBJECTIVE_ID, REPACK_OBJECTIVE_ID)
        );
    }

    protected static Map<String, Supplier<TypeTemplate>> createCriterionTypes(Schema param0) {
        Supplier<TypeTemplate> var0 = () -> DSL.optionalFields("id", References.ITEM_NAME.in(param0));
        Supplier<TypeTemplate> var1 = () -> DSL.optionalFields("id", References.BLOCK_NAME.in(param0));
        Supplier<TypeTemplate> var2 = () -> DSL.optionalFields("id", References.ENTITY_NAME.in(param0));
        Map<String, Supplier<TypeTemplate>> var3 = Maps.newHashMap();
        var3.put("minecraft:mined", var1);
        var3.put("minecraft:crafted", var0);
        var3.put("minecraft:used", var0);
        var3.put("minecraft:broken", var0);
        var3.put("minecraft:picked_up", var0);
        var3.put("minecraft:dropped", var0);
        var3.put("minecraft:killed", var2);
        var3.put("minecraft:killed_by", var2);
        var3.put("minecraft:custom", () -> DSL.optionalFields("id", DSL.constType(namespacedString())));
        var3.put("_special", () -> DSL.optionalFields("id", DSL.constType(DSL.string())));
        return var3;
    }

    public static String packNamespacedWithDot(String param0) {
        ResourceLocation var0 = ResourceLocation.tryParse(param0);
        return var0 != null ? var0.getNamespace() + "." + var0.getPath() : param0;
    }
}
