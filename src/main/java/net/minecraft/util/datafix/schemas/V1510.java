package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;

public class V1510 extends NamespacedSchema {
    public V1510(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        var0.put("minecraft:command_block_minecart", var0.remove("minecraft:commandblock_minecart"));
        var0.put("minecraft:end_crystal", var0.remove("minecraft:ender_crystal"));
        var0.put("minecraft:snow_golem", var0.remove("minecraft:snowman"));
        var0.put("minecraft:evoker", var0.remove("minecraft:evocation_illager"));
        var0.put("minecraft:evoker_fangs", var0.remove("minecraft:evocation_fangs"));
        var0.put("minecraft:illusioner", var0.remove("minecraft:illusion_illager"));
        var0.put("minecraft:vindicator", var0.remove("minecraft:vindication_illager"));
        var0.put("minecraft:iron_golem", var0.remove("minecraft:villager_golem"));
        var0.put("minecraft:experience_orb", var0.remove("minecraft:xp_orb"));
        var0.put("minecraft:experience_bottle", var0.remove("minecraft:xp_bottle"));
        var0.put("minecraft:eye_of_ender", var0.remove("minecraft:eye_of_ender_signal"));
        var0.put("minecraft:firework_rocket", var0.remove("minecraft:fireworks_rocket"));
        return var0;
    }
}
