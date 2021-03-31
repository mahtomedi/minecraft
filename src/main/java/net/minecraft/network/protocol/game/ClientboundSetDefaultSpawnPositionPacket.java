package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetDefaultSpawnPositionPacket implements Packet<ClientGamePacketListener> {
    private final BlockPos pos;
    private final float angle;

    public ClientboundSetDefaultSpawnPositionPacket(BlockPos param0, float param1) {
        this.pos = param0;
        this.angle = param1;
    }

    public ClientboundSetDefaultSpawnPositionPacket(FriendlyByteBuf param0) {
        this.pos = param0.readBlockPos();
        this.angle = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeBlockPos(this.pos);
        param0.writeFloat(this.angle);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetSpawn(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public float getAngle() {
        return this.angle;
    }
}
