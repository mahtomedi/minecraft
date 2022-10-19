package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PaintingRenderer extends EntityRenderer<Painting> {
    public PaintingRenderer(EntityRendererProvider.Context param0) {
        super(param0);
    }

    public void render(Painting param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.mulPose(Vector3f.YP.rotationDegrees(180.0F - param1));
        PaintingVariant var0 = param0.getVariant().value();
        float var1 = 0.0625F;
        param3.scale(0.0625F, 0.0625F, 0.0625F);
        VertexConsumer var2 = param4.getBuffer(RenderType.entitySolid(this.getTextureLocation(param0)));
        PaintingTextureManager var3 = Minecraft.getInstance().getPaintingTextures();
        this.renderPainting(param3, var2, param0, var0.getWidth(), var0.getHeight(), var3.get(var0), var3.getBackSprite());
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(Painting param0) {
        return Minecraft.getInstance().getPaintingTextures().getBackSprite().atlasLocation();
    }

    private void renderPainting(
        PoseStack param0, VertexConsumer param1, Painting param2, int param3, int param4, TextureAtlasSprite param5, TextureAtlasSprite param6
    ) {
        PoseStack.Pose var0 = param0.last();
        Matrix4f var1 = var0.pose();
        Matrix3f var2 = var0.normal();
        float var3 = (float)(-param3) / 2.0F;
        float var4 = (float)(-param4) / 2.0F;
        float var5 = 0.5F;
        float var6 = param6.getU0();
        float var7 = param6.getU1();
        float var8 = param6.getV0();
        float var9 = param6.getV1();
        float var10 = param6.getU0();
        float var11 = param6.getU1();
        float var12 = param6.getV0();
        float var13 = param6.getV(1.0);
        float var14 = param6.getU0();
        float var15 = param6.getU(1.0);
        float var16 = param6.getV0();
        float var17 = param6.getV1();
        int var18 = param3 / 16;
        int var19 = param4 / 16;
        double var20 = 16.0 / (double)var18;
        double var21 = 16.0 / (double)var19;

        for(int var22 = 0; var22 < var18; ++var22) {
            for(int var23 = 0; var23 < var19; ++var23) {
                float var24 = var3 + (float)((var22 + 1) * 16);
                float var25 = var3 + (float)(var22 * 16);
                float var26 = var4 + (float)((var23 + 1) * 16);
                float var27 = var4 + (float)(var23 * 16);
                int var28 = param2.getBlockX();
                int var29 = Mth.floor(param2.getY() + (double)((var26 + var27) / 2.0F / 16.0F));
                int var30 = param2.getBlockZ();
                Direction var31 = param2.getDirection();
                if (var31 == Direction.NORTH) {
                    var28 = Mth.floor(param2.getX() + (double)((var24 + var25) / 2.0F / 16.0F));
                }

                if (var31 == Direction.WEST) {
                    var30 = Mth.floor(param2.getZ() - (double)((var24 + var25) / 2.0F / 16.0F));
                }

                if (var31 == Direction.SOUTH) {
                    var28 = Mth.floor(param2.getX() - (double)((var24 + var25) / 2.0F / 16.0F));
                }

                if (var31 == Direction.EAST) {
                    var30 = Mth.floor(param2.getZ() + (double)((var24 + var25) / 2.0F / 16.0F));
                }

                int var32 = LevelRenderer.getLightColor(param2.level, new BlockPos(var28, var29, var30));
                float var33 = param5.getU(var20 * (double)(var18 - var22));
                float var34 = param5.getU(var20 * (double)(var18 - (var22 + 1)));
                float var35 = param5.getV(var21 * (double)(var19 - var23));
                float var36 = param5.getV(var21 * (double)(var19 - (var23 + 1)));
                this.vertex(var1, var2, param1, var24, var27, var34, var35, -0.5F, 0, 0, -1, var32);
                this.vertex(var1, var2, param1, var25, var27, var33, var35, -0.5F, 0, 0, -1, var32);
                this.vertex(var1, var2, param1, var25, var26, var33, var36, -0.5F, 0, 0, -1, var32);
                this.vertex(var1, var2, param1, var24, var26, var34, var36, -0.5F, 0, 0, -1, var32);
                this.vertex(var1, var2, param1, var24, var26, var7, var8, 0.5F, 0, 0, 1, var32);
                this.vertex(var1, var2, param1, var25, var26, var6, var8, 0.5F, 0, 0, 1, var32);
                this.vertex(var1, var2, param1, var25, var27, var6, var9, 0.5F, 0, 0, 1, var32);
                this.vertex(var1, var2, param1, var24, var27, var7, var9, 0.5F, 0, 0, 1, var32);
                this.vertex(var1, var2, param1, var24, var26, var10, var12, -0.5F, 0, 1, 0, var32);
                this.vertex(var1, var2, param1, var25, var26, var11, var12, -0.5F, 0, 1, 0, var32);
                this.vertex(var1, var2, param1, var25, var26, var11, var13, 0.5F, 0, 1, 0, var32);
                this.vertex(var1, var2, param1, var24, var26, var10, var13, 0.5F, 0, 1, 0, var32);
                this.vertex(var1, var2, param1, var24, var27, var10, var12, 0.5F, 0, -1, 0, var32);
                this.vertex(var1, var2, param1, var25, var27, var11, var12, 0.5F, 0, -1, 0, var32);
                this.vertex(var1, var2, param1, var25, var27, var11, var13, -0.5F, 0, -1, 0, var32);
                this.vertex(var1, var2, param1, var24, var27, var10, var13, -0.5F, 0, -1, 0, var32);
                this.vertex(var1, var2, param1, var24, var26, var15, var16, 0.5F, -1, 0, 0, var32);
                this.vertex(var1, var2, param1, var24, var27, var15, var17, 0.5F, -1, 0, 0, var32);
                this.vertex(var1, var2, param1, var24, var27, var14, var17, -0.5F, -1, 0, 0, var32);
                this.vertex(var1, var2, param1, var24, var26, var14, var16, -0.5F, -1, 0, 0, var32);
                this.vertex(var1, var2, param1, var25, var26, var15, var16, -0.5F, 1, 0, 0, var32);
                this.vertex(var1, var2, param1, var25, var27, var15, var17, -0.5F, 1, 0, 0, var32);
                this.vertex(var1, var2, param1, var25, var27, var14, var17, 0.5F, 1, 0, 0, var32);
                this.vertex(var1, var2, param1, var25, var26, var14, var16, 0.5F, 1, 0, 0, var32);
            }
        }

    }

    private void vertex(
        Matrix4f param0,
        Matrix3f param1,
        VertexConsumer param2,
        float param3,
        float param4,
        float param5,
        float param6,
        float param7,
        int param8,
        int param9,
        int param10,
        int param11
    ) {
        param2.vertex(param0, param3, param4, param7)
            .color(255, 255, 255, 255)
            .uv(param5, param6)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param11)
            .normal(param1, (float)param8, (float)param9, (float)param10)
            .endVertex();
    }
}
