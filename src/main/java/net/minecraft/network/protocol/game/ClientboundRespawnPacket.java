package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public record ClientboundRespawnPacket(CommonPlayerSpawnInfo commonPlayerSpawnInfo, byte dataToKeep) implements Packet<ClientGamePacketListener> {
    public static final byte KEEP_ATTRIBUTES = 1;
    public static final byte KEEP_ENTITY_DATA = 2;
    public static final byte KEEP_ALL_DATA = 3;

    public ClientboundRespawnPacket(FriendlyByteBuf param0) {
        this(new CommonPlayerSpawnInfo(param0), param0.readByte());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        this.commonPlayerSpawnInfo.write(param0);
        param0.writeByte(this.dataToKeep);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRespawn(this);
    }

    public boolean shouldKeep(byte param0) {
        return (this.dataToKeep & param0) != 0;
    }
}
