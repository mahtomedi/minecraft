package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.SpiderEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Spider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderRenderer<T extends Spider> extends MobRenderer<T, SpiderModel<T>> {
    private static final ResourceLocation SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/spider.png");

    public SpiderRenderer(EntityRendererProvider.Context param0) {
        this(param0, ModelLayers.SPIDER);
    }

    public SpiderRenderer(EntityRendererProvider.Context param0, ModelLayerLocation param1) {
        super(param0, new SpiderModel<>(param0.bakeLayer(param1)), 0.8F);
        this.addLayer(new SpiderEyesLayer<>(this));
    }

    protected float getFlipDegrees(T param0) {
        return 180.0F;
    }

    public ResourceLocation getTextureLocation(T param0) {
        return SPIDER_LOCATION;
    }
}
