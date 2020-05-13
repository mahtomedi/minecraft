package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.OptionalLong;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.TickableWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class WorldGenSettingsComponent implements TickableWidget, Widget {
    private static final Map<WorldGenSettings.Preset, WorldGenSettingsComponent.PresetEditor> EDITORS = ImmutableMap.of(
        WorldGenSettings.Preset.FLAT,
        (param0, param1) -> new CreateFlatWorldScreen(
                param0, param2 -> param0.worldGenSettingsComponent.updateSettings(param1.fromFlatSettings(param2)), param1.parseFlatSettings()
            ),
        WorldGenSettings.Preset.BUFFET,
        (param0, param1) -> new CreateBuffetWorldScreen(
                param0,
                param2 -> param0.worldGenSettingsComponent.updateSettings(param1.fromBuffetSettings(param2.getFirst(), param2.getSecond())),
                param1.parseBuffetSettings()
            )
    );
    private static final Component AMPLIFIED_HELP_TEXT = new TranslatableComponent("generator.amplified.info");
    private Font font;
    private int width;
    private EditBox seedEdit;
    private Button featuresButton;
    public Button bonusItemsButton;
    private Button typeButton;
    private Button customizeTypeButton;
    private WorldGenSettings settings;
    private int presetIndex;
    private String initSeed;

    public WorldGenSettingsComponent() {
        this(WorldGenSettings.makeDefault(), "");
    }

    public WorldGenSettingsComponent(WorldGenSettings param0) {
        this(param0, Long.toString(param0.seed()));
    }

    private WorldGenSettingsComponent(WorldGenSettings param0, String param1) {
        this.settings = param0;
        this.presetIndex = WorldGenSettings.Preset.PRESETS.indexOf(param0.preset());
        this.initSeed = param1;
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
                do {
                    ++this.presetIndex;
                    if (this.presetIndex >= WorldGenSettings.Preset.PRESETS.size()) {
                        this.presetIndex = 0;
                    }
    
                    this.settings = this.settings.withPreset(WorldGenSettings.Preset.PRESETS.get(this.presetIndex));
                } while(this.settings.isDebug() && !Screen.hasShiftDown());
    
                param0.updateDisplayOptions();
                param1x.queueNarration(250);
            }) {
                @Override
                public Component getMessage() {
                    return super.getMessage().mutableCopy().append(" ").append(WorldGenSettingsComponent.this.settings.preset().description());
                }
    
                @Override
                protected MutableComponent createNarrationMessage() {
                    return WorldGenSettingsComponent.this.settings.preset() == WorldGenSettings.Preset.AMPLIFIED
                        ? super.createNarrationMessage().append(". ").append(WorldGenSettingsComponent.AMPLIFIED_HELP_TEXT)
                        : super.createNarrationMessage();
                }
            }
        );
        this.typeButton.visible = false;
        this.customizeTypeButton = param0.addButton(
            new Button(param0.width / 2 + 5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), param2x -> {
                WorldGenSettingsComponent.PresetEditor var0 = EDITORS.get(this.settings.preset());
                if (var0 != null) {
                    param1.setScreen(var0.createEditScreen(param0, this.settings));
                }
    
            })
        );
        this.customizeTypeButton.visible = false;
        this.bonusItemsButton = param0.addButton(
            new Button(param0.width / 2 + 5, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), param0x -> {
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
        if (this.settings.preset() == WorldGenSettings.Preset.AMPLIFIED) {
            this.font.drawWordWrap(AMPLIFIED_HELP_TEXT, this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880);
        }

    }

    private void updateSettings(WorldGenSettings param0) {
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
            if (var2.isPresent() && var2.getAsLong() == 0L) {
                var1 = OptionalLong.empty();
            } else {
                var1 = var2;
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
        } else {
            this.featuresButton.visible = param0;
            this.bonusItemsButton.visible = param0;
            this.customizeTypeButton.visible = param0 && EDITORS.containsKey(this.settings.preset());
        }

        this.seedEdit.setVisible(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public interface PresetEditor {
        Screen createEditScreen(CreateWorldScreen var1, WorldGenSettings var2);
    }
}
