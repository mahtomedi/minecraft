package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderEyesLayer<T extends Entity, M extends SpiderModel<T>> extends EyesLayer<T, M> {
    private static final ResourceLocation SPIDER_EYES_LOCATION = new ResourceLocation("textures/entity/spider_eyes.png");

    public SpiderEyesLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return SPIDER_EYES_LOCATION;
    }
}
