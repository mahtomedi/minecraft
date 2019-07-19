package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundSetBeaconPacket implements Packet<ServerGamePacketListener> {
    private int primary;
    private int secondary;

    public ServerboundSetBeaconPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundSetBeaconPacket(int param0, int param1) {
        this.primary = param0;
        this.secondary = param1;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.primary = param0.readVarInt();
        this.secondary = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.primary);
        param0.writeVarInt(this.secondary);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleSetBeaconPacket(this);
    }

    public int getPrimary() {
        return this.primary;
    }

    public int getSecondary() {
        return this.secondary;
    }
}
