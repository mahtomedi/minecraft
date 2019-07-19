package net.minecraft.client.particle;

import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleAnimatedParticle extends TextureSheetParticle {
    protected final SpriteSet sprites;
    private final float baseGravity;
    private float baseAirFriction = 0.91F;
    private float fadeR;
    private float fadeG;
    private float fadeB;
    private boolean hasFade;

    protected SimpleAnimatedParticle(Level param0, double param1, double param2, double param3, SpriteSet param4, float param5) {
        super(param0, param1, param2, param3);
        this.sprites = param4;
        this.baseGravity = param5;
    }

    public void setColor(int param0) {
        float var0 = (float)((param0 & 0xFF0000) >> 16) / 255.0F;
        float var1 = (float)((param0 & 0xFF00) >> 8) / 255.0F;
        float var2 = (float)((param0 & 0xFF) >> 0) / 255.0F;
        float var3 = 1.0F;
        this.setColor(var0 * 1.0F, var1 * 1.0F, var2 * 1.0F);
    }

    public void setFadeColor(int param0) {
        this.fadeR = (float)((param0 & 0xFF0000) >> 16) / 255.0F;
        this.fadeG = (float)((param0 & 0xFF00) >> 8) / 255.0F;
        this.fadeB = (float)((param0 & 0xFF) >> 0) / 255.0F;
        this.hasFade = true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
                if (this.hasFade) {
                    this.rCol += (this.fadeR - this.rCol) * 0.2F;
                    this.gCol += (this.fadeG - this.gCol) * 0.2F;
                    this.bCol += (this.fadeB - this.bCol) * 0.2F;
                }
            }

            this.yd += (double)this.baseGravity;
            this.move(this.xd, this.yd, this.zd);
            this.xd *= (double)this.baseAirFriction;
            this.yd *= (double)this.baseAirFriction;
            this.zd *= (double)this.baseAirFriction;
            if (this.onGround) {
                this.xd *= 0.7F;
                this.zd *= 0.7F;
            }

        }
    }

    @Override
    public int getLightColor(float param0) {
        return 15728880;
    }

    protected void setBaseAirFriction(float param0) {
        this.baseAirFriction = param0;
    }
}
