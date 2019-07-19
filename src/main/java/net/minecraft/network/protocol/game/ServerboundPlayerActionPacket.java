package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
    private BlockPos pos;
    private Direction direction;
    private ServerboundPlayerActionPacket.Action action;

    public ServerboundPlayerActionPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action param0, BlockPos param1, Direction param2) {
        this.action = param0;
        this.pos = param1.immutable();
        this.direction = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.action = param0.readEnum(ServerboundPlayerActionPacket.Action.class);
        this.pos = param0.readBlockPos();
        this.direction = Direction.from3DDataValue(param0.readUnsignedByte());
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeEnum(this.action);
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.direction.get3DDataValue());
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePlayerAction(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public ServerboundPlayerActionPacket.Action getAction() {
        return this.action;
    }

    public static enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_HELD_ITEMS;
    }
}
