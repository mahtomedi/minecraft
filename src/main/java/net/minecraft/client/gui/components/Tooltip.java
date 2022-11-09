package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
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

    private Tooltip(Component param0, @Nullable Component param1) {
        this.message = param0;
        this.narration = param1;
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
}
