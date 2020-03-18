package net.minecraft.world.level.biome;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AmbientMoodSettings {
    public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
    private SoundEvent soundEvent;
    private int tickDelay;
    private int blockSearchExtent;
    private double soundPositionOffset;

    public AmbientMoodSettings(SoundEvent param0, int param1, int param2, double param3) {
        this.soundEvent = param0;
        this.tickDelay = param1;
        this.blockSearchExtent = param2;
        this.soundPositionOffset = param3;
    }

    @OnlyIn(Dist.CLIENT)
    public SoundEvent getSoundEvent() {
        return this.soundEvent;
    }

    @OnlyIn(Dist.CLIENT)
    public int getTickDelay() {
        return this.tickDelay;
    }

    @OnlyIn(Dist.CLIENT)
    public int getBlockSearchExtent() {
        return this.blockSearchExtent;
    }

    @OnlyIn(Dist.CLIENT)
    public double getSoundPositionOffset() {
        return this.soundPositionOffset;
    }
}
