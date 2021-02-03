package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LavaParticle extends TextureSheetParticle {
    private LavaParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.gravity = 0.75F;
        this.friction = 0.999F;
        this.xd *= 0.8F;
        this.yd *= 0.8F;
        this.zd *= 0.8F;
        this.yd = (double)(this.random.nextFloat() * 0.4F + 0.05F);
        this.quadSize *= this.random.nextFloat() * 2.0F + 0.2F;
        this.lifetime = (int)(16.0 / (Math.random() * 0.8 + 0.2));
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float param0) {
        int var0 = super.getLightColor(param0);
        int var1 = 240;
        int var2 = var0 >> 16 & 0xFF;
        return 240 | var2 << 16;
    }

    @Override
    public float getQuadSize(float param0) {
        float var0 = ((float)this.age + param0) / (float)this.lifetime;
        return this.quadSize * (1.0F - var0 * var0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float var0 = (float)this.age / (float)this.lifetime;
            if (this.random.nextFloat() > var0) {
                this.level.addParticle(ParticleTypes.SMOKE, this.x, this.y, this.z, this.xd, this.yd, this.zd);
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
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            LavaParticle var0 = new LavaParticle(param1, param2, param3, param4);
            var0.pickSprite(this.sprite);
            return var0;
        }
    }
}
