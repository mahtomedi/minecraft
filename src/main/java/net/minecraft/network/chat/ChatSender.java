package net.minecraft.network.chat;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record ChatSender(UUID uuid, Component name) {
    public ChatSender(FriendlyByteBuf param0) {
        this(param0.readUUID(), param0.readComponent());
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.uuid);
        param0.writeComponent(this.name);
    }
}
