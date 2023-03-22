package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundOpenSignEditorPacket implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;
    private final boolean isFrontText;

    public ClientboundOpenSignEditorPacket(BlockPos param0, boolean param1) {
        this.pos = param0;
        this.isFrontText = param1;
    }

    public ClientboundOpenSignEditorPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.isFrontText = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeBoolean(this.isFrontText);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleOpenSignEditor(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public boolean isFrontText() {
        return this.isFrontText;
    }
}
