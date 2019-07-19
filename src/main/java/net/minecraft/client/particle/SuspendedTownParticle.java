package net.minecraft.client.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SuspendedTownParticle extends TextureSheetParticle {
    private SuspendedTownParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
        float var0 = this.random.nextFloat() * 0.1F + 0.2F;
        this.rCol = var0;
        this.gCol = var0;
        this.bCol = var0;
        this.setSize(0.02F, 0.02F);
        this.quadSize *= this.random.nextFloat() * 0.6F + 0.5F;
        this.xd *= 0.02F;
        this.yd *= 0.02F;
        this.zd *= 0.02F;
        this.lifetime = (int)(20.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double param0, double param1, double param2) {
        this.setBoundingBox(this.getBoundingBox().move(param0, param1, param2));
        this.setLocationFromBoundingbox();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.xd *= 0.99;
            this.yd *= 0.99;
            this.zd *= 0.99;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class ComposterFillProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ComposterFillProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedTownParticle var0 = new SuspendedTownParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            var0.setColor(1.0F, 1.0F, 1.0F);
            var0.setLifetime(3 + param1.getRandom().nextInt(5));
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class DolphinSpeedProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DolphinSpeedProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedTownParticle var0 = new SuspendedTownParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.setColor(0.3F, 0.5F, 1.0F);
            var0.pickSprite(this.sprite);
            var0.setAlpha(1.0F - param1.random.nextFloat() * 0.7F);
            var0.setLifetime(var0.getLifetime() / 2);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class HappyVillagerProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public HappyVillagerProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SuspendedTownParticle var0 = new SuspendedTownParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            var0.setColor(1.0F, 1.0F, 1.0F);
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
            SuspendedTownParticle var0 = new SuspendedTownParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
