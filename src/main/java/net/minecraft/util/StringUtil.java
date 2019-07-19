package net.minecraft.util;

import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");

    @OnlyIn(Dist.CLIENT)
    public static String formatTickDuration(int param0) {
        int var0 = param0 / 20;
        int var1 = var0 / 60;
        var0 %= 60;
        return var0 < 10 ? var1 + ":0" + var0 : var1 + ":" + var0;
    }

    @OnlyIn(Dist.CLIENT)
    public static String stripColor(String param0) {
        return STRIP_COLOR_PATTERN.matcher(param0).replaceAll("");
    }

    public static boolean isNullOrEmpty(@Nullable String param0) {
        return StringUtils.isEmpty(param0);
    }
}
