package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlowParticle extends TextureSheetParticle {
    static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    GlowParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = param7;
        this.quadSize *= 0.75F;
        this.hasPhysics = false;
        this.setSpriteFromAge(param7);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float param0) {
        float var0 = ((float)this.age + param0) / (float)this.lifetime;
        var0 = Mth.clamp(var0, 0.0F, 1.0F);
        int var1 = super.getLightColor(param0);
        int var2 = var1 & 0xFF;
        int var3 = var1 >> 16 & 0xFF;
        var2 += (int)(var0 * 15.0F * 16.0F);
        if (var2 > 240) {
            var2 = 240;
        }

        return var2 | var3 << 16;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public static class AllayDustProvider implements ParticleProvider<SimpleParticleType> {
        private static final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public AllayDustProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            GlowParticle var0 = new GlowParticle(param1, param2, param3, param4, 0.0, 0.0, 0.0, this.sprite);
            if (param1.random.nextBoolean()) {
                var0.setColor(0.39F, 0.98F, 1.0F);
            } else {
                var0.setColor(0.13F, 0.81F, 1.0F);
            }

            var0.setParticleSpeed(param5 * 0.01, param6 * 0.01, param7 * 0.01);
            int var1 = 20;
            int var2 = 40;
            var0.setLifetime(param1.random.nextInt(20, 40));
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ElectricSparkProvider implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.25;
        private final SpriteSet sprite;

        public ElectricSparkProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            GlowParticle var0 = new GlowParticle(param1, param2, param3, param4, 0.0, 0.0, 0.0, this.sprite);
            var0.setColor(1.0F, 0.9F, 1.0F);
            var0.setParticleSpeed(param5 * 0.25, param6 * 0.25, param7 * 0.25);
            int var1 = 2;
            int var2 = 4;
            var0.setLifetime(param1.random.nextInt(2) + 2);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class GlowSquidProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public GlowSquidProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            GlowParticle var0 = new GlowParticle(
                param1, param2, param3, param4, 0.5 - GlowParticle.RANDOM.nextDouble(), param6, 0.5 - GlowParticle.RANDOM.nextDouble(), this.sprite
            );
            if (param1.random.nextBoolean()) {
                var0.setColor(0.6F, 1.0F, 0.8F);
            } else {
                var0.setColor(0.08F, 0.4F, 0.4F);
            }

            var0.yd *= 0.2F;
            if (param5 == 0.0 && param7 == 0.0) {
                var0.xd *= 0.1F;
                var0.zd *= 0.1F;
            }

            var0.setLifetime((int)(8.0 / (param1.random.nextDouble() * 0.8 + 0.2)));
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ScrapeProvider implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public ScrapeProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            GlowParticle var0 = new GlowParticle(param1, param2, param3, param4, 0.0, 0.0, 0.0, this.sprite);
            if (param1.random.nextBoolean()) {
                var0.setColor(0.29F, 0.58F, 0.51F);
            } else {
                var0.setColor(0.43F, 0.77F, 0.62F);
            }

            var0.setParticleSpeed(param5 * 0.01, param6 * 0.01, param7 * 0.01);
            int var1 = 10;
            int var2 = 40;
            var0.setLifetime(param1.random.nextInt(30) + 10);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WaxOffProvider implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public WaxOffProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            GlowParticle var0 = new GlowParticle(param1, param2, param3, param4, 0.0, 0.0, 0.0, this.sprite);
            var0.setColor(1.0F, 0.9F, 1.0F);
            var0.setParticleSpeed(param5 * 0.01 / 2.0, param6 * 0.01, param7 * 0.01 / 2.0);
            int var1 = 10;
            int var2 = 40;
            var0.setLifetime(param1.random.nextInt(30) + 10);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WaxOnProvider implements ParticleProvider<SimpleParticleType> {
        private final double SPEED_FACTOR = 0.01;
        private final SpriteSet sprite;

        public WaxOnProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            GlowParticle var0 = new GlowParticle(param1, param2, param3, param4, 0.0, 0.0, 0.0, this.sprite);
            var0.setColor(0.91F, 0.55F, 0.08F);
            var0.setParticleSpeed(param5 * 0.01 / 2.0, param6 * 0.01, param7 * 0.01 / 2.0);
            int var1 = 10;
            int var2 = 40;
            var0.setLifetime(param1.random.nextInt(30) + 10);
            return var0;
        }
    }
}
