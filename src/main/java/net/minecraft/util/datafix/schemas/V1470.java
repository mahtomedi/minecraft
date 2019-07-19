package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1470 extends NamespacedSchema {
    public V1470(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> V100.equipment(param0));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        registerMob(param0, var0, "minecraft:turtle");
        registerMob(param0, var0, "minecraft:cod_mob");
        registerMob(param0, var0, "minecraft:tropical_fish");
        registerMob(param0, var0, "minecraft:salmon_mob");
        registerMob(param0, var0, "minecraft:puffer_fish");
        registerMob(param0, var0, "minecraft:phantom");
        registerMob(param0, var0, "minecraft:dolphin");
        registerMob(param0, var0, "minecraft:drowned");
        param0.register(var0, "minecraft:trident", param1 -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(param0)));
        return var0;
    }
}
