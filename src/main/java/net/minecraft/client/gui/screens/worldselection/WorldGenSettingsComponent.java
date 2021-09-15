package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@OnlyIn(Dist.CLIENT)
public class WorldGenSettingsComponent implements Widget {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
    private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
    private static final Component SELECT_FILE_PROMPT = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
    private Font font;
    private int width;
    private EditBox seedEdit;
    private CycleButton<Boolean> featuresButton;
    private CycleButton<Boolean> bonusItemsButton;
    private CycleButton<WorldPreset> typeButton;
    private Button customWorldDummyButton;
    private Button customizeTypeButton;
    private Button importSettingsButton;
    private RegistryAccess.RegistryHolder registryHolder;
    private WorldGenSettings settings;
    private Optional<WorldPreset> preset;
    private OptionalLong seed;

    public WorldGenSettingsComponent(RegistryAccess.RegistryHolder param0, WorldGenSettings param1, Optional<WorldPreset> param2, OptionalLong param3) {
        this.registryHolder = param0;
        this.settings = param1;
        this.preset = param2;
        this.seed = param3;
    }

    public void init(CreateWorldScreen param0, Minecraft param1, Font param2) {
        this.font = param2;
        this.width = param0.width;
        this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
        this.seedEdit.setValue(toString(this.seed));
        this.seedEdit.setResponder(param0x -> this.seed = this.parseSeed());
        param0.addWidget(this.seedEdit);
        int var0 = this.width / 2 - 155;
        int var1 = this.width / 2 + 5;
        this.featuresButton = param0.addRenderableWidget(
            CycleButton.onOffBuilder(this.settings.generateFeatures())
                .withCustomNarration(
                    param0x -> CommonComponents.joinForNarration(
                            param0x.createDefaultNarrationMessage(), new TranslatableComponent("selectWorld.mapFeatures.info")
                        )
                )
                .create(
                    var0,
                    100,
                    150,
                    20,
                    new TranslatableComponent("selectWorld.mapFeatures"),
                    (param0x, param1x) -> this.settings = this.settings.withFeaturesToggled()
                )
        );
        this.featuresButton.visible = false;
        this.typeButton = param0.addRenderableWidget(
            CycleButton.builder(WorldPreset::description)
                .withValues(WorldPreset.PRESETS.stream().filter(WorldPreset::isVisibleByDefault).collect(Collectors.toList()), WorldPreset.PRESETS)
                .create(var1, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), (param1x, param2x) -> {
                    this.preset = Optional.of(param2x);
                    this.settings = param2x.create(
                        this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest()
                    );
                    param0.refreshWorldGenSettingsVisibility();
                })
        );
        this.preset.ifPresent(this.typeButton::setValue);
        this.typeButton.visible = false;
        this.customWorldDummyButton = param0.addRenderableWidget(
            new Button(
                var1, 100, 150, 20, CommonComponents.optionNameValue(new TranslatableComponent("selectWorld.mapType"), CUSTOM_WORLD_DESCRIPTION), param0x -> {
                }
            )
        );
        this.customWorldDummyButton.active = false;
        this.customWorldDummyButton.visible = false;
        this.customizeTypeButton = param0.addRenderableWidget(
            new Button(var1, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), param2x -> {
                WorldPreset.PresetEditor var0x = WorldPreset.EDITORS.get(this.preset);
                if (var0x != null) {
                    param1.setScreen(var0x.createEditScreen(param0, this.settings));
                }
    
            })
        );
        this.customizeTypeButton.visible = false;
        this.bonusItemsButton = param0.addRenderableWidget(
            CycleButton.onOffBuilder(this.settings.generateBonusChest() && !param0.hardCore)
                .create(
                    var0,
                    151,
                    150,
                    20,
                    new TranslatableComponent("selectWorld.bonusItems"),
                    (param0x, param1x) -> this.settings = this.settings.withBonusChestToggled()
                )
        );
        this.bonusItemsButton.visible = false;
        this.importSettingsButton = param0.addRenderableWidget(
            new Button(
                var0,
                185,
                150,
                20,
                new TranslatableComponent("selectWorld.import_worldgen_settings"),
                param2x -> {
                    String var0x = TinyFileDialogs.tinyfd_openFileDialog(SELECT_FILE_PROMPT.getString(), null, null, null, false);
                    if (var0x != null) {
                        RegistryAccess.RegistryHolder var1x = RegistryAccess.builtin();
                        PackRepository var2x = new PackRepository(
                            PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(param0.getTempDataPackDir().toFile(), PackSource.WORLD)
                        );
        
                        ServerResources var4;
                        try {
                            MinecraftServer.configurePackRepository(var2x, param0.dataPacks, false);
                            CompletableFuture<ServerResources> var3 = ServerResources.loadResources(
                                var2x.openAllSelected(), var1x, Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), param1
                            );
                            param1.managedBlock(var3::isDone);
                            var4 = var3.get();
                        } catch (ExecutionException | InterruptedException var15) {
                            LOGGER.error("Error loading data packs when importing world settings", (Throwable)var15);
                            Component var6 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                            Component var7 = new TextComponent(var15.getMessage());
                            param1.getToasts().addToast(SystemToast.multiline(param1, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var6, var7));
                            var2x.close();
                            return;
                        }
        
                        RegistryReadOps<JsonElement> var9 = RegistryReadOps.createAndLoad(JsonOps.INSTANCE, var4.getResourceManager(), var1x);
                        JsonParser var10 = new JsonParser();
        
                        DataResult<WorldGenSettings> var13;
                        try (BufferedReader var11 = Files.newBufferedReader(Paths.get(var0x))) {
                            JsonElement var12 = var10.parse(var11);
                            var13 = WorldGenSettings.CODEC.parse(var9, var12);
                        } catch (JsonIOException | JsonSyntaxException | IOException var17) {
                            var13 = DataResult.error("Failed to parse file: " + var17.getMessage());
                        }
        
                        if (var13.error().isPresent()) {
                            Component var17 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                            String var18 = var13.error().get().message();
                            LOGGER.error("Error parsing world settings: {}", var18);
                            Component var19 = new TextComponent(var18);
                            param1.getToasts().addToast(SystemToast.multiline(param1, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var17, var19));
                        }
        
                        var4.close();
                        Lifecycle var20 = var13.lifecycle();
                        var13.resultOrPartial(LOGGER::error)
                            .ifPresent(
                                param4 -> {
                                    BooleanConsumer var0xx = param4x -> {
                                        param1.setScreen(param0);
                                        if (param4x) {
                                            this.importSettings(var1x, param4);
                                        }
                
                                    };
                                    if (var20 == Lifecycle.stable()) {
                                        this.importSettings(var1x, param4);
                                    } else if (var20 == Lifecycle.experimental()) {
                                        param1.setScreen(
                                            new ConfirmScreen(
                                                var0xx,
                                                new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.title"),
                                                new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.question")
                                            )
                                        );
                                    } else {
                                        param1.setScreen(
                                            new ConfirmScreen(
                                                var0xx,
                                                new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.title"),
                                                new TranslatableComponent("selectWorld.import_worldgen_settings.deprecated.question")
                                            )
                                        );
                                    }
                
                                }
                            );
                    }
                }
            )
        );
        this.importSettingsButton.visible = false;
    }

    private void importSettings(RegistryAccess.RegistryHolder param0, WorldGenSettings param1) {
        this.registryHolder = param0;
        this.settings = param1;
        this.preset = WorldPreset.of(param1);
        this.selectWorldTypeButton(true);
        this.seed = OptionalLong.of(param1.seed());
        this.seedEdit.setValue(toString(this.seed));
    }

    public void tick() {
        this.seedEdit.tick();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.featuresButton.visible) {
            this.font.drawShadow(param0, MAP_FEATURES_INFO, (float)(this.width / 2 - 150), 122.0F, -6250336);
        }

        this.seedEdit.render(param0, param1, param2, param3);
    }

    protected void updateSettings(WorldGenSettings param0) {
        this.settings = param0;
    }

    private static String toString(OptionalLong param0) {
        return param0.isPresent() ? Long.toString(param0.getAsLong()) : "";
    }

    private static OptionalLong parseLong(String param0) {
        try {
            return OptionalLong.of(Long.parseLong(param0));
        } catch (NumberFormatException var2) {
            return OptionalLong.empty();
        }
    }

    public WorldGenSettings makeSettings(boolean param0) {
        OptionalLong var0 = this.parseSeed();
        return this.settings.withSeed(param0, var0);
    }

    private OptionalLong parseSeed() {
        String var0 = this.seedEdit.getValue();
        OptionalLong var1;
        if (StringUtils.isEmpty(var0)) {
            var1 = OptionalLong.empty();
        } else {
            OptionalLong var2 = parseLong(var0);
            if (var2.isPresent() && var2.getAsLong() != 0L) {
                var1 = var2;
            } else {
                var1 = OptionalLong.of((long)var0.hashCode());
            }
        }

        return var1;
    }

    public boolean isDebug() {
        return this.settings.isDebug();
    }

    public void setVisibility(boolean param0) {
        this.selectWorldTypeButton(param0);
        if (this.settings.isDebug()) {
            this.featuresButton.visible = false;
            this.bonusItemsButton.visible = false;
            this.customizeTypeButton.visible = false;
            this.importSettingsButton.visible = false;
        } else {
            this.featuresButton.visible = param0;
            this.bonusItemsButton.visible = param0;
            this.customizeTypeButton.visible = param0 && WorldPreset.EDITORS.containsKey(this.preset);
            this.importSettingsButton.visible = param0;
        }

        this.seedEdit.setVisible(param0);
    }

    private void selectWorldTypeButton(boolean param0) {
        if (this.preset.isPresent()) {
            this.typeButton.visible = param0;
            this.customWorldDummyButton.visible = false;
        } else {
            this.typeButton.visible = false;
            this.customWorldDummyButton.visible = param0;
        }

    }

    public RegistryAccess.RegistryHolder registryHolder() {
        return this.registryHolder;
    }

    void updateDataPacks(ServerResources param0) {
        RegistryAccess.RegistryHolder var0 = RegistryAccess.builtin();
        RegistryWriteOps<JsonElement> var1 = RegistryWriteOps.create(JsonOps.INSTANCE, this.registryHolder);
        RegistryReadOps<JsonElement> var2 = RegistryReadOps.createAndLoad(JsonOps.INSTANCE, param0.getResourceManager(), var0);
        DataResult<WorldGenSettings> var3 = WorldGenSettings.CODEC
            .encodeStart(var1, this.settings)
            .flatMap(param1 -> WorldGenSettings.CODEC.parse(var2, param1));
        var3.resultOrPartial(Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error)).ifPresent(param1 -> {
            this.settings = param1;
            this.registryHolder = var0;
        });
    }

    public void switchToHardcore() {
        this.bonusItemsButton.active = false;
        this.bonusItemsButton.setValue(false);
    }

    public void switchOutOfHardcode() {
        this.bonusItemsButton.active = true;
        this.bonusItemsButton.setValue(this.settings.generateBonusChest());
    }
}
