package net.minecraft.client.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SplashParticle extends WaterDropParticle {
    private SplashParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3);
        this.gravity = 0.04F;
        if (param5 == 0.0 && (param4 != 0.0 || param6 != 0.0)) {
            this.xd = param4;
            this.yd = 0.1;
            this.zd = param6;
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SplashParticle var0 = new SplashParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
