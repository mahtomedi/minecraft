package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSelectTradePacket implements Packet<ServerGamePacketListener> {
    private int item;

    public ServerboundSelectTradePacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSelectTradePacket(int param0) {
        this.item = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.item = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.item);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSelectTrade(this);
    }

    public int getItem() {
        return this.item;
    }
}
