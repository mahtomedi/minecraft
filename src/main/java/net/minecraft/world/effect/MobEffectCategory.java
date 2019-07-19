package net.minecraft.world.effect;

import net.minecraft.ChatFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum MobEffectCategory {
    BENEFICIAL(ChatFormatting.BLUE),
    HARMFUL(ChatFormatting.RED),
    NEUTRAL(ChatFormatting.BLUE);

    private final ChatFormatting tooltipFormatting;

    private MobEffectCategory(ChatFormatting param0) {
        this.tooltipFormatting = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public ChatFormatting getTooltipFormatting() {
        return this.tooltipFormatting;
    }
}
