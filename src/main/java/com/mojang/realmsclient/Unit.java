package com.mojang.realmsclient;

import java.util.Locale;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public enum Unit {
    B,
    KB,
    MB,
    GB;

    private static final int BASE_UNIT = 1024;

    public static Unit getLargest(long param0) {
        if (param0 < 1024L) {
            return B;
        } else {
            try {
                int var0 = (int)(Math.log((double)param0) / Math.log(1024.0));
                String var1 = String.valueOf("KMGTPE".charAt(var0 - 1));
                return valueOf(var1 + "B");
            } catch (Exception var4) {
                return GB;
            }
        }
    }

    public static double convertTo(long param0, Unit param1) {
        return param1 == B ? (double)param0 : (double)param0 / Math.pow(1024.0, (double)param1.ordinal());
    }

    public static String humanReadable(long param0) {
        int var0 = 1024;
        if (param0 < 1024L) {
            return param0 + " B";
        } else {
            int var1 = (int)(Math.log((double)param0) / Math.log(1024.0));
            String var2 = "KMGTPE".charAt(var1 - 1) + "";
            return String.format(Locale.ROOT, "%.1f %sB", (double)param0 / Math.pow(1024.0, (double)var1), var2);
        }
    }

    public static String humanReadable(long param0, Unit param1) {
        return String.format("%." + (param1 == GB ? "1" : "0") + "f %s", convertTo(param0, param1), param1.name());
    }
}
