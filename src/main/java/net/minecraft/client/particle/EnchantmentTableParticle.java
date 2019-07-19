package net.minecraft.client.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnchantmentTableParticle extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;

    private EnchantmentTableParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3);
        this.xd = param4;
        this.yd = param5;
        this.zd = param6;
        this.xStart = param1;
        this.yStart = param2;
        this.zStart = param3;
        this.xo = param1 + param4;
        this.yo = param2 + param5;
        this.zo = param3 + param6;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.quadSize = 0.1F * (this.random.nextFloat() * 0.5F + 0.2F);
        float var0 = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = 0.9F * var0;
        this.gCol = 0.9F * var0;
        this.bCol = var0;
        this.hasPhysics = false;
        this.lifetime = (int)(Math.random() * 10.0) + 30;
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
    public int getLightColor(float param0) {
        int var0 = super.getLightColor(param0);
        float var1 = (float)this.age / (float)this.lifetime;
        var1 *= var1;
        var1 *= var1;
        int var2 = var0 & 0xFF;
        int var3 = var0 >> 16 & 0xFF;
        var3 += (int)(var1 * 15.0F * 16.0F);
        if (var3 > 240) {
            var3 = 240;
        }

        return var2 | var3 << 16;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float var0 = (float)this.age / (float)this.lifetime;
            var0 = 1.0F - var0;
            float var1 = 1.0F - var0;
            var1 *= var1;
            var1 *= var1;
            this.x = this.xStart + this.xd * (double)var0;
            this.y = this.yStart + this.yd * (double)var0 - (double)(var1 * 1.2F);
            this.z = this.zStart + this.zd * (double)var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class NautilusProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public NautilusProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            EnchantmentTableParticle var0 = new EnchantmentTableParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
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
            EnchantmentTableParticle var0 = new EnchantmentTableParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
