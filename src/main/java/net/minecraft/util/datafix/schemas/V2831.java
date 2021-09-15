package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V2831 extends NamespacedSchema {
    public V2831(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        param0.registerType(
            true,
            References.UNTAGGED_SPAWNER,
            () -> DSL.optionalFields(
                    "SpawnPotentials",
                    DSL.list(DSL.fields("data", DSL.fields("entity", References.ENTITY_TREE.in(param0)))),
                    "SpawnData",
                    DSL.fields("entity", References.ENTITY_TREE.in(param0))
                )
        );
    }
}
