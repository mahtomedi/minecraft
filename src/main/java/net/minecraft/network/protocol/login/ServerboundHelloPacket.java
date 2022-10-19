package net.minecraft.network.protocol.login;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.Packet;

public record ServerboundHelloPacket(String name, RemoteChatSession.Data chatSession, Optional<UUID> profileId) implements Packet<ServerLoginPacketListener> {
    public ServerboundHelloPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(16), RemoteChatSession.Data.read(param0), param0.readOptional(FriendlyByteBuf::readUUID));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.name, 16);
        RemoteChatSession.Data.write(param0, this.chatSession);
        param0.writeOptional(this.profileId, FriendlyByteBuf::writeUUID);
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleHello(this);
    }
}
