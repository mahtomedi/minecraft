package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3689 extends NamespacedSchema {
    public V3689(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        param0.register(var0, "minecraft:breeze", () -> V100.equipment(param0));
        param0.registerSimple(var0, "minecraft:wind_charge");
        return var0;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerBlockEntities(param0);
        param0.register(
            var0,
            "minecraft:trial_spawner",
            () -> DSL.optionalFields(
                    "spawn_potentials",
                    DSL.list(DSL.fields("data", DSL.fields("entity", References.ENTITY_TREE.in(param0)))),
                    "spawn_data",
                    DSL.fields("entity", References.ENTITY_TREE.in(param0))
                )
        );
        return var0;
    }
}
