package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class CrashReportCategory {
    private final CrashReport report;
    private final String title;
    private final List<CrashReportCategory.Entry> entries = Lists.newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportCategory(CrashReport param0, String param1) {
        this.report = param0;
        this.title = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public static String formatLocation(double param0, double param1, double param2) {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", param0, param1, param2, formatLocation(new BlockPos(param0, param1, param2)));
    }

    public static String formatLocation(BlockPos param0) {
        return formatLocation(param0.getX(), param0.getY(), param0.getZ());
    }

    public static String formatLocation(int param0, int param1, int param2) {
        StringBuilder var0 = new StringBuilder();

        try {
            var0.append(String.format("World: (%d,%d,%d)", param0, param1, param2));
        } catch (Throwable var161) {
            var0.append("(Error finding world loc)");
        }

        var0.append(", ");

        try {
            int var2 = param0 >> 4;
            int var3 = param2 >> 4;
            int var4 = param0 & 15;
            int var5 = param1 >> 4;
            int var6 = param2 & 15;
            int var7 = var2 << 4;
            int var8 = var3 << 4;
            int var9 = (var2 + 1 << 4) - 1;
            int var10 = (var3 + 1 << 4) - 1;
            var0.append(
                String.format("Chunk: (at %d,%d,%d in %d,%d; contains blocks %d,0,%d to %d,255,%d)", var4, var5, var6, var2, var3, var7, var8, var9, var10)
            );
        } catch (Throwable var151) {
            var0.append("(Error finding chunk loc)");
        }

        var0.append(", ");

        try {
            int var12 = param0 >> 9;
            int var13 = param2 >> 9;
            int var14 = var12 << 5;
            int var15 = var13 << 5;
            int var16 = (var12 + 1 << 5) - 1;
            int var17 = (var13 + 1 << 5) - 1;
            int var18 = var12 << 9;
            int var19 = var13 << 9;
            int var20 = (var12 + 1 << 9) - 1;
            int var21 = (var13 + 1 << 9) - 1;
            var0.append(
                String.format(
                    "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,0,%d to %d,255,%d)",
                    var12,
                    var13,
                    var14,
                    var15,
                    var16,
                    var17,
                    var18,
                    var19,
                    var20,
                    var21
                )
            );
        } catch (Throwable var141) {
            var0.append("(Error finding world loc)");
        }

        return var0.toString();
    }

    public CrashReportCategory setDetail(String param0, CrashReportDetail<String> param1) {
        try {
            this.setDetail(param0, param1.call());
        } catch (Throwable var4) {
            this.setDetailError(param0, var4);
        }

        return this;
    }

    public CrashReportCategory setDetail(String param0, Object param1) {
        this.entries.add(new CrashReportCategory.Entry(param0, param1));
        return this;
    }

    public void setDetailError(String param0, Throwable param1) {
        this.setDetail(param0, param1);
    }

    public int fillInStackTrace(int param0) {
        StackTraceElement[] var0 = Thread.currentThread().getStackTrace();
        if (var0.length <= 0) {
            return 0;
        } else {
            this.stackTrace = new StackTraceElement[var0.length - 3 - param0];
            System.arraycopy(var0, 3 + param0, this.stackTrace, 0, this.stackTrace.length);
            return this.stackTrace.length;
        }
    }

    public boolean validateStackTrace(StackTraceElement param0, StackTraceElement param1) {
        if (this.stackTrace.length != 0 && param0 != null) {
            StackTraceElement var0 = this.stackTrace[0];
            if (var0.isNativeMethod() == param0.isNativeMethod()
                && var0.getClassName().equals(param0.getClassName())
                && var0.getFileName().equals(param0.getFileName())
                && var0.getMethodName().equals(param0.getMethodName())) {
                if (param1 != null != this.stackTrace.length > 1) {
                    return false;
                } else if (param1 != null && !this.stackTrace[1].equals(param1)) {
                    return false;
                } else {
                    this.stackTrace[0] = param0;
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void trimStacktrace(int param0) {
        StackTraceElement[] var0 = new StackTraceElement[this.stackTrace.length - param0];
        System.arraycopy(this.stackTrace, 0, var0, 0, var0.length);
        this.stackTrace = var0;
    }

    public void getDetails(StringBuilder param0) {
        param0.append("-- ").append(this.title).append(" --\n");
        param0.append("Details:");

        for(CrashReportCategory.Entry var0 : this.entries) {
            param0.append("\n\t");
            param0.append(var0.getKey());
            param0.append(": ");
            param0.append(var0.getValue());
        }

        if (this.stackTrace != null && this.stackTrace.length > 0) {
            param0.append("\nStacktrace:");

            for(StackTraceElement var1 : this.stackTrace) {
                param0.append("\n\tat ");
                param0.append(var1);
            }
        }

    }

    public StackTraceElement[] getStacktrace() {
        return this.stackTrace;
    }

    public static void populateBlockDetails(CrashReportCategory param0, BlockPos param1, @Nullable BlockState param2) {
        if (param2 != null) {
            param0.setDetail("Block", param2::toString);
        }

        param0.setDetail("Block location", () -> formatLocation(param1));
    }

    static class Entry {
        private final String key;
        private final String value;

        public Entry(String param0, Object param1) {
            this.key = param0;
            if (param1 == null) {
                this.value = "~~NULL~~";
            } else if (param1 instanceof Throwable) {
                Throwable var0 = (Throwable)param1;
                this.value = "~~ERROR~~ " + var0.getClass().getSimpleName() + ": " + var0.getMessage();
            } else {
                this.value = param1.toString();
            }

        }

        public String getKey() {
            return this.key;
        }

        public String getValue() {
            return this.value;
        }
    }
}
