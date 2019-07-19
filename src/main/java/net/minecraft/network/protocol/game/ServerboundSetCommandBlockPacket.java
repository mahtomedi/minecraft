package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSetCommandBlockPacket implements Packet<ServerGamePacketListener> {
    private BlockPos pos;
    private String command;
    private boolean trackOutput;
    private boolean conditional;
    private boolean automatic;
    private CommandBlockEntity.Mode mode;

    public ServerboundSetCommandBlockPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSetCommandBlockPacket(BlockPos param0, String param1, CommandBlockEntity.Mode param2, boolean param3, boolean param4, boolean param5) {
        this.pos = param0;
        this.command = param1;
        this.trackOutput = param3;
        this.conditional = param4;
        this.automatic = param5;
        this.mode = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.pos = param0.readBlockPos();
        this.command = param0.readUtf(32767);
        this.mode = param0.readEnum(CommandBlockEntity.Mode.class);
        int var0 = param0.readByte();
        this.trackOutput = (var0 & 1) != 0;
        this.conditional = (var0 & 2) != 0;
        this.automatic = (var0 & 4) != 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeBlockPos(this.pos);
        param0.writeUtf(this.command);
        param0.writeEnum(this.mode);
        int var0 = 0;
        if (this.trackOutput) {
            var0 |= 1;
        }

        if (this.conditional) {
            var0 |= 2;
        }

        if (this.automatic) {
            var0 |= 4;
        }

        param0.writeByte(var0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetCommandBlock(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public boolean isConditional() {
        return this.conditional;
    }

    public boolean isAutomatic() {
        return this.automatic;
    }

    public CommandBlockEntity.Mode getMode() {
        return this.mode;
    }
}
