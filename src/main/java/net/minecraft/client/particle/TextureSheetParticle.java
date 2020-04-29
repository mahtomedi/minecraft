package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class TextureSheetParticle extends SingleQuadParticle {
    protected TextureAtlasSprite sprite;

    protected TextureSheetParticle(ClientLevel param0, double param1, double param2, double param3) {
        super(param0, param1, param2, param3);
    }

    protected TextureSheetParticle(ClientLevel param0, double param1, double param2, double param3, double param4, double param5, double param6) {
        super(param0, param1, param2, param3, param4, param5, param6);
    }

    protected void setSprite(TextureAtlasSprite param0) {
        this.sprite = param0;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU0();
    }

    @Override
    protected float getU1() {
        return this.sprite.getU1();
    }

    @Override
    protected float getV0() {
        return this.sprite.getV0();
    }

    @Override
    protected float getV1() {
        return this.sprite.getV1();
    }

    public void pickSprite(SpriteSet param0) {
        this.setSprite(param0.get(this.random));
    }

    public void setSpriteFromAge(SpriteSet param0) {
        this.setSprite(param0.get(this.age, this.lifetime));
    }
}
