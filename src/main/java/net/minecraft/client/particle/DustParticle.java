package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DustParticle extends DustParticleBase<DustParticleOptions> {
    protected DustParticle(
        ClientLevel param0,
        double param1,
        double param2,
        double param3,
        double param4,
        double param5,
        double param6,
        DustParticleOptions param7,
        SpriteSet param8
    ) {
        super(param0, param1, param2, param3, param4, param5, param6, param7, param8);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<DustParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            DustParticleOptions param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new DustParticle(param1, param2, param3, param4, param5, param6, param7, param0, this.sprites);
        }
    }
}
