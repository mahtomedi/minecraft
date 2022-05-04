package net.minecraft.network.protocol.login;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.ProfilePublicKey;

public record ServerboundHelloPacket(String name, Optional<ProfilePublicKey.Data> publicKey) implements Packet<ServerLoginPacketListener> {
    public ServerboundHelloPacket(FriendlyByteBuf param0) {
        this(param0.readUtf(16), param0.readOptional(param0x -> param0x.readWithCodec(ProfilePublicKey.Data.CODEC)));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.name, 16);
        param0.writeOptional(this.publicKey, (param0x, param1) -> param0x.writeWithCodec(ProfilePublicKey.Data.CODEC, param1));
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleHello(this);
    }
}
