package net.minecraft.client.gui.components.spectator;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
public class SpectatorGui implements SpectatorMenuListener {
    private static final ResourceLocation HOTBAR_SPRITE = new ResourceLocation("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = new ResourceLocation("hud/hotbar_selection");
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

    public void renderHotbar(GuiGraphics param0) {
        if (this.menu != null) {
            float var0 = this.getHotbarAlpha();
            if (var0 <= 0.0F) {
                this.menu.exit();
            } else {
                int var1 = param0.guiWidth() / 2;
                param0.pose().pushPose();
                param0.pose().translate(0.0F, 0.0F, -90.0F);
                int var2 = Mth.floor((float)param0.guiHeight() - 22.0F * var0);
                SpectatorPage var3 = this.menu.getCurrentPage();
                this.renderPage(param0, var0, var1, var2, var3);
                param0.pose().popPose();
            }
        }
    }

    protected void renderPage(GuiGraphics param0, float param1, int param2, int param3, SpectatorPage param4) {
        RenderSystem.enableBlend();
        param0.setColor(1.0F, 1.0F, 1.0F, param1);
        param0.blitSprite(HOTBAR_SPRITE, param2 - 91, param3, 182, 22);
        if (param4.getSelectedSlot() >= 0) {
            param0.blitSprite(HOTBAR_SELECTION_SPRITE, param2 - 91 - 1 + param4.getSelectedSlot() * 20, param3 - 1, 24, 23);
        }

        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        for(int var0 = 0; var0 < 9; ++var0) {
            this.renderSlot(param0, var0, param0.guiWidth() / 2 - 90 + var0 * 20 + 2, (float)(param3 + 3), param1, param4.getItem(var0));
        }

        RenderSystem.disableBlend();
    }

    private void renderSlot(GuiGraphics param0, int param1, int param2, float param3, float param4, SpectatorMenuItem param5) {
        if (param5 != SpectatorMenu.EMPTY_SLOT) {
            int var0 = (int)(param4 * 255.0F);
            param0.pose().pushPose();
            param0.pose().translate((float)param2, param3, 0.0F);
            float var1 = param5.isEnabled() ? 1.0F : 0.25F;
            param0.setColor(var1, var1, var1, param4);
            param5.renderIcon(param0, var1, var0);
            param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            param0.pose().popPose();
            if (var0 > 3 && param5.isEnabled()) {
                Component var2 = this.minecraft.options.keyHotbarSlots[param1].getTranslatedKeyMessage();
                param0.drawString(this.minecraft.font, var2, param2 + 19 - 2 - this.minecraft.font.width(var2), (int)param3 + 6 + 3, 16777215 + (var0 << 24));
            }
        }

    }

    public void renderTooltip(GuiGraphics param0) {
        int var0 = (int)(this.getHotbarAlpha() * 255.0F);
        if (var0 > 3 && this.menu != null) {
            SpectatorMenuItem var1 = this.menu.getSelectedItem();
            Component var2 = var1 == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : var1.getName();
            if (var2 != null) {
                int var3 = (param0.guiWidth() - this.minecraft.font.width(var2)) / 2;
                int var4 = param0.guiHeight() - 35;
                param0.drawString(this.minecraft.font, var2, var3, var4, 16777215 + (var0 << 24));
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
