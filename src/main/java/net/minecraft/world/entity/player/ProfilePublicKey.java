package net.minecraft.world.entity.player;

import com.google.common.base.Strings;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.MissingException;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;

public record ProfilePublicKey(ProfilePublicKey.Data data, PublicKey key) {
    public static final Codec<ProfilePublicKey> TRUSTED_CODEC = ProfilePublicKey.Data.CODEC.comapFlatMap(param0 -> {
        try {
            return DataResult.success(parseTrusted(param0));
        } catch (CryptException var2) {
            return DataResult.error("Malformed public key");
        }
    }, ProfilePublicKey::data);
    private static final String PROFILE_PROPERTY_KEY = "publicKey";

    public static ProfilePublicKey parseTrusted(ProfilePublicKey.Data param0) throws CryptException {
        return new ProfilePublicKey(param0, param0.parsedKey());
    }

    public static ProfilePublicKey parseAndValidate(MinecraftSessionService param0, ProfilePublicKey.Data param1) throws InsecurePublicKeyException, CryptException {
        if (Strings.isNullOrEmpty(param1.key())) {
            throw new MissingException();
        } else if (param1.hasExpired()) {
            throw new InvalidException("Expired profile public key");
        } else {
            String var0 = param0.getSecurePropertyValue(param1.signedProperty());
            if (!param1.signedPropertyValue().equals(var0)) {
                throw new InvalidException("Invalid profile public key signature");
            } else {
                return parseTrusted(param1);
            }
        }
    }

    public Signature verifySignature() throws CryptException {
        try {
            Signature var0 = Signature.getInstance("SHA256withRSA");
            var0.initVerify(this.key);
            return var0;
        } catch (GeneralSecurityException var2) {
            throw new CryptException(var2);
        }
    }

    public static record Data(Instant expiresAt, String key, String signature) {
        public static final Codec<ProfilePublicKey.Data> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(ProfilePublicKey.Data::expiresAt),
                        Codec.STRING.fieldOf("key").forGetter(ProfilePublicKey.Data::key),
                        Codec.STRING.fieldOf("signature").forGetter(ProfilePublicKey.Data::signature)
                    )
                    .apply(param0, ProfilePublicKey.Data::new)
        );

        public Property signedProperty() {
            return new Property("publicKey", this.signedPropertyValue(), this.signature);
        }

        public String signedPropertyValue() {
            return this.expiresAt.toEpochMilli() + this.key;
        }

        public PublicKey parsedKey() throws CryptException {
            return Crypt.stringToRsaPublicKey(this.key);
        }

        public boolean hasExpired() {
            return this.expiresAt.isBefore(Instant.now());
        }
    }
}
