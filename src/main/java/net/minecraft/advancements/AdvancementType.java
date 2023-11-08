package net.minecraft.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;

public enum AdvancementType implements StringRepresentable {
    TASK("task", ChatFormatting.GREEN),
    CHALLENGE("challenge", ChatFormatting.DARK_PURPLE),
    GOAL("goal", ChatFormatting.GREEN);

    public static final Codec<AdvancementType> CODEC = StringRepresentable.fromEnum(AdvancementType::values);
    private final String name;
    private final ChatFormatting chatColor;
    private final Component displayName;

    private AdvancementType(String param0, ChatFormatting param1) {
        this.name = param0;
        this.chatColor = param1;
        this.displayName = Component.translatable("advancements.toast." + param0);
    }

    public ChatFormatting getChatColor() {
        return this.chatColor;
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public MutableComponent createAnnouncement(AdvancementHolder param0, ServerPlayer param1) {
        return Component.translatable("chat.type.advancement." + this.name, param1.getDisplayName(), Advancement.name(param0));
    }
}
