package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2100 extends NamespacedSchema {
    public V2100(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> V100.equipment(param0));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        registerMob(param0, var0, "minecraft:bee");
        registerMob(param0, var0, "minecraft:bee_stinger");
        return var0;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        param0.register(
            var0,
            "minecraft:beehive",
            () -> DSL.optionalFields(
                    "Items", DSL.list(References.ITEM_STACK.in(param0)), "Bees", DSL.list(DSL.optionalFields("EntityData", References.ENTITY_TREE.in(param0)))
                )
        );
        return var0;
    }
}
