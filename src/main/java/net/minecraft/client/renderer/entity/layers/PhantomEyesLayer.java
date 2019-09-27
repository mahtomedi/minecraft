package net.minecraft.client.renderer.entity.layers;

import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PhantomEyesLayer<T extends Entity> extends EyesLayer<T, PhantomModel<T>> {
    private static final ResourceLocation PHANTOM_EYES_LOCATION = new ResourceLocation("textures/entity/phantom_eyes.png");

    public PhantomEyesLayer(RenderLayerParent<T, PhantomModel<T>> param0) {
        super(param0);
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return PHANTOM_EYES_LOCATION;
    }
}
