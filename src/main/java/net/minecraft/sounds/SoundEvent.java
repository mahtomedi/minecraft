package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
    public static final Codec<SoundEvent> CODEC = ResourceLocation.CODEC.xmap(SoundEvent::new, param0 -> param0.location);
    private final ResourceLocation location;

    public SoundEvent(ResourceLocation param0) {
        this.location = param0;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }
}
