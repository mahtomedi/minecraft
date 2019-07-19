package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.BufferBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BakedGlyph {
    private final ResourceLocation texture;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;

    public BakedGlyph(ResourceLocation param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8) {
        this.texture = param0;
        this.u0 = param1;
        this.u1 = param2;
        this.v0 = param3;
        this.v1 = param4;
        this.left = param5;
        this.right = param6;
        this.up = param7;
        this.down = param8;
    }

    public void render(
        TextureManager param0, boolean param1, float param2, float param3, BufferBuilder param4, float param5, float param6, float param7, float param8
    ) {
        int var0 = 3;
        float var1 = param2 + this.left;
        float var2 = param2 + this.right;
        float var3 = this.up - 3.0F;
        float var4 = this.down - 3.0F;
        float var5 = param3 + var3;
        float var6 = param3 + var4;
        float var7 = param1 ? 1.0F - 0.25F * var3 : 0.0F;
        float var8 = param1 ? 1.0F - 0.25F * var4 : 0.0F;
        param4.vertex((double)(var1 + var7), (double)var5, 0.0).uv((double)this.u0, (double)this.v0).color(param5, param6, param7, param8).endVertex();
        param4.vertex((double)(var1 + var8), (double)var6, 0.0).uv((double)this.u0, (double)this.v1).color(param5, param6, param7, param8).endVertex();
        param4.vertex((double)(var2 + var8), (double)var6, 0.0).uv((double)this.u1, (double)this.v1).color(param5, param6, param7, param8).endVertex();
        param4.vertex((double)(var2 + var7), (double)var5, 0.0).uv((double)this.u1, (double)this.v0).color(param5, param6, param7, param8).endVertex();
    }

    @Nullable
    public ResourceLocation getTexture() {
        return this.texture;
    }
}
