package net.minecraft.advancements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum FrameType {
    TASK("task", ChatFormatting.GREEN),
    CHALLENGE("challenge", ChatFormatting.DARK_PURPLE),
    GOAL("goal", ChatFormatting.GREEN);

    private final String name;
    private final ChatFormatting chatColor;
    private final Component displayName;

    private FrameType(String param0, ChatFormatting param1) {
        this.name = param0;
        this.chatColor = param1;
        this.displayName = Component.translatable("advancements.toast." + param0);
    }

    public String getName() {
        return this.name;
    }

    public static FrameType byName(String param0) {
        for(FrameType var0 : values()) {
            if (var0.name.equals(param0)) {
                return var0;
            }
        }

        throw new IllegalArgumentException("Unknown frame type '" + param0 + "'");
    }

    public ChatFormatting getChatColor() {
        return this.chatColor;
    }

    public Component getDisplayName() {
        return this.displayName;
    }
}
