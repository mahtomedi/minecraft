package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundSetCameraPacket implements Packet<ClientGamePacketListener> {
    private final int cameraId;

    public ClientboundSetCameraPacket(Entity param0) {
        this.cameraId = param0.getId();
    }

    public ClientboundSetCameraPacket(FriendlyByteBuf param0) {
        this.cameraId = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.cameraId);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetCamera(this);
    }

    @Nullable
    public Entity getEntity(Level param0) {
        return param0.getEntity(this.cameraId);
    }
}
