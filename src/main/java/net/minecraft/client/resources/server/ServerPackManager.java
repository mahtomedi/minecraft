package net.minecraft.client.resources.server;

import com.google.common.hash.HashCode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.server.packs.DownloadQueue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ServerPackManager {
    private final PackDownloader downloader;
    final PackLoadFeedback packLoadFeedback;
    private final PackReloadConfig reloadConfig;
    private final Runnable updateRequest;
    private ServerPackManager.PackPromptStatus packPromptStatus;
    final List<ServerPackManager.ServerPackData> packs = new ArrayList<>();

    public ServerPackManager(
        PackDownloader param0, PackLoadFeedback param1, PackReloadConfig param2, Runnable param3, ServerPackManager.PackPromptStatus param4
    ) {
        this.downloader = param0;
        this.packLoadFeedback = param1;
        this.reloadConfig = param2;
        this.updateRequest = param3;
        this.packPromptStatus = param4;
    }

    void registerForUpdate() {
        this.updateRequest.run();
    }

    private void markExistingPacksAsRemoved(UUID param0) {
        for(ServerPackManager.ServerPackData var0 : this.packs) {
            if (var0.id.equals(param0)) {
                var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.SERVER_REPLACED);
            }
        }

    }

    public void pushPack(UUID param0, URL param1, @Nullable HashCode param2) {
        if (this.packPromptStatus == ServerPackManager.PackPromptStatus.DECLINED) {
            this.packLoadFeedback.sendResponse(param0, PackLoadFeedback.Result.DECLINED);
        } else {
            this.pushNewPack(param0, new ServerPackManager.ServerPackData(param0, param1, param2));
        }
    }

    public void pushLocalPack(UUID param0, Path param1) {
        if (this.packPromptStatus == ServerPackManager.PackPromptStatus.DECLINED) {
            this.packLoadFeedback.sendResponse(param0, PackLoadFeedback.Result.DECLINED);
        } else {
            URL var0;
            try {
                var0 = param1.toUri().toURL();
            } catch (MalformedURLException var5) {
                throw new IllegalStateException("Can't convert path to URL " + param1, var5);
            }

            ServerPackManager.ServerPackData var3 = new ServerPackManager.ServerPackData(param0, var0, null);
            var3.downloadStatus = ServerPackManager.PackDownloadStatus.DONE;
            var3.path = param1;
            this.pushNewPack(param0, var3);
        }
    }

    private void pushNewPack(UUID param0, ServerPackManager.ServerPackData param1) {
        this.markExistingPacksAsRemoved(param0);
        this.packs.add(param1);
        if (this.packPromptStatus == ServerPackManager.PackPromptStatus.ALLOWED) {
            this.acceptPack(param1);
        }

        this.registerForUpdate();
    }

    private void acceptPack(ServerPackManager.ServerPackData param0) {
        this.packLoadFeedback.sendResponse(param0.id, PackLoadFeedback.Result.ACCEPTED);
        param0.promptAccepted = true;
    }

    @Nullable
    private ServerPackManager.ServerPackData findPackInfo(UUID param0) {
        for(ServerPackManager.ServerPackData var0 : this.packs) {
            if (!var0.isRemoved() && var0.id.equals(param0)) {
                return var0;
            }
        }

        return null;
    }

    public void popPack(UUID param0) {
        ServerPackManager.ServerPackData var0 = this.findPackInfo(param0);
        if (var0 != null) {
            var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.SERVER_REMOVED);
            this.registerForUpdate();
        }

    }

    public void popAll() {
        for(ServerPackManager.ServerPackData var0 : this.packs) {
            var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.SERVER_REMOVED);
        }

        this.registerForUpdate();
    }

    public void allowServerPacks() {
        this.packPromptStatus = ServerPackManager.PackPromptStatus.ALLOWED;

        for(ServerPackManager.ServerPackData var0 : this.packs) {
            if (!var0.promptAccepted && !var0.isRemoved()) {
                this.acceptPack(var0);
            }
        }

        this.registerForUpdate();
    }

    public void rejectServerPacks() {
        this.packPromptStatus = ServerPackManager.PackPromptStatus.DECLINED;

        for(ServerPackManager.ServerPackData var0 : this.packs) {
            if (!var0.promptAccepted) {
                var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DECLINED);
            }
        }

        this.registerForUpdate();
    }

    public void resetPromptStatus() {
        this.packPromptStatus = ServerPackManager.PackPromptStatus.PENDING;
    }

    public void tick() {
        boolean var0 = this.updateDownloads();
        if (!var0) {
            this.triggerReloadIfNeeded();
        }

        this.cleanupRemovedPacks();
    }

    private void cleanupRemovedPacks() {
        this.packs.removeIf(param0 -> {
            if (param0.activationStatus != ServerPackManager.ActivationStatus.INACTIVE) {
                return false;
            } else if (param0.removalReason != null) {
                PackLoadFeedback.Result var0 = param0.removalReason.serverResponse;
                if (var0 != null) {
                    this.packLoadFeedback.sendResponse(param0.id, var0);
                }

                return true;
            } else {
                return false;
            }
        });
    }

    private void onDownload(Collection<ServerPackManager.ServerPackData> param0, DownloadQueue.BatchResult param1) {
        if (!param1.failed().isEmpty()) {
            for(ServerPackManager.ServerPackData var0 : this.packs) {
                if (var0.activationStatus != ServerPackManager.ActivationStatus.ACTIVE) {
                    if (param1.failed().contains(var0.id)) {
                        var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DOWNLOAD_FAILED);
                    } else {
                        var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DISCARDED);
                    }
                }
            }
        }

        for(ServerPackManager.ServerPackData var1 : param0) {
            Path var2 = param1.downloaded().get(var1.id);
            if (var2 != null) {
                var1.downloadStatus = ServerPackManager.PackDownloadStatus.DONE;
                var1.path = var2;
            }
        }

        this.registerForUpdate();
    }

    private boolean updateDownloads() {
        List<ServerPackManager.ServerPackData> var0 = new ArrayList<>();
        boolean var1 = false;

        for(ServerPackManager.ServerPackData var2 : this.packs) {
            if (!var2.isRemoved() && var2.promptAccepted) {
                if (var2.downloadStatus != ServerPackManager.PackDownloadStatus.DONE) {
                    var1 = true;
                }

                if (var2.downloadStatus == ServerPackManager.PackDownloadStatus.REQUESTED) {
                    var2.downloadStatus = ServerPackManager.PackDownloadStatus.PENDING;
                    var0.add(var2);
                }
            }
        }

        if (!var0.isEmpty()) {
            Map<UUID, DownloadQueue.DownloadRequest> var3 = new HashMap<>();

            for(ServerPackManager.ServerPackData var4 : var0) {
                var3.put(var4.id, new DownloadQueue.DownloadRequest(var4.url, var4.hash));
            }

            this.downloader.download(var3, param1 -> this.onDownload(var0, param1));
        }

        return var1;
    }

    private void triggerReloadIfNeeded() {
        boolean var0 = false;
        final List<ServerPackManager.ServerPackData> var1 = new ArrayList<>();
        final List<ServerPackManager.ServerPackData> var2 = new ArrayList<>();

        for(ServerPackManager.ServerPackData var3 : this.packs) {
            if (var3.activationStatus == ServerPackManager.ActivationStatus.PENDING) {
                return;
            }

            boolean var4 = var3.promptAccepted && var3.downloadStatus == ServerPackManager.PackDownloadStatus.DONE && !var3.isRemoved();
            if (var4 && var3.activationStatus == ServerPackManager.ActivationStatus.INACTIVE) {
                var1.add(var3);
                var0 = true;
            }

            if (var3.activationStatus == ServerPackManager.ActivationStatus.ACTIVE) {
                if (!var4) {
                    var0 = true;
                    var2.add(var3);
                } else {
                    var1.add(var3);
                }
            }
        }

        if (var0) {
            for(ServerPackManager.ServerPackData var5 : var1) {
                if (var5.activationStatus != ServerPackManager.ActivationStatus.ACTIVE) {
                    var5.activationStatus = ServerPackManager.ActivationStatus.PENDING;
                }
            }

            for(ServerPackManager.ServerPackData var6 : var2) {
                var6.activationStatus = ServerPackManager.ActivationStatus.PENDING;
            }

            this.reloadConfig.scheduleReload(new PackReloadConfig.Callbacks() {
                @Override
                public void onSuccess() {
                    for(ServerPackManager.ServerPackData var0 : var1) {
                        var0.activationStatus = ServerPackManager.ActivationStatus.ACTIVE;
                        if (var0.removalReason == null) {
                            ServerPackManager.this.packLoadFeedback.sendResponse(var0.id, PackLoadFeedback.Result.APPLIED);
                        }
                    }

                    for(ServerPackManager.ServerPackData var1 : var2) {
                        var1.activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
                    }

                    ServerPackManager.this.registerForUpdate();
                }

                @Override
                public void onFailure(boolean param0) {
                    if (!param0) {
                        var1.clear();

                        for(ServerPackManager.ServerPackData var0 : ServerPackManager.this.packs) {
                            switch(var0.activationStatus) {
                                case ACTIVE:
                                    var1.add(var0);
                                    break;
                                case PENDING:
                                    var0.activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
                                    var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.ACTIVATION_FAILED);
                                    break;
                                case INACTIVE:
                                    var0.setRemovalReasonIfNotSet(ServerPackManager.RemovalReason.DISCARDED);
                            }
                        }

                        ServerPackManager.this.registerForUpdate();
                    } else {
                        for(ServerPackManager.ServerPackData var1 : ServerPackManager.this.packs) {
                            if (var1.activationStatus == ServerPackManager.ActivationStatus.PENDING) {
                                var1.activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
                            }
                        }
                    }

                }

                @Override
                public List<PackReloadConfig.IdAndPath> packsToLoad() {
                    return var1.stream().map(param0 -> new PackReloadConfig.IdAndPath(param0.id, param0.path)).toList();
                }
            });
        }

    }

    @OnlyIn(Dist.CLIENT)
    static enum ActivationStatus {
        INACTIVE,
        PENDING,
        ACTIVE;
    }

    @OnlyIn(Dist.CLIENT)
    static enum PackDownloadStatus {
        REQUESTED,
        PENDING,
        DONE;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum PackPromptStatus {
        PENDING,
        ALLOWED,
        DECLINED;
    }

    @OnlyIn(Dist.CLIENT)
    static enum RemovalReason {
        DOWNLOAD_FAILED(PackLoadFeedback.Result.DOWNLOAD_FAILED),
        ACTIVATION_FAILED(PackLoadFeedback.Result.ACTIVATION_FAILED),
        DECLINED(PackLoadFeedback.Result.DECLINED),
        DISCARDED(PackLoadFeedback.Result.DISCARDED),
        SERVER_REMOVED(null),
        SERVER_REPLACED(null);

        @Nullable
        final PackLoadFeedback.Result serverResponse;

        private RemovalReason(PackLoadFeedback.Result param0) {
            this.serverResponse = param0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class ServerPackData {
        final UUID id;
        final URL url;
        @Nullable
        final HashCode hash;
        @Nullable
        Path path;
        @Nullable
        ServerPackManager.RemovalReason removalReason;
        ServerPackManager.PackDownloadStatus downloadStatus = ServerPackManager.PackDownloadStatus.REQUESTED;
        ServerPackManager.ActivationStatus activationStatus = ServerPackManager.ActivationStatus.INACTIVE;
        boolean promptAccepted;

        ServerPackData(UUID param0, URL param1, @Nullable HashCode param2) {
            this.id = param0;
            this.url = param1;
            this.hash = param2;
        }

        public void setRemovalReasonIfNotSet(ServerPackManager.RemovalReason param0) {
            if (this.removalReason == null) {
                this.removalReason = param0;
            }

        }

        public boolean isRemoved() {
            return this.removalReason != null;
        }
    }
}
