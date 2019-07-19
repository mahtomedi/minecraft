package net.minecraft.util;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class FrameTimer {
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

    @OnlyIn(Dist.CLIENT)
    public int scaleSampleTo(long param0, int param1, int param2) {
        double var0 = (double)param0 / (double)(1000000000L / (long)param2);
        return (int)(var0 * (double)param1);
    }

    @OnlyIn(Dist.CLIENT)
    public int getLogStart() {
        return this.logStart;
    }

    @OnlyIn(Dist.CLIENT)
    public int getLogEnd() {
        return this.logEnd;
    }

    public int wrapIndex(int param0) {
        return param0 % 240;
    }

    @OnlyIn(Dist.CLIENT)
    public long[] getLog() {
        return this.loggedTimes;
    }
}
