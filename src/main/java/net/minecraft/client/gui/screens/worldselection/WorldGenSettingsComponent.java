package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldGenSettingsComponent implements Widget {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
    private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
    private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
    private static final Component SELECT_FILE_PROMPT = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
    private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
    private Font font;
    private int width;
    private EditBox seedEdit;
    private CycleButton<Boolean> featuresButton;
    private CycleButton<Boolean> bonusItemsButton;
    private CycleButton<WorldPreset> typeButton;
    private Button customWorldDummyButton;
    private Button customizeTypeButton;
    private Button importSettingsButton;
    private RegistryAccess.Frozen registryHolder;
    private WorldGenSettings settings;
    private Optional<WorldPreset> preset;
    private OptionalLong seed;

    public WorldGenSettingsComponent(RegistryAccess.Frozen param0, WorldGenSettings param1, Optional<WorldPreset> param2, OptionalLong param3) {
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
        this.seedEdit.setResponder(param0x -> this.seed = WorldGenSettings.parseSeed(this.seedEdit.getValue()));
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
                .withCustomNarration(
                    param0x -> param0x.getValue() == WorldPreset.AMPLIFIED
                            ? CommonComponents.joinForNarration(param0x.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT)
                            : param0x.createDefaultNarrationMessage()
                )
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
                        RegistryAccess.Writable var1x = RegistryAccess.builtinCopy();
        
                        DataResult<WorldGenSettings> var7;
                        try (PackRepository var2 = new PackRepository(
                                PackType.SERVER_DATA,
                                new ServerPacksSource(),
                                new FolderRepositorySource(param0.getTempDataPackDir().toFile(), PackSource.WORLD)
                            )) {
                            MinecraftServer.configurePackRepository(var2, param0.dataPacks, false);
        
                            try (CloseableResourceManager var3x = new MultiPackResourceManager(PackType.SERVER_DATA, var2.openAllSelected())) {
                                DynamicOps<JsonElement> var4x = RegistryOps.createAndLoad(JsonOps.INSTANCE, var1x, var3x);
        
                                try (BufferedReader var5 = Files.newBufferedReader(Paths.get(var0x))) {
                                    JsonElement var6 = JsonParser.parseReader(var5);
                                    var7 = WorldGenSettings.CODEC.parse(var4x, var6);
                                } catch (Exception var17) {
                                    var7 = DataResult.error("Failed to parse file: " + var17.getMessage());
                                }
        
                                if (var7.error().isPresent()) {
                                    Component var11 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                                    String var12 = var7.error().get().message();
                                    LOGGER.error("Error parsing world settings: {}", var12);
                                    Component var13 = new TextComponent(var12);
                                    param1.getToasts()
                                        .addToast(SystemToast.multiline(param1, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var11, var13));
                                    return;
                                }
                            }
                        }
        
                        Lifecycle var20 = var7.lifecycle();
                        var7.resultOrPartial(LOGGER::error)
                            .ifPresent(
                                param4 -> {
                                    BooleanConsumer var0xx = param4x -> {
                                        param1.setScreen(param0);
                                        if (param4x) {
                                            this.importSettings(var1x.freeze(), param4);
                                        }
                
                                    };
                                    if (var20 == Lifecycle.stable()) {
                                        this.importSettings(var1x.freeze(), param4);
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
        this.amplifiedWorldInfo = MultiLineLabel.create(param2, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
    }

    private void importSettings(RegistryAccess.Frozen param0, WorldGenSettings param1) {
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

    public WorldGenSettings makeSettings(boolean param0) {
        OptionalLong var0 = WorldGenSettings.parseSeed(this.seedEdit.getValue());
        return this.settings.withSeed(param0, var0);
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

    public RegistryAccess registryHolder() {
        return this.registryHolder;
    }

    void updateDataPacks(WorldStem param0) {
        this.settings = param0.worldData().worldGenSettings();
        this.registryHolder = param0.registryAccess();
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
