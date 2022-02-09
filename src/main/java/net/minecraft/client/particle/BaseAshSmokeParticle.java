package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BaseAshSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected BaseAshSmokeParticle(
        ClientLevel param0,
        double param1,
        double param2,
        double param3,
        float param4,
        float param5,
        float param6,
        double param7,
        double param8,
        double param9,
        float param10,
        SpriteSet param11,
        float param12,
        int param13,
        float param14,
        boolean param15
    ) {
        super(param0, param1, param2, param3, 0.0, 0.0, 0.0);
        this.friction = 0.96F;
        this.gravity = param14;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = param11;
        this.xd *= (double)param4;
        this.yd *= (double)param5;
        this.zd *= (double)param6;
        this.xd += param7;
        this.yd += param8;
        this.zd += param9;
        float var0 = param0.random.nextFloat() * param12;
        this.rCol = var0;
        this.gCol = var0;
        this.bCol = var0;
        this.quadSize *= 0.75F * param10;
        this.lifetime = (int)((double)param13 / ((double)param0.random.nextFloat() * 0.8 + 0.2) * (double)param10);
        this.lifetime = Math.max(this.lifetime, 1);
        this.setSpriteFromAge(param11);
        this.hasPhysics = param15;
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
