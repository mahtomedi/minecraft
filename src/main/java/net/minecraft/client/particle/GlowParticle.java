package net.minecraft.client.particle;

import java.util.Random;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GlowParticle extends TextureSheetParticle {
    private static final Random RANDOM = new Random();
    private final SpriteSet sprites;

    private GlowParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, 0.5 - RANDOM.nextDouble(), param5, 0.5 - RANDOM.nextDouble());
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = param7;
        this.yd *= 0.2F;
        if (param4 == 0.0 && param6 == 0.0) {
            this.xd *= 0.1F;
            this.zd *= 0.1F;
        }

        this.quadSize *= 0.75F;
        this.lifetime = (int)(8.0 / (Math.random() * 0.8 + 0.2));
        if (this.random.nextBoolean()) {
            this.setColor(0.6F, 1.0F, 0.8F);
        } else {
            this.setColor(0.08F, 0.4F, 0.4F);
        }

        this.hasPhysics = false;
        this.setSpriteFromAge(param7);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
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
            return new GlowParticle(param1, param2, param3, param4, param5, param6, param7, this.sprite);
        }
    }
}
