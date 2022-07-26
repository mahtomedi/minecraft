package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundPlayerInputPacket implements Packet<ServerGamePacketListener> {
    private static final int FLAG_JUMPING = 1;
    private static final int FLAG_SHIFT_KEY_DOWN = 2;
    private final float xxa;
    private final float zza;
    private final boolean isJumping;
    private final boolean isShiftKeyDown;

    public ServerboundPlayerInputPacket(float param0, float param1, boolean param2, boolean param3) {
        this.xxa = param0;
        this.zza = param1;
        this.isJumping = param2;
        this.isShiftKeyDown = param3;
    }

    public ServerboundPlayerInputPacket(FriendlyByteBuf param0) {
        this.xxa = param0.readFloat();
        this.zza = param0.readFloat();
        byte var0 = param0.readByte();
        this.isJumping = (var0 & 1) > 0;
        this.isShiftKeyDown = (var0 & 2) > 0;
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeFloat(this.xxa);
        param0.writeFloat(this.zza);
        byte var0 = 0;
        if (this.isJumping) {
            var0 = (byte)(var0 | 1);
        }

        if (this.isShiftKeyDown) {
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

    public boolean isShiftKeyDown() {
        return this.isShiftKeyDown;
    }
}
