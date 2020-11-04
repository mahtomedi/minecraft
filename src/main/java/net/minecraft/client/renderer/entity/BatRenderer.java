package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BatRenderer extends MobRenderer<Bat, BatModel> {
    private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

    public BatRenderer(EntityRendererProvider.Context param0) {
        super(param0, new BatModel(param0.getLayer(ModelLayers.BAT)), 0.25F);
    }

    public ResourceLocation getTextureLocation(Bat param0) {
        return BAT_LOCATION;
    }

    protected void scale(Bat param0, PoseStack param1, float param2) {
        param1.scale(0.35F, 0.35F, 0.35F);
    }

    protected void setupRotations(Bat param0, PoseStack param1, float param2, float param3, float param4) {
        if (param0.isResting()) {
            param1.translate(0.0, -0.1F, 0.0);
        } else {
            param1.translate(0.0, (double)(Mth.cos(param2 * 0.3F) * 0.1F), 0.0);
        }

        super.setupRotations(param0, param1, param2, param3, param4);
    }
}
