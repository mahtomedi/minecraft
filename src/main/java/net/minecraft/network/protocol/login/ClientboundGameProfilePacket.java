package net.minecraft.network.protocol.login;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.core.SerializableUUID;
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
        int[] var0 = new int[4];

        for(int var1 = 0; var1 < var0.length; ++var1) {
            var0[var1] = param0.readInt();
        }

        UUID var2 = SerializableUUID.uuidFromIntArray(var0);
        String var3 = param0.readUtf(16);
        this.gameProfile = new GameProfile(var2, var3);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        for(int var0 : SerializableUUID.uuidToIntArray(this.gameProfile.getId())) {
            param0.writeInt(var0);
        }

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
