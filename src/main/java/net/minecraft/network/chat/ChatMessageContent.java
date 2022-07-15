package net.minecraft.network.chat;

import java.util.Objects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.network.FilteredText;

public record ChatMessageContent(Component plain, Component decorated) {
    public ChatMessageContent(Component param0) {
        this(param0, param0);
    }

    public ChatMessageContent(String param0, Component param1) {
        this(Component.literal(param0), param1);
    }

    public ChatMessageContent(String param0) {
        this(Component.literal(param0));
    }

    public static FilteredText<ChatMessageContent> fromFiltered(FilteredText<String> param0) {
        return param0.map(ChatMessageContent::new);
    }

    public static FilteredText<ChatMessageContent> fromFiltered(FilteredText<String> param0, FilteredText<Component> param1) {
        return param0.map(
            param1x -> new ChatMessageContent(param1x, param1.raw()),
            param1x -> param1.filtered() != null ? new ChatMessageContent(param1x, param1.filtered()) : null
        );
    }

    public boolean isDecorated() {
        return !this.decorated.equals(this.plain);
    }

    public static ChatMessageContent read(FriendlyByteBuf param0) {
        Component var0 = param0.readComponent();
        Component var1 = param0.readNullable(FriendlyByteBuf::readComponent);
        return new ChatMessageContent(var0, Objects.requireNonNullElse(var1, var0));
    }

    public static void write(FriendlyByteBuf param0, ChatMessageContent param1) {
        param0.writeComponent(param1.plain());
        Component var0 = param1.isDecorated() ? param1.decorated() : null;
        param0.writeNullable(var0, FriendlyByteBuf::writeComponent);
    }
}
