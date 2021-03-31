package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;

public class ClientboundPlayerAbilitiesPacket implements Packet<ClientGamePacketListener> {
    private static final int FLAG_INVULNERABLE = 1;
    private static final int FLAG_FLYING = 2;
    private static final int FLAG_CAN_FLY = 4;
    private static final int FLAG_INSTABUILD = 8;
    private final boolean invulnerable;
    private final boolean isFlying;
    private final boolean canFly;
    private final boolean instabuild;
    private final float flyingSpeed;
    private final float walkingSpeed;

    public ClientboundPlayerAbilitiesPacket(Abilities param0) {
        this.invulnerable = param0.invulnerable;
        this.isFlying = param0.flying;
        this.canFly = param0.mayfly;
        this.instabuild = param0.instabuild;
        this.flyingSpeed = param0.getFlyingSpeed();
        this.walkingSpeed = param0.getWalkingSpeed();
    }

    public ClientboundPlayerAbilitiesPacket(FriendlyByteBuf param0) {
        byte var0 = param0.readByte();
        this.invulnerable = (var0 & 1) != 0;
        this.isFlying = (var0 & 2) != 0;
        this.canFly = (var0 & 4) != 0;
        this.instabuild = (var0 & 8) != 0;
        this.flyingSpeed = param0.readFloat();
        this.walkingSpeed = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        byte var0 = 0;
        if (this.invulnerable) {
            var0 = (byte)(var0 | 1);
        }

        if (this.isFlying) {
            var0 = (byte)(var0 | 2);
        }

        if (this.canFly) {
            var0 = (byte)(var0 | 4);
        }

        if (this.instabuild) {
            var0 = (byte)(var0 | 8);
        }

        param0.writeByte(var0);
        param0.writeFloat(this.flyingSpeed);
        param0.writeFloat(this.walkingSpeed);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handlePlayerAbilities(this);
    }

    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public boolean canFly() {
        return this.canFly;
    }

    public boolean canInstabuild() {
        return this.instabuild;
    }

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }
}
