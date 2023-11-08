package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.function.Function;
import net.minecraft.Util;

public class FixProjectileStoredItem extends DataFix {
    private static final String EMPTY_POTION = "minecraft:empty";

    public FixProjectileStoredItem(Schema param0) {
        super(param0, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> var0 = this.getInputSchema().getType(References.ENTITY);
        Type<?> var1 = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped(
            "Fix AbstractArrow item type",
            var0,
            var1,
            this.chainAllFilters(
                this.fixChoice("minecraft:trident", FixProjectileStoredItem::castUnchecked),
                this.fixChoice("minecraft:arrow", FixProjectileStoredItem::fixArrow),
                this.fixChoice("minecraft:spectral_arrow", FixProjectileStoredItem::fixSpectralArrow)
            )
        );
    }

    @SafeVarargs
    private <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>>... param0) {
        return param1 -> {
            for(Function<Typed<?>, Typed<?>> var0 : param0) {
                param1 = var0.apply(param1);
            }

            return param1;
        };
    }

    private Function<Typed<?>, Typed<?>> fixChoice(String param0, FixProjectileStoredItem.SubFixer<?> param1) {
        Type<?> var0 = this.getInputSchema().getChoiceType(References.ENTITY, param0);
        Type<?> var1 = this.getOutputSchema().getChoiceType(References.ENTITY, param0);
        return fixChoiceCap(param0, param1, var0, var1);
    }

    private static <T> Function<Typed<?>, Typed<?>> fixChoiceCap(String param0, FixProjectileStoredItem.SubFixer<?> param1, Type<?> param2, Type<T> param3) {
        OpticFinder<?> var0 = DSL.namedChoice(param0, param2);
        return param3x -> param3x.updateTyped(var0, param3, param2x -> param1.fix(param2x, param3));
    }

    private static <T> Typed<T> fixArrow(Typed<?> param0, Type<T> param1) {
        return Util.writeAndReadTypedOrThrow(param0, param1, param0x -> param0x.set("item", createItemStack(param0x, getArrowType(param0x))));
    }

    private static String getArrowType(Dynamic<?> param0) {
        return param0.get("Potion").asString("minecraft:empty").equals("minecraft:empty") ? "minecraft:arrow" : "minecraft:tipped_arrow";
    }

    private static <T> Typed<T> fixSpectralArrow(Typed<?> param0, Type<T> param1) {
        return Util.writeAndReadTypedOrThrow(param0, param1, param0x -> param0x.set("item", createItemStack(param0x, "minecraft:spectral_arrow")));
    }

    private static Dynamic<?> createItemStack(Dynamic<?> param0, String param1) {
        return param0.createMap(ImmutableMap.of(param0.createString("id"), param0.createString(param1), param0.createString("Count"), param0.createInt(1)));
    }

    private static <T> Typed<T> castUnchecked(Typed<?> param0, Type<T> param1) {
        return new Typed<>(param1, param0.getOps(), (T)param0.getValue());
    }

    interface SubFixer<F> {
        Typed<F> fix(Typed<?> var1, Type<F> var2);
    }
}
