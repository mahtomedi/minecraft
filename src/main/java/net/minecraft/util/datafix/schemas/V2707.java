package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V2707 extends NamespacedSchema {
    public V2707(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerEntity(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> V100.equipment(param0));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        registerEntity(param0, var0, "minecraft:marker");
        return var0;
    }
}