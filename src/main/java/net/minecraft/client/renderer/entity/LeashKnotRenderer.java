package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model;

    public LeashKnotRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.model = new LeashKnotModel<>(param0.bakeLayer(ModelLayers.LEASH_KNOT));
    }

    public void render(LeashFenceKnotEntity param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        param3.pushPose();
        param3.scale(-1.0F, -1.0F, 1.0F);
        this.model.setupAnim(param0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer var0 = param4.getBuffer(this.model.renderType(KNOT_LOCATION));
        this.model.renderToBuffer(param3, var0, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        param3.popPose();
        super.render(param0, param1, param2, param3, param4, param5);
    }

    public ResourceLocation getTextureLocation(LeashFenceKnotEntity param0) {
        return KNOT_LOCATION;
    }
}
