package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.LeashKnotModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LeashKnotRenderer extends EntityRenderer<LeashFenceKnotEntity> {
    private static final ResourceLocation KNOT_LOCATION = new ResourceLocation("textures/entity/lead_knot.png");
    private final LeashKnotModel<LeashFenceKnotEntity> model = new LeashKnotModel<>();

    public LeashKnotRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(
        LeashFenceKnotEntity param0, double param1, double param2, double param3, float param4, float param5, PoseStack param6, MultiBufferSource param7
    ) {
        param6.pushPose();
        float var0 = 0.0625F;
        param6.scale(-1.0F, -1.0F, 1.0F);
        int var1 = param0.getLightColor();
        this.model.setupAnim(param0, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
        VertexConsumer var2 = param7.getBuffer(this.model.renderType(KNOT_LOCATION));
        this.model.renderToBuffer(param6, var2, var1, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F);
        param6.popPose();
        super.render(param0, param1, param2, param3, param4, param5, param6, param7);
    }

    public ResourceLocation getTextureLocation(LeashFenceKnotEntity param0) {
        return KNOT_LOCATION;
    }
}
