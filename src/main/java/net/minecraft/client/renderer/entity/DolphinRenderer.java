package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.entity.layers.DolphinCarryingItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DolphinRenderer extends MobRenderer<Dolphin, DolphinModel<Dolphin>> {
    private static final ResourceLocation DOLPHIN_LOCATION = new ResourceLocation("textures/entity/dolphin.png");

    public DolphinRenderer(EntityRenderDispatcher param0) {
        super(param0, new DolphinModel<>(), 0.7F);
        this.addLayer(new DolphinCarryingItemLayer(this));
    }

    protected ResourceLocation getTextureLocation(Dolphin param0) {
        return DOLPHIN_LOCATION;
    }

    protected void scale(Dolphin param0, float param1) {
        float var0 = 1.0F;
        RenderSystem.scalef(1.0F, 1.0F, 1.0F);
    }

    protected void setupRotations(Dolphin param0, float param1, float param2, float param3) {
        super.setupRotations(param0, param1, param2, param3);
    }
}
