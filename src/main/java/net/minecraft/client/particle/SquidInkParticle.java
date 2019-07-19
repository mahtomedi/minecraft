package net.minecraft.client.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SquidInkParticle extends SimpleAnimatedParticle {
    private SquidInkParticle(Level param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3, param7, 0.0F);
        this.quadSize = 0.5F;
        this.setAlpha(1.0F);
        this.setColor(0.0F, 0.0F, 0.0F);
        this.lifetime = (int)((double)(this.quadSize * 12.0F) / (Math.random() * 0.8F + 0.2F));
        this.setSpriteFromAge(param7);
        this.hasPhysics = false;
        this.xd = param4;
        this.yd = param5;
        this.zd = param6;
        this.setBaseAirFriction(0.0F);
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
            if (this.age > this.lifetime / 2) {
                this.setAlpha(1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            }

            this.move(this.xd, this.yd, this.zd);
            if (this.level.getBlockState(new BlockPos(this.x, this.y, this.z)).isAir()) {
                this.yd -= 0.008F;
            }

            this.xd *= 0.92F;
            this.yd *= 0.92F;
            this.zd *= 0.92F;
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
            return new SquidInkParticle(param1, param2, param3, param4, param5, param6, param7, this.sprites);
        }
    }
}
