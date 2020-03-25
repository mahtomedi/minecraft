package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.StriderModel;
import net.minecraft.client.renderer.entity.layers.SaddleLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Strider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StriderRenderer extends MobRenderer<Strider, StriderModel<Strider>> {
    private static final ResourceLocation STRIDER_LOCATION = new ResourceLocation("textures/entity/strider/strider.png");

    public StriderRenderer(EntityRenderDispatcher param0) {
        super(param0, new StriderModel<>(), 0.5F);
        this.addLayer(new SaddleLayer<>(this, new StriderModel<>(), new ResourceLocation("textures/entity/strider/strider_saddle.png")));
    }

    public ResourceLocation getTextureLocation(Strider param0) {
        return STRIDER_LOCATION;
    }

    protected void scale(Strider param0, PoseStack param1, float param2) {
        float var0 = 0.9375F;
        if (param0.isBaby()) {
            var0 *= 0.5F;
            this.shadowRadius = 0.25F;
        } else {
            this.shadowRadius = 0.5F;
        }

        param1.scale(var0, var0, var0);
    }

    protected boolean isShaking(Strider param0) {
        return param0.isSuffocating();
    }
}
