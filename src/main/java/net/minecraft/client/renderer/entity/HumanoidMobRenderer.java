package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HumanoidMobRenderer<T extends Mob, M extends HumanoidModel<T>> extends MobRenderer<T, M> {
    private static final ResourceLocation DEFAULT_LOCATION = new ResourceLocation("textures/entity/steve.png");

    public HumanoidMobRenderer(EntityRenderDispatcher param0, M param1, float param2) {
        super(param0, param1, param2);
        this.addLayer(new CustomHeadLayer<>(this));
        this.addLayer(new ElytraLayer<>(this));
        this.addLayer(new ItemInHandLayer<>(this));
    }

    protected ResourceLocation getTextureLocation(T param0) {
        return DEFAULT_LOCATION;
    }
}
