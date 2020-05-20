package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WhiteAshParticle extends BaseAshSmokeParticle {
    protected WhiteAshParticle(
        ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, float param7, SpriteSet param8
    ) {
        super(param0, param1, param2, param3, 0.1F, -0.1F, 0.1F, param4, param5, param6, param7, param8, 0.0F, 20, -5.0E-4, false);
        this.rCol = 0.7294118F;
        this.gCol = 0.69411767F;
        this.bCol = 0.7607843F;
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
            Random var0 = param1.random;
            double var1 = (double)var0.nextFloat() * -1.9 * (double)var0.nextFloat() * 0.1;
            double var2 = (double)var0.nextFloat() * -0.5 * (double)var0.nextFloat() * 0.1 * 5.0;
            double var3 = (double)var0.nextFloat() * -1.9 * (double)var0.nextFloat() * 0.1;
            return new WhiteAshParticle(param1, param2, param3, param4, var1, var2, var3, 1.0F, this.sprites);
        }
    }
}
