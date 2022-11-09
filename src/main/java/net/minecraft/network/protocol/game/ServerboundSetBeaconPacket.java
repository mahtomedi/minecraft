package net.minecraft.network.protocol.game;

import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
    private final Optional<MobEffect> primary;
    private final Optional<MobEffect> secondary;

    public ServerboundSetBeaconPacket(Optional<MobEffect> param0, Optional<MobEffect> param1) {
        this.primary = param0;
        this.secondary = param1;
    }

    public ServerboundSetBeaconPacket(FriendlyByteBuf param0) {
        this.primary = param0.readOptional(param0x -> param0x.readById(BuiltInRegistries.MOB_EFFECT));
        this.secondary = param0.readOptional(param0x -> param0x.readById(BuiltInRegistries.MOB_EFFECT));
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeOptional(this.primary, (param0x, param1) -> param0x.writeId(BuiltInRegistries.MOB_EFFECT, param1));
        param0.writeOptional(this.secondary, (param0x, param1) -> param0x.writeId(BuiltInRegistries.MOB_EFFECT, param1));
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetBeaconPacket(this);
    }

    public Optional<MobEffect> getPrimary() {
        return this.primary;
    }

    public Optional<MobEffect> getSecondary() {
        return this.secondary;
    }
}
