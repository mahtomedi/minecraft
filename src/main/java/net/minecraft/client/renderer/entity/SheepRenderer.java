package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SheepModel;
import net.minecraft.client.renderer.entity.layers.SheepFurLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SheepRenderer extends MobRenderer<Sheep, SheepModel<Sheep>> {
    private static final ResourceLocation SHEEP_LOCATION = new ResourceLocation("textures/entity/sheep/sheep.png");

    public SheepRenderer(EntityRenderDispatcher param0) {
        super(param0, new SheepModel<>(), 0.7F);
        this.addLayer(new SheepFurLayer(this));
    }

    protected ResourceLocation getTextureLocation(Sheep param0) {
        return SHEEP_LOCATION;
    }
}
