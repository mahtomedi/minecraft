package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonWriter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult.PartialResult;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.WorldStem;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class EditWorldScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson WORLD_GEN_SETTINGS_GSON = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
    private Button renameButton;
    private final BooleanConsumer callback;
    private EditBox nameEdit;
    private final LevelStorageSource.LevelStorageAccess levelAccess;

    public EditWorldScreen(BooleanConsumer param0, LevelStorageSource.LevelStorageAccess param1) {
        super(Component.translatable("selectWorld.edit.title"));
        this.callback = param0;
        this.levelAccess = param1;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        Button var0 = this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.resetIcon"), param0 -> {
            this.levelAccess.getIconFile().ifPresent(param0x -> FileUtils.deleteQuietly(param0x.toFile()));
            param0.active = false;
        }).bounds(this.width / 2 - 100, this.height / 4 + 0 + 5, 200, 20).build());
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.edit.openFolder"),
                    param0 -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())
                )
                .bounds(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20)
                .build()
        );
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backup"), param0 -> {
            boolean var0x = makeBackupAndShowToast(this.levelAccess);
            this.callback.accept(!var0x);
        }).bounds(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("selectWorld.edit.backupFolder"), param0 -> {
            LevelStorageSource var0x = this.minecraft.getLevelSource();
            Path var1x = var0x.getBackupPath();

            try {
                Files.createDirectories(Files.exists(var1x) ? var1x.toRealPath() : var1x);
            } catch (IOException var5) {
                throw new RuntimeException(var5);
            }

            Util.getPlatform().openFile(var1x.toFile());
        }).bounds(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20).build());
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.edit.optimize"),
                    param0 -> this.minecraft
                            .setScreen(
                                new BackupConfirmScreen(
                                    this,
                                    (param0x, param1) -> {
                                        if (param0x) {
                                            makeBackupAndShowToast(this.levelAccess);
                                        }
                        
                                        this.minecraft
                                            .setScreen(
                                                OptimizeWorldScreen.create(
                                                    this.minecraft, this.callback, this.minecraft.getFixerUpper(), this.levelAccess, param1
                                                )
                                            );
                                    },
                                    Component.translatable("optimizeWorld.confirm.title"),
                                    Component.translatable("optimizeWorld.confirm.description"),
                                    true
                                )
                            )
                )
                .bounds(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.edit.export_worldgen_settings"),
                    param0 -> {
                        DataResult<String> var4;
                        try (WorldStem var0x = this.minecraft.createWorldOpenFlows().loadWorldStem(this.levelAccess, false)) {
                            RegistryAccess.Frozen var1x = var0x.registries().compositeAccess();
                            DynamicOps<JsonElement> var2x = RegistryOps.create(JsonOps.INSTANCE, var1x);
                            DataResult<JsonElement> var3x = WorldGenSettings.encode(var2x, var0x.worldData().worldGenOptions(), var1x);
                            var4 = var3x.flatMap(param0x -> {
                                Path var0x = this.levelAccess.getLevelPath(LevelResource.ROOT).resolve("worldgen_settings_export.json");
            
                                try (JsonWriter var2xx = WORLD_GEN_SETTINGS_GSON.newJsonWriter(Files.newBufferedWriter(var0x, StandardCharsets.UTF_8))) {
                                    WORLD_GEN_SETTINGS_GSON.toJson(param0x, var2xx);
                                } catch (JsonIOException | IOException var8x) {
                                    return DataResult.error("Error writing file: " + var8x.getMessage());
                                }
            
                                return DataResult.success(var0x.toString());
                            });
                        } catch (Exception var91) {
                            LOGGER.warn("Could not parse level data", (Throwable)var91);
                            var4 = DataResult.error("Could not parse level data: " + var91.getMessage());
                        }
            
                        Component var8 = Component.literal(var4.get().map(Function.identity(), PartialResult::message));
                        Component var9 = Component.translatable(
                            var4.result().isPresent()
                                ? "selectWorld.edit.export_worldgen_settings.success"
                                : "selectWorld.edit.export_worldgen_settings.failure"
                        );
                        var4.error().ifPresent(param0x -> LOGGER.error("Error exporting world settings: {}", param0x));
                        this.minecraft
                            .getToasts()
                            .addToast(SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var9, var8));
                    }
                )
                .bounds(this.width / 2 - 100, this.height / 4 + 120 + 5, 200, 20)
                .build()
        );
        this.renameButton = this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.edit.save"), param0 -> this.onRename())
                .bounds(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.callback.accept(false))
                .bounds(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20)
                .build()
        );
        var0.active = this.levelAccess.getIconFile().filter(param0 -> Files.isRegularFile(param0)).isPresent();
        LevelSummary var1 = this.levelAccess.getSummary();
        String var2 = var1 == null ? "" : var1.getLevelName();
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 38, 200, 20, Component.translatable("selectWorld.enterName"));
        this.nameEdit.setValue(var2);
        this.nameEdit.setResponder(param0 -> this.renameButton.active = !param0.trim().isEmpty());
        this.addWidget(this.nameEdit);
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.nameEdit.getValue();
        this.init(param0, param1, param2);
        this.nameEdit.setValue(var0);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onRename() {
        try {
            this.levelAccess.renameLevel(this.nameEdit.getValue().trim());
            this.callback.accept(true);
        } catch (IOException var2) {
            LOGGER.error("Failed to access world '{}'", this.levelAccess.getLevelId(), var2);
            SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
            this.callback.accept(true);
        }

    }

    public static void makeBackupAndShowToast(LevelStorageSource param0, String param1) {
        boolean var0 = false;

        try (LevelStorageSource.LevelStorageAccess var1 = param0.createAccess(param1)) {
            var0 = true;
            makeBackupAndShowToast(var1);
        } catch (IOException var8) {
            if (!var0) {
                SystemToast.onWorldAccessFailure(Minecraft.getInstance(), param1);
            }

            LOGGER.warn("Failed to create backup of level {}", param1, var8);
        }

    }

    public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess param0) {
        long var0 = 0L;
        IOException var1 = null;

        try {
            var0 = param0.makeWorldBackup();
        } catch (IOException var61) {
            var1 = var61;
        }

        if (var1 != null) {
            Component var3 = Component.translatable("selectWorld.edit.backupFailed");
            Component var4 = Component.literal(var1.getMessage());
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, var3, var4));
            return false;
        } else {
            Component var5 = Component.translatable("selectWorld.edit.backupCreated", param0.getLevelId());
            Component var6 = Component.translatable("selectWorld.edit.backupSize", Mth.ceil((double)var0 / 1048576.0));
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, var5, var6));
            return true;
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 15, 16777215);
        drawString(param0, this.font, NAME_LABEL, this.width / 2 - 100, 24, 10526880);
        this.nameEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
