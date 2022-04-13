package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldOpenFlows {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private final LevelStorageSource levelSource;

    public WorldOpenFlows(Minecraft param0, LevelStorageSource param1) {
        this.minecraft = param0;
        this.levelSource = param1;
    }

    public void loadLevel(Screen param0, String param1) {
        this.doLoadLevel(param0, param1, false, true);
    }

    public void createFreshLevel(String param0, LevelSettings param1, RegistryAccess param2, WorldGenSettings param3) {
        LevelStorageSource.LevelStorageAccess var0 = this.createWorldAccess(param0);
        if (var0 != null) {
            PackRepository var1 = createPackRepository(var0);
            DataPackConfig var2 = param1.getDataPackConfig();

            try {
                WorldLoader.PackConfig var3 = new WorldLoader.PackConfig(var1, var2, false);
                WorldStem var4 = this.loadWorldStem(
                    var3, (param3x, param4) -> Pair.of(new PrimaryLevelData(param1, param3, Lifecycle.stable()), param2.freeze())
                );
                this.minecraft.doWorldLoad(param0, var0, var1, var4);
            } catch (Exception var10) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var10);
                safeCloseAccess(var0, param0);
            }

        }
    }

    @Nullable
    private LevelStorageSource.LevelStorageAccess createWorldAccess(String param0) {
        try {
            return this.levelSource.createAccess(param0);
        } catch (IOException var3) {
            LOGGER.warn("Failed to read level {} data", param0, var3);
            SystemToast.onWorldAccessFailure(this.minecraft, param0);
            this.minecraft.setScreen(null);
            return null;
        }
    }

    public void createLevelFromExistingSettings(
        LevelStorageSource.LevelStorageAccess param0, ReloadableServerResources param1, RegistryAccess.Frozen param2, WorldData param3
    ) {
        PackRepository var0 = createPackRepository(param0);
        CloseableResourceManager var1 = new WorldLoader.PackConfig(var0, param3.getDataPackConfig(), false).createResourceManager().getSecond();
        this.minecraft.doWorldLoad(param0.getLevelId(), param0, var0, new WorldStem(var1, param1, param2, param3));
    }

    private static PackRepository createPackRepository(LevelStorageSource.LevelStorageAccess param0) {
        return new PackRepository(
            PackType.SERVER_DATA,
            new ServerPacksSource(),
            new FolderRepositorySource(param0.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD)
        );
    }

    private WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess param0, boolean param1, PackRepository param2) throws Exception {
        DataPackConfig var0 = param0.getDataPacks();
        if (var0 == null) {
            throw new IllegalStateException("Failed to load data pack config");
        } else {
            WorldLoader.PackConfig var1 = new WorldLoader.PackConfig(param2, var0, param1);
            return this.loadWorldStem(var1, (param1x, param2x) -> {
                RegistryAccess.Writable var0x = RegistryAccess.builtinCopy();
                DynamicOps<Tag> var1x = RegistryOps.createAndLoad(NbtOps.INSTANCE, var0x, param1x);
                WorldData var2x = param0.getDataTag(var1x, param2x, var0x.allElementsLifecycle());
                if (var2x == null) {
                    throw new IllegalStateException("Failed to load world");
                } else {
                    return Pair.of(var2x, var0x.freeze());
                }
            });
        }
    }

    public WorldStem loadWorldStem(LevelStorageSource.LevelStorageAccess param0, boolean param1) throws Exception {
        PackRepository var0 = createPackRepository(param0);
        return this.loadWorldStem(param0, param1, var0);
    }

    private WorldStem loadWorldStem(WorldLoader.PackConfig param0, WorldLoader.WorldDataSupplier<WorldData> param1) throws Exception {
        WorldLoader.InitConfig var0 = new WorldLoader.InitConfig(param0, Commands.CommandSelection.INTEGRATED, 2);
        CompletableFuture<WorldStem> var1 = WorldStem.load(var0, param1, Util.backgroundExecutor(), this.minecraft);
        this.minecraft.managedBlock(var1::isDone);
        return var1.get();
    }

    private void doLoadLevel(Screen param0, String param1, boolean param2, boolean param3) {
        LevelStorageSource.LevelStorageAccess var0 = this.createWorldAccess(param1);
        if (var0 != null) {
            PackRepository var1 = createPackRepository(var0);

            WorldStem var2;
            try {
                var2 = this.loadWorldStem(var0, param2, var1);
            } catch (Exception var11) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var11);
                this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> this.doLoadLevel(param0, param1, true, param3)));
                safeCloseAccess(var0, param1);
                return;
            }

            WorldData var5 = var2.worldData();
            boolean var6 = var5.worldGenSettings().isOldCustomizedWorld();
            boolean var7 = var5.worldGenSettingsLifecycle() != Lifecycle.stable();
            if (!param3 || !var6 && !var7) {
                this.minecraft.getClientPackSource().loadBundledResourcePack(var0).thenApply(param0x -> true).exceptionallyComposeAsync(param0x -> {
                    LOGGER.warn("Failed to load pack: ", param0x);
                    return this.promptBundledPackLoadFailure();
                }, this.minecraft).thenAcceptAsync(param5 -> {
                    if (param5) {
                        this.minecraft.doWorldLoad(param1, var0, var1, var2);
                    } else {
                        var2.close();
                        safeCloseAccess(var0, param1);
                        this.minecraft.getClientPackSource().clearServerPack().thenRunAsync(() -> this.minecraft.setScreen(param0), this.minecraft);
                    }

                }, this.minecraft).exceptionally(param0x -> {
                    this.minecraft.delayCrash(() -> CrashReport.forThrowable(param0x, "Load world"));
                    return null;
                });
            } else {
                this.askForBackup(param0, param1, var6, () -> this.doLoadLevel(param0, param1, param2, false));
                var2.close();
                safeCloseAccess(var0, param1);
            }
        }
    }

    private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
        CompletableFuture<Boolean> var0 = new CompletableFuture<>();
        this.minecraft
            .setScreen(
                new ConfirmScreen(
                    var0::complete,
                    new TranslatableComponent("multiplayer.texturePrompt.failure.line1"),
                    new TranslatableComponent("multiplayer.texturePrompt.failure.line2"),
                    CommonComponents.GUI_PROCEED,
                    CommonComponents.GUI_CANCEL
                )
            );
        return var0;
    }

    private static void safeCloseAccess(LevelStorageSource.LevelStorageAccess param0, String param1) {
        try {
            param0.close();
        } catch (IOException var3) {
            LOGGER.warn("Failed to unlock access to level {}", param1, var3);
        }

    }

    private void askForBackup(Screen param0, String param1, boolean param2, Runnable param3) {
        Component var0;
        Component var1;
        if (param2) {
            var0 = new TranslatableComponent("selectWorld.backupQuestion.customized");
            var1 = new TranslatableComponent("selectWorld.backupWarning.customized");
        } else {
            var0 = new TranslatableComponent("selectWorld.backupQuestion.experimental");
            var1 = new TranslatableComponent("selectWorld.backupWarning.experimental");
        }

        this.minecraft.setScreen(new BackupConfirmScreen(param0, (param2x, param3x) -> {
            if (param2x) {
                EditWorldScreen.makeBackupAndShowToast(this.levelSource, param1);
            }

            param3.run();
        }, var0, var1, false));
    }

    public static void confirmWorldCreation(Minecraft param0, CreateWorldScreen param1, Lifecycle param2, Runnable param3) {
        BooleanConsumer var0 = param3x -> {
            if (param3x) {
                param3.run();
            } else {
                param0.setScreen(param1);
            }

        };
        if (param2 == Lifecycle.stable()) {
            param3.run();
        } else if (param2 == Lifecycle.experimental()) {
            param0.setScreen(
                new ConfirmScreen(
                    var0,
                    new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.title"),
                    new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.question")
                )
            );
        } else {
            param0.setScreen(
                new ConfirmScreen(
                    var0,
                    new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.title"),
                    new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.question")
                )
            );
        }

    }
}
