package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundAcceptTeleportationPacket implements Packet<ServerGamePacketListener> {
    private int id;

    public ServerboundAcceptTeleportationPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundAcceptTeleportationPacket(int param0) {
        this.id = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.id = param0.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeVarInt(this.id);
    }

    public void handle(ServerGamePacketListener param0) {
        param0.handleAcceptTeleportPacket(this);
    }

    public int getId() {
        return this.id;
    }
}
