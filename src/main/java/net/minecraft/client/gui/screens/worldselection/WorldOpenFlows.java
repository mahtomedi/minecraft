package net.minecraft.client.gui.screens.worldselection;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.DatapackLoadFailureScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.RecoverWorldDataScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.util.MemoryReserve;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.storage.LevelDataAndDimensions;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;
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

    public void createFreshLevel(String param0, LevelSettings param1, WorldOptions param2, Function<RegistryAccess, WorldDimensions> param3, Screen param4) {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
        LevelStorageSource.LevelStorageAccess var0 = this.createWorldAccess(param0);
        if (var0 != null) {
            PackRepository var1 = ServerPacksSource.createPackRepository(var0);
            WorldDataConfiguration var2 = param1.getDataConfiguration();

            try {
                WorldLoader.PackConfig var3 = new WorldLoader.PackConfig(var1, var2, false, false);
                WorldStem var4 = this.loadWorldDataBlocking(
                    var3,
                    param3x -> {
                        WorldDimensions.Complete var0x = param3.apply(param3x.datapackWorldgen())
                            .bake(param3x.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM));
                        return new WorldLoader.DataLoadOutput<>(
                            new PrimaryLevelData(param1, param2, var0x.specialWorldProperty(), var0x.lifecycle()), var0x.dimensionsRegistryAccess()
                        );
                    },
                    WorldStem::new
                );
                this.minecraft.doWorldLoad(var0, var1, var4, true);
            } catch (Exception var11) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load", (Throwable)var11);
                var0.safeClose();
                this.minecraft.setScreen(param4);
            }

        }
    }

    @Nullable
    private LevelStorageSource.LevelStorageAccess createWorldAccess(String param0) {
        try {
            return this.levelSource.validateAndCreateAccess(param0);
        } catch (IOException var3) {
            LOGGER.warn("Failed to read level {} data", param0, var3);
            SystemToast.onWorldAccessFailure(this.minecraft, param0);
            this.minecraft.setScreen(null);
            return null;
        } catch (ContentValidationException var4) {
            LOGGER.warn("{}", var4.getMessage());
            this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(null)));
            return null;
        }
    }

    public void createLevelFromExistingSettings(
        LevelStorageSource.LevelStorageAccess param0, ReloadableServerResources param1, LayeredRegistryAccess<RegistryLayer> param2, WorldData param3
    ) {
        PackRepository var0 = ServerPacksSource.createPackRepository(param0);
        CloseableResourceManager var1 = new WorldLoader.PackConfig(var0, param3.getDataConfiguration(), false, false).createResourceManager().getSecond();
        this.minecraft.doWorldLoad(param0, var0, new WorldStem(var1, param1, param2, param3), true);
    }

    public WorldStem loadWorldStem(Dynamic<?> param0, boolean param1, PackRepository param2) throws Exception {
        WorldLoader.PackConfig var0 = LevelStorageSource.getPackConfig(param0, param2, param1);
        return this.loadWorldDataBlocking(var0, param1x -> {
            Registry<LevelStem> var0x = param1x.datapackDimensions().registryOrThrow(Registries.LEVEL_STEM);
            LevelDataAndDimensions var1x = LevelStorageSource.getLevelDataAndDimensions(param0, param1x.dataConfiguration(), var0x, param1x.datapackWorldgen());
            return new WorldLoader.DataLoadOutput<>(var1x.worldData(), var1x.dimensions().dimensionsRegistryAccess());
        }, WorldStem::new);
    }

    public Pair<LevelSettings, WorldCreationContext> recreateWorldData(LevelStorageSource.LevelStorageAccess param0) throws Exception {
        PackRepository var0 = ServerPacksSource.createPackRepository(param0);
        Dynamic<?> var1 = param0.getDataTag();
        WorldLoader.PackConfig var2 = LevelStorageSource.getPackConfig(var1, var0, false);
        return this.loadWorldDataBlocking(
            var2,
            param1 -> {
                Registry<LevelStem> var0x = new MappedRegistry<>(Registries.LEVEL_STEM, Lifecycle.stable()).freeze();
                LevelDataAndDimensions var1x = LevelStorageSource.getLevelDataAndDimensions(var1, param1.dataConfiguration(), var0x, param1.datapackWorldgen());
                return new WorldLoader.DataLoadOutput<>(
                    new Data(var1x.worldData().getLevelSettings(), var1x.worldData().worldGenOptions(), var1x.dimensions().dimensions()),
                    param1.datapackDimensions()
                );
            },
            (param0x, param1, param2, param3) -> {
                param0x.close();
                return Pair.of(
                    param3.levelSettings,
                    new WorldCreationContext(
                        param3.options, new WorldDimensions(param3.existingDimensions), param2, param1, param3.levelSettings.getDataConfiguration()
                    )
                );
            }
        );

        @OnlyIn(Dist.CLIENT)
        record Data(LevelSettings levelSettings, WorldOptions options, Registry<LevelStem> existingDimensions) {
        }

    }

    private <D, R> R loadWorldDataBlocking(WorldLoader.PackConfig param0, WorldLoader.WorldDataSupplier<D> param1, WorldLoader.ResultFactory<D, R> param2) throws Exception {
        WorldLoader.InitConfig var0 = new WorldLoader.InitConfig(param0, Commands.CommandSelection.INTEGRATED, 2);
        CompletableFuture<R> var1 = WorldLoader.load(var0, param1, param2, Util.backgroundExecutor(), this.minecraft);
        this.minecraft.managedBlock(var1::isDone);
        return var1.get();
    }

    private void askForBackup(LevelStorageSource.LevelStorageAccess param0, boolean param1, Runnable param2, Runnable param3) {
        Component var0;
        Component var1;
        if (param1) {
            var0 = Component.translatable("selectWorld.backupQuestion.customized");
            var1 = Component.translatable("selectWorld.backupWarning.customized");
        } else {
            var0 = Component.translatable("selectWorld.backupQuestion.experimental");
            var1 = Component.translatable("selectWorld.backupWarning.experimental");
        }

        this.minecraft.setScreen(new BackupConfirmScreen(param3, (param2x, param3x) -> {
            if (param2x) {
                EditWorldScreen.makeBackupAndShowToast(param0);
            }

            param2.run();
        }, var0, var1, false));
    }

    public static void confirmWorldCreation(Minecraft param0, CreateWorldScreen param1, Lifecycle param2, Runnable param3, boolean param4) {
        BooleanConsumer var0 = param3x -> {
            if (param3x) {
                param3.run();
            } else {
                param0.setScreen(param1);
            }

        };
        if (param4 || param2 == Lifecycle.stable()) {
            param3.run();
        } else if (param2 == Lifecycle.experimental()) {
            param0.setScreen(
                new ConfirmScreen(
                    var0, Component.translatable("selectWorld.warning.experimental.title"), Component.translatable("selectWorld.warning.experimental.question")
                )
            );
        } else {
            param0.setScreen(
                new ConfirmScreen(
                    var0, Component.translatable("selectWorld.warning.deprecated.title"), Component.translatable("selectWorld.warning.deprecated.question")
                )
            );
        }

    }

    public void checkForBackupAndLoad(String param0, Runnable param1) {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
        LevelStorageSource.LevelStorageAccess var0 = this.createWorldAccess(param0);
        if (var0 != null) {
            this.checkForBackupAndLoad(var0, param1);
        }
    }

    private void checkForBackupAndLoad(LevelStorageSource.LevelStorageAccess param0, Runnable param1) {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));

        Dynamic<?> var0;
        LevelSummary var1;
        try {
            var0 = param0.getDataTag();
            var1 = param0.getSummary(var0);
        } catch (NbtException | ReportedNbtException | IOException var101) {
            this.minecraft.setScreen(new RecoverWorldDataScreen(this.minecraft, param2 -> {
                if (param2) {
                    this.checkForBackupAndLoad(param0, param1);
                } else {
                    param0.safeClose();
                    param1.run();
                }

            }, param0));
            return;
        } catch (OutOfMemoryError var111) {
            MemoryReserve.release();
            System.gc();
            String var4 = "Ran out of memory trying to read level data of world folder \"" + param0.getLevelId() + "\"";
            LOGGER.error(LogUtils.FATAL_MARKER, var4);
            OutOfMemoryError var5 = new OutOfMemoryError("Ran out of memory reading level data");
            var5.initCause(var111);
            CrashReport var6 = CrashReport.forThrowable(var5, var4);
            CrashReportCategory var7 = var6.addCategory("World details");
            var7.setDetail("World folder", param0.getLevelId());
            throw new ReportedException(var6);
        }

        if (!var1.isCompatible()) {
            param0.safeClose();
            this.minecraft
                .setScreen(
                    new AlertScreen(
                        param1,
                        Component.translatable("selectWorld.incompatible.title").withColor(-65536),
                        Component.translatable("selectWorld.incompatible.description", var1.getWorldVersionName())
                    )
                );
        } else {
            LevelSummary.BackupStatus var10 = var1.backupStatus();
            if (var10.shouldBackup()) {
                String var11 = "selectWorld.backupQuestion." + var10.getTranslationKey();
                String var12 = "selectWorld.backupWarning." + var10.getTranslationKey();
                MutableComponent var13 = Component.translatable(var11);
                if (var10.isSevere()) {
                    var13.withColor(-2142128);
                }

                Component var14 = Component.translatable(var12, var1.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
                this.minecraft.setScreen(new BackupConfirmScreen(() -> {
                    param0.safeClose();
                    param1.run();
                }, (param3, param4) -> {
                    if (param3) {
                        EditWorldScreen.makeBackupAndShowToast(param0);
                    }

                    this.loadLevel(param0, var0, false, true, param1);
                }, var13, var14, false));
            } else {
                this.loadLevel(param0, var0, false, true, param1);
            }

        }
    }

    private void loadLevel(LevelStorageSource.LevelStorageAccess param0, Dynamic<?> param1, boolean param2, boolean param3, Runnable param4) {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.resource_load")));
        PackRepository var0 = ServerPacksSource.createPackRepository(param0);

        WorldStem var1;
        try {
            var1 = this.loadWorldStem(param1, param2, var0);
        } catch (Exception var11) {
            LOGGER.warn("Failed to load level data or datapacks, can't proceed with server load", (Throwable)var11);
            if (!param2) {
                this.minecraft.setScreen(new DatapackLoadFailureScreen(() -> {
                    param0.safeClose();
                    param4.run();
                }, () -> this.loadLevel(param0, param1, true, param3, param4)));
            } else {
                param0.safeClose();
                this.minecraft
                    .setScreen(
                        new AlertScreen(
                            param4,
                            Component.translatable("datapackFailure.safeMode.failed.title"),
                            Component.translatable("datapackFailure.safeMode.failed.description"),
                            CommonComponents.GUI_BACK,
                            true
                        )
                    );
            }

            return;
        }

        WorldData var4 = var1.worldData();
        boolean var5 = var4.worldGenOptions().isOldCustomizedWorld();
        boolean var6 = var4.worldGenSettingsLifecycle() != Lifecycle.stable();
        if (!param3 || !var5 && !var6) {
            this.minecraft.getDownloadedPackSource().loadBundledResourcePack(param0).thenApply(param0x -> true).exceptionallyComposeAsync(param0x -> {
                LOGGER.warn("Failed to load pack: ", param0x);
                return this.promptBundledPackLoadFailure();
            }, this.minecraft).thenAcceptAsync(param4x -> {
                if (param4x) {
                    this.minecraft.doWorldLoad(param0, var0, var1, false);
                } else {
                    var1.close();
                    param0.safeClose();
                    this.minecraft.getDownloadedPackSource().clearServerPack().thenRunAsync(param4, this.minecraft);
                }

            }, this.minecraft).exceptionally(param0x -> {
                this.minecraft.delayCrash(CrashReport.forThrowable(param0x, "Load world"));
                return null;
            });
        } else {
            this.askForBackup(param0, var5, () -> this.loadLevel(param0, param1, param2, false, param4), () -> {
                param0.safeClose();
                param4.run();
            });
            var1.close();
        }
    }

    private CompletableFuture<Boolean> promptBundledPackLoadFailure() {
        CompletableFuture<Boolean> var0 = new CompletableFuture<>();
        this.minecraft
            .setScreen(
                new ConfirmScreen(
                    var0::complete,
                    Component.translatable("multiplayer.texturePrompt.failure.line1"),
                    Component.translatable("multiplayer.texturePrompt.failure.line2"),
                    CommonComponents.GUI_PROCEED,
                    CommonComponents.GUI_CANCEL
                )
            );
        return var0;
    }
}
