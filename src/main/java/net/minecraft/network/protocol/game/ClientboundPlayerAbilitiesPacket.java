package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.player.Abilities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundPlayerAbilitiesPacket implements Packet<ClientGamePacketListener> {
    private boolean invulnerable;
    private boolean isFlying;
    private boolean canFly;
    private boolean instabuild;
    private float flyingSpeed;
    private float walkingSpeed;

    public ClientboundPlayerAbilitiesPacket() {
    }

    public ClientboundPlayerAbilitiesPacket(Abilities param0) {
        this.invulnerable = param0.invulnerable;
        this.isFlying = param0.flying;
        this.canFly = param0.mayfly;
        this.instabuild = param0.instabuild;
        this.flyingSpeed = param0.getFlyingSpeed();
        this.walkingSpeed = param0.getWalkingSpeed();
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        byte var0 = param0.readByte();
        this.invulnerable = (var0 & 1) != 0;
        this.isFlying = (var0 & 2) != 0;
        this.canFly = (var0 & 4) != 0;
        this.instabuild = (var0 & 8) != 0;
        this.flyingSpeed = param0.readFloat();
        this.walkingSpeed = param0.readFloat();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
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

    @OnlyIn(Dist.CLIENT)
    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isFlying() {
        return this.isFlying;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canFly() {
        return this.canFly;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canInstabuild() {
        return this.instabuild;
    }

    @OnlyIn(Dist.CLIENT)
    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    @OnlyIn(Dist.CLIENT)
    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }
}
