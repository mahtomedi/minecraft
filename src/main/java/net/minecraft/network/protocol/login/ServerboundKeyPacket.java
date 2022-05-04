package net.minecraft.network.protocol.login;

import com.mojang.datafixers.util.Either;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Optional;
import javax.crypto.SecretKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfilePublicKey;

public class ServerboundKeyPacket implements Packet<ServerLoginPacketListener> {
    private final byte[] keybytes;
    private final Either<byte[], Crypt.SaltSignaturePair> nonceOrSaltSignature;

    public ServerboundKeyPacket(SecretKey param0, PublicKey param1, byte[] param2) throws CryptException {
        this.keybytes = Crypt.encryptUsingKey(param1, param0.getEncoded());
        this.nonceOrSaltSignature = Either.left(Crypt.encryptUsingKey(param1, param2));
    }

    public ServerboundKeyPacket(SecretKey param0, PublicKey param1, long param2, byte[] param3) throws CryptException {
        this.keybytes = Crypt.encryptUsingKey(param1, param0.getEncoded());
        this.nonceOrSaltSignature = Either.right(new Crypt.SaltSignaturePair(param2, param3));
    }

    public ServerboundKeyPacket(FriendlyByteBuf param0) {
        this.keybytes = param0.readByteArray();
        this.nonceOrSaltSignature = param0.readEither(FriendlyByteBuf::readByteArray, Crypt.SaltSignaturePair::new);
    }

    @Override
    public void write(FriendlyByteBuf param0) {
        param0.writeByteArray(this.keybytes);
        param0.writeEither(this.nonceOrSaltSignature, FriendlyByteBuf::writeByteArray, (param0x, param1) -> param1.write(param0x));
    }

    public void handle(ServerLoginPacketListener param0) {
        param0.handleKey(this);
    }

    public SecretKey getSecretKey(PrivateKey param0) throws CryptException {
        return Crypt.decryptByteToSecretKey(param0, this.keybytes);
    }

    public boolean isChallengeSignatureValid(byte[] param0, ProfilePublicKey param1) {
        return this.nonceOrSaltSignature.map(param0x -> false, param2 -> {
            try {
                Signature var1x = param1.verifySignature();
                var1x.update(param0);
                var1x.update(param2.saltAsBytes());
                return var1x.verify(param2.signature());
            } catch (CryptException | GeneralSecurityException var4) {
                return false;
            }
        });
    }

    public boolean isNonceValid(byte[] param0, PrivateKey param1) {
        Optional<byte[]> var0 = this.nonceOrSaltSignature.left();

        try {
            return var0.isPresent() && Arrays.equals(param0, Crypt.decryptUsingKey(param1, var0.get()));
        } catch (CryptException var5) {
            return false;
        }
    }
}
