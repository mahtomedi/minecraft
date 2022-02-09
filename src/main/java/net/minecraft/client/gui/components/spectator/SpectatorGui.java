package net.minecraft.client.gui.components.spectator;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpectatorGui extends GuiComponent implements SpectatorMenuListener {
    private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation SPECTATOR_LOCATION = new ResourceLocation("textures/gui/spectator_widgets.png");
    private static final long FADE_OUT_DELAY = 5000L;
    private static final long FADE_OUT_TIME = 2000L;
    private final Minecraft minecraft;
    private long lastSelectionTime;
    @Nullable
    private SpectatorMenu menu;

    public SpectatorGui(Minecraft param0) {
        this.minecraft = param0;
    }

    public void onHotbarSelected(int param0) {
        this.lastSelectionTime = Util.getMillis();
        if (this.menu != null) {
            this.menu.selectSlot(param0);
        } else {
            this.menu = new SpectatorMenu(this);
        }

    }

    private float getHotbarAlpha() {
        long var0 = this.lastSelectionTime - Util.getMillis() + 5000L;
        return Mth.clamp((float)var0 / 2000.0F, 0.0F, 1.0F);
    }

    public void renderHotbar(PoseStack param0) {
        if (this.menu != null) {
            float var0 = this.getHotbarAlpha();
            if (var0 <= 0.0F) {
                this.menu.exit();
            } else {
                int var1 = this.minecraft.getWindow().getGuiScaledWidth() / 2;
                int var2 = this.getBlitOffset();
                this.setBlitOffset(-90);
                int var3 = Mth.floor((float)this.minecraft.getWindow().getGuiScaledHeight() - 22.0F * var0);
                SpectatorPage var4 = this.menu.getCurrentPage();
                this.renderPage(param0, var0, var1, var3, var4);
                this.setBlitOffset(var2);
            }
        }
    }

    protected void renderPage(PoseStack param0, float param1, int param2, int param3, SpectatorPage param4) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, param1);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        this.blit(param0, param2 - 91, param3, 0, 0, 182, 22);
        if (param4.getSelectedSlot() >= 0) {
            this.blit(param0, param2 - 91 - 1 + param4.getSelectedSlot() * 20, param3 - 1, 0, 22, 24, 22);
        }

        for(int var0 = 0; var0 < 9; ++var0) {
            this.renderSlot(
                param0, var0, this.minecraft.getWindow().getGuiScaledWidth() / 2 - 90 + var0 * 20 + 2, (float)(param3 + 3), param1, param4.getItem(var0)
            );
        }

        RenderSystem.disableBlend();
    }

    private void renderSlot(PoseStack param0, int param1, int param2, float param3, float param4, SpectatorMenuItem param5) {
        RenderSystem.setShaderTexture(0, SPECTATOR_LOCATION);
        if (param5 != SpectatorMenu.EMPTY_SLOT) {
            int var0 = (int)(param4 * 255.0F);
            param0.pushPose();
            param0.translate((double)param2, (double)param3, 0.0);
            float var1 = param5.isEnabled() ? 1.0F : 0.25F;
            RenderSystem.setShaderColor(var1, var1, var1, param4);
            param5.renderIcon(param0, var1, var0);
            param0.popPose();
            if (var0 > 3 && param5.isEnabled()) {
                Component var2 = this.minecraft.options.keyHotbarSlots[param1].getTranslatedKeyMessage();
                this.minecraft
                    .font
                    .drawShadow(param0, var2, (float)(param2 + 19 - 2 - this.minecraft.font.width(var2)), param3 + 6.0F + 3.0F, 16777215 + (var0 << 24));
            }
        }

    }

    public void renderTooltip(PoseStack param0) {
        int var0 = (int)(this.getHotbarAlpha() * 255.0F);
        if (var0 > 3 && this.menu != null) {
            SpectatorMenuItem var1 = this.menu.getSelectedItem();
            Component var2 = var1 == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : var1.getName();
            if (var2 != null) {
                int var3 = (this.minecraft.getWindow().getGuiScaledWidth() - this.minecraft.font.width(var2)) / 2;
                int var4 = this.minecraft.getWindow().getGuiScaledHeight() - 35;
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                this.minecraft.font.drawShadow(param0, var2, (float)var3, (float)var4, 16777215 + (var0 << 24));
                RenderSystem.disableBlend();
            }
        }

    }

    @Override
    public void onSpectatorMenuClosed(SpectatorMenu param0) {
        this.menu = null;
        this.lastSelectionTime = 0L;
    }

    public boolean isMenuActive() {
        return this.menu != null;
    }

    public void onMouseScrolled(int param0) {
        int var0 = this.menu.getSelectedSlot() + param0;

        while(var0 >= 0 && var0 <= 8 && (this.menu.getItem(var0) == SpectatorMenu.EMPTY_SLOT || !this.menu.getItem(var0).isEnabled())) {
            var0 += param0;
        }

        if (var0 >= 0 && var0 <= 8) {
            this.menu.selectSlot(var0);
            this.lastSelectionTime = Util.getMillis();
        }

    }

    public void onMouseMiddleClick() {
        this.lastSelectionTime = Util.getMillis();
        if (this.isMenuActive()) {
            int var0 = this.menu.getSelectedSlot();
            if (var0 != -1) {
                this.menu.selectSlot(var0);
            }
        } else {
            this.menu = new SpectatorMenu(this);
        }

    }
}
