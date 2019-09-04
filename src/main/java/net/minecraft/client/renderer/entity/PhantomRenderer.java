package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PhantomRenderer extends MobRenderer<Phantom, PhantomModel<Phantom>> {
    private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

    public PhantomRenderer(EntityRenderDispatcher param0) {
        super(param0, new PhantomModel<>(), 0.75F);
        this.addLayer(new PhantomEyesLayer<>(this));
    }

    protected ResourceLocation getTextureLocation(Phantom param0) {
        return PHANTOM_LOCATION;
    }

    protected void scale(Phantom param0, float param1) {
        int var0 = param0.getPhantomSize();
        float var1 = 1.0F + 0.15F * (float)var0;
        RenderSystem.scalef(var1, var1, var1);
        RenderSystem.translatef(0.0F, 1.3125F, 0.1875F);
    }

    protected void setupRotations(Phantom param0, float param1, float param2, float param3) {
        super.setupRotations(param0, param1, param2, param3);
        RenderSystem.rotatef(param0.xRot, 1.0F, 0.0F, 0.0F);
    }
}
