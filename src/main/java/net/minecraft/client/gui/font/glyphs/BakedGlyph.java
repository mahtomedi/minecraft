package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import javax.annotation.Nullable;
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
        boolean param0, float param1, float param2, Matrix4f param3, VertexConsumer param4, float param5, float param6, float param7, float param8, int param9
    ) {
        int var0 = 3;
        float var1 = param1 + this.left;
        float var2 = param1 + this.right;
        float var3 = this.up - 3.0F;
        float var4 = this.down - 3.0F;
        float var5 = param2 + var3;
        float var6 = param2 + var4;
        float var7 = param0 ? 1.0F - 0.25F * var3 : 0.0F;
        float var8 = param0 ? 1.0F - 0.25F * var4 : 0.0F;
        param4.vertex(param3, var1 + var7, var5, 0.0F).uv(this.u0, this.v0).uv2(param9).color(param5, param6, param7, param8).endVertex();
        param4.vertex(param3, var1 + var8, var6, 0.0F).uv(this.u0, this.v1).uv2(param9).color(param5, param6, param7, param8).endVertex();
        param4.vertex(param3, var2 + var8, var6, 0.0F).uv(this.u1, this.v1).uv2(param9).color(param5, param6, param7, param8).endVertex();
        param4.vertex(param3, var2 + var7, var5, 0.0F).uv(this.u1, this.v0).uv2(param9).color(param5, param6, param7, param8).endVertex();
    }

    public void renderEffect(BakedGlyph.Effect param0, Matrix4f param1, VertexConsumer param2, int param3) {
        param2.vertex(param1, param0.x0, param0.y0, param0.depth).uv(this.u0, this.v0).uv2(param3).color(param0.r, param0.g, param0.b, param0.a).endVertex();
        param2.vertex(param1, param0.x1, param0.y0, param0.depth).uv(this.u0, this.v1).uv2(param3).color(param0.r, param0.g, param0.b, param0.a).endVertex();
        param2.vertex(param1, param0.x1, param0.y1, param0.depth).uv(this.u1, this.v1).uv2(param3).color(param0.r, param0.g, param0.b, param0.a).endVertex();
        param2.vertex(param1, param0.x0, param0.y1, param0.depth).uv(this.u1, this.v0).uv2(param3).color(param0.r, param0.g, param0.b, param0.a).endVertex();
    }

    @Nullable
    public ResourceLocation getTexture() {
        return this.texture;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Effect {
        protected final float x0;
        protected final float y0;
        protected final float x1;
        protected final float y1;
        protected final float depth;
        protected final float r;
        protected final float g;
        protected final float b;
        protected final float a;

        public Effect(float param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7, float param8) {
            this.x0 = param0;
            this.y0 = param1;
            this.x1 = param2;
            this.y1 = param3;
            this.depth = param4;
            this.r = param5;
            this.g = param6;
            this.b = param7;
            this.a = param8;
        }
    }
}
