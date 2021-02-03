package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.DustParticleOptionsBase;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DustParticleBase<T extends DustParticleOptionsBase> extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected DustParticleBase(
        ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6, T param7, SpriteSet param8
    ) {
        super(param0, param1, param2, param3, param4, param5, param6);
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = param8;
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        float var0 = this.random.nextFloat() * 0.4F + 0.6F;
        this.rCol = this.randomizeColor(param7.getColor().x(), var0);
        this.gCol = this.randomizeColor(param7.getColor().y(), var0);
        this.bCol = this.randomizeColor(param7.getColor().z(), var0);
        this.quadSize *= 0.75F * param7.getScale();
        int var1 = (int)(8.0 / (this.random.nextDouble() * 0.8 + 0.2));
        this.lifetime = (int)Math.max((float)var1 * param7.getScale(), 1.0F);
        this.setSpriteFromAge(param8);
    }

    protected float randomizeColor(float param0, float param1) {
        return (this.random.nextFloat() * 0.2F + 0.8F) * param0 * param1;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public float getQuadSize(float param0) {
        return this.quadSize * Mth.clamp(((float)this.age + param0) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }
}
