package net.minecraft.sounds;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoundEvent {
    public static final Codec<SoundEvent> CODEC = ResourceLocation.CODEC.xmap(SoundEvent::new, param0 -> param0.location);
    private final ResourceLocation location;

    public SoundEvent(ResourceLocation param0) {
        this.location = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getLocation() {
        return this.location;
    }
}
