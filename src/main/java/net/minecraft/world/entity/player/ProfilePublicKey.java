package net.minecraft.world.entity.player;

import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.SignatureValidator;

public record ProfilePublicKey(ProfilePublicKey.Data data) {
    public static final Codec<ProfilePublicKey> TRUSTED_CODEC = ProfilePublicKey.Data.CODEC.comapFlatMap(param0 -> {
        try {
            return DataResult.success(createTrusted(param0));
        } catch (CryptException var2) {
            return DataResult.error("Malformed public key");
        }
    }, ProfilePublicKey::data);

    public static ProfilePublicKey createTrusted(ProfilePublicKey.Data param0) throws CryptException {
        return new ProfilePublicKey(param0);
    }

    public static ProfilePublicKey createValidated(SignatureValidator param0, ProfilePublicKey.Data param1) throws InsecurePublicKeyException, CryptException {
        if (param1.hasExpired()) {
            throw new InvalidException("Expired profile public key");
        } else if (!param1.validateSignature(param0)) {
            throw new InvalidException("Invalid profile public key signature");
        } else {
            return createTrusted(param1);
        }
    }

    public SignatureValidator createSignatureValidator() {
        return SignatureValidator.from(this.data.key, "SHA256withRSA");
    }

    public static record Data(Instant expiresAt, PublicKey key, byte[] keySignature) {
        private static final int MAX_KEY_SIGNATURE_SIZE = 4096;
        public static final Codec<ProfilePublicKey.Data> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(ProfilePublicKey.Data::expiresAt),
                        Crypt.PUBLIC_KEY_CODEC.fieldOf("key").forGetter(ProfilePublicKey.Data::key),
                        ExtraCodecs.BASE64_STRING.fieldOf("signature").forGetter(ProfilePublicKey.Data::keySignature)
                    )
                    .apply(param0, ProfilePublicKey.Data::new)
        );

        public Data(FriendlyByteBuf param0) {
            this(param0.readInstant(), param0.readPublicKey(), param0.readByteArray(4096));
        }

        public void write(FriendlyByteBuf param0) {
            param0.writeInstant(this.expiresAt);
            param0.writePublicKey(this.key);
            param0.writeByteArray(this.keySignature);
        }

        boolean validateSignature(SignatureValidator param0) {
            return param0.validate(this.signedPayload().getBytes(StandardCharsets.US_ASCII), this.keySignature);
        }

        private String signedPayload() {
            String var0 = Crypt.rsaPublicKeyToString(this.key);
            return this.expiresAt.toEpochMilli() + var0;
        }

        public boolean hasExpired() {
            return this.expiresAt.isBefore(Instant.now());
        }
    }
}
