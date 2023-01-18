package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2688 extends NamespacedSchema {
    public V2688(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        param0.register(var0, "minecraft:glow_squid", () -> V100.equipment(param0));
        param0.register(var0, "minecraft:glow_item_frame", param1 -> DSL.optionalFields("Item", References.ITEM_STACK.in(param0)));
        return var0;
    }
}
