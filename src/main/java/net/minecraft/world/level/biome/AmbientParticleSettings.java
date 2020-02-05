package net.minecraft.world.level.biome;

import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AmbientParticleSettings {
    private final SimpleParticleType particleType;
    private final float probability;
    private final Function<Random, Double> xVelocity;
    private final Function<Random, Double> yVelocity;
    private final Function<Random, Double> zVelocity;

    public AmbientParticleSettings(
        SimpleParticleType param0, float param1, Function<Random, Double> param2, Function<Random, Double> param3, Function<Random, Double> param4
    ) {
        this.particleType = param0;
        this.probability = param1;
        this.xVelocity = param2;
        this.yVelocity = param3;
        this.zVelocity = param4;
    }

    @OnlyIn(Dist.CLIENT)
    public SimpleParticleType getParticleType() {
        return this.particleType;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canSpawn(Random param0) {
        return param0.nextFloat() <= this.probability;
    }

    @OnlyIn(Dist.CLIENT)
    public double getXVelocity(Random param0) {
        return this.xVelocity.apply(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public double getYVelocity(Random param0) {
        return this.yVelocity.apply(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public double getZVelocity(Random param0) {
        return this.zVelocity.apply(param0);
    }
}
