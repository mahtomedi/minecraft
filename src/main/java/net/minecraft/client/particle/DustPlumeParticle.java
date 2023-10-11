package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DustPlumeParticle extends BaseAshSmokeParticle {
    private static final int COLOR_RGB24 = 12235202;

    protected DustPlumeParticle(
        ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, float param7, SpriteSet param8
    ) {
        super(param0, param1, param2, param3, 0.7F, 0.6F, 0.7F, param4, param5 + 0.15F, param6, param7, param8, 0.5F, 7, 0.5F, false);
        float var0 = (float)Math.random() * 0.2F;
        this.rCol = (float)FastColor.ARGB32.red(12235202) / 255.0F - var0;
        this.gCol = (float)FastColor.ARGB32.green(12235202) / 255.0F - var0;
        this.bCol = (float)FastColor.ARGB32.blue(12235202) / 255.0F - var0;
    }

    @Override
    public void tick() {
        this.gravity = 0.88F * this.gravity;
        this.friction = 0.92F * this.friction;
        super.tick();
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
            return new DustPlumeParticle(param1, param2, param3, param4, param5, param6, param7, 1.0F, this.sprites);
        }
    }
}
