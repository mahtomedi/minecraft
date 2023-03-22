package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V3438 extends NamespacedSchema {
    public V3438(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        var0.put("minecraft:brushable_block", var0.remove("minecraft:suspicious_sand"));
        param0.registerSimple(var0, "minecraft:calibrated_sculk_sensor");
        return var0;
    }
}
