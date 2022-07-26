package net.minecraft.network.chat;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;

public record ChatMessageContent(String plain, Component decorated) {
    public ChatMessageContent(String param0) {
        this(param0, Component.literal(param0));
    }

    public boolean isDecorated() {
        return !this.decorated.equals(Component.literal(this.plain));
    }

    public static ChatMessageContent read(FriendlyByteBuf param0) {
        String var0 = param0.readUtf(256);
        Component var1 = param0.readNullable(FriendlyByteBuf::readComponent);
        return new ChatMessageContent(var0, Objects.requireNonNullElse(var1, Component.literal(var0)));
    }

    public static void write(FriendlyByteBuf param0, ChatMessageContent param1) {
        param0.writeUtf(param1.plain(), 256);
        Component var0 = param1.isDecorated() ? param1.decorated() : null;
        param0.writeNullable(var0, FriendlyByteBuf::writeComponent);
    }
}
