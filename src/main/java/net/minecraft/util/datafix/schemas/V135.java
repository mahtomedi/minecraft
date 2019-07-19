package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V135 extends Schema {
    public V135(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        param0.registerType(
            false,
            References.PLAYER,
            () -> DSL.optionalFields(
                    "RootVehicle",
                    DSL.optionalFields("Entity", References.ENTITY_TREE.in(param0)),
                    "Inventory",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "EnderItems",
                    DSL.list(References.ITEM_STACK.in(param0))
                )
        );
        param0.registerType(
            true, References.ENTITY_TREE, () -> DSL.optionalFields("Passengers", DSL.list(References.ENTITY_TREE.in(param0)), References.ENTITY.in(param0))
        );
    }
}
