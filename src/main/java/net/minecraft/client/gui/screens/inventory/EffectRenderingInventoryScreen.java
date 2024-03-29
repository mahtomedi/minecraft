package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class EffectRenderingInventoryScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation EFFECT_BACKGROUND_LARGE_SPRITE = new ResourceLocation("container/inventory/effect_background_large");
    private static final ResourceLocation EFFECT_BACKGROUND_SMALL_SPRITE = new ResourceLocation("container/inventory/effect_background_small");

    public EffectRenderingInventoryScreen(T param0, Inventory param1, Component param2) {
        super(param0, param1, param2);
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        this.renderEffects(param0, param1, param2);
    }

    public boolean canSeeEffects() {
        int var0 = this.leftPos + this.imageWidth + 2;
        int var1 = this.width - var0;
        return var1 >= 32;
    }

    private void renderEffects(GuiGraphics param0, int param1, int param2) {
        int var0 = this.leftPos + this.imageWidth + 2;
        int var1 = this.width - var0;
        Collection<MobEffectInstance> var2 = this.minecraft.player.getActiveEffects();
        if (!var2.isEmpty() && var1 >= 32) {
            boolean var3 = var1 >= 120;
            int var4 = 33;
            if (var2.size() > 5) {
                var4 = 132 / (var2.size() - 1);
            }

            Iterable<MobEffectInstance> var5 = Ordering.natural().sortedCopy(var2);
            this.renderBackgrounds(param0, var0, var4, var5, var3);
            this.renderIcons(param0, var0, var4, var5, var3);
            if (var3) {
                this.renderLabels(param0, var0, var4, var5);
            } else if (param1 >= var0 && param1 <= var0 + 33) {
                int var6 = this.topPos;
                MobEffectInstance var7 = null;

                for(MobEffectInstance var8 : var5) {
                    if (param2 >= var6 && param2 <= var6 + var4) {
                        var7 = var8;
                    }

                    var6 += var4;
                }

                if (var7 != null) {
                    List<Component> var9 = List.of(this.getEffectName(var7), MobEffectUtil.formatDuration(var7, 1.0F));
                    param0.renderTooltip(this.font, var9, Optional.empty(), param1, param2);
                }
            }

        }
    }

    private void renderBackgrounds(GuiGraphics param0, int param1, int param2, Iterable<MobEffectInstance> param3, boolean param4) {
        int var0 = this.topPos;

        for(MobEffectInstance var1 : param3) {
            if (param4) {
                param0.blitSprite(EFFECT_BACKGROUND_LARGE_SPRITE, param1, var0, 120, 32);
            } else {
                param0.blitSprite(EFFECT_BACKGROUND_SMALL_SPRITE, param1, var0, 32, 32);
            }

            var0 += param2;
        }

    }

    private void renderIcons(GuiGraphics param0, int param1, int param2, Iterable<MobEffectInstance> param3, boolean param4) {
        MobEffectTextureManager var0 = this.minecraft.getMobEffectTextures();
        int var1 = this.topPos;

        for(MobEffectInstance var2 : param3) {
            MobEffect var3 = var2.getEffect();
            TextureAtlasSprite var4 = var0.get(var3);
            param0.blit(param1 + (param4 ? 6 : 7), var1 + 7, 0, 18, 18, var4);
            var1 += param2;
        }

    }

    private void renderLabels(GuiGraphics param0, int param1, int param2, Iterable<MobEffectInstance> param3) {
        int var0 = this.topPos;

        for(MobEffectInstance var1 : param3) {
            Component var2 = this.getEffectName(var1);
            param0.drawString(this.font, var2, param1 + 10 + 18, var0 + 6, 16777215);
            Component var3 = MobEffectUtil.formatDuration(var1, 1.0F);
            param0.drawString(this.font, var3, param1 + 10 + 18, var0 + 6 + 10, 8355711);
            var0 += param2;
        }

    }

    private Component getEffectName(MobEffectInstance param0) {
        MutableComponent var0 = param0.getEffect().getDisplayName().copy();
        if (param0.getAmplifier() >= 1 && param0.getAmplifier() <= 9) {
            var0.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (param0.getAmplifier() + 1)));
        }

        return var0;
    }
}
