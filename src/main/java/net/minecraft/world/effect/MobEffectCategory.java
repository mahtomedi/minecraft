package net.minecraft.world.effect;

import net.minecraft.ChatFormatting;

public enum MobEffectCategory {
    BENEFICIAL(ChatFormatting.BLUE),
    HARMFUL(ChatFormatting.RED),
    NEUTRAL(ChatFormatting.BLUE);

    private final ChatFormatting tooltipFormatting;

    private MobEffectCategory(ChatFormatting param0) {
        this.tooltipFormatting = param0;
    }

    public ChatFormatting getTooltipFormatting() {
        return this.tooltipFormatting;
    }
}
