package net.minecraft.client.multiplayer;

import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkReceiveSpeedAccumulator {
    private final int[] batchSizes;
    private final int[] batchDurations;
    private int index;
    private int filledSize;

    public ChunkReceiveSpeedAccumulator(int param0) {
        this.batchSizes = new int[param0];
        this.batchDurations = new int[param0];
    }

    public void accumulate(int param0, long param1) {
        this.batchSizes[this.index] = param0;
        this.batchDurations[this.index] = (int)Mth.clamp((float)param1, 0.0F, 15000.0F);
        this.index = (this.index + 1) % this.batchSizes.length;
        if (this.filledSize < this.batchSizes.length) {
            ++this.filledSize;
        }

    }

    public double getMillisPerChunk() {
        int var0 = 0;
        int var1 = 0;

        for(int var2 = 0; var2 < Math.min(this.filledSize, this.batchSizes.length); ++var2) {
            var0 += this.batchSizes[var2];
            var1 += this.batchDurations[var2];
        }

        return (double)var1 * 1.0 / (double)var0;
    }
}
