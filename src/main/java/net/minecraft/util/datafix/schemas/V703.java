package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V703 extends Schema {
    public V703(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        var0.remove("EntityHorse");
        param0.register(
            var0,
            "Horse",
            () -> DSL.optionalFields("ArmorItem", References.ITEM_STACK.in(param0), "SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0))
        );
        param0.register(
            var0,
            "Donkey",
            () -> DSL.optionalFields(
                    "Items", DSL.list(References.ITEM_STACK.in(param0)), "SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)
                )
        );
        param0.register(
            var0,
            "Mule",
            () -> DSL.optionalFields(
                    "Items", DSL.list(References.ITEM_STACK.in(param0)), "SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)
                )
        );
        param0.register(var0, "ZombieHorse", () -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)));
        param0.register(var0, "SkeletonHorse", () -> DSL.optionalFields("SaddleItem", References.ITEM_STACK.in(param0), V100.equipment(param0)));
        return var0;
    }
}
