package net.minecraft.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RunningTrimmedMean {
    private final long[] values;
    private int count;
    private int cursor;

    public RunningTrimmedMean(int param0) {
        this.values = new long[param0];
    }

    public long registerValueAndGetMean(long param0) {
        if (this.count < this.values.length) {
            ++this.count;
        }

        this.values[this.cursor] = param0;
        this.cursor = (this.cursor + 1) % this.values.length;
        long var0 = Long.MAX_VALUE;
        long var1 = Long.MIN_VALUE;
        long var2 = 0L;

        for(int var3 = 0; var3 < this.count; ++var3) {
            long var4 = this.values[var3];
            var2 += var4;
            var0 = Math.min(var0, var4);
            var1 = Math.max(var1, var4);
        }

        if (this.count > 2) {
            var2 -= var0 + var1;
            return var2 / (long)(this.count - 2);
        } else {
            return var2 > 0L ? (long)this.count / var2 : 0L;
        }
    }
}
