package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundOpenSignEditorPacket implements Packet<ClientGamePacketListener> {
    private BlockPos pos;

    public ClientboundOpenSignEditorPacket() {
    }

    public ClientboundOpenSignEditorPacket(BlockPos param0) {
        this.pos = param0;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleOpenSignEditor(this);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }
}
