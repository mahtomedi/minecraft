package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V1451_2 extends NamespacedSchema {
    public V1451_2(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        param0.register(var0, "minecraft:piston", param1 -> DSL.optionalFields("blockState", References.BLOCK_STATE.in(param0)));
        return var0;
    }
}
