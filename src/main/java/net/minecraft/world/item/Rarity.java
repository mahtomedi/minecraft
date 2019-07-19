package net.minecraft.world.item;

import net.minecraft.ChatFormatting;

public enum Rarity {
    COMMON(ChatFormatting.WHITE),
    UNCOMMON(ChatFormatting.YELLOW),
    RARE(ChatFormatting.AQUA),
    EPIC(ChatFormatting.LIGHT_PURPLE);

    public final ChatFormatting color;

    private Rarity(ChatFormatting param0) {
        this.color = param0;
    }
}
