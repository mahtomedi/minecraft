package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundEntityTagQuery implements Packet<ServerGamePacketListener> {
    private int transactionId;
    private int entityId;

    public ServerboundEntityTagQuery() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundEntityTagQuery(int param0, int param1) {
        this.transactionId = param0;
        this.entityId = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.transactionId = param0.readVarInt();
        this.entityId = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
