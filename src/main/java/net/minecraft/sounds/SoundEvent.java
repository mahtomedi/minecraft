package net.minecraft.sounds;

import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
    private static final float DEFAULT_RANGE = 16.0F;
    private final ResourceLocation location;
    private final float range;
    private final boolean newSystem;

    static SoundEvent createVariableRangeEvent(ResourceLocation param0) {
        return new SoundEvent(param0, 16.0F, false);
    }

    static SoundEvent createFixedRangeEvent(ResourceLocation param0, float param1) {
        return new SoundEvent(param0, param1, true);
    }

    private SoundEvent(ResourceLocation param0, float param1, boolean param2) {
        this.location = param0;
        this.range = param1;
        this.newSystem = param2;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public float getRange(float param0) {
        return this.newSystem ? this.range : legacySoundRange(param0);
    }

    public static float legacySoundRange(float param0) {
        return param0 > 1.0F ? 16.0F * param0 : 16.0F;
    }
}
