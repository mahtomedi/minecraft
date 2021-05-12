package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DragonBreathParticle extends TextureSheetParticle {
    private static final int COLOR_MIN = 11993298;
    private static final int COLOR_MAX = 14614777;
    private static final float COLOR_MIN_RED = 0.7176471F;
    private static final float COLOR_MIN_GREEN = 0.0F;
    private static final float COLOR_MIN_BLUE = 0.8235294F;
    private static final float COLOR_MAX_RED = 0.8745098F;
    private static final float COLOR_MAX_GREEN = 0.0F;
    private static final float COLOR_MAX_BLUE = 0.9764706F;
    private boolean hasHitGround;
    private final SpriteSet sprites;

    DragonBreathParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, SpriteSet param7) {
        super(param0, param1, param2, param3);
        this.friction = 0.96F;
        this.xd = param4;
        this.yd = param5;
        this.zd = param6;
        this.rCol = Mth.nextFloat(this.random, 0.7176471F, 0.8745098F);
        this.gCol = Mth.nextFloat(this.random, 0.0F, 0.0F);
        this.bCol = Mth.nextFloat(this.random, 0.8235294F, 0.9764706F);
        this.quadSize *= 0.75F;
        this.lifetime = (int)(20.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
        this.hasHitGround = false;
        this.hasPhysics = false;
        this.sprites = param7;
        this.setSpriteFromAge(param7);
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
            if (this.onGround) {
                this.yd = 0.0;
                this.hasHitGround = true;
            }

            if (this.hasHitGround) {
                this.yd += 0.002;
            }

            this.move(this.xd, this.yd, this.zd);
            if (this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }

            this.xd *= (double)this.friction;
            this.zd *= (double)this.friction;
            if (this.hasHitGround) {
                this.yd *= (double)this.friction;
            }

        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float param0) {
        return this.quadSize * Mth.clamp(((float)this.age + param0) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
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
            return new DragonBreathParticle(param1, param2, param3, param4, param5, param6, param7, this.sprites);
        }
    }
}
