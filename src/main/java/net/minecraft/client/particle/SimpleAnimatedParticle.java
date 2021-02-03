package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleAnimatedParticle extends TextureSheetParticle {
    protected final SpriteSet sprites;
    private float fadeR;
    private float fadeG;
    private float fadeB;
    private boolean hasFade;

    protected SimpleAnimatedParticle(ClientLevel param0, double param1, double param2, double param3, SpriteSet param4, float param5) {
        super(param0, param1, param2, param3);
        this.friction = 0.91F;
        this.gravity = param5;
        this.sprites = param4;
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
        super.tick();
        this.setSpriteFromAge(this.sprites);
        if (this.age > this.lifetime / 2) {
            this.setAlpha(1.0F - ((float)this.age - (float)(this.lifetime / 2)) / (float)this.lifetime);
            if (this.hasFade) {
                this.rCol += (this.fadeR - this.rCol) * 0.2F;
                this.gCol += (this.fadeG - this.gCol) * 0.2F;
                this.bCol += (this.fadeB - this.bCol) * 0.2F;
            }
        }

    }

    @Override
    public int getLightColor(float param0) {
        return 15728880;
    }
}
