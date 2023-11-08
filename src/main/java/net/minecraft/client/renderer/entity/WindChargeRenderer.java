package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.WindChargeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.WindCharge;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindChargeRenderer extends EntityRenderer<WindCharge> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/projectiles/wind_charge.png");
    private final WindChargeModel model;

    public WindChargeRenderer(EntityRendererProvider.Context param0) {
        super(param0);
        this.model = new WindChargeModel(param0.bakeLayer(ModelLayers.WIND_CHARGE));
    }

    public void render(WindCharge param0, float param1, float param2, PoseStack param3, MultiBufferSource param4, int param5) {
        float var0 = (float)param0.tickCount + param2;
        VertexConsumer var1 = param4.getBuffer(RenderType.breezeWind(TEXTURE_LOCATION, this.xOffset(var0) % 1.0F, 0.0F));
        this.model.setupAnim(param0, 0.0F, 0.0F, var0, 0.0F, 0.0F);
        this.model.renderToBuffer(param3, var1, param5, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.5F);
        super.render(param0, param1, param2, param3, param4, param5);
    }

    protected float xOffset(float param0) {
        return param0 * 0.03F;
    }

    public ResourceLocation getTextureLocation(WindCharge param0) {
        return TEXTURE_LOCATION;
    }
}
