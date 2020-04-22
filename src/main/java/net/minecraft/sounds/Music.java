package net.minecraft.sounds;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Music {
    private final SoundEvent event;
    private final int minDelay;
    private final int maxDelay;
    private final boolean replaceCurrentMusic;

    public Music(SoundEvent param0, int param1, int param2, boolean param3) {
        this.event = param0;
        this.minDelay = param1;
        this.maxDelay = param2;
        this.replaceCurrentMusic = param3;
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getEvent() {
        return this.event;
    }

    @OnlyIn(Dist.CLIENT)
    public int getMinDelay() {
        return this.minDelay;
    }

    @OnlyIn(Dist.CLIENT)
    public int getMaxDelay() {
        return this.maxDelay;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean replaceCurrentMusic() {
        return this.replaceCurrentMusic;
    }
}
