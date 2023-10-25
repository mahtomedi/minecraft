package net.minecraft.client;

import it.unimi.dsi.fastutil.floats.FloatUnaryOperator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Timer {
    public float partialTick;
    public float tickDelta;
    private long lastMs;
    private final float msPerTick;
    private final FloatUnaryOperator targetMsptProvider;

    public Timer(float param0, long param1, FloatUnaryOperator param2) {
        this.msPerTick = 1000.0F / param0;
        this.lastMs = param1;
        this.targetMsptProvider = param2;
    }

    public int advanceTime(long param0) {
        this.tickDelta = (float)(param0 - this.lastMs) / this.targetMsptProvider.apply(this.msPerTick);
        this.lastMs = param0;
        this.partialTick += this.tickDelta;
        int var0 = (int)this.partialTick;
        this.partialTick -= (float)var0;
        return var0;
    }
}
