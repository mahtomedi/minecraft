package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
    public static final Codec<SoundEvent> CODEC = ResourceLocation.CODEC.xmap(SoundEvent::new, param0 -> param0.location);
    private final ResourceLocation location;
    private final float range;
    private final boolean newSystem;

    public SoundEvent(ResourceLocation param0) {
        this(param0, 16.0F, false);
    }

    public SoundEvent(ResourceLocation param0, float param1) {
        this(param0, param1, true);
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
        if (this.newSystem) {
            return this.range;
        } else {
            return param0 > 1.0F ? 16.0F * param0 : 16.0F;
        }
    }
}
