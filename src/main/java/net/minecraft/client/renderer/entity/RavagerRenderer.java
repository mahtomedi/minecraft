package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.RavagerModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RavagerRenderer extends MobRenderer<Ravager, RavagerModel> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/ravager.png");

    public RavagerRenderer(EntityRenderDispatcher param0) {
        super(param0, new RavagerModel(), 1.1F);
    }

    protected ResourceLocation getTextureLocation(Ravager param0) {
        return TEXTURE_LOCATION;
    }
}
