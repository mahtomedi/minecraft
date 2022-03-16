package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.TadpoleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TadpoleRenderer extends MobRenderer<Tadpole, TadpoleModel<Tadpole>> {
    private static final ResourceLocation TADPOLE_TEXTURE = new ResourceLocation("textures/entity/tadpole/tadpole.png");

    public TadpoleRenderer(EntityRendererProvider.Context param0) {
        super(param0, new TadpoleModel<>(param0.bakeLayer(ModelLayers.TADPOLE)), 0.14F);
    }

    public ResourceLocation getTextureLocation(Tadpole param0) {
        return TADPOLE_TEXTURE;
    }
}
