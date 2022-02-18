package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRemoveMobEffectPacket implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final MobEffect effect;

    public ClientboundRemoveMobEffectPacket(int param0, MobEffect param1) {
        this.entityId = param0;
        this.effect = param1;
    }

    public ClientboundRemoveMobEffectPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
        this.effect = MobEffect.byId(param0.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeVarInt(MobEffect.getId(this.effect));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleRemoveMobEffect(this);
    }

    @Nullable
    public Entity getEntity(Level param0) {
        return param0.getEntity(this.entityId);
    }

    @Nullable
    public MobEffect getEffect() {
        return this.effect;
    }
}
