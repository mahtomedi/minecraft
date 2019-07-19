package net.minecraft.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Timer {
    public int ticks;
    public float partialTick;
    public float tickDelta;
    private long lastMs;
    private final float msPerTick;

    public Timer(float param0, long param1) {
        this.msPerTick = 1000.0F / param0;
        this.lastMs = param1;
    }

    public void advanceTime(long param0) {
        this.tickDelta = (float)(param0 - this.lastMs) / this.msPerTick;
        this.lastMs = param0;
        this.partialTick += this.tickDelta;
        this.ticks = (int)this.partialTick;
        this.partialTick -= (float)this.ticks;
    }
}
