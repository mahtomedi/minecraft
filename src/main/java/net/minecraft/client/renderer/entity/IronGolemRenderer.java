package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class IronGolemRenderer extends MobRenderer<IronGolem, IronGolemModel<IronGolem>> {
    private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem.png");

    public IronGolemRenderer(EntityRenderDispatcher param0) {
        super(param0, new IronGolemModel<>(), 0.7F);
        this.addLayer(new IronGolemFlowerLayer(this));
    }

    protected ResourceLocation getTextureLocation(IronGolem param0) {
        return GOLEM_LOCATION;
    }

    protected void setupRotations(IronGolem param0, float param1, float param2, float param3) {
        super.setupRotations(param0, param1, param2, param3);
        if (!((double)param0.animationSpeed < 0.01)) {
            float var0 = 13.0F;
            float var1 = param0.animationPosition - param0.animationSpeed * (1.0F - param3) + 6.0F;
            float var2 = (Math.abs(var1 % 13.0F - 6.5F) - 3.25F) / 3.25F;
            GlStateManager.rotatef(6.5F * var2, 0.0F, 0.0F, 1.0F);
        }
    }
}
