package net.minecraft.network.protocol.game;

import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class ClientboundUpdateMobEffectPacket implements Packet<ClientGamePacketListener> {
    private static final int FLAG_AMBIENT = 1;
    private static final int FLAG_VISIBLE = 2;
    private static final int FLAG_SHOW_ICON = 4;
    private final int entityId;
    private final MobEffect effect;
    private final byte effectAmplifier;
    private final int effectDurationTicks;
    private final byte flags;
    @Nullable
    private final MobEffectInstance.FactorData factorData;

    public ClientboundUpdateMobEffectPacket(int param0, MobEffectInstance param1) {
        this.entityId = param0;
        this.effect = param1.getEffect();
        this.effectAmplifier = (byte)(param1.getAmplifier() & 0xFF);
        this.effectDurationTicks = param1.getDuration();
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
        this.factorData = param1.getFactorData().orElse(null);
    }

    public ClientboundUpdateMobEffectPacket(FriendlyByteBuf param0) {
        this.entityId = param0.readVarInt();
        this.effect = param0.readById(BuiltInRegistries.MOB_EFFECT);
        this.effectAmplifier = param0.readByte();
        this.effectDurationTicks = param0.readVarInt();
        this.flags = param0.readByte();
        this.factorData = param0.readNullable(param0x -> param0x.readWithCodecTrusted(NbtOps.INSTANCE, MobEffectInstance.FactorData.CODEC));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeVarInt(this.entityId);
        param0.writeId(BuiltInRegistries.MOB_EFFECT, this.effect);
        param0.writeByte(this.effectAmplifier);
        param0.writeVarInt(this.effectDurationTicks);
        param0.writeByte(this.flags);
        param0.writeNullable(this.factorData, (param0x, param1) -> param0x.writeWithCodec(NbtOps.INSTANCE, MobEffectInstance.FactorData.CODEC, param1));
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleUpdateMobEffect(this);
    }

    public int getEntityId() {
        return this.entityId;
    }

    public MobEffect getEffect() {
        return this.effect;
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

    @Nullable
    public MobEffectInstance.FactorData getFactorData() {
        return this.factorData;
    }
}
