package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TabButton extends AbstractWidget {
    private static final WidgetSprites SPRITES = new WidgetSprites(
        new ResourceLocation("widget/tab_selected"),
        new ResourceLocation("widget/tab"),
        new ResourceLocation("widget/tab_selected_highlighted"),
        new ResourceLocation("widget/tab_highlighted")
    );
    private static final int SELECTED_OFFSET = 3;
    private static final int TEXT_MARGIN = 1;
    private static final int UNDERLINE_HEIGHT = 1;
    private static final int UNDERLINE_MARGIN_X = 4;
    private static final int UNDERLINE_MARGIN_BOTTOM = 2;
    private final TabManager tabManager;
    private final Tab tab;

    public TabButton(TabManager param0, Tab param1, int param2, int param3) {
        super(0, 0, param2, param3, param1.getTabTitle());
        this.tabManager = param0;
        this.tab = param1;
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        param0.blitSprite(SPRITES.get(this.isSelected(), this.isHovered()), this.getX(), this.getY(), this.width, this.height);
        Font var0 = Minecraft.getInstance().font;
        int var1 = this.active ? -1 : -6250336;
        this.renderString(param0, var0, var1);
        if (this.isSelected()) {
            this.renderFocusUnderline(param0, var0, var1);
        }

    }

    public void renderString(GuiGraphics param0, Font param1, int param2) {
        int var0 = this.getX() + 1;
        int var1 = this.getY() + (this.isSelected() ? 0 : 3);
        int var2 = this.getX() + this.getWidth() - 1;
        int var3 = this.getY() + this.getHeight();
        renderScrollingString(param0, param1, this.getMessage(), var0, var1, var2, var3, param2);
    }

    private void renderFocusUnderline(GuiGraphics param0, Font param1, int param2) {
        int var0 = Math.min(param1.width(this.getMessage()), this.getWidth() - 4);
        int var1 = this.getX() + (this.getWidth() - var0) / 2;
        int var2 = this.getY() + this.getHeight() - 2;
        param0.fill(var1, var2, var1 + var0, var2 + 1, param2);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)Component.translatable("gui.narrate.tab", this.tab.getTabTitle()));
    }

    @Override
    public void playDownSound(SoundManager param0) {
    }

    public Tab tab() {
        return this.tab;
    }

    public boolean isSelected() {
        return this.tabManager.getCurrentTab() == this.tab;
    }
}
