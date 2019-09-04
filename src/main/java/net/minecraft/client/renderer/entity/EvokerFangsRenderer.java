package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.EvokerFangsModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EvokerFangsRenderer extends EntityRenderer<EvokerFangs> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("textures/entity/illager/evoker_fangs.png");
    private final EvokerFangsModel<EvokerFangs> model = new EvokerFangsModel<>();

    public EvokerFangsRenderer(EntityRenderDispatcher param0) {
        super(param0);
    }

    public void render(EvokerFangs param0, double param1, double param2, double param3, float param4, float param5) {
        float var0 = param0.getAnimationProgress(param5);
        if (var0 != 0.0F) {
            float var1 = 2.0F;
            if (var0 > 0.9F) {
                var1 = (float)((double)var1 * ((1.0 - (double)var0) / 0.1F));
            }

            RenderSystem.pushMatrix();
            RenderSystem.disableCull();
            RenderSystem.enableAlphaTest();
            this.bindTexture(param0);
            RenderSystem.translatef((float)param1, (float)param2, (float)param3);
            RenderSystem.rotatef(90.0F - param0.yRot, 0.0F, 1.0F, 0.0F);
            RenderSystem.scalef(-var1, -var1, var1);
            float var2 = 0.03125F;
            RenderSystem.translatef(0.0F, -0.626F, 0.0F);
            this.model.render(param0, var0, 0.0F, 0.0F, param0.yRot, param0.xRot, 0.03125F);
            RenderSystem.popMatrix();
            RenderSystem.enableCull();
            super.render(param0, param1, param2, param3, param4, param5);
        }
    }

    protected ResourceLocation getTextureLocation(EvokerFangs param0) {
        return TEXTURE_LOCATION;
    }
}
