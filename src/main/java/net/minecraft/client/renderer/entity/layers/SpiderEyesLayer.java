package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SpiderModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpiderEyesLayer<T extends Entity, M extends SpiderModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation SPIDER_EYES_LOCATION = new ResourceLocation("textures/entity/spider_eyes.png");

    public SpiderEyesLayer(RenderLayerParent<T, M> param0) {
        super(param0);
    }

    @Override
    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        this.bindTexture(SPIDER_EYES_LOCATION);
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        if (param0.isInvisible()) {
            GlStateManager.depthMask(false);
        } else {
            GlStateManager.depthMask(true);
        }

        int var0 = 61680;
        int var1 = var0 % 65536;
        int var2 = var0 / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var1, (float)var2);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GameRenderer var3 = Minecraft.getInstance().gameRenderer;
        var3.resetFogColor(true);
        this.getParentModel().render(param0, param1, param2, param4, param5, param6, param7);
        var3.resetFogColor(false);
        var0 = param0.getLightColor();
        var1 = var0 % 65536;
        var2 = var0 / 65536;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)var1, (float)var2);
        this.setLightColor(param0);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableAlphaTest();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
