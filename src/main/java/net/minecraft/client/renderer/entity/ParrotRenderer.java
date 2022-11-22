package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ParrotRenderer extends MobRenderer<Parrot, ParrotModel> {
    private static final ResourceLocation RED_BLUE = new ResourceLocation("textures/entity/parrot/parrot_red_blue.png");
    private static final ResourceLocation BLUE = new ResourceLocation("textures/entity/parrot/parrot_blue.png");
    private static final ResourceLocation GREEN = new ResourceLocation("textures/entity/parrot/parrot_green.png");
    private static final ResourceLocation YELLOW_BLUE = new ResourceLocation("textures/entity/parrot/parrot_yellow_blue.png");
    private static final ResourceLocation GREY = new ResourceLocation("textures/entity/parrot/parrot_grey.png");

    public ParrotRenderer(EntityRendererProvider.Context param0) {
        super(param0, new ParrotModel(param0.bakeLayer(ModelLayers.PARROT)), 0.3F);
    }

    public ResourceLocation getTextureLocation(Parrot param0) {
        return getVariantTexture(param0.getVariant());
    }

    public static ResourceLocation getVariantTexture(Parrot.Variant param0) {
        return switch(param0) {
            case RED_BLUE -> RED_BLUE;
            case BLUE -> BLUE;
            case GREEN -> GREEN;
            case YELLOW_BLUE -> YELLOW_BLUE;
            case GRAY -> GREY;
        };
    }

    public float getBob(Parrot param0, float param1) {
        float var0 = Mth.lerp(param1, param0.oFlap, param0.flap);
        float var1 = Mth.lerp(param1, param0.oFlapSpeed, param0.flapSpeed);
        return (Mth.sin(var0) + 1.0F) * var1;
    }
}
