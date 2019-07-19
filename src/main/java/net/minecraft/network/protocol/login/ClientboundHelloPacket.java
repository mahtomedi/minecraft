package net.minecraft.network.protocol.login;

import java.io.IOException;
import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
    private String serverId;
    private PublicKey publicKey;
    private byte[] nonce;

    public ClientboundHelloPacket() {
    }

    public ClientboundHelloPacket(String param0, PublicKey param1, byte[] param2) {
        this.serverId = param0;
        this.publicKey = param1;
        this.nonce = param2;
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.serverId = param0.readUtf(20);
        this.publicKey = Crypt.byteToPublicKey(param0.readByteArray());
        this.nonce = param0.readByteArray();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeUtf(this.serverId);
        param0.writeByteArray(this.publicKey.getEncoded());
        param0.writeByteArray(this.nonce);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleHello(this);
    }

    @OnlyIn(Dist.CLIENT)
    public String getServerId() {
        return this.serverId;
    }

    @OnlyIn(Dist.CLIENT)
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    @OnlyIn(Dist.CLIENT)
    public byte[] getNonce() {
        return this.nonce;
    }
}
