package net.minecraft.client.telemetry;

import com.google.common.base.Suppliers;
import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.minecraft.UserApiService;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientTelemetryManager implements AutoCloseable {
    private static final AtomicInteger THREAD_COUNT = new AtomicInteger(1);
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor(param0 -> {
        Thread var0 = new Thread(param0);
        var0.setName("Telemetry-Sender-#" + THREAD_COUNT.getAndIncrement());
        return var0;
    });
    private final UserApiService userApiService;
    private final TelemetryPropertyMap deviceSessionProperties;
    private final Path logDirectory;
    private final CompletableFuture<Optional<TelemetryLogManager>> logManager;
    private final Supplier<TelemetryEventSender> outsideSessionSender = Suppliers.memoize(this::createEventSender);

    public ClientTelemetryManager(Minecraft param0, UserApiService param1, User param2) {
        this.userApiService = param1;
        TelemetryPropertyMap.Builder var0 = TelemetryPropertyMap.builder();
        param2.getXuid().ifPresent(param1x -> var0.put(TelemetryProperty.USER_ID, param1x));
        param2.getClientId().ifPresent(param1x -> var0.put(TelemetryProperty.CLIENT_ID, param1x));
        var0.put(TelemetryProperty.MINECRAFT_SESSION_ID, UUID.randomUUID());
        var0.put(TelemetryProperty.GAME_VERSION, SharedConstants.getCurrentVersion().getId());
        var0.put(TelemetryProperty.OPERATING_SYSTEM, Util.getPlatform().telemetryName());
        var0.put(TelemetryProperty.PLATFORM, System.getProperty("os.name"));
        var0.put(TelemetryProperty.CLIENT_MODDED, Minecraft.checkModStatus().shouldReportAsModified());
        var0.putIfNotNull(TelemetryProperty.LAUNCHER_NAME, System.getProperty("minecraft.launcher.brand"));
        this.deviceSessionProperties = var0.build();
        this.logDirectory = param0.gameDirectory.toPath().resolve("logs/telemetry");
        this.logManager = TelemetryLogManager.open(this.logDirectory);
    }

    public WorldSessionTelemetryManager createWorldSessionManager(boolean param0, @Nullable Duration param1, @Nullable String param2) {
        return new WorldSessionTelemetryManager(this.createEventSender(), param0, param1, param2);
    }

    public TelemetryEventSender getOutsideSessionSender() {
        return this.outsideSessionSender.get();
    }

    private TelemetryEventSender createEventSender() {
        if (SharedConstants.IS_RUNNING_IN_IDE) {
            return TelemetryEventSender.DISABLED;
        } else {
            TelemetrySession var0x = this.userApiService.newTelemetrySession(EXECUTOR);
            if (!var0x.isEnabled()) {
                return TelemetryEventSender.DISABLED;
            } else {
                CompletableFuture<Optional<TelemetryEventLogger>> var1 = this.logManager
                    .thenCompose(param0x -> param0x.map(TelemetryLogManager::openLogger).orElseGet(() -> CompletableFuture.completedFuture(Optional.empty())));
                return (param2x, param3) -> {
                    if (!param2x.isOptIn() || Minecraft.getInstance().telemetryOptInExtra()) {
                        TelemetryPropertyMap.Builder var0xx = TelemetryPropertyMap.builder();
                        var0xx.putAll(this.deviceSessionProperties);
                        var0xx.put(TelemetryProperty.EVENT_TIMESTAMP_UTC, Instant.now());
                        var0xx.put(TelemetryProperty.OPT_IN, param2x.isOptIn());
                        param3.accept(var0xx);
                        TelemetryEventInstance var1x = new TelemetryEventInstance(param2x, var0xx.build());
                        var1.thenAccept(param2xx -> {
                            if (!param2xx.isEmpty()) {
                                param2xx.get().log(var1x);
                                var1x.export(var0x).send();
                            }
                        });
                    }
                };
            }
        }
    }

    public Path getLogDirectory() {
        return this.logDirectory;
    }

    @Override
    public void close() {
        this.logManager.thenAccept(param0 -> param0.ifPresent(TelemetryLogManager::close));
    }
}
