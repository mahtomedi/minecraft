package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1928 extends NamespacedSchema {
    public V1928(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static TypeTemplate equipment(Schema param0) {
        return DSL.optionalFields("ArmorItems", DSL.list(References.ITEM_STACK.in(param0)), "HandItems", DSL.list(References.ITEM_STACK.in(param0)));
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> equipment(param0));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        var0.remove("minecraft:illager_beast");
        registerMob(param0, var0, "minecraft:ravager");
        return var0;
    }
}
