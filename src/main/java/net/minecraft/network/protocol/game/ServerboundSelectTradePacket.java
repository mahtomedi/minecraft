package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSelectTradePacket implements Packet<ServerGamePacketListener> {
    private final int item;

    public ServerboundSelectTradePacket(int param0) {
        this.item = param0;
    }

    public ServerboundSelectTradePacket(FriendlyByteBuf param0) {
        this.item = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.item);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSelectTrade(this);
    }

    public int getItem() {
        return this.item;
    }
}
