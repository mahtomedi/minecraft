package net.minecraft.network.protocol.game;

import java.io.IOException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundResourcePackPacket implements Packet<ClientGamePacketListener> {
    private String url;
    private String hash;

    public ClientboundResourcePackPacket() {
    }

    public ClientboundResourcePackPacket(String param0, String param1) {
        this.url = param0;
        this.hash = param1;
        if (param1.length() > 40) {
            throw new IllegalArgumentException("Hash is too long (max 40, was " + param1.length() + ")");
        }
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.url = param0.readUtf(32767);
        this.hash = param0.readUtf(40);
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.url);
        param0.writeUtf(this.hash);
    }

    public void handle(ClientGamePacketListener param0) {
        param0.handleResourcePack(this);
    }

    @OnlyIn(Dist.CLIENT)
    public String getUrl() {
        return this.url;
    }

    @OnlyIn(Dist.CLIENT)
    public String getHash() {
        return this.hash;
    }
}
