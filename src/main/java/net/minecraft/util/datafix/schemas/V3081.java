package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3081 extends NamespacedSchema {
    public V3081(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static void registerMob(Schema param0, Map<String, Supplier<TypeTemplate>> param1, String param2) {
        param0.register(param1, param2, () -> V100.equipment(param0));
        param0.register(
            param1,
            "minecraft:warden",
            () -> DSL.optionalFields(
                    "ArmorItems",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "HandItems",
                    DSL.list(References.ITEM_STACK.in(param0)),
                    "listener",
                    DSL.optionalFields("event", DSL.optionalFields("game_event", References.GAME_EVENT_NAME.in(param0)))
                )
        );
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        registerMob(param0, var0, "minecraft:warden");
        return var0;
    }
}
