package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.minecraft.client.model.CodModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CodRenderer extends MobRenderer<Cod, CodModel<Cod>> {
    private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

    public CodRenderer(EntityRenderDispatcher param0) {
        super(param0, new CodModel<>(), 0.3F);
    }

    @Nullable
    protected ResourceLocation getTextureLocation(Cod param0) {
        return COD_LOCATION;
    }

    protected void setupRotations(Cod param0, float param1, float param2, float param3) {
        super.setupRotations(param0, param1, param2, param3);
        float var0 = 4.3F * Mth.sin(0.6F * param1);
        GlStateManager.rotatef(var0, 0.0F, 1.0F, 0.0F);
        if (!param0.isInWater()) {
            GlStateManager.translatef(0.1F, 0.1F, -0.1F);
            GlStateManager.rotatef(90.0F, 0.0F, 0.0F, 1.0F);
        }

    }
}
