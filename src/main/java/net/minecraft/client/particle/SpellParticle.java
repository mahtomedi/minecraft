package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpellParticle extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    private SpellParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, 0.5 - RANDOM.nextDouble(), param5, 0.5 - RANDOM.nextDouble());
        this.sprites = param7;
        this.yd *= 0.2F;
        if (param4 == 0.0 && param6 == 0.0) {
            this.xd *= 0.1F;
            this.zd *= 0.1F;
        }

        this.quadSize *= 0.75F;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(param7);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.setSpriteFromAge(this.sprites);
            this.yd += 0.004;
            this.move(this.xd, this.yd, this.zd);
            if (this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd *= 0.96F;
            this.yd *= 0.96F;
            this.zd *= 0.96F;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class AmbientMobProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public AmbientMobProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            Particle var0 = new SpellParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
            var0.setAlpha(0.15F);
            var0.setColor((float)param5, (float)param6, (float)param7);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class InstantProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new SpellParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class MobProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MobProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            Particle var0 = new SpellParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
            var0.setColor((float)param5, (float)param6, (float)param7);
            return var0;
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
            return new SpellParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class WitchProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SpellParticle var0 = new SpellParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
            float var1 = param1.random.nextFloat() * 0.5F + 0.35F;
            var0.setColor(1.0F * var1, 0.0F * var1, 1.0F * var1);
            return var0;
        }
    }
}
