package net.minecraft.client.particle;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SmokeParticle(
        Level param0, double param1, double param2, double param3, double param4, double param5, double param6, float param7, SpriteSet param8
    ) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.sprites = param8;
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.xd += param4;
        this.yd += param5;
        this.zd += param6;
        float var0 = (float)(Math.random() * 0.3F);
        this.rCol = var0;
        this.gCol = var0;
        this.bCol = var0;
        this.quadSize *= 0.75F * param7;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        this.lifetime = (int)((float)this.lifetime * param7);
        this.lifetime = Math.max(this.lifetime, 1);
        this.setSpriteFromAge(param8);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float param0) {
        return this.quadSize * Mth.clamp(((float)this.age + param0) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
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
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, Level param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new SmokeParticle(param1, param2, param3, param4, param5, param6, param7, 1.0F, this.sprites);
        }
    }
}
