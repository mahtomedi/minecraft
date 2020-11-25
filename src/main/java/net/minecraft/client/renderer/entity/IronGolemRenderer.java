package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemRenderer extends MobRenderer<IronGolem, IronGolemModel<IronGolem>> {
    private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem/iron_golem.png");

    public IronGolemRenderer(EntityRendererProvider.Context param0) {
        super(param0, new IronGolemModel<>(param0.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7F);
        this.addLayer(new IronGolemCrackinessLayer(this));
        this.addLayer(new IronGolemFlowerLayer(this));
    }

    public ResourceLocation getTextureLocation(IronGolem param0) {
        return GOLEM_LOCATION;
    }

    protected void setupRotations(IronGolem param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        if (!((double)param0.animationSpeed < 0.01)) {
            float var0 = 13.0F;
            float var1 = param0.animationPosition - param0.animationSpeed * (1.0F - param4) + 6.0F;
            float var2 = (Math.abs(var1 % 13.0F - 6.5F) - 3.25F) / 3.25F;
            param1.mulPose(Vector3f.ZP.rotationDegrees(6.5F * var2));
        }
    }
}
