package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderEyesLayer<T extends LivingEntity> extends RenderLayer<T, EndermanModel<T>> {
    private static final ResourceLocation ENDERMAN_EYES_LOCATION = new ResourceLocation("textures/entity/enderman/enderman_eyes.png");

    public EnderEyesLayer(RenderLayerParent<T, EndermanModel<T>> param0) {
        super(param0);
    }

    public void render(T param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        this.bindTexture(ENDERMAN_EYES_LOCATION);
        GlStateManager.enableBlend();
        GlStateManager.disableAlphaTest();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        GlStateManager.disableLighting();
        GlStateManager.depthMask(!param0.isInvisible());
        int var0 = 61680;
        int var1 = 61680;
        int var2 = 0;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, 61680.0F, 0.0F);
        GlStateManager.enableLighting();
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GameRenderer var3 = Minecraft.getInstance().gameRenderer;
        var3.resetFogColor(true);
        this.getParentModel().render(param0, param1, param2, param4, param5, param6, param7);
        var3.resetFogColor(false);
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
