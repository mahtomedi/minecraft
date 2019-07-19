package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundBlockEventPacket implements Packet<ClientGamePacketListener> {
    private BlockPos pos;
    private int b0;
    private int b1;
    private Block block;

    public ClientboundBlockEventPacket() {
    }

    public ClientboundBlockEventPacket(BlockPos param0, Block param1, int param2, int param3) {
        this.pos = param0;
        this.block = param1;
        this.b0 = param2;
        this.b1 = param3;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
        this.b0 = param0.readUnsignedByte();
        this.b1 = param0.readUnsignedByte();
        this.block = Registry.BLOCK.byId(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.b0);
        param0.writeByte(this.b1);
        param0.writeVarInt(Registry.BLOCK.getId(this.block));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockEvent(this);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }

    @OnlyIn(Dist.CLIENT)
    public int getB0() {
        return this.b0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getB1() {
        return this.b1;
    }

    @OnlyIn(Dist.CLIENT)
    public Block getBlock() {
        return this.block;
    }
}
