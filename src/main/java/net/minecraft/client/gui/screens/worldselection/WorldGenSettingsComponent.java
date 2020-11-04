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
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
public class WorldGenSettingsComponent implements TickableWidget, Widget {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
    private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
    private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
    private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
    private Font font;
    private int width;
    private EditBox seedEdit;
    private Button featuresButton;
    public Button bonusItemsButton;
    private Button typeButton;
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

    public void init(final CreateWorldScreen param0, Minecraft param1, Font param2) {
        this.font = param2;
        this.width = param0.width;
        this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
        this.seedEdit.setValue(toString(this.seed));
        this.seedEdit.setResponder(param0x -> this.seed = this.parseSeed());
        param0.addWidget(this.seedEdit);
        int var0 = this.width / 2 - 155;
        int var1 = this.width / 2 + 5;
        this.featuresButton = param0.addButton(new Button(var0, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), param0x -> {
            this.settings = this.settings.withFeaturesToggled();
            param0x.queueNarration(250);
        }) {
            @Override
            public Component getMessage() {
                return CommonComponents.optionStatus(super.getMessage(), WorldGenSettingsComponent.this.settings.generateFeatures());
            }

            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.mapFeatures.info"));
            }
        });
        this.featuresButton.visible = false;
        this.typeButton = param0.addButton(
            new Button(var1, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), param1x -> {
                while(this.preset.isPresent()) {
                    int var0x = WorldPreset.PRESETS.indexOf(this.preset.get()) + 1;
                    if (var0x >= WorldPreset.PRESETS.size()) {
                        var0x = 0;
                    }
    
                    WorldPreset var1x = WorldPreset.PRESETS.get(var0x);
                    this.preset = Optional.of(var1x);
                    this.settings = var1x.create(
                        this.registryHolder, this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest()
                    );
                    if (!this.settings.isDebug() || Screen.hasShiftDown()) {
                        break;
                    }
                }
    
                param0.updateDisplayOptions();
                param1x.queueNarration(250);
            }) {
                @Override
                public Component getMessage() {
                    return super.getMessage()
                        .copy()
                        .append(" ")
                        .append(WorldGenSettingsComponent.this.preset.map(WorldPreset::description).orElse(WorldGenSettingsComponent.CUSTOM_WORLD_DESCRIPTION));
                }
    
                @Override
                protected MutableComponent createNarrationMessage() {
                    return Objects.equals(WorldGenSettingsComponent.this.preset, Optional.of(WorldPreset.AMPLIFIED))
                        ? super.createNarrationMessage().append(". ").append(WorldGenSettingsComponent.AMPLIFIED_HELP_TEXT)
                        : super.createNarrationMessage();
                }
            }
        );
        this.typeButton.visible = false;
        this.typeButton.active = this.preset.isPresent();
        this.customizeTypeButton = param0.addButton(new Button(var1, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), param2x -> {
            WorldPreset.PresetEditor var0x = WorldPreset.EDITORS.get(this.preset);
            if (var0x != null) {
                param1.setScreen(var0x.createEditScreen(param0, this.settings));
            }

        }));
        this.customizeTypeButton.visible = false;
        this.bonusItemsButton = param0.addButton(new Button(var0, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), param0x -> {
            this.settings = this.settings.withBonusChestToggled();
            param0x.queueNarration(250);
        }) {
            @Override
            public Component getMessage() {
                return CommonComponents.optionStatus(super.getMessage(), WorldGenSettingsComponent.this.settings.generateBonusChest() && !param0.hardCore);
            }
        });
        this.bonusItemsButton.visible = false;
        this.importSettingsButton = param0.addButton(
            new Button(
                var0,
                185,
                150,
                20,
                new TranslatableComponent("selectWorld.import_worldgen_settings"),
                param2x -> {
                    TranslatableComponent var0x = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
                    String var1x = TinyFileDialogs.tinyfd_openFileDialog(var0x.getString(), null, null, null, false);
                    if (var1x != null) {
                        RegistryAccess.RegistryHolder var2x = RegistryAccess.builtin();
                        PackRepository var3x = new PackRepository(
                            PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(param0.getTempDataPackDir().toFile(), PackSource.WORLD)
                        );
        
                        ServerResources var5;
                        try {
                            MinecraftServer.configurePackRepository(var3x, param0.dataPacks, false);
                            CompletableFuture<ServerResources> var4 = ServerResources.loadResources(
                                var3x.openAllSelected(), Commands.CommandSelection.INTEGRATED, 2, Util.backgroundExecutor(), param1
                            );
                            param1.managedBlock(var4::isDone);
                            var5 = var4.get();
                        } catch (ExecutionException | InterruptedException var25) {
                            LOGGER.error("Error loading data packs when importing world settings", (Throwable)var25);
                            Component var7 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                            Component var8 = new TextComponent(var25.getMessage());
                            param1.getToasts().addToast(SystemToast.multiline(param1, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var7, var8));
                            var3x.close();
                            return;
                        }
        
                        RegistryReadOps<JsonElement> var10 = RegistryReadOps.create(JsonOps.INSTANCE, var5.getResourceManager(), var2x);
                        JsonParser var11 = new JsonParser();
        
                        DataResult<WorldGenSettings> var14;
                        try (BufferedReader var12 = Files.newBufferedReader(Paths.get(var1x))) {
                            JsonElement var13 = var11.parse(var12);
                            var14 = WorldGenSettings.CODEC.parse(var10, var13);
                        } catch (JsonIOException | JsonSyntaxException | IOException var27) {
                            var14 = DataResult.error("Failed to parse file: " + var27.getMessage());
                        }
        
                        if (var14.error().isPresent()) {
                            Component var18 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                            String var19 = var14.error().get().message();
                            LOGGER.error("Error parsing world settings: {}", var19);
                            Component var20 = new TextComponent(var19);
                            param1.getToasts().addToast(SystemToast.multiline(param1, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var18, var20));
                        }
        
                        var5.close();
                        Lifecycle var21 = var14.lifecycle();
                        var14.resultOrPartial(LOGGER::error)
                            .ifPresent(
                                param4 -> {
                                    BooleanConsumer var0xx = param4x -> {
                                        param1.setScreen(param0);
                                        if (param4x) {
                                            this.importSettings(var2x, param4);
                                        }
                
                                    };
                                    if (var21 == Lifecycle.stable()) {
                                        this.importSettings(var2x, param4);
                                    } else if (var21 == Lifecycle.experimental()) {
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
        this.amplifiedWorldInfo = MultiLineLabel.create(param2, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
    }

    private void importSettings(RegistryAccess.RegistryHolder param0, WorldGenSettings param1) {
        this.registryHolder = param0;
        this.settings = param1;
        this.preset = WorldPreset.of(param1);
        this.seed = OptionalLong.of(param1.seed());
        this.seedEdit.setValue(toString(this.seed));
        this.typeButton.active = this.preset.isPresent();
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.featuresButton.visible) {
            this.font.drawShadow(param0, MAP_FEATURES_INFO, (float)(this.width / 2 - 150), 122.0F, -6250336);
        }

        this.seedEdit.render(param0, param1, param2, param3);
        if (this.preset.equals(Optional.of(WorldPreset.AMPLIFIED))) {
            this.amplifiedWorldInfo.renderLeftAligned(param0, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
        }

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

    public void setDisplayOptions(boolean param0) {
        this.typeButton.visible = param0;
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

    public RegistryAccess.RegistryHolder registryHolder() {
        return this.registryHolder;
    }

    void updateDataPacks(ServerResources param0) {
        RegistryAccess.RegistryHolder var0 = RegistryAccess.builtin();
        RegistryWriteOps<JsonElement> var1 = RegistryWriteOps.create(JsonOps.INSTANCE, this.registryHolder);
        RegistryReadOps<JsonElement> var2 = RegistryReadOps.create(JsonOps.INSTANCE, param0.getResourceManager(), var0);
        DataResult<WorldGenSettings> var3 = WorldGenSettings.CODEC
            .encodeStart(var1, this.settings)
            .flatMap(param1 -> WorldGenSettings.CODEC.parse(var2, param1));
        var3.resultOrPartial(Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error)).ifPresent(param1 -> {
            this.settings = param1;
            this.registryHolder = var0;
        });
    }
}
