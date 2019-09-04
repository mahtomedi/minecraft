package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class EnderDragonEyesLayer extends RenderLayer<EnderDragon, DragonModel> {
    private static final ResourceLocation DRAGON_EYES_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_eyes.png");

    public EnderDragonEyesLayer(RenderLayerParent<EnderDragon, DragonModel> param0) {
        super(param0);
    }

    public void render(EnderDragon param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        this.bindTexture(DRAGON_EYES_LOCATION);
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.disableLighting();
        RenderSystem.depthFunc(514);
        int var0 = 61680;
        int var1 = 61680;
        int var2 = 0;
        RenderSystem.glMultiTexCoord2f(33985, 61680.0F, 0.0F);
        RenderSystem.enableLighting();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        GameRenderer var3 = Minecraft.getInstance().gameRenderer;
        var3.resetFogColor(true);
        this.getParentModel().render(param0, param1, param2, param4, param5, param6, param7);
        var3.resetFogColor(false);
        this.setLightColor(param0);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.depthFunc(515);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
