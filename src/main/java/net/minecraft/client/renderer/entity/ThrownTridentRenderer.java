package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.TridentModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ThrownTridentRenderer extends EntityRenderer<ThrownTrident> {
    public static final ResourceLocation TRIDENT_LOCATION = new ResourceLocation("textures/entity/trident.png");
    private final TridentModel model;

    public ThrownTridentRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.model = new TridentModel(param0.getLayer(ModelLayers.TRIDENT));
    }

    public void render(ThrownTrident param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(param2, param0.yRotO, param0.yRot) - 90.0F));
        param3.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(param2, param0.xRotO, param0.xRot) + 90.0F));
        VertexConsumer var0 = ItemRenderer.getFoilBufferDirect(param4, this.model.renderType(this.getTextureLocation(param0)), false, param0.isFoil());
        this.model.renderToBuffer(param3, var0, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(ThrownTrident param0) {
        return TRIDENT_LOCATION;
    }
}
