package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
    private int entityId;
    private byte effectId;
    private byte effectAmplifier;
    private int effectDurationTicks;
    private byte flags;

    public ClientboundUpdateMobEffectPacket() {
    }

    public ClientboundUpdateMobEffectPacket(int param0, MobEffectInstance param1) {
        this.entityId = param0;
        this.effectId = (byte)(MobEffect.getId(param1.getEffect()) & 0xFF);
        this.effectAmplifier = (byte)(param1.getAmplifier() & 0xFF);
        if (param1.getDuration() > 32767) {
            this.effectDurationTicks = 32767;
        } else {
            this.effectDurationTicks = param1.getDuration();
        }

        this.flags = 0;
        if (param1.isAmbient()) {
            this.flags = (byte)(this.flags | 1);
        }

        if (param1.isVisible()) {
            this.flags = (byte)(this.flags | 2);
        }

        if (param1.showIcon()) {
            this.flags = (byte)(this.flags | 4);
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.entityId = param0.readVarInt();
        this.effectId = param0.readByte();
        this.effectAmplifier = param0.readByte();
        this.effectDurationTicks = param0.readVarInt();
        this.flags = param0.readByte();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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
