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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
    private Font font;
    private int width;
    private EditBox seedEdit;
    private Button featuresButton;
    public Button bonusItemsButton;
    private Button typeButton;
    private Button customizeTypeButton;
    private Button importSettingsButton;
    private WorldGenSettings settings;
    private Optional<WorldPreset> preset;
    private String initSeed;

    public WorldGenSettingsComponent() {
        this.settings = WorldGenSettings.makeDefault();
        this.preset = Optional.of(WorldPreset.NORMAL);
        this.initSeed = "";
    }

    public WorldGenSettingsComponent(WorldGenSettings param0) {
        this.settings = param0;
        this.preset = WorldPreset.of(param0);
        this.initSeed = Long.toString(param0.seed());
    }

    public void init(final CreateWorldScreen param0, Minecraft param1, Font param2) {
        this.font = param2;
        this.width = param0.width;
        this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
        this.seedEdit.setValue(this.initSeed);
        this.seedEdit.setResponder(param0x -> this.initSeed = this.seedEdit.getValue());
        param0.addWidget(this.seedEdit);
        this.featuresButton = param0.addButton(
            new Button(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), param0x -> {
                this.settings = this.settings.withFeaturesToggled();
                param0x.queueNarration(250);
            }) {
                @Override
                public Component getMessage() {
                    return super.getMessage()
                        .mutableCopy()
                        .append(" ")
                        .append(CommonComponents.optionStatus(WorldGenSettingsComponent.this.settings.generateFeatures()));
                }
    
                @Override
                protected MutableComponent createNarrationMessage() {
                    return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.mapFeatures.info"));
                }
            }
        );
        this.featuresButton.visible = false;
        this.typeButton = param0.addButton(
            new Button(this.width / 2 + 5, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), param1x -> {
                while(this.preset.isPresent()) {
                    int var0 = WorldPreset.PRESETS.indexOf(this.preset.get()) + 1;
                    if (var0 >= WorldPreset.PRESETS.size()) {
                        var0 = 0;
                    }
    
                    WorldPreset var1x = WorldPreset.PRESETS.get(var0);
                    this.preset = Optional.of(var1x);
                    this.settings = var1x.create(this.settings.seed(), this.settings.generateFeatures(), this.settings.generateBonusChest());
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
                        .mutableCopy()
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
        this.customizeTypeButton = param0.addButton(
            new Button(param0.width / 2 + 5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), param2x -> {
                WorldPreset.PresetEditor var0 = WorldPreset.EDITORS.get(this.preset);
                if (var0 != null) {
                    param1.setScreen(var0.createEditScreen(param0, this.settings));
                }
    
            })
        );
        this.customizeTypeButton.visible = false;
        this.bonusItemsButton = param0.addButton(
            new Button(param0.width / 2 - 155, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), param0x -> {
                this.settings = this.settings.withBonusChestToggled();
                param0x.queueNarration(250);
            }) {
                @Override
                public Component getMessage() {
                    return super.getMessage()
                        .mutableCopy()
                        .append(" ")
                        .append(CommonComponents.optionStatus(WorldGenSettingsComponent.this.settings.generateBonusChest() && !param0.hardCore));
                }
            }
        );
        this.bonusItemsButton.visible = false;
        this.importSettingsButton = param0.addButton(
            new Button(
                this.width / 2 - 155,
                185,
                150,
                20,
                new TranslatableComponent("selectWorld.import_worldgen_settings"),
                param2x -> {
                    TranslatableComponent var0 = new TranslatableComponent("selectWorld.import_worldgen_settings.select_file");
                    String var1x = TinyFileDialogs.tinyfd_openFileDialog(var0.getString(), null, null, null, false);
                    if (var1x != null) {
                        JsonParser var2x = new JsonParser();
        
                        DataResult<WorldGenSettings> var5;
                        try (BufferedReader var3 = Files.newBufferedReader(Paths.get(var1x))) {
                            JsonElement var4 = var2x.parse(var3);
                            var5 = WorldGenSettings.CODEC.parse(JsonOps.INSTANCE, var4);
                        } catch (JsonIOException | JsonSyntaxException | IOException var21) {
                            var5 = DataResult.error("Failed to parse file: " + var21.getMessage());
                        }
        
                        if (var5.error().isPresent()) {
                            Component var9 = new TranslatableComponent("selectWorld.import_worldgen_settings.failure");
                            String var10 = var5.error().get().message();
                            LOGGER.error("Error parsing world settings: {}", var10);
                            Component var11 = new TextComponent(var10);
                            param1.getToasts().addToast(SystemToast.multiline(SystemToast.SystemToastIds.WORLD_GEN_SETTINGS_TRANSFER, var9, var11));
                        }
        
                        Lifecycle var12 = var5.lifecycle();
                        var5.resultOrPartial(LOGGER::error)
                            .ifPresent(
                                param3 -> {
                                    BooleanConsumer var0x = param3x -> {
                                        param1.setScreen(param0);
                                        if (param3x) {
                                            this.importSettings(param3);
                                        }
                
                                    };
                                    if (var12 == Lifecycle.stable()) {
                                        this.importSettings(param3);
                                    } else if (var12 == Lifecycle.experimental()) {
                                        param1.setScreen(
                                            new ConfirmScreen(
                                                var0x,
                                                new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.title"),
                                                new TranslatableComponent("selectWorld.import_worldgen_settings.experimental.question")
                                            )
                                        );
                                    } else {
                                        param1.setScreen(
                                            new ConfirmScreen(
                                                var0x,
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

    private void importSettings(WorldGenSettings param0) {
        this.settings = param0;
        this.preset = WorldPreset.of(param0);
        this.initSeed = Long.toString(param0.seed());
        this.seedEdit.setValue(this.initSeed);
        this.typeButton.active = this.preset.isPresent();
    }

    @Override
    public void tick() {
        this.seedEdit.tick();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.featuresButton.visible) {
            this.font.drawShadow(param0, I18n.get("selectWorld.mapFeatures.info"), (float)(this.width / 2 - 150), 122.0F, -6250336);
        }

        this.seedEdit.render(param0, param1, param2, param3);
        if (this.preset.equals(Optional.of(WorldPreset.AMPLIFIED))) {
            this.font.drawWordWrap(AMPLIFIED_HELP_TEXT, this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880);
        }

    }

    protected void updateSettings(WorldGenSettings param0) {
        this.settings = param0;
    }

    private static OptionalLong parseLong(String param0) {
        try {
            return OptionalLong.of(Long.parseLong(param0));
        } catch (NumberFormatException var2) {
            return OptionalLong.empty();
        }
    }

    public WorldGenSettings makeSettings(boolean param0) {
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

        return this.settings.withSeed(param0, var1);
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
}
