package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundSetTimePacket implements Packet<ClientGamePacketListener> {
    private long gameTime;
    private long dayTime;

    public ClientboundSetTimePacket() {
    }

    public ClientboundSetTimePacket(long param0, long param1, boolean param2) {
        this.gameTime = param0;
        this.dayTime = param1;
        if (!param2) {
            this.dayTime = -this.dayTime;
            if (this.dayTime == 0L) {
                this.dayTime = -1L;
            }
        }

    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.gameTime = param0.readLong();
        this.dayTime = param0.readLong();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeLong(this.gameTime);
        param0.writeLong(this.dayTime);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleSetTime(this);
    }

    @OnlyIn(Dist.CLIENT)
    public long getGameTime() {
        return this.gameTime;
    }

    @OnlyIn(Dist.CLIENT)
    public long getDayTime() {
        return this.dayTime;
    }
}
