package net.minecraft.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SoundEvent {
    private final ResourceLocation location;

    public SoundEvent(ResourceLocation param0) {
        this.location = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getLocation() {
        return this.location;
    }
}
