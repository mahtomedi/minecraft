package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WitherArmorLayer extends RenderLayer<WitherBoss, WitherBossModel<WitherBoss>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final WitherBossModel<WitherBoss> model = new WitherBossModel<>(0.5F);

    public WitherArmorLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> param0) {
        super(param0);
    }

    public void render(WitherBoss param0, float param1, float param2, float param3, float param4, float param5, float param6, float param7) {
        if (param0.isPowered()) {
            GlStateManager.depthMask(!param0.isInvisible());
            this.bindTexture(WITHER_ARMOR_LOCATION);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            float var0 = (float)param0.tickCount + param3;
            float var1 = Mth.cos(var0 * 0.02F) * 3.0F;
            float var2 = var0 * 0.01F;
            GlStateManager.translatef(var1, var2, 0.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.enableBlend();
            float var3 = 0.5F;
            GlStateManager.color4f(0.5F, 0.5F, 0.5F, 1.0F);
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            this.model.prepareMobModel(param0, param1, param2, param3);
            this.getParentModel().copyPropertiesTo(this.model);
            GameRenderer var4 = Minecraft.getInstance().gameRenderer;
            var4.resetFogColor(true);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
            var4.resetFogColor(false);
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
