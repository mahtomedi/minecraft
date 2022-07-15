package net.minecraft;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.state.BlockState;

public class CrashReportCategory {
    private final String title;
    private final List<CrashReportCategory.Entry> entries = Lists.newArrayList();
    private StackTraceElement[] stackTrace = new StackTraceElement[0];

    public CrashReportCategory(String param0) {
        this.title = param0;
    }

    public static String formatLocation(LevelHeightAccessor param0, double param1, double param2, double param3) {
        return String.format(Locale.ROOT, "%.2f,%.2f,%.2f - %s", param1, param2, param3, formatLocation(param0, new BlockPos(param1, param2, param3)));
    }

    public static String formatLocation(LevelHeightAccessor param0, BlockPos param1) {
        return formatLocation(param0, param1.getX(), param1.getY(), param1.getZ());
    }

    public static String formatLocation(LevelHeightAccessor param0, int param1, int param2, int param3) {
        StringBuilder var0 = new StringBuilder();

        try {
            var0.append(String.format(Locale.ROOT, "World: (%d,%d,%d)", param1, param2, param3));
        } catch (Throwable var191) {
            var0.append("(Error finding world loc)");
        }

        var0.append(", ");

        try {
            int var2 = SectionPos.blockToSectionCoord(param1);
            int var3 = SectionPos.blockToSectionCoord(param2);
            int var4 = SectionPos.blockToSectionCoord(param3);
            int var5 = param1 & 15;
            int var6 = param2 & 15;
            int var7 = param3 & 15;
            int var8 = SectionPos.sectionToBlockCoord(var2);
            int var9 = param0.getMinBuildHeight();
            int var10 = SectionPos.sectionToBlockCoord(var4);
            int var11 = SectionPos.sectionToBlockCoord(var2 + 1) - 1;
            int var12 = param0.getMaxBuildHeight() - 1;
            int var13 = SectionPos.sectionToBlockCoord(var4 + 1) - 1;
            var0.append(
                String.format(
                    Locale.ROOT,
                    "Section: (at %d,%d,%d in %d,%d,%d; chunk contains blocks %d,%d,%d to %d,%d,%d)",
                    var5,
                    var6,
                    var7,
                    var2,
                    var3,
                    var4,
                    var8,
                    var9,
                    var10,
                    var11,
                    var12,
                    var13
                )
            );
        } catch (Throwable var181) {
            var0.append("(Error finding chunk loc)");
        }

        var0.append(", ");

        try {
            int var15 = param1 >> 9;
            int var16 = param3 >> 9;
            int var17 = var15 << 5;
            int var18 = var16 << 5;
            int var19 = (var15 + 1 << 5) - 1;
            int var20 = (var16 + 1 << 5) - 1;
            int var21 = var15 << 9;
            int var22 = param0.getMinBuildHeight();
            int var23 = var16 << 9;
            int var24 = (var15 + 1 << 9) - 1;
            int var25 = param0.getMaxBuildHeight() - 1;
            int var26 = (var16 + 1 << 9) - 1;
            var0.append(
                String.format(
                    Locale.ROOT,
                    "Region: (%d,%d; contains chunks %d,%d to %d,%d, blocks %d,%d,%d to %d,%d,%d)",
                    var15,
                    var16,
                    var17,
                    var18,
                    var19,
                    var20,
                    var21,
                    var22,
                    var23,
                    var24,
                    var25,
                    var26
                )
            );
        } catch (Throwable var171) {
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

    public static void populateBlockDetails(CrashReportCategory param0, LevelHeightAccessor param1, BlockPos param2, @Nullable BlockState param3) {
        if (param3 != null) {
            param0.setDetail("Block", param3::toString);
        }

        param0.setDetail("Block location", () -> formatLocation(param1, param2));
    }

    static class Entry {
        private final String key;
        private final String value;

        public Entry(String param0, @Nullable Object param1) {
            this.key = param0;
            if (param1 == null) {
                this.value = "~~NULL~~";
            } else if (param1 instanceof Throwable var0) {
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
