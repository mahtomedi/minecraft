package net.minecraft.core.particles;

import java.util.Random;
import java.util.function.BiFunction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ParticleType<T extends ParticleOptions> {
    private final boolean overrideLimiter;
    private final ParticleOptions.Deserializer<T> deserializer;
    private final BiFunction<Random, ParticleType<T>, T> randomOptionProvider;

    public ParticleType(boolean param0, ParticleOptions.Deserializer<T> param1, BiFunction<Random, ParticleType<T>, T> param2) {
        this.overrideLimiter = param0;
        this.deserializer = param1;
        this.randomOptionProvider = param2;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean getOverrideLimiter() {
        return this.overrideLimiter;
    }

    public ParticleOptions.Deserializer<T> getDeserializer() {
        return this.deserializer;
    }

    public T getRandom(Random param0) {
        return this.randomOptionProvider.apply(param0, this);
    }
}
