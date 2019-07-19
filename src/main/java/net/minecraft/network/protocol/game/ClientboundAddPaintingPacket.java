package net.minecraft.network.protocol.game;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundAddPaintingPacket implements Packet<ClientGamePacketListener> {
    private int id;
    private UUID uuid;
    private BlockPos pos;
    private Direction direction;
    private int motive;

    public ClientboundAddPaintingPacket() {
    }

    public ClientboundAddPaintingPacket(Painting param0) {
        this.id = param0.getId();
        this.uuid = param0.getUUID();
        this.pos = param0.getPos();
        this.direction = param0.getDirection();
        this.motive = Registry.MOTIVE.getId(param0.motive);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
        this.uuid = param0.readUUID();
        this.motive = param0.readVarInt();
        this.pos = param0.readBlockPos();
        this.direction = Direction.from2DDataValue(param0.readUnsignedByte());
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
        param0.writeUUID(this.uuid);
        param0.writeVarInt(this.motive);
        param0.writeBlockPos(this.pos);
        param0.writeByte(this.direction.get2DDataValue());
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleAddPainting(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public UUID getUUID() {
        return this.uuid;
    }

    @OnlyIn(Dist.CLIENT)
    public BlockPos getPos() {
        return this.pos;
    }

    @OnlyIn(Dist.CLIENT)
    public Direction getDirection() {
        return this.direction;
    }

    @OnlyIn(Dist.CLIENT)
    public Motive getMotive() {
        return Registry.MOTIVE.byId(this.motive);
    }
}
