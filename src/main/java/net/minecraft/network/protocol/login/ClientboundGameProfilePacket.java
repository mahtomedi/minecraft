package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundGameProfilePacket implements Packet<ClientLoginPacketListener> {
    private GameProfile gameProfile;

    public ClientboundGameProfilePacket() {
    }

    public ClientboundGameProfilePacket(GameProfile param0) {
        this.gameProfile = param0;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        String var0 = param0.readUtf(36);
        String var1 = param0.readUtf(16);
        UUID var2 = UUID.fromString(var0);
        this.gameProfile = new GameProfile(var2, var1);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        UUID var0 = this.gameProfile.getId();
        param0.writeUtf(var0 == null ? "" : var0.toString());
        param0.writeUtf(this.gameProfile.getName());
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleGameProfile(this);
    }

    @OnlyIn(Dist.CLIENT)
    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
}
