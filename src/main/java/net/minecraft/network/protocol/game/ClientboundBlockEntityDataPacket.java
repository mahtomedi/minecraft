package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundBlockEntityDataPacket implements Packet<ClientGamePacketListener> {
    public static final int TYPE_MOB_SPAWNER = 1;
    public static final int TYPE_ADV_COMMAND = 2;
    public static final int TYPE_BEACON = 3;
    public static final int TYPE_SKULL = 4;
    public static final int TYPE_CONDUIT = 5;
    public static final int TYPE_BANNER = 6;
    public static final int TYPE_STRUCT_COMMAND = 7;
    public static final int TYPE_END_GATEWAY = 8;
    public static final int TYPE_SIGN = 9;
    public static final int TYPE_BED = 11;
    public static final int TYPE_JIGSAW = 12;
    public static final int TYPE_CAMPFIRE = 13;
    public static final int TYPE_BEEHIVE = 14;
    private final BlockPos pos;
    private final int type;
    private final CompoundTag tag;

    public ClientboundBlockEntityDataPacket(BlockPos param0, int param1, CompoundTag param2) {
        this.pos = param0;
        this.type = param1;
        this.tag = param2;
    }

    public ClientboundBlockEntityDataPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.type = param0.readUnsignedByte();
        this.tag = param0.readNbt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeByte((byte)this.type);
        param0.writeNbt(this.tag);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleBlockEntityData(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public int getType() {
        return this.type;
    }

    public CompoundTag getTag() {
        return this.tag;
    }
}
