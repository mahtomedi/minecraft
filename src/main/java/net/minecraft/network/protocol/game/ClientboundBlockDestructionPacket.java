package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final BlockPos pos;
    private final int progress;

    public ClientboundBlockDestructionPacket(int param0, BlockPos param1, int param2) {
        this.id = param0;
        this.pos = param1;
        this.progress = param2;
    }

    public ClientboundBlockDestructionPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.pos = param0.readBlockPos();
        this.progress = param0.readUnsignedByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.progress);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockDestruction(this);
    }

    public int getId() {
        return this.id;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getProgress() {
        return this.progress;
    }
}
