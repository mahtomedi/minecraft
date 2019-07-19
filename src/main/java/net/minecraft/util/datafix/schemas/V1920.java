package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1920 extends NamespacedSchema {
    public V1920(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerInventory(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(param0))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        registerInventory(param0, var0, "minecraft:campfire");
        return var0;
    }
}
