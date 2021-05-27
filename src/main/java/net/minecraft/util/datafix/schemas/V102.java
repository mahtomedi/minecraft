package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.types.templates.Hook.HookFunction;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V102 extends Schema {
    public V102(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public void registerTypes(Schema param0, Map<String, Supplier<TypeTemplate>> param1, Map<String, Supplier<TypeTemplate>> param2) {
        super.registerTypes(param0, param1, param2);
        param0.registerType(
            true,
            References.ITEM_STACK,
            () -> DSL.hook(
                    DSL.optionalFields(
                        "id",
                        References.ITEM_NAME.in(param0),
                        "tag",
                        DSL.optionalFields(
                            "EntityTag",
                            References.ENTITY_TREE.in(param0),
                            "BlockEntityTag",
                            References.BLOCK_ENTITY.in(param0),
                            "CanDestroy",
                            DSL.list(References.BLOCK_NAME.in(param0)),
                            "CanPlaceOn",
                            DSL.list(References.BLOCK_NAME.in(param0)),
                            "Items",
                            DSL.list(References.ITEM_STACK.in(param0))
                        )
                    ),
                    V99.ADD_NAMES,
                    HookFunction.IDENTITY
                )
        );
    }
}
