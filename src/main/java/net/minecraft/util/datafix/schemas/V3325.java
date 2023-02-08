package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3325 extends NamespacedSchema {
    public V3325(int param0, Schema param1) {
        super(param0, param1);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        param0.register(var0, "minecraft:item_display", param1 -> DSL.optionalFields("item", References.ITEM_STACK.in(param0)));
        param0.register(var0, "minecraft:block_display", param1 -> DSL.optionalFields("block_state", References.BLOCK_STATE.in(param0)));
        param0.registerSimple(var0, "minecraft:text_display");
        return var0;
    }
}
