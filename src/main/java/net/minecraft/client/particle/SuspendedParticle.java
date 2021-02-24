package net.minecraft.client.particle;

import java.util.Optional;
import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleGroup;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuspendedParticle extends TextureSheetParticle {
    private SuspendedParticle(ClientLevel param0, SpriteSet param1, double param2, double param3, double param4) {
        super(param0, param2, param3 - 0.125, param4);
        this.setSize(0.01F, 0.01F);
        this.pickSprite(param1);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.2F;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
    }

    private SuspendedParticle(ClientLevel param0, SpriteSet param1, double param2, double param3, double param4, double param5, double param6, double param7) {
        super(param0, param2, param3 - 0.125, param4, param5, param6, param7);
        this.setSize(0.01F, 0.01F);
        this.pickSprite(param1);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.6F;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.friction = 1.0F;
        this.gravity = 0.0F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class CrimsonSporeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public CrimsonSporeProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            Random var0 = param1.random;
            double var1 = var0.nextGaussian() * 1.0E-6F;
            double var2 = var0.nextGaussian() * 1.0E-4F;
            double var3 = var0.nextGaussian() * 1.0E-6F;
            SuspendedParticle var4 = new SuspendedParticle(param1, this.sprite, param2, param3, param4, var1, var2, var3);
            var4.setColor(0.9F, 0.4F, 0.5F);
            return var4;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SporeBlossomAirProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public SporeBlossomAirProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedParticle var0 = new SuspendedParticle(param1, this.sprite, param2, param3, param4, 0.0, -0.8F, 0.0) {
                @Override
                public Optional<ParticleGroup> getParticleGroup() {
                    return Optional.of(ParticleGroup.SPORE_BLOSSOM);
                }
            };
            var0.lifetime = Mth.randomBetweenInclusive(param1.random, 500, 1000);
            var0.gravity = 0.01F;
            var0.setColor(0.32F, 0.5F, 0.22F);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class UnderwaterProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public UnderwaterProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedParticle var0 = new SuspendedParticle(param1, this.sprite, param2, param3, param4);
            var0.setColor(0.4F, 0.4F, 0.7F);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WarpedSporeProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WarpedSporeProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            double var0 = (double)param1.random.nextFloat() * -1.9 * (double)param1.random.nextFloat() * 0.1;
            SuspendedParticle var1 = new SuspendedParticle(param1, this.sprite, param2, param3, param4, 0.0, var0, 0.0);
            var1.setColor(0.1F, 0.1F, 0.3F);
            var1.setSize(0.001F, 0.001F);
            return var1;
        }
    }
}
