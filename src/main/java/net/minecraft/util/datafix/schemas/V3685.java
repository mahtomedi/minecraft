package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;

public class V3685 extends NamespacedSchema {
    public V3685(int param0, Schema param1) {
        super(param0, param1);
    }

    protected static TypeTemplate abstractArrow(Schema param0) {
        return DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(param0), "item", References.ITEM_STACK.in(param0));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema param0) {
        Map<String, Supplier<TypeTemplate>> var0 = super.registerEntities(param0);
        param0.register(var0, "minecraft:trident", () -> abstractArrow(param0));
        param0.register(var0, "minecraft:spectral_arrow", () -> abstractArrow(param0));
        param0.register(var0, "minecraft:arrow", () -> abstractArrow(param0));
        return var0;
    }
}
