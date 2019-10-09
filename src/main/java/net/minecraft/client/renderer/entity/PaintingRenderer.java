package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PaintingRenderer extends EntityRenderer<Painting> {
    public PaintingRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(Painting param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7) {
        param6.pushPose();
        param6.mulPose(Vector3f.YP.rotationDegrees(180.0F - param4));
        Motive var0 = param0.motive;
        float var1 = 0.0625F;
        param6.scale(0.0625F, 0.0625F, 0.0625F);
        VertexConsumer var2 = param7.getBuffer(RenderType.entitySolid(this.getTextureLocation(param0)));
        PaintingTextureManager var3 = Minecraft.getInstance().getPaintingTextures();
        this.renderPainting(param6.getPose(), var2, param0, var0.getWidth(), var0.getHeight(), var3.get(var0), var3.getBackSprite());
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(Painting param0) {
        return TextureAtlas.LOCATION_PAINTINGS;
    }

    private void renderPainting(
        Matrix4f param0, VertexConsumer param1, Painting param2, int param3, int param4, TextureAtlasSprite param5, TextureAtlasSprite param6
    ) {
        float var0 = (float)(-param3) / 2.0F;
        float var1 = (float)(-param4) / 2.0F;
        float var2 = 0.5F;
        float var3 = param6.getU0();
        float var4 = param6.getU1();
        float var5 = param6.getV0();
        float var6 = param6.getV1();
        float var7 = param6.getU0();
        float var8 = param6.getU1();
        float var9 = param6.getV0();
        float var10 = param6.getV(1.0);
        float var11 = param6.getU0();
        float var12 = param6.getU(1.0);
        float var13 = param6.getV0();
        float var14 = param6.getV1();
        int var15 = param3 / 16;
        int var16 = param4 / 16;
        double var17 = 16.0 / (double)var15;
        double var18 = 16.0 / (double)var16;

        for(int var19 = 0; var19 < var15; ++var19) {
            for(int var20 = 0; var20 < var16; ++var20) {
                float var21 = var0 + (float)((var19 + 1) * 16);
                float var22 = var0 + (float)(var19 * 16);
                float var23 = var1 + (float)((var20 + 1) * 16);
                float var24 = var1 + (float)(var20 * 16);
                int var25 = Mth.floor(param2.getX());
                int var26 = Mth.floor(param2.getY() + (double)((var23 + var24) / 2.0F / 16.0F));
                int var27 = Mth.floor(param2.getZ());
                Direction var28 = param2.getDirection();
                if (var28 == Direction.NORTH) {
                    var25 = Mth.floor(param2.getX() + (double)((var21 + var22) / 2.0F / 16.0F));
                }

                if (var28 == Direction.WEST) {
                    var27 = Mth.floor(param2.getZ() - (double)((var21 + var22) / 2.0F / 16.0F));
                }

                if (var28 == Direction.SOUTH) {
                    var25 = Mth.floor(param2.getX() - (double)((var21 + var22) / 2.0F / 16.0F));
                }

                if (var28 == Direction.EAST) {
                    var27 = Mth.floor(param2.getZ() + (double)((var21 + var22) / 2.0F / 16.0F));
                }

                int var29 = param2.level.getLightColor(new BlockPos(var25, var26, var27));
                float var30 = param5.getU(var17 * (double)(var15 - var19));
                float var31 = param5.getU(var17 * (double)(var15 - (var19 + 1)));
                float var32 = param5.getV(var18 * (double)(var16 - var20));
                float var33 = param5.getV(var18 * (double)(var16 - (var20 + 1)));
                this.vertex(param0, param1, var21, var24, var31, var32, -0.5F, 0, 0, -1, var29);
                this.vertex(param0, param1, var22, var24, var30, var32, -0.5F, 0, 0, -1, var29);
                this.vertex(param0, param1, var22, var23, var30, var33, -0.5F, 0, 0, -1, var29);
                this.vertex(param0, param1, var21, var23, var31, var33, -0.5F, 0, 0, -1, var29);
                this.vertex(param0, param1, var21, var23, var3, var5, 0.5F, 0, 0, 1, var29);
                this.vertex(param0, param1, var22, var23, var4, var5, 0.5F, 0, 0, 1, var29);
                this.vertex(param0, param1, var22, var24, var4, var6, 0.5F, 0, 0, 1, var29);
                this.vertex(param0, param1, var21, var24, var3, var6, 0.5F, 0, 0, 1, var29);
                this.vertex(param0, param1, var21, var23, var7, var9, -0.5F, 0, 1, 0, var29);
                this.vertex(param0, param1, var22, var23, var8, var9, -0.5F, 0, 1, 0, var29);
                this.vertex(param0, param1, var22, var23, var8, var10, 0.5F, 0, 1, 0, var29);
                this.vertex(param0, param1, var21, var23, var7, var10, 0.5F, 0, 1, 0, var29);
                this.vertex(param0, param1, var21, var24, var7, var9, 0.5F, 0, -1, 0, var29);
                this.vertex(param0, param1, var22, var24, var8, var9, 0.5F, 0, -1, 0, var29);
                this.vertex(param0, param1, var22, var24, var8, var10, -0.5F, 0, -1, 0, var29);
                this.vertex(param0, param1, var21, var24, var7, var10, -0.5F, 0, -1, 0, var29);
                this.vertex(param0, param1, var21, var23, var12, var13, 0.5F, -1, 0, 0, var29);
                this.vertex(param0, param1, var21, var24, var12, var14, 0.5F, -1, 0, 0, var29);
                this.vertex(param0, param1, var21, var24, var11, var14, -0.5F, -1, 0, 0, var29);
                this.vertex(param0, param1, var21, var23, var11, var13, -0.5F, -1, 0, 0, var29);
                this.vertex(param0, param1, var22, var23, var12, var13, -0.5F, 1, 0, 0, var29);
                this.vertex(param0, param1, var22, var24, var12, var14, -0.5F, 1, 0, 0, var29);
                this.vertex(param0, param1, var22, var24, var11, var14, 0.5F, 1, 0, 0, var29);
                this.vertex(param0, param1, var22, var23, var11, var13, 0.5F, 1, 0, 0, var29);
            }
        }

    }

    private void vertex(
        Matrix4f param0,
        VertexConsumer param1,
        float param2,
        float param3,
        float param4,
        float param5,
        float param6,
        int param7,
        int param8,
        int param9,
        int param10
    ) {
        param1.vertex(param0, param2, param3, param6)
            .color(255, 255, 255, 255)
            .uv(param4, param5)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param10)
            .normal((float)param7, (float)param8, (float)param9)
            .endVertex();
    }
}
