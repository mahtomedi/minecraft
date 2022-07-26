package net.minecraft.network.protocol.login;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
    private final byte[] keybytes;
    private final byte[] encryptedChallenge;

    public ServerboundKeyPacket(SecretKey param0, PublicKey param1, byte[] param2) throws CryptException {
        this.keybytes = Crypt.encryptUsingKey(param1, param0.getEncoded());
        this.encryptedChallenge = Crypt.encryptUsingKey(param1, param2);
    }

    public ServerboundKeyPacket(FriendlyByteBuf param0) {
        this.keybytes = param0.readByteArray();
        this.encryptedChallenge = param0.readByteArray();
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByteArray(this.keybytes);
        param0.writeByteArray(this.encryptedChallenge);
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleKey(this);
    }

    public SecretKey getSecretKey(PrivateKey param0) throws CryptException {
        return Crypt.decryptByteToSecretKey(param0, this.keybytes);
    }

    public boolean isChallengeValid(byte[] param0, PrivateKey param1) {
        try {
            return Arrays.equals(param0, Crypt.decryptUsingKey(param1, this.encryptedChallenge));
        } catch (CryptException var4) {
            return false;
        }
    }
}
