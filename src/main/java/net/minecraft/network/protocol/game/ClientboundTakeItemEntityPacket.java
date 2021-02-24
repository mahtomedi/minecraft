package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundTakeItemEntityPacket implements Packet<ClientGamePacketListener> {
    private final int itemId;
    private final int playerId;
    private final int amount;

    public ClientboundTakeItemEntityPacket(int param0, int param1, int param2) {
        this.itemId = param0;
        this.playerId = param1;
        this.amount = param2;
    }

    public ClientboundTakeItemEntityPacket(FriendlyByteBuf param0) {
        this.itemId = param0.readVarInt();
        this.playerId = param0.readVarInt();
        this.amount = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.itemId);
        param0.writeVarInt(this.playerId);
        param0.writeVarInt(this.amount);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTakeItemEntity(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getItemId() {
        return this.itemId;
    }

    @OnlyIn(Dist.CLIENT)
    public int getPlayerId() {
        return this.playerId;
    }

    @OnlyIn(Dist.CLIENT)
    public int getAmount() {
        return this.amount;
    }
}
