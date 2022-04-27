package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
    private final GameProfile gameProfile;

    public ClientboundGameProfilePacket(GameProfile param0) {
        this.gameProfile = param0;
    }

    public ClientboundGameProfilePacket(FriendlyByteBuf param0) {
        this.gameProfile = param0.readGameProfile();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeGameProfile(this.gameProfile);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleGameProfile(this);
    }

    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
}
