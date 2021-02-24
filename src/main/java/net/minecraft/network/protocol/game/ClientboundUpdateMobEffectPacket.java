package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
    private final int entityId;
    private final byte effectId;
    private final byte effectAmplifier;
    private final int effectDurationTicks;
    private final byte flags;

    public ClientboundUpdateMobEffectPacket(int param0, MobEffectInstance param1) {
        this.entityId = param0;
        this.effectId = (byte)(MobEffect.getId(param1.getEffect()) & 0xFF);
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
        this.effectId = param0.readByte();
        this.effectAmplifier = param0.readByte();
        this.effectDurationTicks = param0.readVarInt();
        this.flags = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeByte(this.effectId);
        param0.writeByte(this.effectAmplifier);
        param0.writeVarInt(this.effectDurationTicks);
        param0.writeByte(this.flags);
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isSuperLongDuration() {
        return this.effectDurationTicks == 32767;
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateMobEffect(this);
    }

    @OnlyIn(Dist.CLIENT)
    public int getEntityId() {
        return this.entityId;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getEffectId() {
        return this.effectId;
    }

    @OnlyIn(Dist.CLIENT)
    public byte getEffectAmplifier() {
        return this.effectAmplifier;
    }

    @OnlyIn(Dist.CLIENT)
    public int getEffectDurationTicks() {
        return this.effectDurationTicks;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isEffectVisible() {
        return (this.flags & 2) == 2;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isEffectAmbient() {
        return (this.flags & 1) == 1;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean effectShowsIcon() {
        return (this.flags & 4) == 4;
    }
}
