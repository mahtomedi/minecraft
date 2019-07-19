package net.minecraft.client.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlameParticle extends TextureSheetParticle {
    private FlameParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.xd = this.xd * 0.01F + param4;
        this.yd = this.yd * 0.01F + param5;
        this.zd = this.zd * 0.01F + param6;
        this.x += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.y += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.z += (double)((this.random.nextFloat() - this.random.nextFloat()) * 0.05F);
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2)) + 4;
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
    public float getQuadSize(float param0) {
        float var0 = ((float)this.age + param0) / (float)this.lifetime;
        return this.quadSize * (1.0F - var0 * var0 * 0.5F);
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
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
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
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            FlameParticle var0 = new FlameParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
