package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExplodeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected ExplodeParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3);
        this.gravity = -0.1F;
        this.friction = 0.9F;
        this.sprites = param7;
        this.xd = param4 + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.yd = param5 + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.zd = param6 + (Math.random() * 2.0 - 1.0) * 0.05F;
        float var0 = this.random.nextFloat() * 0.3F + 0.7F;
        this.rCol = var0;
        this.gCol = var0;
        this.bCol = var0;
        this.quadSize = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 6.0F + 1.0F);
        this.lifetime = (int)(16.0 / ((double)this.random.nextFloat() * 0.8 + 0.2)) + 2;
        this.setSpriteFromAge(param7);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
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
            return new ExplodeParticle(param1, param2, param3, param4, param5, param6, param7, this.sprites);
        }
    }
}
