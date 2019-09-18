package net.minecraft.client.renderer.block.model;

import java.util.Arrays;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BreakingQuad extends BakedQuad {
    private final TextureAtlasSprite breakingIcon;

    public BreakingQuad(BakedQuad param0, TextureAtlasSprite param1) {
        super(
            Arrays.copyOf(param0.getVertices(), param0.getVertices().length),
            param0.tintIndex,
            FaceBakery.calculateFacing(param0.getVertices()),
            param0.getSprite()
        );
        this.breakingIcon = param1;
        this.calculateBreakingUVs();
    }

    private void calculateBreakingUVs() {
        for(int var0 = 0; var0 < 4; ++var0) {
            int var1 = 8 * var0;
            this.vertices[var1 + 4] = Float.floatToRawIntBits(
                this.breakingIcon.getU((double)this.sprite.getUOffset(Float.intBitsToFloat(this.vertices[var1 + 4])))
            );
            this.vertices[var1 + 4 + 1] = Float.floatToRawIntBits(
                this.breakingIcon.getV((double)this.sprite.getVOffset(Float.intBitsToFloat(this.vertices[var1 + 4 + 1])))
            );
        }

    }
}
