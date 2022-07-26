package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerActionPacket implements Packet<ServerGamePacketListener> {
    private final BlockPos pos;
    private final Direction direction;
    private final ServerboundPlayerActionPacket.Action action;
    private final int sequence;

    public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action param0, BlockPos param1, Direction param2, int param3) {
        this.action = param0;
        this.pos = param1.immutable();
        this.direction = param2;
        this.sequence = param3;
    }

    public ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action param0, BlockPos param1, Direction param2) {
        this(param0, param1, param2, 0);
    }

    public ServerboundPlayerActionPacket(FriendlyByteBuf param0) {
        this.action = param0.readEnum(ServerboundPlayerActionPacket.Action.class);
        this.pos = param0.readBlockPos();
        this.direction = Direction.from3DDataValue(param0.readUnsignedByte());
        this.sequence = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeEnum(this.action);
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.direction.get3DDataValue());
        param0.writeVarInt(this.sequence);
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

    public int getSequence() {
        return this.sequence;
    }

    public static enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND;
    }
}
