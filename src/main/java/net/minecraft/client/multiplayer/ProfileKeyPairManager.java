package net.minecraft.client.multiplayer;

import com.google.common.base.Strings;
import com.google.gson.JsonParser;
import com.mojang.authlib.exceptions.MinecraftClientException;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.InsecurePublicKeyException.MissingException;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.network.chat.LocalChatSession;
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
    private final UserApiService userApiService;
    private final Path profileKeyPairPath;
    private CompletableFuture<Optional<ProfileKeyPair>> keyPair;

    public ProfileKeyPairManager(UserApiService param0, UUID param1, Path param2) {
        this.userApiService = param0;
        this.profileKeyPairPath = param2.resolve(PROFILE_KEY_PAIR_DIR).resolve(param1 + ".json");
        this.keyPair = CompletableFuture.<Optional<ProfileKeyPair>>supplyAsync(
                () -> this.readProfileKeyPair().filter(param0x -> !param0x.publicKey().data().hasExpired()), Util.backgroundExecutor()
            )
            .thenCompose(this::readOrFetchProfileKeyPair);
    }

    public CompletableFuture<LocalChatSession> prepareChatSession() {
        this.keyPair = this.keyPair.thenCompose(this::readOrFetchProfileKeyPair);
        return this.keyPair.thenApply(param0 -> LocalChatSession.create(param0.orElse(null)));
    }

    private CompletableFuture<Optional<ProfileKeyPair>> readOrFetchProfileKeyPair(Optional<ProfileKeyPair> param0x) {
        return CompletableFuture.supplyAsync(() -> {
            if (param0x.isPresent() && !param0x.get().dueRefresh()) {
                if (SharedConstants.IS_RUNNING_IN_IDE) {
                    return param0x;
                }

                this.writeProfileKeyPair(null);
            }

            try {
                ProfileKeyPair var1x = this.fetchProfileKeyPair(this.userApiService);
                this.writeProfileKeyPair(var1x);
                return Optional.of(var1x);
            } catch (CryptException | MinecraftClientException | IOException var3) {
                LOGGER.error("Failed to retrieve profile key pair", (Throwable)var3);
                this.writeProfileKeyPair(null);
                return param0x;
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
            if (SharedConstants.IS_RUNNING_IN_IDE) {
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
    }

    private ProfileKeyPair fetchProfileKeyPair(UserApiService param0) throws CryptException, IOException {
        KeyPairResponse var0 = param0.getKeyPair();
        if (var0 != null) {
            ProfilePublicKey.Data var1 = parsePublicKey(var0);
            return new ProfileKeyPair(Crypt.stringToPemRsaPrivateKey(var0.getPrivateKey()), new ProfilePublicKey(var1), Instant.parse(var0.getRefreshedAfter()));
        } else {
            throw new IOException("Could not retrieve profile key pair");
        }
    }

    private static ProfilePublicKey.Data parsePublicKey(KeyPairResponse param0) throws CryptException {
        if (!Strings.isNullOrEmpty(param0.getPublicKey()) && param0.getPublicKeySignature() != null && param0.getPublicKeySignature().array().length != 0) {
            try {
                Instant var0 = Instant.parse(param0.getExpiresAt());
                PublicKey var1 = Crypt.stringToRsaPublicKey(param0.getPublicKey());
                ByteBuffer var2 = param0.getPublicKeySignature();
                return new ProfilePublicKey.Data(var0, var1, var2.array());
            } catch (IllegalArgumentException | DateTimeException var4) {
                throw new CryptException(var4);
            }
        } else {
            throw new CryptException(new MissingException());
        }
    }
}
