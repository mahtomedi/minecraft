package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundJigsawGeneratePacket implements Packet<ServerGamePacketListener> {
    private BlockPos pos;
    private int levels;
    private boolean keepJigsaws;

    public ServerboundJigsawGeneratePacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundJigsawGeneratePacket(BlockPos param0, int param1, boolean param2) {
        this.pos = param0;
        this.levels = param1;
        this.keepJigsaws = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
        this.levels = param0.readVarInt();
        this.keepJigsaws = param0.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
