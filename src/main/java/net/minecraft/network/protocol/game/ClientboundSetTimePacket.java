package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundSetTimePacket implements Packet<ClientGamePacketListener> {
    private final long gameTime;
    private final long dayTime;

    public ClientboundSetTimePacket(long param0, long param1, boolean param2) {
        this.gameTime = param0;
        long var0 = param1;
        if (!param2) {
            var0 = -param1;
            if (var0 == 0L) {
                var0 = -1L;
            }
        }

        this.dayTime = var0;
    }

    public ClientboundSetTimePacket(FriendlyByteBuf param0) {
        this.gameTime = param0.readLong();
        this.dayTime = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeLong(this.gameTime);
        param0.writeLong(this.dayTime);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetTime(this);
    }

    public long getGameTime() {
        return this.gameTime;
    }

    public long getDayTime() {
        return this.dayTime;
    }
}
