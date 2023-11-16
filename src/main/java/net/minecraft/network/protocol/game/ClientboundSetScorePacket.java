package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.network.protocol.Packet;

public record ClientboundSetScorePacket(String owner, String objectiveName, int score, @Nullable Component display, @Nullable NumberFormat numberFormat)
    implements Packet<ClientGamePacketListener> {
    public ClientboundSetScorePacket(FriendlyByteBuf param0) {
        this(
            param0.readUtf(),
            param0.readUtf(),
            param0.readVarInt(),
            param0.readNullable(FriendlyByteBuf::readComponentTrusted),
            param0.readNullable(NumberFormatTypes::readFromStream)
        );
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.owner);
        param0.writeUtf(this.objectiveName);
        param0.writeVarInt(this.score);
        param0.writeNullable(this.display, FriendlyByteBuf::writeComponent);
        param0.writeNullable(this.numberFormat, NumberFormatTypes::writeToStream);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetScore(this);
    }
}
