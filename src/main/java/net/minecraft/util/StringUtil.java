package net.minecraft.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Pattern LINE_PATTERN = Pattern.compile("\\r\\n|\\v");
    private static final Pattern LINE_END_PATTERN = Pattern.compile("(?:\\r\\n|\\v)$");

    public static String formatTickDuration(int param0) {
        int var0 = param0 / 20;
        int var1 = var0 / 60;
        var0 %= 60;
        return var0 < 10 ? var1 + ":0" + var0 : var1 + ":" + var0;
    }

    public static String stripColor(String param0) {
        return STRIP_COLOR_PATTERN.matcher(param0).replaceAll("");
    }

    public static boolean isNullOrEmpty(@Nullable String param0) {
        return StringUtils.isEmpty(param0);
    }

    public static String truncateStringIfNecessary(String param0, int param1, boolean param2) {
        if (param0.length() <= param1) {
            return param0;
        } else {
            return param2 && param1 > 3 ? param0.substring(0, param1 - 3) + "..." : param0.substring(0, param1);
        }
    }

    public static int lineCount(String param0) {
        if (param0.isEmpty()) {
            return 0;
        } else {
            Matcher var0 = LINE_PATTERN.matcher(param0);
            int var1 = 1;

            while(var0.find()) {
                ++var1;
            }

            return var1;
        }
    }

    public static boolean endsWithNewLine(String param0) {
        return LINE_END_PATTERN.matcher(param0).find();
    }
}
