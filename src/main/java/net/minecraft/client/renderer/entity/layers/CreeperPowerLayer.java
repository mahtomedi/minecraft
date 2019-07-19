package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreeperPowerLayer extends RenderLayer<Creeper, CreeperModel<Creeper>> {
    private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final CreeperModel<Creeper> model = new CreeperModel<>(2.0F);

    public CreeperPowerLayer(RenderLayerParent<Creeper, CreeperModel<Creeper>> param0) {
        super(param0);
    }

    public void render(Creeper param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.isPowered()) {
            boolean var0 = param0.isInvisible();
            GlStateManager.depthMask(!var0);
            this.bindTexture(POWER_LOCATION);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float var1 = (float)param0.tickCount + param3;
            GlStateManager.translatef(var1 * 0.01F, var1 * 0.01F, 0.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.enableBlend();
            float var2 = 0.5F;
            GlStateManager.color4f(0.5F, 0.5F, 0.5F, 1.0F);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            this.getParentModel().copyPropertiesTo(this.model);
            GameRenderer var3 = Minecraft.getInstance().gameRenderer;
            var3.resetFogColor(true);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
            var3.resetFogColor(false);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(5888);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
