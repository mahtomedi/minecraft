package net.minecraft.advancements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum FrameType {
    TASK("task", 0, ChatFormatting.GREEN),
    CHALLENGE("challenge", 26, ChatFormatting.DARK_PURPLE),
    GOAL("goal", 52, ChatFormatting.GREEN);

    private final String name;
    private final int texture;
    private final ChatFormatting chatColor;
    private final Component displayName;

    private FrameType(String param0, int param1, ChatFormatting param2) {
        this.name = param0;
        this.texture = param1;
        this.chatColor = param2;
        this.displayName = Component.translatable("advancements.toast." + param0);
    }

    public String getName() {
        return this.name;
    }

    public int getTexture() {
        return this.texture;
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
