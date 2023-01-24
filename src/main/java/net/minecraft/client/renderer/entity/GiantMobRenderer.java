package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GiantMobRenderer extends MobRenderer<Giant, HumanoidModel<Giant>> {
    private static final ResourceLocation ZOMBIE_LOCATION = new ResourceLocation("textures/entity/zombie/zombie.png");
    private final float scale;

    public GiantMobRenderer(EntityRendererProvider.Context param0, float param1) {
        super(param0, new GiantZombieModel(param0.bakeLayer(ModelLayers.GIANT)), 0.5F * param1);
        this.scale = param1;
        this.addLayer(new ItemInHandLayer<>(this, param0.getItemInHandRenderer()));
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new GiantZombieModel(param0.bakeLayer(ModelLayers.GIANT_INNER_ARMOR)),
                new GiantZombieModel(param0.bakeLayer(ModelLayers.GIANT_OUTER_ARMOR)),
                param0.getModelManager()
            )
        );
    }

    protected void scale(Giant param0, PoseStack param1, float param2) {
        param1.scale(this.scale, this.scale, this.scale);
    }

    public ResourceLocation getTextureLocation(Giant param0) {
        return ZOMBIE_LOCATION;
    }
}
