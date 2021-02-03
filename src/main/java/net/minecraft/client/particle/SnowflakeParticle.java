package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SnowflakeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected SnowflakeParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3);
        this.gravity = 0.225F;
        this.friction = 1.0F;
        this.sprites = param7;
        this.xd = param4 + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.yd = param5 + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.zd = param6 + (Math.random() * 2.0 - 1.0) * 0.05F;
        this.quadSize = 0.1F * (this.random.nextFloat() * this.random.nextFloat() * 1.0F + 1.0F);
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
        this.xd *= 0.95F;
        this.yd *= 0.9F;
        this.zd *= 0.95F;
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
            SnowflakeParticle var0 = new SnowflakeParticle(param1, param2, param3, param4, param5, param6, param7, this.sprites);
            var0.setColor(0.923F, 0.964F, 0.999F);
            return var0;
        }
    }
}
