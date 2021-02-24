package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public class ServerboundTeleportToEntityPacket implements Packet<ServerGamePacketListener> {
    private final UUID uuid;

    public ServerboundTeleportToEntityPacket(UUID param0) {
        this.uuid = param0;
    }

    public ServerboundTeleportToEntityPacket(FriendlyByteBuf param0) {
        this.uuid = param0.readUUID();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUUID(this.uuid);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleTeleportToEntityPacket(this);
    }

    @Nullable
    public Entity getEntity(ServerLevel param0) {
        return param0.getEntity(this.uuid);
    }
}
