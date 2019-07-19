package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1906 extends NamespacedSchema {
    public V1906(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        registerInventory(param0, var0, "minecraft:barrel");
        registerInventory(param0, var0, "minecraft:smoker");
        registerInventory(param0, var0, "minecraft:blast_furnace");
        param0.register(var0, "minecraft:lectern", param1 -> DSL.optionalFields("Book", References.ITEM_STACK.in(param0)));
        param0.registerSimple(var0, "minecraft:bell");
        return var0;
    }

    protected static void registerInventory(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(param0))));
    }
}
