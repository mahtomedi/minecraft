package net.minecraft.client.multiplayer;

import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ProfileKeyPairManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path PROFILE_KEY_PAIR_DIR = Path.of("profilekeys");
    private final Path profileKeyPairPath;
    private final CompletableFuture<ProfileKeyPair> profileKeyPair;

    public ProfileKeyPairManager(UserApiService param0, UUID param1, Path param2) {
        this.profileKeyPairPath = param2.resolve(PROFILE_KEY_PAIR_DIR).resolve(param1 + ".json");
        this.profileKeyPair = this.readOrFetchProfileKeyPair(param0);
    }

    private CompletableFuture<ProfileKeyPair> readOrFetchProfileKeyPair(UserApiService param0) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<ProfileKeyPair> var0 = this.readProfileKeyPair().filter(param0x -> !param0x.publicKey().data().hasExpired());
            if (var0.isPresent() && !var0.get().dueRefresh()) {
                return var0.get();
            } else {
                try {
                    ProfileKeyPair var1 = this.fetchProfileKeyPair(param0);
                    this.writeProfileKeyPair(var1);
                    return var1;
                } catch (CryptException | MinecraftClientException | IOException var4) {
                    LOGGER.error("Failed to retrieve profile key pair", (Throwable)var4);
                    this.writeProfileKeyPair(null);
                    return var0.orElse(null);
                }
            }
        }, Util.backgroundExecutor());
    }

    private Optional<ProfileKeyPair> readProfileKeyPair() {
        if (Files.notExists(this.profileKeyPairPath)) {
            return Optional.empty();
        } else {
            try {
                Optional var2;
                try (BufferedReader var0 = Files.newBufferedReader(this.profileKeyPairPath)) {
                    var2 = ProfileKeyPair.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(var0)).result();
                }

                return var2;
            } catch (Exception var6) {
                LOGGER.error("Failed to read profile key pair file {}", this.profileKeyPairPath, var6);
                return Optional.empty();
            }
        }
    }

    private void writeProfileKeyPair(@Nullable ProfileKeyPair param0) {
        try {
            Files.deleteIfExists(this.profileKeyPairPath);
        } catch (IOException var3) {
            LOGGER.error("Failed to delete profile key pair file {}", this.profileKeyPairPath, var3);
        }

        if (param0 != null) {
            ProfileKeyPair.CODEC.encodeStart(JsonOps.INSTANCE, param0).result().ifPresent(param0x -> {
                try {
                    Files.createDirectories(this.profileKeyPairPath.getParent());
                    Files.writeString(this.profileKeyPairPath, param0x.toString());
                } catch (Exception var3x) {
                    LOGGER.error("Failed to write profile key pair file {}", this.profileKeyPairPath, var3x);
                }

            });
        }
    }

    private ProfileKeyPair fetchProfileKeyPair(UserApiService param0) throws CryptException, IOException {
        KeyPairResponse var0 = param0.getKeyPair();
        if (var0 != null) {
            ProfilePublicKey.Data var1 = new ProfilePublicKey.Data(Instant.parse(var0.getExpiresAt()), var0.getPublicKey(), var0.getPublicKeySignature());
            return new ProfileKeyPair(
                Crypt.stringToPemRsaPrivateKey(var0.getPrivateKey()), ProfilePublicKey.parseTrusted(var1), Instant.parse(var0.getRefreshedAfter())
            );
        } else {
            throw new IOException("Could not retrieve profile key pair");
        }
    }

    @Nullable
    public Signature createSignature() throws GeneralSecurityException {
        PrivateKey var0 = this.profilePrivateKey();
        if (var0 == null) {
            return null;
        } else {
            Signature var1 = Signature.getInstance("SHA256withRSA");
            var1.initSign(var0);
            return var1;
        }
    }

    public Optional<ProfilePublicKey.Data> profilePublicKeyData() {
        ProfilePublicKey var0 = this.profilePublicKey();
        return Optional.ofNullable(var0).map(ProfilePublicKey::data);
    }

    @Nullable
    public ProfilePublicKey profilePublicKey() {
        ProfileKeyPair var0 = this.profileKeyPair.join();
        return var0 != null ? var0.publicKey() : null;
    }

    @Nullable
    private PrivateKey profilePrivateKey() {
        ProfileKeyPair var0 = this.profileKeyPair.join();
        return var0 != null ? var0.privateKey() : null;
    }
}