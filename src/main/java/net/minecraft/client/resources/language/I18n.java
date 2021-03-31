package net.minecraft.client.resources.language;

import java.util.IllegalFormatException;
import net.minecraft.locale.Language;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class I18n {
    private static volatile Language language = Language.getInstance();

    private I18n() {
    }

    static void setLanguage(Language param0) {
        language = param0;
    }

    public static String get(String param0, Object... param1) {
        String var0 = language.getOrDefault(param0);

        try {
            return String.format(var0, param1);
        } catch (IllegalFormatException var4) {
            return "Format error: " + var0;
        }
    }

    public static boolean exists(String param0) {
        return language.has(param0);
    }
}
