package net.minecraft.client.resources.server;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.Unit;
import com.mojang.util.UndashedUuid;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.main.GameConfig;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.server.packs.DownloadQueue;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DownloadedPackSource implements AutoCloseable {
    private static final Component SERVER_NAME = Component.translatable("resourcePack.server.name");
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final RepositorySource EMPTY_SOURCE = param0 -> {
    };
    private static final PackLoadFeedback LOG_ONLY_FEEDBACK = (param0, param1) -> LOGGER.debug("Downloaded pack {} changed state to {}", param0, param1);
    final Minecraft minecraft;
    private RepositorySource packSource = EMPTY_SOURCE;
    @Nullable
    private PackReloadConfig.Callbacks pendingReload;
    final ServerPackManager manager;
    private final DownloadQueue downloadQueue;
    private PackSource packType = PackSource.SERVER;
    private PackLoadFeedback packFeedback = LOG_ONLY_FEEDBACK;

    public DownloadedPackSource(Minecraft param0, Path param1, GameConfig.UserData param2) {
        this.minecraft = param0;

        try {
            this.downloadQueue = new DownloadQueue(param1);
        } catch (IOException var5) {
            throw new UncheckedIOException("Failed to open download queue in directory " + param1, var5);
        }

        Executor var1 = param0::tell;
        this.manager = new ServerPackManager(
            this.createDownloader(this.downloadQueue, var1, param2.user, param2.proxy),
            (param0x, param1x) -> this.packFeedback.sendResponse(param0x, param1x),
            this.createReloadConfig(),
            this.createUpdateScheduler(var1),
            ServerPackManager.PackPromptStatus.PENDING
        );
    }

    HttpUtil.DownloadProgressListener createDownloadNotifier(final int param0) {
        return new HttpUtil.DownloadProgressListener() {
            private final SystemToast.SystemToastId toastId = new SystemToast.SystemToastId(10000L);
            private Component title = Component.empty();
            @Nullable
            private Component message = null;
            private int count;
            private OptionalLong totalBytes = OptionalLong.empty();

            private void updateToast() {
                SystemToast.addOrUpdate(DownloadedPackSource.this.minecraft.getToasts(), this.toastId, this.title, this.message);
            }

            private void updateProgress(long param0x) {
                if (this.totalBytes.isPresent()) {
                    this.message = Component.translatable("download.pack.progress.percent", param0 * 100L / this.totalBytes.getAsLong());
                } else {
                    this.message = Component.translatable("download.pack.progress.bytes", Unit.humanReadable(param0));
                }

                this.updateToast();
            }

            @Override
            public void requestStart() {
                ++this.count;
                this.title = Component.translatable("download.pack.title", this.count, param0);
                this.updateToast();
                DownloadedPackSource.LOGGER.debug("Starting pack {}/{} download", this.count, param0);
            }

            @Override
            public void downloadStart(OptionalLong param0x) {
                DownloadedPackSource.LOGGER.debug("File size = {} bytes", param0);
                this.totalBytes = param0;
                this.updateProgress(0L);
            }

            @Override
            public void downloadedBytes(long param0x) {
                DownloadedPackSource.LOGGER.debug("Progress for pack {}: {} bytes", this.count, param0);
                this.updateProgress(param0);
            }

            @Override
            public void requestFinished() {
                DownloadedPackSource.LOGGER.debug("Download ended for pack {}", this.count);
                if (this.count == param0) {
                    SystemToast.forceHide(DownloadedPackSource.this.minecraft.getToasts(), this.toastId);
                }

            }
        };
    }

    private PackDownloader createDownloader(final DownloadQueue param0, final Executor param1, final User param2, final Proxy param3) {
        return new PackDownloader() {
            private static final int MAX_PACK_SIZE_BYTES = 262144000;
            private static final HashFunction CACHE_HASHING_FUNCTION = Hashing.sha1();

            private Map<String, String> createDownloadHeaders() {
                WorldVersion var0 = SharedConstants.getCurrentVersion();
                return Map.of(
                    "X-Minecraft-Username",
                    param2.getName(),
                    "X-Minecraft-UUID",
                    UndashedUuid.toString(param2.getProfileId()),
                    "X-Minecraft-Version",
                    var0.getName(),
                    "X-Minecraft-Version-ID",
                    var0.getId(),
                    "X-Minecraft-Pack-Format",
                    String.valueOf(var0.getPackVersion(PackType.CLIENT_RESOURCES)),
                    "User-Agent",
                    "Minecraft Java/" + var0.getName()
                );
            }

            @Override
            public void download(Map<UUID, DownloadQueue.DownloadRequest> param0x, Consumer<DownloadQueue.BatchResult> param1x) {
                param0.downloadBatch(
                        new DownloadQueue.BatchConfig(
                            CACHE_HASHING_FUNCTION,
                            262144000,
                            this.createDownloadHeaders(),
                            param3,
                            DownloadedPackSource.this.createDownloadNotifier(param0.size())
                        ),
                        param0
                    )
                    .thenAcceptAsync(param1, param1);
            }
        };
    }

    private Runnable createUpdateScheduler(final Executor param0) {
        return new Runnable() {
            private boolean scheduledInMainExecutor;
            private boolean hasUpdates;

            @Override
            public void run() {
                this.hasUpdates = true;
                if (!this.scheduledInMainExecutor) {
                    this.scheduledInMainExecutor = true;
                    param0.execute(this::runAllUpdates);
                }

            }

            private void runAllUpdates() {
                while(this.hasUpdates) {
                    this.hasUpdates = false;
                    DownloadedPackSource.this.manager.tick();
                }

                this.scheduledInMainExecutor = false;
            }
        };
    }

    private PackReloadConfig createReloadConfig() {
        return this::startReload;
    }

    @Nullable
    private List<Pack> loadRequestedPacks(List<PackReloadConfig.IdAndPath> param0) {
        List<Pack> var0 = new ArrayList<>(param0.size());

        for(PackReloadConfig.IdAndPath var1 : param0) {
            String var2 = "server/" + var1.id();
            Path var3 = var1.path();
            Pack.ResourcesSupplier var4 = new FilePackResources.FileResourcesSupplier(var3, false);
            int var5 = SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES);
            Pack.Info var6 = Pack.readPackInfo(var2, var4, var5);
            if (var6 == null) {
                LOGGER.warn("Invalid pack metadata in {}, ignoring all", var3);
                return null;
            }

            var0.add(Pack.create(var2, SERVER_NAME, true, var4, var6, Pack.Position.TOP, true, this.packType));
        }

        return var0;
    }

    public RepositorySource createRepositorySource() {
        return param0 -> this.packSource.loadPacks(param0);
    }

    private static RepositorySource configureSource(List<Pack> param0) {
        return param0.isEmpty() ? EMPTY_SOURCE : param0::forEach;
    }

    private void startReload(PackReloadConfig.Callbacks param0) {
        this.pendingReload = param0;
        List<PackReloadConfig.IdAndPath> var0 = param0.packsToLoad();
        List<Pack> var1 = this.loadRequestedPacks(var0);
        if (var1 == null) {
            param0.onFailure(false);
            List<PackReloadConfig.IdAndPath> var2 = param0.packsToLoad();
            var1 = this.loadRequestedPacks(var2);
            if (var1 == null) {
                LOGGER.warn("Double failure in loading server packs");
                var1 = List.of();
            }
        }

        this.packSource = configureSource(var1);
        this.minecraft.reloadResourcePacks();
    }

    public void onRecovery() {
        if (this.pendingReload != null) {
            this.pendingReload.onFailure(false);
            List<Pack> var0 = this.loadRequestedPacks(this.pendingReload.packsToLoad());
            if (var0 == null) {
                LOGGER.warn("Double failure in loading server packs");
                var0 = List.of();
            }

            this.packSource = configureSource(var0);
        }

    }

    public void onRecoveryFailure() {
        if (this.pendingReload != null) {
            this.pendingReload.onFailure(true);
            this.pendingReload = null;
            this.packSource = EMPTY_SOURCE;
        }

    }

    public void onReloadSuccess() {
        if (this.pendingReload != null) {
            this.pendingReload.onSuccess();
            this.pendingReload = null;
        }

    }

    @Nullable
    private static HashCode tryParseSha1Hash(@Nullable String param0) {
        return param0 != null && SHA1.matcher(param0).matches() ? HashCode.fromString(param0.toLowerCase(Locale.ROOT)) : null;
    }

    public void pushPack(UUID param0, URL param1, @Nullable String param2) {
        HashCode var0 = tryParseSha1Hash(param2);
        this.manager.pushPack(param0, param1, var0);
    }

    public void pushLocalPack(UUID param0, Path param1) {
        this.manager.pushLocalPack(param0, param1);
    }

    public void popPack(UUID param0) {
        this.manager.popPack(param0);
    }

    public void popAll() {
        this.manager.popAll();
    }

    private static PackLoadFeedback createPackResponseSender(Connection param0) {
        return (param1, param2) -> {
            LOGGER.debug("Pack {} changed status to {}", param1, param2);

            ServerboundResourcePackPacket.Action var0x = switch(param2) {
                case ACCEPTED -> ServerboundResourcePackPacket.Action.ACCEPTED;
                case APPLIED -> ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED;
                case DOWNLOAD_FAILED -> ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD;
                case DECLINED -> ServerboundResourcePackPacket.Action.DECLINED;
                case DISCARDED -> ServerboundResourcePackPacket.Action.DISCARDED;
                case ACTIVATION_FAILED -> ServerboundResourcePackPacket.Action.FAILED_RELOAD;
            };
            param0.send(new ServerboundResourcePackPacket(param1, var0x));
        };
    }

    public void configureForServerControl(Connection param0, ServerPackManager.PackPromptStatus param1) {
        this.packType = PackSource.SERVER;
        this.packFeedback = createPackResponseSender(param0);
        switch(param1) {
            case ALLOWED:
                this.manager.allowServerPacks();
                break;
            case DECLINED:
                this.manager.rejectServerPacks();
                break;
            case PENDING:
                this.manager.resetPromptStatus();
        }

    }

    public void configureForLocalWorld() {
        this.packType = PackSource.WORLD;
        this.packFeedback = LOG_ONLY_FEEDBACK;
        this.manager.allowServerPacks();
    }

    public void allowServerPacks() {
        this.manager.allowServerPacks();
    }

    public void rejectServerPacks() {
        this.manager.rejectServerPacks();
    }

    public CompletableFuture<Void> waitForPackFeedback(UUID param0) {
        CompletableFuture<Void> var0 = new CompletableFuture<>();
        PackLoadFeedback var1 = this.packFeedback;
        this.packFeedback = (param3, param4) -> {
            if (param0.equals(param3)) {
                if (param4 == PackLoadFeedback.Result.ACCEPTED) {
                    var1.sendResponse(param3, param4);
                    return;
                }

                this.packFeedback = var1;
                if (param4 == PackLoadFeedback.Result.APPLIED) {
                    var0.complete(null);
                } else {
                    var0.completeExceptionally(new IllegalStateException("Failed to apply pack " + param3 + ", reason: " + param4));
                }

                var1.sendResponse(param3, param4);
            }

        };
        return var0;
    }

    public void cleanupAfterDisconnect() {
        this.manager.popAll();
        this.packFeedback = LOG_ONLY_FEEDBACK;
        this.manager.resetPromptStatus();
    }

    @Override
    public void close() throws IOException {
        this.downloadQueue.close();
    }
}
