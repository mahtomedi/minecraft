package net.minecraft.network.protocol.game;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.effect.MobEffect;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
    private final MobEffect primary;
    private final MobEffect secondary;

    public ServerboundSetBeaconPacket(MobEffect param0, MobEffect param1) {
        this.primary = param0;
        this.secondary = param1;
    }

    public ServerboundSetBeaconPacket(FriendlyByteBuf param0) {
        this.primary = param0.readById(Registry.MOB_EFFECT);
        this.secondary = param0.readById(Registry.MOB_EFFECT);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeId(Registry.MOB_EFFECT, this.primary);
        param0.writeId(Registry.MOB_EFFECT, this.secondary);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetBeaconPacket(this);
    }

    public MobEffect getPrimary() {
        return this.primary;
    }

    public MobEffect getSecondary() {
        return this.secondary;
    }
}
