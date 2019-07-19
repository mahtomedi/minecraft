package net.minecraft.core.particles;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParticleType<T extends ParticleOptions> {
    private final boolean overrideLimiter;
    private final ParticleOptions.Deserializer<T> deserializer;

    protected ParticleType(boolean param0, ParticleOptions.Deserializer<T> param1) {
        this.overrideLimiter = param0;
        this.deserializer = param1;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getOverrideLimiter() {
        return this.overrideLimiter;
    }

    public ParticleOptions.Deserializer<T> getDeserializer() {
        return this.deserializer;
    }
}
