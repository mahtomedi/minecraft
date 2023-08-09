package net.minecraft.client.multiplayer;

import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChunkBatchSizeCalculator {
    private static final int MAX_OLD_SAMPLES_WEIGHT = 49;
    private static final int CLAMP_COEFFICIENT = 3;
    private double aggregatedNanosPerChunk = 2000000.0;
    private int oldSamplesWeight = 1;
    private volatile long chunkBatchStartTime = Util.getNanos();

    public void onBatchStart() {
        this.chunkBatchStartTime = Util.getNanos();
    }

    public void onBatchFinished(int param0) {
        if (param0 > 0) {
            double var0 = (double)(Util.getNanos() - this.chunkBatchStartTime);
            double var1 = var0 / (double)param0;
            double var2 = Mth.clamp(var1, this.aggregatedNanosPerChunk / 3.0, this.aggregatedNanosPerChunk * 3.0);
            this.aggregatedNanosPerChunk = (this.aggregatedNanosPerChunk * (double)this.oldSamplesWeight + var2) / (double)(this.oldSamplesWeight + 1);
            this.oldSamplesWeight = Math.min(49, this.oldSamplesWeight + 1);
        }

    }

    public float getDesiredChunksPerTick() {
        return (float)(7000000.0 / this.aggregatedNanosPerChunk);
    }
}
