package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundEntityTagQuery implements Packet<ServerGamePacketListener> {
    private final int transactionId;
    private final int entityId;

    @OnlyIn(Dist.CLIENT)
    public ServerboundEntityTagQuery(int param0, int param1) {
        this.transactionId = param0;
        this.entityId = param1;
    }

    public ServerboundEntityTagQuery(FriendlyByteBuf param0) {
        this.transactionId = param0.readVarInt();
        this.entityId = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        param0.writeVarInt(this.entityId);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleEntityTagQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public int getEntityId() {
        return this.entityId;
    }
}
