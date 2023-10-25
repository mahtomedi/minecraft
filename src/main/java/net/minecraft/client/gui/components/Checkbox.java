package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Checkbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = new ResourceLocation("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = new ResourceLocation("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = new ResourceLocation("widget/checkbox");
    private static final int TEXT_COLOR = 14737632;
    private static final int SPACING = 4;
    private static final int BOX_PADDING = 8;
    private boolean selected;
    private final Checkbox.OnValueChange onValueChange;

    Checkbox(int param0, int param1, Component param2, Font param3, boolean param4, Checkbox.OnValueChange param5) {
        super(param0, param1, boxSize(param3) + 4 + param3.width(param2), boxSize(param3), param2);
        this.selected = param4;
        this.onValueChange = param5;
    }

    public static Checkbox.Builder builder(Component param0, Font param1) {
        return new Checkbox.Builder(param0, param1);
    }

    private static int boxSize(Font param0) {
        return 9 + 8;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        param0.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.focused"));
            } else {
                param0.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.hovered"));
            }
        }

    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        Minecraft var0 = Minecraft.getInstance();
        RenderSystem.enableDepthTest();
        Font var1 = var0.font;
        param0.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        ResourceLocation var2;
        if (this.selected) {
            var2 = this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            var2 = this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }

        int var4 = boxSize(var1);
        int var5 = this.getX() + var4 + 4;
        int var6 = this.getY() + (this.height >> 1) - (9 >> 1);
        param0.blitSprite(var2, this.getX(), this.getY(), var4, var4);
        param0.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        param0.drawString(var1, this.getMessage(), var5, var6, 14737632 | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Font font;
        private int x = 0;
        private int y = 0;
        private Checkbox.OnValueChange onValueChange = Checkbox.OnValueChange.NOP;
        private boolean selected = false;
        @Nullable
        private OptionInstance<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component param0, Font param1) {
            this.message = param0;
            this.font = param1;
        }

        public Checkbox.Builder pos(int param0, int param1) {
            this.x = param0;
            this.y = param1;
            return this;
        }

        public Checkbox.Builder onValueChange(Checkbox.OnValueChange param0) {
            this.onValueChange = param0;
            return this;
        }

        public Checkbox.Builder selected(boolean param0) {
            this.selected = param0;
            this.option = null;
            return this;
        }

        public Checkbox.Builder selected(OptionInstance<Boolean> param0) {
            this.option = param0;
            this.selected = param0.get();
            return this;
        }

        public Checkbox.Builder tooltip(Tooltip param0) {
            this.tooltip = param0;
            return this;
        }

        public Checkbox build() {
            Checkbox.OnValueChange var0 = this.option == null ? this.onValueChange : (param0, param1) -> {
                this.option.set(param1);
                this.onValueChange.onValueChange(param0, param1);
            };
            Checkbox var1 = new Checkbox(this.x, this.y, this.message, this.font, this.selected, var0);
            var1.setTooltip(this.tooltip);
            return var1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnValueChange {
        Checkbox.OnValueChange NOP = (param0, param1) -> {
        };

        void onValueChange(Checkbox var1, boolean var2);
    }
}
