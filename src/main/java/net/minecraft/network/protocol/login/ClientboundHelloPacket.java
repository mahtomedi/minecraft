package net.minecraft.network.protocol.login;

import java.security.PublicKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ClientboundHelloPacket implements Packet<ClientLoginPacketListener> {
    private final String serverId;
    private final byte[] publicKey;
    private final byte[] nonce;

    public ClientboundHelloPacket(String param0, byte[] param1, byte[] param2) {
        this.serverId = param0;
        this.publicKey = param1;
        this.nonce = param2;
    }

    public ClientboundHelloPacket(FriendlyByteBuf param0) {
        this.serverId = param0.readUtf(20);
        this.publicKey = param0.readByteArray();
        this.nonce = param0.readByteArray();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeUtf(this.serverId);
        param0.writeByteArray(this.publicKey);
        param0.writeByteArray(this.nonce);
    }

    public void handle(ClientLoginPacketListener param0) {
        param0.handleHello(this);
    }

    public String getServerId() {
        return this.serverId;
    }

    public PublicKey getPublicKey() throws CryptException {
        return Crypt.byteToPublicKey(this.publicKey);
    }

    public byte[] getNonce() {
        return this.nonce;
    }
}
