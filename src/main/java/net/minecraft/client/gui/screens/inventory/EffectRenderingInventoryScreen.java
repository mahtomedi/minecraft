package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected boolean doRenderEffects;

    public EffectRenderingInventoryScreen(T param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    protected void init() {
        super.init();
        this.checkEffectRendering();
    }

    protected void checkEffectRendering() {
        if (this.minecraft.player.getActiveEffects().isEmpty()) {
            this.leftPos = (this.width - this.imageWidth) / 2;
            this.doRenderEffects = false;
        } else {
            this.leftPos = 160 + (this.width - this.imageWidth - 200) / 2;
            this.doRenderEffects = true;
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        if (this.doRenderEffects) {
            this.renderEffects(param0);
        }

    }

    private void renderEffects(PoseStack param0) {
        int var0 = this.leftPos - 124;
        Collection<MobEffectInstance> var1 = this.minecraft.player.getActiveEffects();
        if (!var1.isEmpty()) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int var2 = 33;
            if (var1.size() > 5) {
                var2 = 132 / (var1.size() - 1);
            }

            Iterable<MobEffectInstance> var3 = Ordering.natural().sortedCopy(var1);
            this.renderBackgrounds(param0, var0, var2, var3);
            this.renderIcons(param0, var0, var2, var3);
            this.renderLabels(param0, var0, var2, var3);
        }
    }

    private void renderBackgrounds(PoseStack param0, int param1, int param2, Iterable<MobEffectInstance> param3) {
        RenderSystem.setShaderTexture(0, INVENTORY_LOCATION);
        int var0 = this.topPos;

        for(MobEffectInstance var1 : param3) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.blit(param0, param1, var0, 0, 166, 140, 32);
            var0 += param2;
        }

    }

    private void renderIcons(PoseStack param0, int param1, int param2, Iterable<MobEffectInstance> param3) {
        MobEffectTextureManager var0 = this.minecraft.getMobEffectTextures();
        int var1 = this.topPos;

        for(MobEffectInstance var2 : param3) {
            MobEffect var3 = var2.getEffect();
            TextureAtlasSprite var4 = var0.get(var3);
            RenderSystem.setShaderTexture(0, var4.atlas().location());
            blit(param0, param1 + 6, var1 + 7, this.getBlitOffset(), 18, 18, var4);
            var1 += param2;
        }

    }

    private void renderLabels(PoseStack param0, int param1, int param2, Iterable<MobEffectInstance> param3) {
        int var0 = this.topPos;

        for(MobEffectInstance var1 : param3) {
            String var2 = I18n.get(var1.getEffect().getDescriptionId());
            if (var1.getAmplifier() >= 1 && var1.getAmplifier() <= 9) {
                var2 = var2 + ' ' + I18n.get("enchantment.level." + (var1.getAmplifier() + 1));
            }

            this.font.drawShadow(param0, var2, (float)(param1 + 10 + 18), (float)(var0 + 6), 16777215);
            String var3 = MobEffectUtil.formatDuration(var1, 1.0F);
            this.font.drawShadow(param0, var3, (float)(param1 + 10 + 18), (float)(var0 + 6 + 10), 8355711);
            var0 += param2;
        }

    }
}
