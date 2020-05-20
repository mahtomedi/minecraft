package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List.ListType;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class VillagerTradeFix extends NamedEntityFix {
    public VillagerTradeFix(Schema param0, boolean param1) {
        super(param0, param1, "Villager trade fix", References.ENTITY, "minecraft:villager");
    }

    @Override
    protected Typed<?> fix(Typed<?> param0) {
        OpticFinder<?> var0 = param0.getType().findField("Offers");
        OpticFinder<?> var1 = var0.type().findField("Recipes");
        Type<?> var2 = var1.type();
        if (!(var2 instanceof ListType)) {
            throw new IllegalStateException("Recipes are expected to be a list.");
        } else {
            ListType<?> var3 = (ListType)var2;
            Type<?> var4 = var3.getElement();
            OpticFinder<?> var5 = DSL.typeFinder(var4);
            OpticFinder<?> var6 = var4.findField("buy");
            OpticFinder<?> var7 = var4.findField("buyB");
            OpticFinder<?> var8 = var4.findField("sell");
            OpticFinder<Pair<String, String>> var9 = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
            Function<Typed<?>, Typed<?>> var10 = param1 -> this.updateItemStack(var9, param1);
            return param0.updateTyped(
                var0,
                param6 -> param6.updateTyped(
                        var1,
                        param5x -> param5x.updateTyped(var5, param4x -> param4x.updateTyped(var6, var10).updateTyped(var7, var10).updateTyped(var8, var10))
                    )
            );
        }
    }

    private Typed<?> updateItemStack(OpticFinder<Pair<String, String>> param0, Typed<?> param1) {
        return param1.update(
            param0, param0x -> param0x.mapSecond(param0xx -> Objects.equals(param0xx, "minecraft:carved_pumpkin") ? "minecraft:pumpkin" : param0xx)
        );
    }
}
