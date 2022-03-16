package net.minecraft.network.protocol.game;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;

public class ClientboundAddPaintingPacket implements Packet<ClientGamePacketListener> {
    private final int id;
    private final UUID uuid;
    private final BlockPos pos;
    private final Direction direction;
    private final Motive motive;

    public ClientboundAddPaintingPacket(Painting param0) {
        this.id = param0.getId();
        this.uuid = param0.getUUID();
        this.pos = param0.getPos();
        this.direction = param0.getDirection();
        this.motive = param0.motive;
    }

    public ClientboundAddPaintingPacket(FriendlyByteBuf param0) {
        this.id = param0.readVarInt();
        this.uuid = param0.readUUID();
        this.motive = param0.readById(Registry.MOTIVE);
        this.pos = param0.readBlockPos();
        this.direction = Direction.from2DDataValue(param0.readUnsignedByte());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeUUID(this.uuid);
        param0.writeId(Registry.MOTIVE, this.motive);
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.direction.get2DDataValue());
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddPainting(this);
    }

    public int getId() {
        return this.id;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Motive getMotive() {
        return this.motive;
    }
}
