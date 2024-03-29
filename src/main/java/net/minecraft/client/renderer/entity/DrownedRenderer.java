package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.DrownedModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DrownedRenderer extends AbstractZombieRenderer<Drowned, DrownedModel<Drowned>> {
    private static final ResourceLocation DROWNED_LOCATION = new ResourceLocation("textures/entity/zombie/drowned.png");

    public DrownedRenderer(EntityRendererProvider.Context param0) {
        super(
            param0,
            new DrownedModel<>(param0.bakeLayer(ModelLayers.DROWNED)),
            new DrownedModel<>(param0.bakeLayer(ModelLayers.DROWNED_INNER_ARMOR)),
            new DrownedModel<>(param0.bakeLayer(ModelLayers.DROWNED_OUTER_ARMOR))
        );
        this.addLayer(new DrownedOuterLayer<>(this, param0.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie param0) {
        return DROWNED_LOCATION;
    }

    protected void setupRotations(Drowned param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        float var0 = param0.getSwimAmount(param4);
        if (var0 > 0.0F) {
            float var1 = -10.0F - param0.getXRot();
            float var2 = Mth.lerp(var0, 0.0F, var1);
            param1.rotateAround(Axis.XP.rotationDegrees(var2), 0.0F, param0.getBbHeight() / 2.0F, 0.0F);
        }

    }
}
