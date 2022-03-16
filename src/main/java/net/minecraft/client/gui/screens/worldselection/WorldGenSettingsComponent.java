package net.minecraft.client.gui.screens.worldselection;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.DataResult.PartialResult;
import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
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
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldGenSettingsComponent implements Widget {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component CUSTOM_WORLD_DESCRIPTION = new TranslatableComponent("generator.custom");
    private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.minecraft.amplified.info");
    private static final Component MAP_FEATURES_INFO = new TranslatableComponent("selectWorld.mapFeatures.info");
    private static final Component SELECT_FILE_PROMPT = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
    private MultiLineLabel amplifiedWorldInfo = MultiLineLabel.EMPTY;
    private Font font;
    private int width;
    private EditBox seedEdit;
    private CycleButton<Boolean> featuresButton;
    private CycleButton<Boolean> bonusItemsButton;
    private CycleButton<Holder<WorldPreset>> typeButton;
    private Button customWorldDummyButton;
    private Button customizeTypeButton;
    private Button importSettingsButton;
    private WorldCreationContext settings;
    private Optional<Holder<WorldPreset>> preset;
    private OptionalLong seed;

    public WorldGenSettingsComponent(WorldCreationContext param0, Optional<ResourceKey<WorldPreset>> param1, OptionalLong param2) {
        this.settings = param0;
        this.preset = findPreset(param0, param1);
        this.seed = param2;
    }

    private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext param0, Optional<ResourceKey<WorldPreset>> param1) {
        return param1.flatMap(param1x -> param0.registryAccess().<WorldPreset>registryOrThrow(Registry.WORLD_PRESET_REGISTRY).getHolder(param1x));
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
            CycleButton.onOffBuilder(this.settings.worldGenSettings().generateStructures())
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
                    (param0x, param1x) -> this.updateSettings(WorldGenSettings::withStructuresToggled)
                )
        );
        this.featuresButton.visible = false;
        Registry<WorldPreset> var2 = this.settings.registryAccess().registryOrThrow(Registry.WORLD_PRESET_REGISTRY);
        List<Holder<WorldPreset>> var3 = getNonEmptyList(var2, WorldPresetTags.NORMAL).orElseGet(() -> var2.holders().collect(Collectors.toUnmodifiableList()));
        List<Holder<WorldPreset>> var4 = getNonEmptyList(var2, WorldPresetTags.EXTENDED).orElse(var3);
        this.typeButton = param0.addRenderableWidget(
            CycleButton.builder(WorldGenSettingsComponent::describePreset)
                .withValues(var3, var4)
                .withCustomNarration(
                    param0x -> isAmplified(param0x.getValue())
                            ? CommonComponents.joinForNarration(param0x.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT)
                            : param0x.createDefaultNarrationMessage()
                )
                .create(var1, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), (param1x, param2x) -> {
                    this.preset = Optional.of(param2x);
                    this.updateSettings(param1xx -> ((WorldPreset)param2x.value()).recreateWorldGenSettings(param1xx));
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
                PresetEditor var0x = PresetEditor.EDITORS.get(this.preset.flatMap(Holder::unwrapKey));
                if (var0x != null) {
                    param1.setScreen(var0x.createEditScreen(param0, this.settings));
                }
    
            })
        );
        this.customizeTypeButton.visible = false;
        this.bonusItemsButton = param0.addRenderableWidget(
            CycleButton.onOffBuilder(this.settings.worldGenSettings().generateBonusChest() && !param0.hardCore)
                .create(
                    var0,
                    151,
                    150,
                    20,
                    new TranslatableComponent("selectWorld.bonusItems"),
                    (param0x, param1x) -> this.updateSettings(WorldGenSettings::withBonusChestToggled)
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
                        DynamicOps<JsonElement> var1x = RegistryOps.create(JsonOps.INSTANCE, this.settings.registryAccess());
        
                        DataResult<WorldGenSettings> var4x;
                        try (BufferedReader var2x = Files.newBufferedReader(Paths.get(var0x))) {
                            JsonElement var3x = JsonParser.parseReader(var2x);
                            var7x = WorldGenSettings.CODEC.parse(var1x, var3x);
                        } catch (Exception var12) {
                            var7x = DataResult.error("Failed to parse file: " + var12.getMessage());
                        }
        
                        if (var7x.error().isPresent()) {
                            Component var8 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                            String var9 = ((PartialResult)var7x.error().get()).message();
                            LOGGER.error("Error parsing world settings: {}", var9);
                            Component var10 = new TextComponent(var9);
                            param1.getToasts().addToast(SystemToast.multiline(param1, SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var8, var10));
                        } else {
                            Lifecycle var11 = var7x.lifecycle();
                            var7x.resultOrPartial(LOGGER::error)
                                .ifPresent(param3 -> WorldOpenFlows.confirmWorldCreation(param1, param0, var11, () -> this.importSettings(param3)));
                        }
                    }
                }
            )
        );
        this.importSettingsButton.visible = false;
        this.amplifiedWorldInfo = MultiLineLabel.create(param2, AMPLIFIED_HELP_TEXT, this.typeButton.getWidth());
    }

    private static Optional<List<Holder<WorldPreset>>> getNonEmptyList(Registry<WorldPreset> param0, TagKey<WorldPreset> param1) {
        return param0.getTag(param1).map(param0x -> param0x.stream().toList()).filter(param0x -> !param0x.isEmpty());
    }

    private static boolean isAmplified(Holder<WorldPreset> param0) {
        return param0.unwrapKey().filter(param0x -> param0x.equals(WorldPresets.AMPLIFIED)).isPresent();
    }

    private static Component describePreset(Holder<WorldPreset> param0x) {
        return param0x.unwrapKey().map(param0xx -> new TranslatableComponent(param0xx.location().toLanguageKey("generator"))).orElse(CUSTOM_WORLD_DESCRIPTION);
    }

    private void importSettings(WorldGenSettings param0) {
        this.settings = this.settings.withSettings(param0);
        this.preset = findPreset(this.settings, WorldPresets.fromSettings(param0));
        this.selectWorldTypeButton(true);
        this.seed = OptionalLong.of(param0.seed());
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
        if (this.preset.filter(WorldGenSettingsComponent::isAmplified).isPresent()) {
            this.amplifiedWorldInfo.renderLeftAligned(param0, this.typeButton.x + 2, this.typeButton.y + 22, 9, 10526880);
        }

    }

    void updateSettings(WorldCreationContext.SimpleUpdater param0) {
        this.settings = this.settings.withSettings(param0);
    }

    void updateSettings(WorldCreationContext.Updater param0) {
        this.settings = this.settings.withSettings(param0);
    }

    void updateSettings(WorldCreationContext param0) {
        this.settings = param0;
    }

    private static String toString(OptionalLong param0) {
        return param0.isPresent() ? Long.toString(param0.getAsLong()) : "";
    }

    public WorldCreationContext createFinalSettings(boolean param0) {
        OptionalLong var0 = WorldGenSettings.parseSeed(this.seedEdit.getValue());
        return this.settings.withSettings(param2 -> param2.withSeed(param0, var0));
    }

    public boolean isDebug() {
        return this.settings.worldGenSettings().isDebug();
    }

    public void setVisibility(boolean param0) {
        this.selectWorldTypeButton(param0);
        if (this.isDebug()) {
            this.featuresButton.visible = false;
            this.bonusItemsButton.visible = false;
            this.customizeTypeButton.visible = false;
            this.importSettingsButton.visible = false;
        } else {
            this.featuresButton.visible = param0;
            this.bonusItemsButton.visible = param0;
            this.customizeTypeButton.visible = param0 && PresetEditor.EDITORS.containsKey(this.preset.flatMap(Holder::unwrapKey));
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

    public WorldCreationContext settings() {
        return this.settings;
    }

    public RegistryAccess registryHolder() {
        return this.settings.registryAccess();
    }

    public void switchToHardcore() {
        this.bonusItemsButton.active = false;
        this.bonusItemsButton.setValue(false);
    }

    public void switchOutOfHardcode() {
        this.bonusItemsButton.active = true;
        this.bonusItemsButton.setValue(this.settings.worldGenSettings().generateBonusChest());
    }
}
