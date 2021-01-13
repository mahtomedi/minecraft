package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlameParticle extends RisingParticle {
    private FlameParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
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

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            FlameParticle var0 = new FlameParticle(param1, param2, param3, param4, param5, param6, param7);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
