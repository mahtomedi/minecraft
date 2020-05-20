package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL.TypeReference;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const.PrimitiveType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.resources.ResourceLocation;

public class NamespacedSchema extends Schema {
    public static final PrimitiveCodec<String> NAMESPACED_STRING_CODEC = new PrimitiveCodec<String>() {
        @Override
        public <T> DataResult<String> read(DynamicOps<T> param0, T param1) {
            return param0.getStringValue(param1).map(NamespacedSchema::ensureNamespaced);
        }

        public <T> T write(DynamicOps<T> param0, String param1) {
            return param0.createString(param1);
        }

        @Override
        public String toString() {
            return "NamespacedString";
        }
    };
    private static final Type<String> NAMESPACED_STRING = new PrimitiveType<>(NAMESPACED_STRING_CODEC);

    public NamespacedSchema(int param0, Schema param1) {
        super(param0, param1);
    }

    public static String ensureNamespaced(String param0) {
        ResourceLocation var0 = ResourceLocation.tryParse(param0);
        return var0 != null ? var0.toString() : param0;
    }

    public static Type<String> namespacedString() {
        return NAMESPACED_STRING;
    }

    @Override
    public Type<?> getChoiceType(TypeReference param0, String param1) {
        return super.getChoiceType(param0, ensureNamespaced(param1));
    }
}
