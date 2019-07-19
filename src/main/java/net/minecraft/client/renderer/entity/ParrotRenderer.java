package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ParrotModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotRenderer extends MobRenderer<Parrot, ParrotModel> {
    public static final ResourceLocation[] PARROT_LOCATIONS = new ResourceLocation[]{
        new ResourceLocation("textures/entity/parrot/parrot_red_blue.png"),
        new ResourceLocation("textures/entity/parrot/parrot_blue.png"),
        new ResourceLocation("textures/entity/parrot/parrot_green.png"),
        new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png"),
        new ResourceLocation("textures/entity/parrot/parrot_grey.png")
    };

    public ParrotRenderer(EntityRenderDispatcher param0) {
        super(param0, new ParrotModel(), 0.3F);
    }

    protected ResourceLocation getTextureLocation(Parrot param0) {
        return PARROT_LOCATIONS[param0.getVariant()];
    }

    public float getBob(Parrot param0, float param1) {
        float var0 = Mth.lerp(param1, param0.oFlap, param0.flap);
        float var1 = Mth.lerp(param1, param0.oFlapSpeed, param0.flapSpeed);
        return (Mth.sin(var0) + 1.0F) * var1;
    }
}
