package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SalmonRenderer extends MobRenderer<Salmon, SalmonModel<Salmon>> {
    private static final ResourceLocation SALMON_LOCATION = new ResourceLocation("textures/entity/fish/salmon.png");

    public SalmonRenderer(EntityRenderDispatcher param0) {
        super(param0, new SalmonModel<>(), 0.4F);
    }

    @Nullable
    protected ResourceLocation getTextureLocation(Salmon param0) {
        return SALMON_LOCATION;
    }

    protected void setupRotations(Salmon param0, float param1, float param2, float param3) {
        super.setupRotations(param0, param1, param2, param3);
        float var0 = 1.0F;
        float var1 = 1.0F;
        if (!param0.isInWater()) {
            var0 = 1.3F;
            var1 = 1.7F;
        }

        float var2 = var0 * 4.3F * Mth.sin(var1 * 0.6F * param1);
        RenderSystem.rotatef(var2, 0.0F, 1.0F, 0.0F);
        RenderSystem.translatef(0.0F, 0.0F, -0.4F);
        if (!param0.isInWater()) {
            RenderSystem.translatef(0.2F, 0.1F, 0.0F);
            RenderSystem.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
        }

    }
}
