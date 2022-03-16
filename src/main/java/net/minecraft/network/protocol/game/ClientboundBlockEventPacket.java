package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;

public class ClientboundBlockEventPacket implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;
    private final int b0;
    private final int b1;
    private final Block block;

    public ClientboundBlockEventPacket(BlockPos param0, Block param1, int param2, int param3) {
        this.pos = param0;
        this.block = param1;
        this.b0 = param2;
        this.b1 = param3;
    }

    public ClientboundBlockEventPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.b0 = param0.readUnsignedByte();
        this.b1 = param0.readUnsignedByte();
        this.block = param0.readById(Registry.BLOCK);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.b0);
        param0.writeByte(this.b1);
        param0.writeId(Registry.BLOCK, this.block);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockEvent(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getB0() {
        return this.b0;
    }

    public int getB1() {
        return this.b1;
    }

    public Block getBlock() {
        return this.block;
    }
}
