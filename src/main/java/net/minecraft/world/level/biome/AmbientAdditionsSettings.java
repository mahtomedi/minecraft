package net.minecraft.world.level.biome;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AmbientAdditionsSettings {
    private SoundEvent soundEvent;
    private double tickChance;

    public AmbientAdditionsSettings(SoundEvent param0, double param1) {
        this.soundEvent = param0;
        this.tickChance = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    @OnlyIn(Dist.CLIENT)
    public double getTickChance() {
        return this.tickChance;
    }
}
