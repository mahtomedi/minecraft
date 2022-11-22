package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FoxRenderer extends MobRenderer<Fox, FoxModel<Fox>> {
    private static final ResourceLocation RED_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/fox.png");
    private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/fox_sleep.png");
    private static final ResourceLocation SNOW_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox.png");
    private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox_sleep.png");

    public FoxRenderer(EntityRendererProvider.Context param0) {
        super(param0, new FoxModel<>(param0.bakeLayer(ModelLayers.FOX)), 0.4F);
        this.addLayer(new FoxHeldItemLayer(this, param0.getItemInHandRenderer()));
    }

    protected void setupRotations(Fox param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        if (param0.isPouncing() || param0.isFaceplanted()) {
            float var0 = -Mth.lerp(param4, param0.xRotO, param0.getXRot());
            param1.mulPose(Axis.XP.rotationDegrees(var0));
        }

    }

    public ResourceLocation getTextureLocation(Fox param0) {
        if (param0.getVariant() == Fox.Type.RED) {
            return param0.isSleeping() ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
        } else {
            return param0.isSleeping() ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
        }
    }
}
