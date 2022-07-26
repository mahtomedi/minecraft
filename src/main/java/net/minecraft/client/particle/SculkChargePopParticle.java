package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SculkChargePopParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    SculkChargePopParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.friction = 0.96F;
        this.sprites = param7;
        this.scale(1.0F);
        this.hasPhysics = false;
        this.setSpriteFromAge(param7);
    }

    @Override
    public int getLightColor(float param0) {
        return 240;
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
    public static record Provider(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SculkChargePopParticle var0 = new SculkChargePopParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
            var0.setAlpha(1.0F);
            var0.setParticleSpeed(param5, param6, param7);
            var0.setLifetime(param1.random.nextInt(4) + 6);
            return var0;
        }
    }
}
