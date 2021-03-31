package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
    private final BlockPos pos;
    private final int levels;
    private final boolean keepJigsaws;

    public ServerboundJigsawGeneratePacket(BlockPos param0, int param1, boolean param2) {
        this.pos = param0;
        this.levels = param1;
        this.keepJigsaws = param2;
    }

    public ServerboundJigsawGeneratePacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.levels = param0.readVarInt();
        this.keepJigsaws = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeVarInt(this.levels);
        param0.writeBoolean(this.keepJigsaws);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleJigsawGenerate(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int levels() {
        return this.levels;
    }

    public boolean keepJigsaws() {
        return this.keepJigsaws;
    }
}
