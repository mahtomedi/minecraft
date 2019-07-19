package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
    private BlockPos pos;
    private int type;
    private CompoundTag tag;

    public ClientboundBlockEntityDataPacket() {
    }

    public ClientboundBlockEntityDataPacket(BlockPos param0, int param1, CompoundTag param2) {
        this.pos = param0;
        this.type = param1;
        this.tag = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
        this.type = param0.readUnsignedByte();
        this.tag = param0.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);
        param0.writeByte((byte)this.type);
        param0.writeNbt(this.tag);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockEntityData(this);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }

    @OnlyIn(Dist.CLIENT)
    public int getType() {
        return this.type;
    }

    @OnlyIn(Dist.CLIENT)
    public CompoundTag getTag() {
        return this.tag;
    }
}
