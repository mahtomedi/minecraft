package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HugeExplosionParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected HugeExplosionParticle(ClientLevel param0, double param1, double param2, double param3, double param4, SpriteSet param5) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.lifetime = 6 + this.random.nextInt(4);
        float var0 = this.random.nextFloat() * 0.6F + 0.4F;
        this.rCol = var0;
        this.gCol = var0;
        this.bCol = var0;
        this.quadSize = 2.0F * (1.0F - (float)param4 * 0.5F);
        this.sprites = param5;
        this.setSpriteFromAge(param5);
    }

    @Override
    public int getLightColor(float param0) {
        return 15728880;
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
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
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
            return new HugeExplosionParticle(param1, param2, param3, param4, param5, this.sprites);
        }
    }
}
