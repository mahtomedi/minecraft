package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CodRenderer extends MobRenderer<Cod, CodModel<Cod>> {
    private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

    public CodRenderer(EntityRendererProvider.Context param0) {
        super(param0, new CodModel<>(param0.bakeLayer(ModelLayers.COD)), 0.3F);
    }

    public ResourceLocation getTextureLocation(Cod param0) {
        return COD_LOCATION;
    }

    protected void setupRotations(Cod param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        float var0 = 4.3F * Mth.sin(0.6F * param2);
        param1.mulPose(Axis.YP.rotationDegrees(var0));
        if (!param0.isInWater()) {
            param1.translate(0.1F, 0.1F, -0.1F);
            param1.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }

    }
}
