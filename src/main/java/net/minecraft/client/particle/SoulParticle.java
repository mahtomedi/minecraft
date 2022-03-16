package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SoulParticle extends RisingParticle {
    private final SpriteSet sprites;
    protected boolean isGlowing;

    SoulParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.sprites = param7;
        this.scale(1.5F);
        this.setSpriteFromAge(param7);
    }

    @Override
    public int getLightColor(float param0) {
        return this.isGlowing ? 240 : super.getLightColor(param0);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @OnlyIn(Dist.CLIENT)
    public static class EmissiveProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public EmissiveProvider(SpriteSet param0) {
            this.sprite = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SoulParticle var0 = new SoulParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
            var0.setAlpha(1.0F);
            var0.isGlowing = true;
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
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SoulParticle var0 = new SoulParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
            var0.setAlpha(1.0F);
            return var0;
        }
    }
}
