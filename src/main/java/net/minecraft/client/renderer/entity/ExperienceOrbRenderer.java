package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(EXPERIENCE_ORB_LOCATION);

    public ExperienceOrbRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    protected int getBlockLightLevel(ExperienceOrb param0, BlockPos param1) {
        return Mth.clamp(super.getBlockLightLevel(param0, param1) + 7, 0, 15);
    }

    public void render(ExperienceOrb param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        int var0 = param0.getIcon();
        float var1 = (float)(var0 % 4 * 16 + 0) / 64.0F;
        float var2 = (float)(var0 % 4 * 16 + 16) / 64.0F;
        float var3 = (float)(var0 / 4 * 16 + 0) / 64.0F;
        float var4 = (float)(var0 / 4 * 16 + 16) / 64.0F;
        float var5 = 1.0F;
        float var6 = 0.5F;
        float var7 = 0.25F;
        float var8 = 255.0F;
        float var9 = ((float)param0.tickCount + param2) / 2.0F;
        int var10 = (int)((Mth.sin(var9 + 0.0F) + 1.0F) * 0.5F * 255.0F);
        int var11 = 255;
        int var12 = (int)((Mth.sin(var9 + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
        param3.translate(0.0, 0.1F, 0.0);
        param3.mulPose(this.entityRenderDispatcher.cameraOrientation());
        param3.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        float var13 = 0.3F;
        param3.scale(0.3F, 0.3F, 0.3F);
        VertexConsumer var14 = param4.getBuffer(RENDER_TYPE);
        PoseStack.Pose var15 = param3.last();
        Matrix4f var16 = var15.pose();
        Matrix3f var17 = var15.normal();
        vertex(var14, var16, var17, -0.5F, -0.25F, var10, 255, var12, var1, var4, param5);
        vertex(var14, var16, var17, 0.5F, -0.25F, var10, 255, var12, var2, var4, param5);
        vertex(var14, var16, var17, 0.5F, 0.75F, var10, 255, var12, var2, var3, param5);
        vertex(var14, var16, var17, -0.5F, 0.75F, var10, 255, var12, var1, var3, param5);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    private static void vertex(
        VertexConsumer param0,
        Matrix4f param1,
        Matrix3f param2,
        float param3,
        float param4,
        int param5,
        int param6,
        int param7,
        float param8,
        float param9,
        int param10
    ) {
        param0.vertex(param1, param3, param4, 0.0F)
            .color(param5, param6, param7, 128)
            .uv(param8, param9)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(param10)
            .normal(param2, 0.0F, 1.0F, 0.0F)
            .endVertex();
    }

    public ResourceLocation getTextureLocation(ExperienceOrb param0) {
        return EXPERIENCE_ORB_LOCATION;
    }
}
