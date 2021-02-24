package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundBlockEntityTagQuery implements Packet<ServerGamePacketListener> {
    private final int transactionId;
    private final BlockPos pos;

    @OnlyIn(Dist.CLIENT)
    public ServerboundBlockEntityTagQuery(int param0, BlockPos param1) {
        this.transactionId = param0;
        this.pos = param1;
    }

    public ServerboundBlockEntityTagQuery(FriendlyByteBuf param0) {
        this.transactionId = param0.readVarInt();
        this.pos = param0.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.transactionId);
        param0.writeBlockPos(this.pos);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleBlockEntityTagQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}
