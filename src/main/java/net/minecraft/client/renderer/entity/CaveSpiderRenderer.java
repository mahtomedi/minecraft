package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveSpiderRenderer extends SpiderRenderer<CaveSpider> {
    private static final ResourceLocation CAVE_SPIDER_LOCATION = new ResourceLocation("textures/entity/spider/cave_spider.png");

    public CaveSpiderRenderer(EntityRendererProvider.Context param0) {
        super(param0, ModelLayers.CAVE_SPIDER);
        this.shadowRadius *= 0.7F;
    }

    protected void scale(CaveSpider param0, PoseStack param1, float param2) {
        param1.scale(0.7F, 0.7F, 0.7F);
    }

    public ResourceLocation getTextureLocation(CaveSpider param0) {
        return CAVE_SPIDER_LOCATION;
    }
}
