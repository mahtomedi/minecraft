package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundSignUpdatePacket implements Packet<ServerGamePacketListener> {
    private static final int MAX_STRING_LENGTH = 384;
    private final BlockPos pos;
    private final String[] lines;

    public ServerboundSignUpdatePacket(BlockPos param0, String param1, String param2, String param3, String param4) {
        this.pos = param0;
        this.lines = new String[]{param1, param2, param3, param4};
    }

    public ServerboundSignUpdatePacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.lines = new String[4];

        for(int var0 = 0; var0 < 4; ++var0) {
            this.lines[var0] = param0.readUtf(384);
        }

    }

    @Override
    public void write(FriendlyByteBuf param0) {
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
