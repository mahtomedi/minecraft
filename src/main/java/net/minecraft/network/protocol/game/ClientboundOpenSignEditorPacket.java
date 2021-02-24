package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundOpenSignEditorPacket implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;

    public ClientboundOpenSignEditorPacket(BlockPos param0) {
        this.pos = param0;
    }

    public ClientboundOpenSignEditorPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleOpenSignEditor(this);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }
}
