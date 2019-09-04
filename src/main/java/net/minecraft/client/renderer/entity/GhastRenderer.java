package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.GhastModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GhastRenderer extends MobRenderer<Ghast, GhastModel<Ghast>> {
    private static final ResourceLocation GHAST_LOCATION = new ResourceLocation("textures/entity/ghast/ghast.png");
    private static final ResourceLocation GHAST_SHOOTING_LOCATION = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

    public GhastRenderer(EntityRenderDispatcher param0) {
        super(param0, new GhastModel<>(), 1.5F);
    }

    protected ResourceLocation getTextureLocation(Ghast param0) {
        return param0.isCharging() ? GHAST_SHOOTING_LOCATION : GHAST_LOCATION;
    }

    protected void scale(Ghast param0, float param1) {
        float var0 = 1.0F;
        float var1 = 4.5F;
        float var2 = 4.5F;
        RenderSystem.scalef(4.5F, 4.5F, 4.5F);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
