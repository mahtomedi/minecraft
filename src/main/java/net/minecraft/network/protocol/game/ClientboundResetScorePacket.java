package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundResetScorePacket(String owner, @Nullable String objectiveName) implements Packet<ClientGamePacketListener> {
    public ClientboundResetScorePacket(FriendlyByteBuf param0) {
        this(param0.readUtf(), param0.readNullable(FriendlyByteBuf::readUtf));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.owner);
        param0.writeNullable(this.objectiveName, FriendlyByteBuf::writeUtf);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleResetScore(this);
    }
}
