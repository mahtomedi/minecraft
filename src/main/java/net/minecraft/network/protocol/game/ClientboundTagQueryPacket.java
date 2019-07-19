package net.minecraft.network.protocol.game;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundTagQueryPacket implements Packet<ClientGamePacketListener> {
    private int transactionId;
    @Nullable
    private CompoundTag tag;

    public ClientboundTagQueryPacket() {
    }

    public ClientboundTagQueryPacket(int param0, @Nullable CompoundTag param1) {
        this.transactionId = param0;
        this.tag = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.transactionId = param0.readVarInt();
        this.tag = param0.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.transactionId);
        param0.writeNbt(this.tag);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleTagQueryPacket(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getTransactionId() {
        return this.transactionId;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public CompoundTag getTag() {
        return this.tag;
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}
