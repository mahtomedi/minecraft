package com.mojang.realmsclient.dto;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.util.JsonUtils;
import javax.annotation.Nullable;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsText {
    private static final String TRANSLATION_KEY = "translationKey";
    private static final String ARGS = "args";
    private final String translationKey;
    @Nullable
    private final Object[] args;

    private RealmsText(String param0, @Nullable Object[] param1) {
        this.translationKey = param0;
        this.args = param1;
    }

    public Component createComponent(Component param0) {
        if (!I18n.exists(this.translationKey)) {
            return param0;
        } else {
            return this.args == null ? Component.translatable(this.translationKey) : Component.translatable(this.translationKey, this.args);
        }
    }

    public static RealmsText parse(JsonObject param0) {
        String var0 = JsonUtils.getRequiredString("translationKey", param0);
        JsonElement var1 = param0.get("args");
        String[] var4;
        if (var1 != null && !var1.isJsonNull()) {
            JsonArray var3 = var1.getAsJsonArray();
            var4 = new String[var3.size()];

            for(int var5 = 0; var5 < var3.size(); ++var5) {
                var4[var5] = var3.get(var5).getAsString();
            }
        } else {
            var4 = null;
        }

        return new RealmsText(var0, var4);
    }
}
