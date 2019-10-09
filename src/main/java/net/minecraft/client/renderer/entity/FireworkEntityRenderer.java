package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FireworkEntityRenderer extends EntityRenderer<FireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public FireworkEntityRenderer(EntityRenderDispatcher param0, ItemRenderer param1) {
        super(param0);
        this.itemRenderer = param1;
    }

    public void render(
        FireworkRocketEntity param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        param6.pushPose();
        param6.mulPose(Vector3f.YP.rotationDegrees(-this.entityRenderDispatcher.playerRotY));
        float var0 = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX;
        param6.mulPose(Vector3f.XP.rotationDegrees(var0));
        if (param0.isShotAtAngle()) {
            param6.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        } else {
            param6.mulPose(Vector3f.YP.rotationDegrees(180.0F));
        }

        this.itemRenderer
            .renderStatic(param0.getItem(), ItemTransforms.TransformType.GROUND, param0.getLightColor(), OverlayTexture.NO_OVERLAY, param6, param7);
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(FireworkRocketEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
