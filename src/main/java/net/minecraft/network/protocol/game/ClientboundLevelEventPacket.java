package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundLevelEventPacket implements Packet<ClientGamePacketListener> {
    private final int type;
    private final BlockPos pos;
    private final int data;
    private final boolean globalEvent;

    public ClientboundLevelEventPacket(int param0, BlockPos param1, int param2, boolean param3) {
        this.type = param0;
        this.pos = param1.immutable();
        this.data = param2;
        this.globalEvent = param3;
    }

    public ClientboundLevelEventPacket(FriendlyByteBuf param0) {
        this.type = param0.readInt();
        this.pos = param0.readBlockPos();
        this.data = param0.readInt();
        this.globalEvent = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
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
