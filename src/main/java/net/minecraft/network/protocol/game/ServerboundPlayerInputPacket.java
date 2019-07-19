package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundPlayerInputPacket implements Packet<ServerGamePacketListener> {
    private float xxa;
    private float zza;
    private boolean isJumping;
    private boolean isSneaking;

    public ServerboundPlayerInputPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundPlayerInputPacket(float param0, float param1, boolean param2, boolean param3) {
        this.xxa = param0;
        this.zza = param1;
        this.isJumping = param2;
        this.isSneaking = param3;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.xxa = param0.readFloat();
        this.zza = param0.readFloat();
        byte var0 = param0.readByte();
        this.isJumping = (var0 & 1) > 0;
        this.isSneaking = (var0 & 2) > 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeFloat(this.xxa);
        param0.writeFloat(this.zza);
        byte var0 = 0;
        if (this.isJumping) {
            var0 = (byte)(var0 | 1);
        }

        if (this.isSneaking) {
            var0 = (byte)(var0 | 2);
        }

        param0.writeByte(var0);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handlePlayerInput(this);
    }

    public float getXxa() {
        return this.xxa;
    }

    public float getZza() {
        return this.zza;
    }

    public boolean isJumping() {
        return this.isJumping;
    }

    public boolean isSneaking() {
        return this.isSneaking;
    }
}
