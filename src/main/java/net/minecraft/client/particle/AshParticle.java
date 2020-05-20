package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AshParticle extends BaseAshSmokeParticle {
    protected AshParticle(
        ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, float param7, SpriteSet param8
    ) {
        super(param0, param1, param2, param3, 0.1F, -0.1F, 0.1F, param4, param5, param6, param7, param8, 0.5F, 20, -0.004, false);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new AshParticle(param1, param2, param3, param4, 0.0, 0.0, 0.0, 1.0F, this.sprites);
        }
    }
}
