package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WhiteAshParticle extends BaseAshSmokeParticle {
    private static final int COLOR_RGB24 = 12235202;

    protected WhiteAshParticle(
        ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, float param7, SpriteSet param8
    ) {
        super(param0, param1, param2, param3, 0.1F, -0.1F, 0.1F, param4, param5, param6, param7, param8, 0.0F, 20, 0.0125F, false);
        this.rCol = (float)FastColor.ARGB32.red(12235202) / 255.0F;
        this.gCol = (float)FastColor.ARGB32.green(12235202) / 255.0F;
        this.bCol = (float)FastColor.ARGB32.blue(12235202) / 255.0F;
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
            RandomSource var0 = param1.random;
            double var1 = (double)var0.nextFloat() * -1.9 * (double)var0.nextFloat() * 0.1;
            double var2 = (double)var0.nextFloat() * -0.5 * (double)var0.nextFloat() * 0.1 * 5.0;
            double var3 = (double)var0.nextFloat() * -1.9 * (double)var0.nextFloat() * 0.1;
            return new WhiteAshParticle(param1, param2, param3, param4, var1, var2, var3, 1.0F, this.sprites);
        }
    }
}
