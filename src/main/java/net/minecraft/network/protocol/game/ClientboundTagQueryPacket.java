package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundTagQueryPacket implements Packet<ClientGamePacketListener> {
    private final int transactionId;
    @Nullable
    private final CompoundTag tag;

    public ClientboundTagQueryPacket(int param0, @Nullable CompoundTag param1) {
        this.transactionId = param0;
        this.tag = param1;
    }

    public ClientboundTagQueryPacket(FriendlyByteBuf param0) {
        this.transactionId = param0.readVarInt();
        this.tag = param0.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        param0.writeNbt(this.tag);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTagQueryPacket(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    public CompoundTag getTag() {
        return this.tag;
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}
