package net.minecraft.client.resources;

import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DownloadedPackSource implements RepositorySource {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private static final int MAX_PACK_SIZE_BYTES = 262144000;
    private static final int MAX_KEPT_PACKS = 10;
    private static final String SERVER_ID = "server";
    private static final Component SERVER_NAME = Component.translatable("resourcePack.server.name");
    private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
    private final File serverPackDir;
    private final ReentrantLock downloadLock = new ReentrantLock();
    @Nullable
    private CompletableFuture<?> currentDownload;
    @Nullable
    private Pack serverPack;

    public DownloadedPackSource(File param0) {
        this.serverPackDir = param0;
    }

    @Override
    public void loadPacks(Consumer<Pack> param0) {
        if (this.serverPack != null) {
            param0.accept(this.serverPack);
        }

    }

    private static Map<String, String> getDownloadHeaders() {
        return Map.of(
            "X-Minecraft-Username",
            Minecraft.getInstance().getUser().getName(),
            "X-Minecraft-UUID",
            Minecraft.getInstance().getUser().getUuid(),
            "X-Minecraft-Version",
            SharedConstants.getCurrentVersion().getName(),
            "X-Minecraft-Version-ID",
            SharedConstants.getCurrentVersion().getId(),
            "X-Minecraft-Pack-Format",
            String.valueOf(SharedConstants.getCurrentVersion().getPackVersion(PackType.CLIENT_RESOURCES)),
            "User-Agent",
            "Minecraft Java/" + SharedConstants.getCurrentVersion().getName()
        );
    }

    public CompletableFuture<?> downloadAndSelectResourcePack(URL param0, String param1, boolean param2) {
        String var0 = Hashing.sha1().hashString(param0.toString(), StandardCharsets.UTF_8).toString();
        String var1 = SHA1.matcher(param1).matches() ? param1 : "";
        this.downloadLock.lock();

        CompletableFuture var14;
        try {
            Minecraft var2 = Minecraft.getInstance();
            File var3 = new File(this.serverPackDir, var0);
            CompletableFuture<?> var4;
            if (var3.exists()) {
                var4 = CompletableFuture.completedFuture("");
            } else {
                ProgressScreen var5 = new ProgressScreen(param2);
                Map<String, String> var6 = getDownloadHeaders();
                var2.executeBlocking(() -> var2.setScreen(var5));
                var4 = HttpUtil.downloadTo(var3, param0, var6, 262144000, var5, var2.getProxy());
            }

            this.currentDownload = var4.<Void>thenCompose(param4 -> {
                    if (!this.checkHash(var1, var3)) {
                        return CompletableFuture.failedFuture(new RuntimeException("Hash check failure for file " + var3 + ", see log"));
                    } else {
                        var2.execute(() -> {
                            if (!param2) {
                                var2.setScreen(new GenericDirtMessageScreen(APPLYING_PACK_TEXT));
                            }
    
                        });
                        return this.setServerPack(var3, PackSource.SERVER);
                    }
                })
                .exceptionallyCompose(
                    param2x -> this.clearServerPack()
                            .thenAcceptAsync(param2xx -> {
                                LOGGER.warn("Pack application failed: {}, deleting file {}", param2x.getMessage(), var3);
                                deleteQuietly(var3);
                            }, Util.ioPool())
                            .thenAcceptAsync(
                                param1x -> var2.setScreen(
                                        new ConfirmScreen(
                                            param1xx -> {
                                                if (param1xx) {
                                                    var2.setScreen(null);
                                                } else {
                                                    ClientPacketListener var0x = var2.getConnection();
                                                    if (var0x != null) {
                                                        var0x.getConnection().disconnect(Component.translatable("connect.aborted"));
                                                    }
                                                }
                        
                                            },
                                            Component.translatable("multiplayer.texturePrompt.failure.line1"),
                                            Component.translatable("multiplayer.texturePrompt.failure.line2"),
                                            CommonComponents.GUI_PROCEED,
                                            Component.translatable("menu.disconnect")
                                        )
                                    ),
                                var2
                            )
                )
                .thenAcceptAsync(param0x -> this.clearOldDownloads(), Util.ioPool());
            var14 = this.currentDownload;
        } finally {
            this.downloadLock.unlock();
        }

        return var14;
    }

    private static void deleteQuietly(File param0) {
        try {
            Files.delete(param0.toPath());
        } catch (IOException var2) {
            LOGGER.warn("Failed to delete file {}: {}", param0, var2.getMessage());
        }

    }

    public CompletableFuture<Void> clearServerPack() {
        this.downloadLock.lock();

        CompletableFuture var1;
        try {
            if (this.currentDownload != null) {
                this.currentDownload.cancel(true);
            }

            this.currentDownload = null;
            if (this.serverPack == null) {
                return CompletableFuture.completedFuture(null);
            }

            this.serverPack = null;
            var1 = Minecraft.getInstance().delayTextureReload();
        } finally {
            this.downloadLock.unlock();
        }

        return var1;
    }

    private boolean checkHash(String param0, File param1) {
        try {
            String var0 = com.google.common.io.Files.asByteSource(param1).hash(Hashing.sha1()).toString();
            if (param0.isEmpty()) {
                LOGGER.info("Found file {} without verification hash", param1);
                return true;
            }

            if (var0.toLowerCase(Locale.ROOT).equals(param0.toLowerCase(Locale.ROOT))) {
                LOGGER.info("Found file {} matching requested hash {}", param1, param0);
                return true;
            }

            LOGGER.warn("File {} had wrong hash (expected {}, found {}).", param1, param0, var0);
        } catch (IOException var4) {
            LOGGER.warn("File {} couldn't be hashed.", param1, var4);
        }

        return false;
    }

    private void clearOldDownloads() {
        if (this.serverPackDir.isDirectory()) {
            try {
                List<File> var0 = new ArrayList<>(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
                var0.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
                int var1 = 0;

                for(File var2 : var0) {
                    if (var1++ >= 10) {
                        LOGGER.info("Deleting old server resource pack {}", var2.getName());
                        FileUtils.deleteQuietly(var2);
                    }
                }
            } catch (Exception var5) {
                LOGGER.error("Error while deleting old server resource pack : {}", var5.getMessage());
            }

        }
    }

    public CompletableFuture<Void> setServerPack(File param0, PackSource param1) {
        Pack.ResourcesSupplier var0 = param1x -> new FilePackResources(param1x, param0, false);
        Pack.Info var1 = Pack.readPackInfo("server", var0);
        if (var1 == null) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid pack metadata at " + param0));
        } else {
            LOGGER.info("Applying server pack {}", param0);
            this.serverPack = Pack.create("server", SERVER_NAME, true, var0, var1, PackType.CLIENT_RESOURCES, Pack.Position.TOP, true, param1);
            return Minecraft.getInstance().delayTextureReload();
        }
    }

    public CompletableFuture<Void> loadBundledResourcePack(LevelStorageSource.LevelStorageAccess param0) {
        Path var0 = param0.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
        return Files.exists(var0) && !Files.isDirectory(var0) ? this.setServerPack(var0.toFile(), PackSource.WORLD) : CompletableFuture.completedFuture(null);
    }
}
