package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.LivingEntity;

public record ClientboundHurtAnimationPacket(int id, float yaw) implements Packet<ClientGamePacketListener> {
    public ClientboundHurtAnimationPacket(LivingEntity param0) {
        this(param0.getId(), param0.getHurtDir());
    }

    public ClientboundHurtAnimationPacket(FriendlyByteBuf param0) {
        this(param0.readVarInt(), param0.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.id);
        param0.writeFloat(this.yaw);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleHurtAnimation(this);
    }
}
