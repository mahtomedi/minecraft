package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.BatModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BatRenderer extends MobRenderer<Bat, BatModel> {
    private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

    public BatRenderer(EntityRenderDispatcher param0) {
        super(param0, new BatModel(), 0.25F);
    }

    protected ResourceLocation getTextureLocation(Bat param0) {
        return BAT_LOCATION;
    }

    protected void scale(Bat param0, float param1) {
        GlStateManager.scalef(0.35F, 0.35F, 0.35F);
    }

    protected void setupRotations(Bat param0, float param1, float param2, float param3) {
        if (param0.isResting()) {
            GlStateManager.translatef(0.0F, -0.1F, 0.0F);
        } else {
            GlStateManager.translatef(0.0F, Mth.cos(param1 * 0.3F) * 0.1F, 0.0F);
        }

        super.setupRotations(param0, param1, param2, param3);
    }
}
