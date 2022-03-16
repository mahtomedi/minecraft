package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SculkChargeParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SculkChargeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    SculkChargeParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.friction = 0.96F;
        this.sprites = param7;
        this.scale(1.5F);
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
    public static record Provider(SpriteSet sprite) implements ParticleProvider<SculkChargeParticleOptions> {
        public Particle createParticle(
            SculkChargeParticleOptions param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            SculkChargeParticle var0 = new SculkChargeParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
            var0.setAlpha(1.0F);
            var0.setParticleSpeed(param5, param6, param7);
            var0.oRoll = param0.roll();
            var0.roll = param0.roll();
            var0.setLifetime(param1.random.nextInt(12) + 8);
            return var0;
        }
    }
}
