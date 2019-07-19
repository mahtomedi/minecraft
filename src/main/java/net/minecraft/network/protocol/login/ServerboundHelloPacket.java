package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ServerboundHelloPacket implements Packet<ServerLoginPacketListener> {
    private GameProfile gameProfile;

    public ServerboundHelloPacket() {
    }

    public ServerboundHelloPacket(GameProfile param0) {
        this.gameProfile = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.gameProfile = new GameProfile(null, param0.readUtf(16));
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.gameProfile.getName());
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleHello(this);
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
}
