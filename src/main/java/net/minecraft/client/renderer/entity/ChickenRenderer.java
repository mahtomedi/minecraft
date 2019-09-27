package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenRenderer extends MobRenderer<Chicken, ChickenModel<Chicken>> {
    private static final ResourceLocation CHICKEN_LOCATION = new ResourceLocation("textures/entity/chicken.png");

    public ChickenRenderer(EntityRenderDispatcher param0) {
        super(param0, new ChickenModel<>(), 0.3F);
    }

    public ResourceLocation getTextureLocation(Chicken param0) {
        return CHICKEN_LOCATION;
    }

    protected float getBob(Chicken param0, float param1) {
        float var0 = Mth.lerp(param1, param0.oFlap, param0.flap);
        float var1 = Mth.lerp(param1, param0.oFlapSpeed, param0.flapSpeed);
        return (Mth.sin(var0) + 1.0F) * var1;
    }
}
