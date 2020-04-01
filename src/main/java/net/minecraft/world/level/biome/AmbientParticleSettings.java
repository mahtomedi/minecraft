package net.minecraft.world.level.biome;

import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AmbientParticleSettings {
    private final ParticleOptions particleType;
    private final float probability;
    private final double xVelocity;
    private final double yVelocity;
    private final double zVelocity;

    public AmbientParticleSettings(ParticleOptions param0, float param1, double param2, double param3, double param4) {
        this.particleType = param0;
        this.probability = param1;
        this.xVelocity = param2;
        this.yVelocity = param3;
        this.zVelocity = param4;
    }

    @OnlyIn(Dist.CLIENT)
    public ParticleOptions getParticleType() {
        return this.particleType;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canSpawn(Random param0) {
        return param0.nextFloat() <= this.probability;
    }

    @OnlyIn(Dist.CLIENT)
    public double getXVelocity() {
        return this.xVelocity;
    }

    @OnlyIn(Dist.CLIENT)
    public double getYVelocity() {
        return this.yVelocity;
    }

    @OnlyIn(Dist.CLIENT)
    public double getZVelocity() {
        return this.zVelocity;
    }

    public static AmbientParticleSettings random(Random param0) {
        return new AmbientParticleSettings(
            Registry.PARTICLE_TYPE.getRandom(param0).getRandom(param0),
            param0.nextFloat() * 0.2F,
            param0.nextDouble(),
            param0.nextDouble(),
            param0.nextDouble()
        );
    }
}
