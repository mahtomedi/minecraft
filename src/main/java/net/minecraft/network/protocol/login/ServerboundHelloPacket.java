package net.minecraft.network.protocol.login;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ServerboundHelloPacket(String name, Optional<ProfilePublicKey.Data> publicKey, Optional<UUID> profileId)
    implements Packet<ServerLoginPacketListener> {
    public ServerboundHelloPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(16), param0.readOptional(ProfilePublicKey.Data::new), param0.readOptional(FriendlyByteBuf::readUUID));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.name, 16);
        param0.writeOptional(this.publicKey, (param1, param2) -> param2.write(param0));
        param0.writeOptional(this.profileId, FriendlyByteBuf::writeUUID);
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleHello(this);
    }
}
