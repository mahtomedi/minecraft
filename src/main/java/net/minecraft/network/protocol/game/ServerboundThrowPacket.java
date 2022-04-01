package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.Vec3;

public class ServerboundThrowPacket implements Packet<ServerGamePacketListener> {
    private final Vec3 facing;

    public ServerboundThrowPacket(Vec3 param0) {
        this.facing = param0;
    }

    public ServerboundThrowPacket(FriendlyByteBuf param0) {
        this.facing = new Vec3(param0.readDouble(), param0.readDouble(), param0.readDouble());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeDouble(this.facing.x());
        param0.writeDouble(this.facing.y());
        param0.writeDouble(this.facing.z());
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleThrow(this);
    }

    public Vec3 getFacing() {
        return this.facing;
    }
}
