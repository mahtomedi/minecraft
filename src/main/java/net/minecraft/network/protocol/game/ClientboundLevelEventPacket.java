package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLevelEventPacket implements Packet<ClientGamePacketListener> {
    private int type;
    private BlockPos pos;
    private int data;
    private boolean globalEvent;

    public ClientboundLevelEventPacket() {
    }

    public ClientboundLevelEventPacket(int param0, BlockPos param1, int param2, boolean param3) {
        this.type = param0;
        this.pos = param1.immutable();
        this.data = param2;
        this.globalEvent = param3;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.type = param0.readInt();
        this.pos = param0.readBlockPos();
        this.data = param0.readInt();
        this.globalEvent = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeInt(this.type);
        param0.writeBlockPos(this.pos);
        param0.writeInt(this.data);
        param0.writeBoolean(this.globalEvent);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleLevelEvent(this);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isGlobalEvent() {
        return this.globalEvent;
    }

    @OnlyIn(Dist.CLIENT)
    public int getType() {
        return this.type;
    }

    @OnlyIn(Dist.CLIENT)
    public int getData() {
        return this.data;
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }
}
