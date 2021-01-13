package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
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

    public GiantMobRenderer(EntityRenderDispatcher param0, float param1) {
        super(param0, new GiantZombieModel(), 0.5F * param1);
        this.scale = param1;
        this.addLayer(new ItemInHandLayer<>(this));
        this.addLayer(new HumanoidArmorLayer<>(this, new GiantZombieModel(0.5F, true), new GiantZombieModel(1.0F, true)));
    }

    protected void scale(Giant param0, PoseStack param1, float param2) {
        param1.scale(this.scale, this.scale, this.scale);
    }

    public ResourceLocation getTextureLocation(Giant param0) {
        return ZOMBIE_LOCATION;
    }
}
