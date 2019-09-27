package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
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

    public ResourceLocation getTextureLocation(Salmon param0) {
        return SALMON_LOCATION;
    }

    protected void setupRotations(Salmon param0, PoseStack param1, float param2, float param3, float param4) {
        super.setupRotations(param0, param1, param2, param3, param4);
        float var0 = 1.0F;
        float var1 = 1.0F;
        if (!param0.isInWater()) {
            var0 = 1.3F;
            var1 = 1.7F;
        }

        float var2 = var0 * 4.3F * Mth.sin(var1 * 0.6F * param2);
        param1.mulPose(Vector3f.YP.rotation(var2, true));
        param1.translate(0.0, 0.0, -0.4F);
        if (!param0.isInWater()) {
            param1.translate(0.2F, 0.1F, 0.0);
            param1.mulPose(Vector3f.ZP.rotation(90.0F, true));
        }

    }
}
