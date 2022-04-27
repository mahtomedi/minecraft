package net.minecraft.network.protocol.login;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ServerboundHelloPacket(String name, Optional<ProfilePublicKey> publicKey) implements Packet<ServerLoginPacketListener> {
    public ServerboundHelloPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(16), param0.readOptional(param0x -> param0x.readWithCodec(ProfilePublicKey.CODEC)));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.name, 16);
        param0.writeOptional(this.publicKey, (param0x, param1) -> param0x.writeWithCodec(ProfilePublicKey.CODEC, param1));
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleHello(this);
    }
}
