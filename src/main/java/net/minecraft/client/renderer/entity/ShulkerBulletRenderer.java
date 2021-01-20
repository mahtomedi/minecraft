package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBulletRenderer extends EntityRenderer<ShulkerBullet> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
    private static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE_LOCATION);
    private final ShulkerBulletModel<ShulkerBullet> model;

    public ShulkerBulletRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.model = new ShulkerBulletModel<>(param0.bakeLayer(ModelLayers.SHULKER_BULLET));
    }

    protected int getBlockLightLevel(ShulkerBullet param0, BlockPos param1) {
        return 15;
    }

    public void render(ShulkerBullet param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        float var0 = Mth.rotlerp(param0.yRotO, param0.yRot, param2);
        float var1 = Mth.lerp(param2, param0.xRotO, param0.xRot);
        float var2 = (float)param0.tickCount + param2;
        param3.translate(0.0, 0.15F, 0.0);
        param3.mulPose(Vector3f.YP.rotationDegrees(Mth.sin(var2 * 0.1F) * 180.0F));
        param3.mulPose(Vector3f.XP.rotationDegrees(Mth.cos(var2 * 0.1F) * 180.0F));
        param3.mulPose(Vector3f.ZP.rotationDegrees(Mth.sin(var2 * 0.15F) * 360.0F));
        param3.scale(-0.5F, -0.5F, 0.5F);
        this.model.setupAnim(param0, 0.0F, 0.0F, 0.0F, var0, var1);
        VertexConsumer var3 = param4.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(param3, var3, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        param3.scale(1.5F, 1.5F, 1.5F);
        VertexConsumer var4 = param4.getBuffer(RENDER_TYPE);
        this.model.renderToBuffer(param3, var4, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.15F);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(ShulkerBullet param0) {
        return TEXTURE_LOCATION;
    }
}
