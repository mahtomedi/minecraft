package net.minecraft.client.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.FolderPackResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
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
public class ClientPackSource implements RepositorySource {
    private static final PackMetadataSection BUILT_IN = new PackMetadataSection(
        Component.translatable("resourcePack.vanilla.description"), PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SHA1 = Pattern.compile("^[a-fA-F0-9]{40}$");
    private static final int MAX_PACK_SIZE_BYTES = 262144000;
    private static final int MAX_KEPT_PACKS = 10;
    private static final String VANILLA_ID = "vanilla";
    private static final String SERVER_ID = "server";
    private static final String PROGRAMMER_ART_ID = "programer_art";
    private static final String PROGRAMMER_ART_NAME = "Programmer Art";
    private static final Component APPLYING_PACK_TEXT = Component.translatable("multiplayer.applyingPack");
    private final VanillaPackResources vanillaPack;
    private final File serverPackDir;
    private final ReentrantLock downloadLock = new ReentrantLock();
    private final AssetIndex assetIndex;
    @Nullable
    private CompletableFuture<?> currentDownload;
    @Nullable
    private Pack serverPack;

    public ClientPackSource(File param0, AssetIndex param1) {
        this.serverPackDir = param0;
        this.assetIndex = param1;
        this.vanillaPack = new DefaultClientPackResources(BUILT_IN, param1);
    }

    @Override
    public void loadPacks(Consumer<Pack> param0, Pack.PackConstructor param1) {
        Pack var0 = Pack.create("vanilla", true, () -> this.vanillaPack, param1, Pack.Position.BOTTOM, PackSource.BUILT_IN);
        if (var0 != null) {
            param0.accept(var0);
        }

        if (this.serverPack != null) {
            param0.accept(this.serverPack);
        }

        Pack var1 = this.createProgrammerArtPack(param1);
        if (var1 != null) {
            param0.accept(var1);
        }

    }

    public VanillaPackResources getVanillaPack() {
        return this.vanillaPack;
    }

    private static Map<String, String> getDownloadHeaders() {
        Map<String, String> var0 = Maps.newHashMap();
        var0.put("X-Minecraft-Username", Minecraft.getInstance().getUser().getName());
        var0.put("X-Minecraft-UUID", Minecraft.getInstance().getUser().getUuid());
        var0.put("X-Minecraft-Version", SharedConstants.getCurrentVersion().getName());
        var0.put("X-Minecraft-Version-ID", SharedConstants.getCurrentVersion().getId());
        var0.put("X-Minecraft-Pack-Format", String.valueOf(PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())));
        var0.put("User-Agent", "Minecraft Java/" + SharedConstants.getCurrentVersion().getName());
        return var0;
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
                        return Util.failedFuture(new RuntimeException("Hash check failure for file " + var3 + ", see log"));
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
                List<File> var0 = Lists.newArrayList(FileUtils.listFiles(this.serverPackDir, TrueFileFilter.TRUE, null));
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

    public CompletableFuture<Void> loadBundledResourcePack(LevelStorageSource.LevelStorageAccess param0) {
        Path var0 = param0.getLevelPath(LevelResource.MAP_RESOURCE_FILE);
        return Files.exists(var0) && !Files.isDirectory(var0) ? this.setServerPack(var0.toFile(), PackSource.WORLD) : CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> setServerPack(File param0, PackSource param1) {
        PackMetadataSection var1;
        try (FilePackResources var0 = new FilePackResources(param0)) {
            var1 = var0.getMetadataSection(PackMetadataSection.SERIALIZER);
        } catch (IOException var9) {
            return Util.failedFuture(new IOException(String.format("Invalid resourcepack at %s", param0), var9));
        }

        LOGGER.info("Applying server pack {}", param0);
        this.serverPack = new Pack(
            "server",
            true,
            () -> new FilePackResources(param0),
            Component.translatable("resourcePack.server.name"),
            var1.getDescription(),
            PackCompatibility.forMetadata(var1, PackType.CLIENT_RESOURCES),
            Pack.Position.TOP,
            true,
            param1
        );
        return Minecraft.getInstance().delayTextureReload();
    }

    @Nullable
    private Pack createProgrammerArtPack(Pack.PackConstructor param0) {
        Pack var0 = null;
        File var1 = this.assetIndex.getFile(new ResourceLocation("resourcepacks/programmer_art.zip"));
        if (var1 != null && var1.isFile()) {
            var0 = createProgrammerArtPack(param0, () -> createProgrammerArtZipPack(var1));
        }

        if (var0 == null && SharedConstants.IS_RUNNING_IN_IDE) {
            File var2 = this.assetIndex.getRootFile("../resourcepacks/programmer_art");
            if (var2 != null && var2.isDirectory()) {
                var0 = createProgrammerArtPack(param0, () -> createProgrammerArtDirPack(var2));
            }
        }

        return var0;
    }

    @Nullable
    private static Pack createProgrammerArtPack(Pack.PackConstructor param0, Supplier<PackResources> param1) {
        return Pack.create("programer_art", false, param1, param0, Pack.Position.TOP, PackSource.BUILT_IN);
    }

    private static FolderPackResources createProgrammerArtDirPack(File param0) {
        return new FolderPackResources(param0) {
            @Override
            public String getName() {
                return "Programmer Art";
            }
        };
    }

    private static PackResources createProgrammerArtZipPack(File param0) {
        return new FilePackResources(param0) {
            @Override
            public String getName() {
                return "Programmer Art";
            }
        };
    }
}
