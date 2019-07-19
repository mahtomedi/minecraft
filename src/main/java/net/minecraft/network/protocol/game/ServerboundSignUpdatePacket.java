package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSignUpdatePacket implements Packet<ServerGamePacketListener> {
    private BlockPos pos;
    private String[] lines;

    public ServerboundSignUpdatePacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSignUpdatePacket(BlockPos param0, Component param1, Component param2, Component param3, Component param4) {
        this.pos = param0;
        this.lines = new String[]{param1.getString(), param2.getString(), param3.getString(), param4.getString()};
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
        this.lines = new String[4];

        for(int var0 = 0; var0 < 4; ++var0) {
            this.lines[var0] = param0.readUtf(384);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);

        for(int var0 = 0; var0 < 4; ++var0) {
            param0.writeUtf(this.lines[var0]);
        }

    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSignUpdate(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public String[] getLines() {
        return this.lines;
    }
}
