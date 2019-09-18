package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.FogRenderer;
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
            RenderSystem.depthMask(!param0.isInvisible());
            this.bindTexture(WITHER_ARMOR_LOCATION);
            RenderSystem.matrixMode(5890);
            RenderSystem.loadIdentity();
            float var0 = (float)param0.tickCount + param3;
            float var1 = Mth.cos(var0 * 0.02F) * 3.0F;
            float var2 = var0 * 0.01F;
            RenderSystem.translatef(var1, var2, 0.0F);
            RenderSystem.matrixMode(5888);
            RenderSystem.enableBlend();
            float var3 = 0.5F;
            RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
            RenderSystem.disableLighting();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            this.model.prepareMobModel(param0, param1, param2, param3);
            this.getParentModel().copyPropertiesTo(this.model);
            FogRenderer.resetFogColor(true);
            this.model.render(param0, param1, param2, param4, param5, param6, param7);
            FogRenderer.resetFogColor(false);
            RenderSystem.matrixMode(5890);
            RenderSystem.loadIdentity();
            RenderSystem.matrixMode(5888);
            RenderSystem.enableLighting();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}
