package net.minecraft.world.entity.player;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecurePublicKeyException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.InvalidException;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.MissingException;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.ExtraCodecs;

public record ProfilePublicKey(Instant expiresAt, String keyString, String signature) {
    public static final Codec<ProfilePublicKey> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.INSTANT_ISO8601.fieldOf("expires_at").forGetter(ProfilePublicKey::expiresAt),
                    Codec.STRING.fieldOf("key").forGetter(ProfilePublicKey::keyString),
                    Codec.STRING.fieldOf("signature").forGetter(ProfilePublicKey::signature)
                )
                .apply(param0, ProfilePublicKey::new)
    );
    private static final String PROFILE_PROPERTY_KEY = "publicKey";

    public ProfilePublicKey(Pair<Instant, String> param0, String param1) {
        this(param0.getFirst(), param0.getSecond(), param1);
    }

    public static Optional<ProfilePublicKey> parseFromGameProfile(GameProfile param0) {
        Property var0 = Iterables.getFirst(param0.getProperties().get("publicKey"), null);
        if (var0 == null) {
            return Optional.empty();
        } else {
            String var1 = var0.getValue();
            String var2 = var0.getSignature();
            return !Strings.isNullOrEmpty(var1) && !Strings.isNullOrEmpty(var2)
                ? parsePublicKeyString(var1).map(param1 -> new ProfilePublicKey(param1, var2))
                : Optional.empty();
        }
    }

    public GameProfile fillGameProfile(GameProfile param0) {
        param0.getProperties().put("publicKey", this.property());
        return param0;
    }

    public ProfilePublicKey.Trusted verify(MinecraftSessionService param0) throws InsecurePublicKeyException, CryptException {
        if (Strings.isNullOrEmpty(this.keyString)) {
            throw new MissingException();
        } else {
            String var0 = param0.getSecurePropertyValue(this.property());
            if (!(this.expiresAt.toEpochMilli() + this.keyString).equals(var0)) {
                throw new InvalidException("Invalid profile public key signature");
            } else {
                Pair<Instant, String> var1 = parsePublicKeyString(var0).orElseThrow(() -> new InvalidException("Invalid profile public key"));
                if (var1.getFirst().isBefore(Instant.now())) {
                    throw new InvalidException("Expired profile public key");
                } else {
                    PublicKey var2 = Crypt.stringToRsaPublicKey(var1.getSecond());
                    return new ProfilePublicKey.Trusted(var2, this);
                }
            }
        }
    }

    private static Optional<Pair<Instant, String>> parsePublicKeyString(String param0) {
        int var0 = param0.indexOf("-----BEGIN RSA PUBLIC KEY-----");

        long var1;
        try {
            var1 = Long.parseLong(param0.substring(0, var0));
        } catch (NumberFormatException var5) {
            return Optional.empty();
        }

        return Optional.of(Pair.of(Instant.ofEpochMilli(var1), param0.substring(var0)));
    }

    public Property property() {
        return new Property("publicKey", this.expiresAt.toEpochMilli() + this.keyString, this.signature);
    }

    public boolean hasExpired() {
        return this.expiresAt.isBefore(Instant.now());
    }

    public static record Trusted(PublicKey key, ProfilePublicKey data) {
        public Signature verifySignature() throws CryptException {
            try {
                Signature var0 = Signature.getInstance("SHA1withRSA");
                var0.initVerify(this.key);
                return var0;
            } catch (GeneralSecurityException var2) {
                throw new CryptException(var2);
            }
        }
    }
}
