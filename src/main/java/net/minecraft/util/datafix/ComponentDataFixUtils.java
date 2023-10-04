package net.minecraft.util.datafix;

import com.google.gson.JsonObject;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.util.GsonHelper;

public class ComponentDataFixUtils {
    private static final String EMPTY_CONTENTS = createTextComponentJson("");

    public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> param0, String param1) {
        String var0 = createTextComponentJson(param1);
        return new Dynamic<>(param0, param0.createString(var0));
    }

    public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createString(EMPTY_CONTENTS));
    }

    private static String createTextComponentJson(String param0) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("text", param0);
        return GsonHelper.toStableString(var0);
    }

    public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> param0, String param1) {
        JsonObject var0 = new JsonObject();
        var0.addProperty("translate", param1);
        return new Dynamic<>(param0, param0.createString(GsonHelper.toStableString(var0)));
    }

    public static <T> Dynamic<T> wrapLiteralStringAsComponent(Dynamic<T> param0) {
        return DataFixUtils.orElse(param0.asString().map(param1 -> createPlainTextComponent(param0.getOps(), param1)).result(), param0);
    }
}
