package net.minecraft.network.protocol.login;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
    private byte[] keybytes = new byte[0];
    private byte[] nonce = new byte[0];

    public ServerboundKeyPacket() {
    }

    @OnlyIn(Dist.CLIENT)
    public ServerboundKeyPacket(SecretKey param0, PublicKey param1, byte[] param2) throws CryptException {
        this.keybytes = Crypt.encryptUsingKey(param1, param0.getEncoded());
        this.nonce = Crypt.encryptUsingKey(param1, param2);
    }

    @Override
    public void read(FriendlyByteBuf param0) throws IOException {
        this.keybytes = param0.readByteArray();
        this.nonce = param0.readByteArray();
    }

    @Override
    public void write(FriendlyByteBuf param0) throws IOException {
        param0.writeByteArray(this.keybytes);
        param0.writeByteArray(this.nonce);
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleKey(this);
    }

    public SecretKey getSecretKey(PrivateKey param0) throws CryptException {
        return Crypt.decryptByteToSecretKey(param0, this.keybytes);
    }

    public byte[] getNonce(PrivateKey param0) throws CryptException {
        return Crypt.decryptUsingKey(param0, this.nonce);
    }
}
