package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PolarBearRenderer extends MobRenderer<PolarBear, PolarBearModel<PolarBear>> {
    private static final ResourceLocation BEAR_LOCATION = new ResourceLocation("textures/entity/bear/polarbear.png");

    public PolarBearRenderer(EntityRendererProvider.Context param0) {
        super(param0, new PolarBearModel<>(param0.getLayer(ModelLayers.POLAR_BEAR)), 0.9F);
    }

    public ResourceLocation getTextureLocation(PolarBear param0) {
        return BEAR_LOCATION;
    }

    protected void scale(PolarBear param0, PoseStack param1, float param2) {
        param1.scale(1.2F, 1.2F, 1.2F);
        super.scale(param0, param1, param2);
    }
}
