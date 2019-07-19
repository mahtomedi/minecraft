package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BakedQuad {
    protected final int[] vertices;
    protected final int tintIndex;
    protected final Direction direction;
    protected final TextureAtlasSprite sprite;

    public BakedQuad(int[] param0, int param1, Direction param2, TextureAtlasSprite param3) {
        this.vertices = param0;
        this.tintIndex = param1;
        this.direction = param2;
        this.sprite = param3;
    }

    public TextureAtlasSprite getSprite() {
        return this.sprite;
    }

    public int[] getVertices() {
        return this.vertices;
    }

    public boolean isTinted() {
        return this.tintIndex != -1;
    }

    public int getTintIndex() {
        return this.tintIndex;
    }

    public Direction getDirection() {
        return this.direction;
    }
}
