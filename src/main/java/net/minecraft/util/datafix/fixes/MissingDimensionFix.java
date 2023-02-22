package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList.CompoundListType;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MissingDimensionFix extends DataFix {
    public MissingDimensionFix(Schema param0, boolean param1) {
        super(param0, param1);
    }

    protected static <A> Type<Pair<A, Dynamic<?>>> fields(String param0, Type<A> param1) {
        return DSL.and(DSL.field(param0, param1), DSL.remainderType());
    }

    protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> optionalFields(String param0, Type<A> param1) {
        return DSL.and(DSL.optional(DSL.field(param0, param1)), DSL.remainderType());
    }

    protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> optionalFields(
        String param0, Type<A1> param1, String param2, Type<A2> param3
    ) {
        return DSL.and(DSL.optional(DSL.field(param0, param1)), DSL.optional(DSL.field(param2, param3)), DSL.remainderType());
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema var0 = this.getInputSchema();
        Type<?> var1 = DSL.taggedChoiceType(
            "type",
            DSL.string(),
            ImmutableMap.of(
                "minecraft:debug",
                DSL.remainderType(),
                "minecraft:flat",
                flatType(var0),
                "minecraft:noise",
                optionalFields(
                    "biome_source",
                    DSL.taggedChoiceType(
                        "type",
                        DSL.string(),
                        ImmutableMap.of(
                            "minecraft:fixed",
                            fields("biome", var0.getType(References.BIOME)),
                            "minecraft:multi_noise",
                            DSL.list(fields("biome", var0.getType(References.BIOME))),
                            "minecraft:checkerboard",
                            fields("biomes", DSL.list(var0.getType(References.BIOME))),
                            "minecraft:vanilla_layered",
                            DSL.remainderType(),
                            "minecraft:the_end",
                            DSL.remainderType()
                        )
                    ),
                    "settings",
                    DSL.or(
                        DSL.string(),
                        optionalFields("default_block", var0.getType(References.BLOCK_NAME), "default_fluid", var0.getType(References.BLOCK_NAME))
                    )
                )
            )
        );
        CompoundListType<String, ?> var2 = DSL.compoundList(NamespacedSchema.namespacedString(), fields("generator", var1));
        Type<?> var3 = DSL.and(var2, DSL.remainderType());
        Type<?> var4 = var0.getType(References.WORLD_GEN_SETTINGS);
        FieldFinder<?> var5 = new FieldFinder<>("dimensions", var3);
        if (!var4.findFieldType("dimensions").equals(var3)) {
            throw new IllegalStateException();
        } else {
            OpticFinder<? extends List<? extends Pair<String, ?>>> var6 = var2.finder();
            return this.fixTypeEverywhereTyped(
                "MissingDimensionFix", var4, param3 -> param3.updateTyped(var5, param3x -> param3x.updateTyped(var6, param2x -> {
                            if (!(param2x.getValue() instanceof List)) {
                                throw new IllegalStateException("List exptected");
                            } else if (((List)param2x.getValue()).isEmpty()) {
                                Dynamic<?> var0x = param3.get(DSL.remainderFinder());
                                Dynamic<?> var1x = this.recreateSettings(var0x);
                                return DataFixUtils.orElse(var2.readTyped(var1x).result().map(Pair::getFirst), param2x);
                            } else {
                                return param2x;
                            }
                        }))
            );
        }
    }

    protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> flatType(
        Schema param0
    ) {
        return optionalFields(
            "settings",
            optionalFields("biome", param0.getType(References.BIOME), "layers", DSL.list(optionalFields("block", param0.getType(References.BLOCK_NAME))))
        );
    }

    private <T> Dynamic<T> recreateSettings(Dynamic<T> param0) {
        long var0 = param0.get("seed").asLong(0L);
        return new Dynamic<>(param0.getOps(), WorldGenSettingsFix.vanillaLevels(param0, var0, WorldGenSettingsFix.defaultOverworld(param0, var0), false));
    }
}
