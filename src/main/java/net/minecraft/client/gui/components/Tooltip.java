package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Tooltip implements NarrationSupplier {
    private static final int MAX_WIDTH = 170;
    private final Component message;
    @Nullable
    private List<FormattedCharSequence> cachedTooltip;
    @Nullable
    private final Component narration;
    private int msDelay;
    private long hoverOrFocusedStartTime;
    private boolean wasHoveredOrFocused;

    private Tooltip(Component param0, @Nullable Component param1) {
        this.message = param0;
        this.narration = param1;
    }

    public void setDelay(int param0) {
        this.msDelay = param0;
    }

    public static Tooltip create(Component param0, @Nullable Component param1) {
        return new Tooltip(param0, param1);
    }

    public static Tooltip create(Component param0) {
        return new Tooltip(param0, param0);
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        if (this.narration != null) {
            param0.add(NarratedElementType.HINT, this.narration);
        }

    }

    public List<FormattedCharSequence> toCharSequence(Minecraft param0) {
        if (this.cachedTooltip == null) {
            this.cachedTooltip = splitTooltip(param0, this.message);
        }

        return this.cachedTooltip;
    }

    public static List<FormattedCharSequence> splitTooltip(Minecraft param0, Component param1) {
        return param0.font.split(param1, 170);
    }

    public void refreshTooltipForNextRenderPass(boolean param0, boolean param1, ScreenRectangle param2) {
        boolean var0 = param0 || param1 && Minecraft.getInstance().getLastInputType().isKeyboard();
        if (var0 != this.wasHoveredOrFocused) {
            if (var0) {
                this.hoverOrFocusedStartTime = Util.getMillis();
            }

            this.wasHoveredOrFocused = var0;
        }

        if (var0 && Util.getMillis() - this.hoverOrFocusedStartTime > (long)this.msDelay) {
            Screen var1 = Minecraft.getInstance().screen;
            if (var1 != null) {
                var1.setTooltipForNextRenderPass(this, this.createTooltipPositioner(param0, param1, param2), param1);
            }
        }

    }

    protected ClientTooltipPositioner createTooltipPositioner(boolean param0, boolean param1, ScreenRectangle param2) {
        return (ClientTooltipPositioner)(!param0 && param1 && Minecraft.getInstance().getLastInputType().isKeyboard()
            ? new BelowOrAboveWidgetTooltipPositioner(param2)
            : new MenuTooltipPositioner(param2));
    }
}
