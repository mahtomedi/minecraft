package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.VexModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Vex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class VexRenderer extends HumanoidMobRenderer<Vex, VexModel> {
    private static final ResourceLocation VEX_LOCATION = new ResourceLocation("textures/entity/illager/vex.png");
    private static final ResourceLocation VEX_CHARGING_LOCATION = new ResourceLocation("textures/entity/illager/vex_charging.png");

    public VexRenderer(EntityRenderDispatcher param0) {
        super(param0, new VexModel(), 0.3F);
    }

    public ResourceLocation getTextureLocation(Vex param0) {
        return param0.isCharging() ? VEX_CHARGING_LOCATION : VEX_LOCATION;
    }

    protected void scale(Vex param0, PoseStack param1, float param2) {
        param1.scale(0.4F, 0.4F, 0.4F);
    }
}
