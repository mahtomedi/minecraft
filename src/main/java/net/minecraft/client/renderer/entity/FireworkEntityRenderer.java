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

    public void render(FireworkRocketEntity param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.mulPose(this.entityRenderDispatcher.cameraOrientation());
        if (param0.isShotAtAngle()) {
            param3.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            param3.mulPose(Vector3f.XP.rotationDegrees(90.0F));
        }

        this.itemRenderer.renderStatic(param0.getItem(), ItemTransforms.TransformType.GROUND, param5, OverlayTexture.NO_OVERLAY, param3, param4);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(FireworkRocketEntity param0) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
