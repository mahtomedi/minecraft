package net.minecraft.client.resources.language;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class I18n {
    private static Locale locale;

    static void setLocale(Locale param0) {
        locale = param0;
    }

    public static String get(String param0, Object... param1) {
        return locale.get(param0, param1);
    }

    public static boolean exists(String param0) {
        return locale.has(param0);
    }
}
