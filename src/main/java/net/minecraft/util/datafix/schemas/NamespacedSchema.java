package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.resources.ResourceLocation;

public class NamespacedSchema extends Schema {
    public NamespacedSchema(int param0, Schema param1) {
        super(param0, param1);
    }

    public static String ensureNamespaced(String param0) {
        ResourceLocation var0 = ResourceLocation.tryParse(param0);
        return var0 != null ? var0.toString() : param0;
    }

    @Override
    public Type<?> getChoiceType(TypeReference param0, String param1) {
        return super.getChoiceType(param0, ensureNamespaced(param1));
    }
}
