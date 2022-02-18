package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
    private static final int FLAG_AMBIENT = 1;
    private static final int FLAG_VISIBLE = 2;
    private static final int FLAG_SHOW_ICON = 4;
    private final int entityId;
    private final int effectId;
    private final byte effectAmplifier;
    private final int effectDurationTicks;
    private final byte flags;

    public ClientboundUpdateMobEffectPacket(int param0, MobEffectInstance param1) {
        this.entityId = param0;
        this.effectId = MobEffect.getId(param1.getEffect());
        this.effectAmplifier = (byte)(param1.getAmplifier() & 0xFF);
        if (param1.getDuration() > 32767) {
            this.effectDurationTicks = 32767;
        } else {
            this.effectDurationTicks = param1.getDuration();
        }

        byte var0 = 0;
        if (param1.isAmbient()) {
            var0 = (byte)(var0 | 1);
        }

        if (param1.isVisible()) {
            var0 = (byte)(var0 | 2);
        }

        if (param1.showIcon()) {
            var0 = (byte)(var0 | 4);
        }

        this.flags = var0;
    }

    public ClientboundUpdateMobEffectPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
        this.effectId = param0.readVarInt();
        this.effectAmplifier = param0.readByte();
        this.effectDurationTicks = param0.readVarInt();
        this.flags = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeVarInt(this.effectId);
        param0.writeByte(this.effectAmplifier);
        param0.writeVarInt(this.effectDurationTicks);
        param0.writeByte(this.flags);
    }

    public boolean isSuperLongDuration() {
        return this.effectDurationTicks == 32767;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateMobEffect(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public int getEffectId() {
        return this.effectId;
    }

    public byte getEffectAmplifier() {
        return this.effectAmplifier;
    }

    public int getEffectDurationTicks() {
        return this.effectDurationTicks;
    }

    public boolean isEffectVisible() {
        return (this.flags & 2) == 2;
    }

    public boolean isEffectAmbient() {
        return (this.flags & 1) == 1;
    }

    public boolean effectShowsIcon() {
        return (this.flags & 4) == 4;
    }
}
