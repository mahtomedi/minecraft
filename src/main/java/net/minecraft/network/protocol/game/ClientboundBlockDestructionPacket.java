package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundBlockDestructionPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private BlockPos pos;
    private int progress;

    public ClientboundBlockDestructionPacket() {
    }

    public ClientboundBlockDestructionPacket(int param0, BlockPos param1, int param2) {
        this.id = param0;
        this.pos = param1;
        this.progress = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.pos = param0.readBlockPos();
        this.progress = param0.readUnsignedByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.progress);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockDestruction(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }

    @OnlyIn(Dist.CLIENT)
    public int getProgress() {
        return this.progress;
    }
}
