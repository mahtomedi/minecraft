package net.minecraft.world.entity.player;

import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;

public record ProfilePublicKey(ProfilePublicKey.Data data) {
    public static final Codec<ProfilePublicKey> TRUSTED_CODEC = ProfilePublicKey.Data.CODEC.comapFlatMap(param0 -> {
        try {
            return DataResult.success(createTrusted(param0));
        } catch (CryptException var2) {
            return DataResult.error("Malformed public key");
        }
    }, ProfilePublicKey::data);
    private static final String PROFILE_PROPERTY_KEY = "publicKey";

    public static ProfilePublicKey createTrusted(ProfilePublicKey.Data param0) throws CryptException {
        return new ProfilePublicKey(param0);
    }

    public static ProfilePublicKey createValidated(MinecraftSessionService param0, ProfilePublicKey.Data param1) throws InsecurePublicKeyException, CryptException {
        if (param1.hasExpired()) {
            throw new InvalidException("Expired profile public key");
        } else {
            String var0 = param0.getSecurePropertyValue(param1.signedKeyProperty());
            if (!param1.signedKeyPropertyValue().equals(var0)) {
                throw new InvalidException("Invalid profile public key signature");
            } else {
                return createTrusted(param1);
            }
        }
    }

    public Signature verifySignature() throws CryptException {
        try {
            Signature var0 = Signature.getInstance("SHA256withRSA");
            var0.initVerify(this.data.key());
            return var0;
        } catch (GeneralSecurityException var2) {
            throw new CryptException(var2);
        }
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

        Property signedKeyProperty() {
            String var0 = Base64.getEncoder().encodeToString(this.keySignature);
            return new Property("publicKey", this.signedKeyPropertyValue(), var0);
        }

        String signedKeyPropertyValue() {
            String var0 = Crypt.rsaPublicKeyToString(this.key);
            return this.expiresAt.toEpochMilli() + var0;
        }

        public boolean hasExpired() {
            return this.expiresAt.isBefore(Instant.now());
        }
    }
}
