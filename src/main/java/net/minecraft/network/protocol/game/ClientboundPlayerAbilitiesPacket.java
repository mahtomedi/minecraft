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
        this.setInvulnerable(param0.invulnerable);
        this.setFlying(param0.flying);
        this.setCanFly(param0.mayfly);
        this.setInstabuild(param0.instabuild);
        this.setFlyingSpeed(param0.getFlyingSpeed());
        this.setWalkingSpeed(param0.getWalkingSpeed());
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        byte var0 = param0.readByte();
        this.setInvulnerable((var0 & 1) > 0);
        this.setFlying((var0 & 2) > 0);
        this.setCanFly((var0 & 4) > 0);
        this.setInstabuild((var0 & 8) > 0);
        this.setFlyingSpeed(param0.readFloat());
        this.setWalkingSpeed(param0.readFloat());
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        byte var0 = 0;
        if (this.isInvulnerable()) {
            var0 = (byte)(var0 | 1);
        }

        if (this.isFlying()) {
            var0 = (byte)(var0 | 2);
        }

        if (this.canFly()) {
            var0 = (byte)(var0 | 4);
        }

        if (this.canInstabuild()) {
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

    public void setInvulnerable(boolean param0) {
        this.invulnerable = param0;
    }

    public boolean isFlying() {
        return this.isFlying;
    }

    public void setFlying(boolean param0) {
        this.isFlying = param0;
    }

    public boolean canFly() {
        return this.canFly;
    }

    public void setCanFly(boolean param0) {
        this.canFly = param0;
    }

    public boolean canInstabuild() {
        return this.instabuild;
    }

    public void setInstabuild(boolean param0) {
        this.instabuild = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float param0) {
        this.flyingSpeed = param0;
    }

    @OnlyIn(Dist.CLIENT)
    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float param0) {
        this.walkingSpeed = param0;
    }
}
