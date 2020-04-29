package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CampfireSmokeParticle extends TextureSheetParticle {
    private CampfireSmokeParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, boolean param7) {
        super(param0, param1, param2, param3);
        this.scale(3.0F);
        this.setSize(0.25F, 0.25F);
        if (param7) {
            this.lifetime = this.random.nextInt(50) + 280;
        } else {
            this.lifetime = this.random.nextInt(50) + 80;
        }

        this.gravity = 3.0E-6F;
        this.xd = param4;
        this.yd = param5 + (double)(this.random.nextFloat() / 500.0F);
        this.zd = param6;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ < this.lifetime && !(this.alpha <= 0.0F)) {
            this.xd += (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
            this.zd += (double)(this.random.nextFloat() / 5000.0F * (float)(this.random.nextBoolean() ? 1 : -1));
            this.yd -= (double)this.gravity;
            this.move(this.xd, this.yd, this.zd);
            if (this.age >= this.lifetime - 60 && this.alpha > 0.01F) {
                this.alpha -= 0.015F;
            }

        } else {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class CosyProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public CosyProvider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            CampfireSmokeParticle var0 = new CampfireSmokeParticle(param1, param2, param3, param4, param5, param6, param7, false);
            var0.setAlpha(0.9F);
            var0.pickSprite(this.sprites);
            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class SignalProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public SignalProvider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            CampfireSmokeParticle var0 = new CampfireSmokeParticle(param1, param2, param3, param4, param5, param6, param7, true);
            var0.setAlpha(0.95F);
            var0.pickSprite(this.sprites);
            return var0;
        }
    }
}
