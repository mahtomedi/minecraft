package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ExperienceOrbRenderer extends EntityRenderer<ExperienceOrb> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");

    public ExperienceOrbRenderer(EntityRenderDispatcher param0) {
        super(param0);
        this.shadowRadius = 0.15F;
        this.shadowStrength = 0.75F;
    }

    public void render(
        ExperienceOrb param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        param6.pushPose();
        int var0 = param0.getIcon();
        float var1 = (float)(var0 % 4 * 16 + 0) / 64.0F;
        float var2 = (float)(var0 % 4 * 16 + 16) / 64.0F;
        float var3 = (float)(var0 / 4 * 16 + 0) / 64.0F;
        float var4 = (float)(var0 / 4 * 16 + 16) / 64.0F;
        float var5 = 1.0F;
        float var6 = 0.5F;
        float var7 = 0.25F;
        float var8 = 255.0F;
        float var9 = ((float)param0.tickCount + param5) / 2.0F;
        int var10 = (int)((Mth.sin(var9 + 0.0F) + 1.0F) * 0.5F * 255.0F);
        int var11 = 255;
        int var12 = (int)((Mth.sin(var9 + (float) (Math.PI * 4.0 / 3.0)) + 1.0F) * 0.1F * 255.0F);
        param6.translate(0.0, 0.1F, 0.0);
        param6.mulPose(Vector3f.YP.rotation(180.0F - this.entityRenderDispatcher.playerRotY, true));
        param6.mulPose(
            Vector3f.XP.rotation((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, true)
        );
        float var13 = 0.3F;
        param6.scale(0.3F, 0.3F, 0.3F);
        int var14 = param0.getLightColor();
        VertexConsumer var15 = param7.getBuffer(RenderType.NEW_ENTITY(EXPERIENCE_ORB_LOCATION));
        OverlayTexture.setDefault(var15);
        Matrix4f var16 = param6.getPose();
        vertex(var15, var16, -0.5F, -0.25F, var10, 255, var12, var1, var4, var14);
        vertex(var15, var16, 0.5F, -0.25F, var10, 255, var12, var2, var4, var14);
        vertex(var15, var16, 0.5F, 0.75F, var10, 255, var12, var2, var3, var14);
        vertex(var15, var16, -0.5F, 0.75F, var10, 255, var12, var1, var3, var14);
        var15.unsetDefaultOverlayCoords();
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    private static void vertex(
        VertexConsumer param0, Matrix4f param1, float param2, float param3, int param4, int param5, int param6, float param7, float param8, int param9
    ) {
        param0.vertex(param1, param2, param3, 0.0F).color(param4, param5, param6, 128).uv(param7, param8).uv2(param9).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    public ResourceLocation getTextureLocation(ExperienceOrb param0) {
        return EXPERIENCE_ORB_LOCATION;
    }
}
