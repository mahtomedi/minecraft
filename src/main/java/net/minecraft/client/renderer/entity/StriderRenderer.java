package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Strider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StriderRenderer extends MobRenderer<Strider, StriderModel<Strider>> {
    private static final ResourceLocation STRIDER_LOCATION = new ResourceLocation("textures/entity/strider/strider.png");
    private static final ResourceLocation COLD_LOCATION = new ResourceLocation("textures/entity/strider/strider_cold.png");

    public StriderRenderer(EntityRendererProvider.Context param0) {
        super(param0, new StriderModel<>(param0.getLayer(ModelLayers.STRIDER)), 0.5F);
        this.addLayer(
            new SaddleLayer<>(
                this, new StriderModel<>(param0.getLayer(ModelLayers.STRIDER_SADDLE)), new ResourceLocation("textures/entity/strider/strider_saddle.png")
            )
        );
    }

    public ResourceLocation getTextureLocation(Strider param0) {
        return param0.isSuffocating() ? COLD_LOCATION : STRIDER_LOCATION;
    }

    protected void scale(Strider param0, PoseStack param1, float param2) {
        if (param0.isBaby()) {
            param1.scale(0.5F, 0.5F, 0.5F);
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

    }

    protected boolean isShaking(Strider param0) {
        return param0.isSuffocating();
    }
}
