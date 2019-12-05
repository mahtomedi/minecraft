package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FileResourcePack;
import net.minecraft.server.packs.VanillaPack;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.util.HttpUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ClientPackSource implements RepositorySource {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private final VanillaPack vanillaPack;
    private final File serverPackDir;
    private final ReentrantLock downloadLock = new ReentrantLock();
    private final AssetIndex assetIndex;
    @Nullable
    private CompletableFuture<?> currentDownload;
    @Nullable
    private UnopenedResourcePack serverPack;

    public ClientPackSource(File param0, AssetIndex param1) {
        this.serverPackDir = param0;
        this.assetIndex = param1;
        this.vanillaPack = new DefaultClientResourcePack(param1);
    }

    @Override
    public <T extends UnopenedPack> void loadPacks(Map<String, T> param0, UnopenedPack.UnopenedPackConstructor<T> param1) {
        T var0 = UnopenedPack.create("vanilla", true, () -> this.vanillaPack, param1, UnopenedPack.Position.BOTTOM);
        if (var0 != null) {
            param0.put("vanilla", var0);
        }

        if (this.serverPack != null) {
            param0.put("server", (T)this.serverPack);
        }

        File var1 = this.assetIndex.getFile(new ResourceLocation("resourcepacks/programmer_art.zip"));
        if (var1 != null && var1.isFile()) {
            T var2 = UnopenedPack.create("programer_art", false, () -> new FileResourcePack(var1) {
                    @Override
                    public String getName() {
                        return "Programmer Art";
                    }
                }, param1, UnopenedPack.Position.TOP);
            if (var2 != null) {
                param0.put("programer_art", var2);
            }
        }

    }

    public VanillaPack getVanillaPack() {
        return this.vanillaPack;
    }

    public static Map<String, String> getDownloadHeaders() {
        Map<String, String> var0 = Maps.newHashMap();
        var0.put("X-Minecraft-Username", Minecraft.getInstance().getUser().getName());
        var0.put("X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid());
        var0.put("X-Minecraft-Version", SharedConstants.getCurrentVersion().getName());
        var0.put("X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId());
        var0.put("X-Minecraft-Pack-Format", String.valueOf(SharedConstants.getCurrentVersion().getPackVersion()));
        var0.put("User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
        return var0;
    }

    public CompletableFuture<?> downloadAndSelectResourcePack(String param0, String param1) {
        String var0 = DigestUtils.sha1Hex(param0);
        String var1 = SHA1.matcher(param1).matches() ? param1 : "";
        this.downloadLock.lock();

        CompletableFuture var13;
        try {
            this.clearServerPack();
            this.clearOldDownloads();
            File var2 = new File(this.serverPackDir, var0);
            CompletableFuture<?> var3;
            if (var2.exists()) {
                var3 = CompletableFuture.completedFuture("");
            } else {
                ProgressScreen var4 = new ProgressScreen();
                Map<String, String> var5 = getDownloadHeaders();
                Minecraft var6 = Minecraft.getInstance();
                var6.executeBlocking(() -> var6.setScreen(var4));
                var3 = HttpUtil.downloadTo(var2, param0, var5, 104857600, var4, var6.getProxy());
            }

            this.currentDownload = var3.<Void>thenCompose(
                    param2 -> !this.checkHash(var1, var2)
                            ? Util.failedFuture(new RuntimeException("Hash check failure for file " + var2 + ", see log"))
                            : this.setServerPack(var2)
                )
                .whenComplete((param1x, param2) -> {
                    if (param2 != null) {
                        LOGGER.warn("Pack application failed: {}, deleting file {}", param2.getMessage(), var2);
                        deleteQuietly(var2);
                    }
    
                });
            var13 = this.currentDownload;
        } finally {
            this.downloadLock.unlock();
        }

        return var13;
    }

    private static void deleteQuietly(File param0) {
        try {
            Files.delete(param0.toPath());
        } catch (IOException var2) {
            LOGGER.warn("Failed to delete file {}: {}", param0, var2.getMessage());
        }

    }

    public void clearServerPack() {
        this.downloadLock.lock();

        try {
            if (this.currentDownload != null) {
                this.currentDownload.cancel(true);
            }

            this.currentDownload = null;
            if (this.serverPack != null) {
                this.serverPack = null;
                Minecraft.getInstance().delayTextureReload();
            }
        } finally {
            this.downloadLock.unlock();
        }

    }

    private boolean checkHash(String param0, File param1) {
        try (FileInputStream var0 = new FileInputStream(param1)) {
            String var1 = DigestUtils.sha1Hex((InputStream)var0);
            if (param0.isEmpty()) {
                LOGGER.info("Found file {} without verification hash", param1);
                return true;
            }

            if (var1.toLowerCase(Locale.ROOT).equals(param0.toLowerCase(Locale.ROOT))) {
                LOGGER.info("Found file {} matching requested hash {}", param1, param0);
                return true;
            }

            LOGGER.warn("File {} had wrong hash (expected {}, found {}).", param1, param0, var1);
        } catch (IOException var17) {
            LOGGER.warn("File {} couldn't be hashed.", param1, var17);
        }

        return false;
    }

    private void clearOldDownloads() {
        try {
            List<File> var0 = Lists.newArrayList(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
            var0.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            int var1 = 0;

            for(File var2 : var0) {
                if (var1++ >= 10) {
                    LOGGER.info("Deleting old server resource pack {}", var2.getName());
                    FileUtils.deleteQuietly(var2);
                }
            }
        } catch (IllegalArgumentException var5) {
            LOGGER.error("Error while deleting old server resource pack : {}", var5.getMessage());
        }

    }

    public CompletableFuture<Void> setServerPack(File param0) {
        PackMetadataSection var0 = null;
        NativeImage var1 = null;
        String var2 = null;

        try (FileResourcePack var3 = new FileResourcePack(param0)) {
            var0 = var3.getMetadataSection(PackMetadataSection.SERIALIZER);

            try (InputStream var4 = var3.getRootResource("pack.png")) {
                var1 = NativeImage.read(var4);
            } catch (IllegalArgumentException | IOException var37) {
                LOGGER.info("Could not read pack.png: {}", var37.getMessage());
            }
        } catch (IOException var40) {
            var2 = var40.getMessage();
        }

        if (var2 != null) {
            return Util.failedFuture(new RuntimeException(String.format("Invalid resourcepack at %s: %s", param0, var2)));
        } else {
            LOGGER.info("Applying server pack {}", param0);
            this.serverPack = new UnopenedResourcePack(
                "server",
                true,
                () -> new FileResourcePack(param0),
                new TranslatableComponent("resourcePack.server.name"),
                var0.getDescription(),
                PackCompatibility.forFormat(var0.getPackFormat()),
                UnopenedPack.Position.TOP,
                true,
                var1
            );
            return Minecraft.getInstance().delayTextureReload();
        }
    }
}
