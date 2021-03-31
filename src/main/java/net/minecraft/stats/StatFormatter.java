package net.minecraft.stats;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.Util;

public interface StatFormatter {
    DecimalFormat DECIMAL_FORMAT = Util.make(
        new DecimalFormat("########0.00"), param0 -> param0.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT))
    );
    StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
    StatFormatter DIVIDE_BY_TEN = param0 -> DECIMAL_FORMAT.format((double)param0 * 0.1);
    StatFormatter DISTANCE = param0 -> {
        double var0 = (double)param0 / 100.0;
        double var1 = var0 / 1000.0;
        if (var1 > 0.5) {
            return DECIMAL_FORMAT.format(var1) + " km";
        } else {
            return var0 > 0.5 ? DECIMAL_FORMAT.format(var0) + " m" : param0 + " cm";
        }
    };
    StatFormatter TIME = param0 -> {
        double var0 = (double)param0 / 20.0;
        double var1 = var0 / 60.0;
        double var2 = var1 / 60.0;
        double var3 = var2 / 24.0;
        double var4 = var3 / 365.0;
        if (var4 > 0.5) {
            return DECIMAL_FORMAT.format(var4) + " y";
        } else if (var3 > 0.5) {
            return DECIMAL_FORMAT.format(var3) + " d";
        } else if (var2 > 0.5) {
            return DECIMAL_FORMAT.format(var2) + " h";
        } else {
            return var1 > 0.5 ? DECIMAL_FORMAT.format(var1) + " m" : var0 + " s";
        }
    };

    String format(int var1);
}
