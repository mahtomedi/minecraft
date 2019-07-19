package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1929 extends NamespacedSchema {
    public V1929(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        param0.register(
            var0,
            "minecraft:wandering_trader",
            param1 -> DSL.optionalFields(
                    "Inventory",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "Offers",
                    DSL.optionalFields(
                        "Recipes",
                        DSL.list(
                            DSL.optionalFields(
                                "buy", References.ITEM_STACK.in(param0), "buyB", References.ITEM_STACK.in(param0), "sell", References.ITEM_STACK.in(param0)
                            )
                        )
                    ),
                    V100.equipment(param0)
                )
        );
        param0.register(
            var0,
            "minecraft:trader_llama",
            param1 -> DSL.optionalFields(
                    "Items",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "SaddleItem",
                    References.ITEM_STACK.in(param0),
                    "DecorItem",
                    References.ITEM_STACK.in(param0),
                    V100.equipment(param0)
                )
        );
        return var0;
    }
}
