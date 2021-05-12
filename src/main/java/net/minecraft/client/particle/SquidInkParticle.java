package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidInkParticle extends SimpleAnimatedParticle {
    SquidInkParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, int param7, SpriteSet param8) {
        super(param0, param1, param2, param3, param8, 0.0F);
        this.friction = 0.92F;
        this.quadSize = 0.5F;
        this.setAlpha(1.0F);
        this.setColor((float)FastColor.ARGB32.red(param7), (float)FastColor.ARGB32.green(param7), (float)FastColor.ARGB32.blue(param7));
        this.lifetime = (int)((double)(this.quadSize * 12.0F) / (Math.random() * 0.8F + 0.2F));
        this.setSpriteFromAge(param8);
        this.hasPhysics = false;
        this.xd = param4;
        this.yd = param5;
        this.zd = param6;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setSpriteFromAge(this.sprites);
            if (this.age > this.lifetime / 2) {
                this.setAlpha(1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            }

            if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).isAir()) {
                this.yd -= 0.0074F;
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class GlowInkProvider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public GlowInkProvider(SpriteSet param0) {
            this.sprites = param0;
        }

        public Particle createParticle(
            SimpleParticleType param0, ClientLevel param1, double param2, double param3, double param4, double param5, double param6, double param7
        ) {
            return new SquidInkParticle(param1, param2, param3, param4, param5, param6, param7, FastColor.ARGB32.color(255, 204, 31, 102), this.sprites);
        }
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
            return new SquidInkParticle(param1, param2, param3, param4, param5, param6, param7, FastColor.ARGB32.color(255, 255, 255, 255), this.sprites);
        }
    }
}
