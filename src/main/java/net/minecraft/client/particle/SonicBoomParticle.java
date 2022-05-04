package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SonicBoomParticle extends HugeExplosionParticle {
    protected SonicBoomParticle(ClientLevel param0, double param1, double param2, double param3, double param4, SpriteSet param5) {
        super(param0, param1, param2, param3, param4, param5);
        this.lifetime = 16;
        this.quadSize = 1.5F;
        this.setSpriteFromAge(param5);
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
            return new SonicBoomParticle(param1, param2, param3, param4, param5, this.sprites);
        }
    }
}
