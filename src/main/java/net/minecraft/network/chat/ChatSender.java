package net.minecraft.network.chat;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.FriendlyByteBuf;

public record ChatSender(UUID profileId, Component name, @Nullable Component teamName) {
    public ChatSender(UUID param0, Component param1) {
        this(param0, param1, null);
    }

    public ChatSender(FriendlyByteBuf param0) {
        this(param0.readUUID(), param0.readComponent(), param0.readNullable(FriendlyByteBuf::readComponent));
    }

    public static ChatSender system(Component param0) {
        return new ChatSender(Util.NIL_UUID, param0);
    }

    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.profileId);
        param0.writeComponent(this.name);
        param0.writeNullable(this.teamName, FriendlyByteBuf::writeComponent);
    }

    public ChatSender withTeamName(Component param0) {
        return new ChatSender(this.profileId, this.name, param0);
    }

    public boolean isPlayer() {
        return !this.profileId.equals(Util.NIL_UUID);
    }
}
