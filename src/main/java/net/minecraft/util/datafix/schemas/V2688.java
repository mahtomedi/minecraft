package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V2688 extends NamespacedSchema {
    public V2688(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> V100.equipment(param0));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        registerMob(param0, var0, "minecraft:glow_squid");
        param0.registerSimple(var0, "minecraft:glow_item_frame");
        return var0;
    }
}