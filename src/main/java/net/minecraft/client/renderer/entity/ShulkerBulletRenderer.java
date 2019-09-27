package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ShulkerBulletModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ShulkerBulletRenderer extends EntityRenderer<ShulkerBullet> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/shulker/spark.png");
    private final ShulkerBulletModel<ShulkerBullet> model = new ShulkerBulletModel<>();

    public ShulkerBulletRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(
        ShulkerBullet param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        param6.pushPose();
        float var0 = Mth.rotlerp(param0.yRotO, param0.yRot, param5);
        float var1 = Mth.lerp(param5, param0.xRotO, param0.xRot);
        float var2 = (float)param0.tickCount + param5;
        param6.translate(0.0, 0.15F, 0.0);
        param6.mulPose(Vector3f.YP.rotation(Mth.sin(var2 * 0.1F) * 180.0F, true));
        param6.mulPose(Vector3f.XP.rotation(Mth.cos(var2 * 0.1F) * 180.0F, true));
        param6.mulPose(Vector3f.ZP.rotation(Mth.sin(var2 * 0.15F) * 360.0F, true));
        float var3 = 0.03125F;
        param6.scale(-1.0F, -1.0F, 1.0F);
        int var4 = param0.getLightColor();
        VertexConsumer var5 = param7.getBuffer(RenderType.NEW_ENTITY(TEXTURE_LOCATION));
        OverlayTexture.setDefault(var5);
        this.model.setupAnim(param0, 0.0F, 0.0F, 0.0F, var0, var1, 0.03125F);
        this.model.renderToBuffer(param6, var5, var4);
        var5.unsetDefaultOverlayCoords();
        param6.scale(1.5F, 1.5F, 1.5F);
        VertexConsumer var6 = param7.getBuffer(RenderType.NEW_ENTITY(TEXTURE_LOCATION, true, true, false));
        OverlayTexture.setDefault(var5);
        this.model.renderToBuffer(param6, var6, var4);
        var5.unsetDefaultOverlayCoords();
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(ShulkerBullet param0) {
        return TEXTURE_LOCATION;
    }
}
