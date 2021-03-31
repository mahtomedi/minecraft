package net.minecraft.util;

public class FrameTimer {
    public static final int LOGGING_LENGTH = 240;
    private final long[] loggedTimes = new long[240];
    private int logStart;
    private int logLength;
    private int logEnd;

    public void logFrameDuration(long param0) {
        this.loggedTimes[this.logEnd] = param0;
        ++this.logEnd;
        if (this.logEnd == 240) {
            this.logEnd = 0;
        }

        if (this.logLength < 240) {
            this.logStart = 0;
            ++this.logLength;
        } else {
            this.logStart = this.wrapIndex(this.logEnd + 1);
        }

    }

    public long getAverageDuration(int param0) {
        int var0 = (this.logStart + param0) % 240;
        int var1 = this.logStart;

        long var2;
        for(var2 = 0L; var1 != var0; ++var1) {
            var2 += this.loggedTimes[var1];
        }

        return var2 / (long)param0;
    }

    public int scaleAverageDurationTo(int param0, int param1) {
        return this.scaleSampleTo(this.getAverageDuration(param0), param1, 60);
    }

    public int scaleSampleTo(long param0, int param1, int param2) {
        double var0 = (double)param0 / (double)(1000000000L / (long)param2);
        return (int)(var0 * (double)param1);
    }

    public int getLogStart() {
        return this.logStart;
    }

    public int getLogEnd() {
        return this.logEnd;
    }

    public int wrapIndex(int param0) {
        return param0 % 240;
    }

    public long[] getLog() {
        return this.loggedTimes;
    }
}
