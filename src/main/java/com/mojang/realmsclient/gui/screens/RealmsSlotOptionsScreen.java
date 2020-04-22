package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSlotOptionsScreen extends RealmsScreen {
    public static final Component[] DIFFICULTIES = new Component[]{
        new TranslatableComponent("options.difficulty.peaceful"),
        new TranslatableComponent("options.difficulty.easy"),
        new TranslatableComponent("options.difficulty.normal"),
        new TranslatableComponent("options.difficulty.hard")
    };
    public static final Component[] GAME_MODES = new Component[]{
        new TranslatableComponent("selectWorld.gameMode.survival"),
        new TranslatableComponent("selectWorld.gameMode.creative"),
        new TranslatableComponent("selectWorld.gameMode.adventure")
    };
    private static final TranslatableComponent TEXT_ON = new TranslatableComponent("mco.configure.world.on");
    private static final TranslatableComponent TEXT_OFF = new TranslatableComponent("mco.configure.world.off");
    private EditBox nameEdit;
    protected final RealmsConfigureWorldScreen parent;
    private int column1X;
    private int columnWidth;
    private int column2X;
    private final RealmsWorldOptions options;
    private final RealmsServer.WorldType worldType;
    private final int activeSlot;
    private int difficulty;
    private int gameMode;
    private Boolean pvp;
    private Boolean spawnNPCs;
    private Boolean spawnAnimals;
    private Boolean spawnMonsters;
    private Integer spawnProtection;
    private Boolean commandBlocks;
    private Boolean forceGameMode;
    private Button pvpButton;
    private Button spawnAnimalsButton;
    private Button spawnMonstersButton;
    private Button spawnNPCsButton;
    private RealmsSlotOptionsScreen.SettingsSlider spawnProtectionButton;
    private Button commandBlocksButton;
    private Button forceGameModeButton;
    private RealmsLabel titleLabel;
    private RealmsLabel warningLabel;

    public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen param0, RealmsWorldOptions param1, RealmsServer.WorldType param2, int param3) {
        this.parent = param0;
        this.options = param1;
        this.worldType = param2;
        this.activeSlot = param3;
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.parent);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void init() {
        this.columnWidth = 170;
        this.column1X = this.width / 2 - this.columnWidth;
        this.column2X = this.width / 2 + 10;
        this.difficulty = this.options.difficulty;
        this.gameMode = this.options.gameMode;
        if (this.worldType == RealmsServer.WorldType.NORMAL) {
            this.pvp = this.options.pvp;
            this.spawnProtection = this.options.spawnProtection;
            this.forceGameMode = this.options.forceGameMode;
            this.spawnAnimals = this.options.spawnAnimals;
            this.spawnMonsters = this.options.spawnMonsters;
            this.spawnNPCs = this.options.spawnNPCs;
            this.commandBlocks = this.options.commandBlocks;
        } else {
            Component var0;
            if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
                var0 = new TranslatableComponent("mco.configure.world.edit.subscreen.adventuremap");
            } else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
                var0 = new TranslatableComponent("mco.configure.world.edit.subscreen.inspiration");
            } else {
                var0 = new TranslatableComponent("mco.configure.world.edit.subscreen.experience");
            }

            this.warningLabel = new RealmsLabel(var0, this.width / 2, 26, 16711680);
            this.pvp = true;
            this.spawnProtection = 0;
            this.forceGameMode = false;
            this.spawnAnimals = true;
            this.spawnMonsters = true;
            this.spawnNPCs = true;
            this.commandBlocks = true;
        }

        this.nameEdit = new EditBox(
            this.minecraft.font, this.column1X + 2, row(1), this.columnWidth - 4, 20, null, new TranslatableComponent("mco.configure.world.edit.slot.name")
        );
        this.nameEdit.setMaxLength(10);
        this.nameEdit.setValue(this.options.getSlotName(this.activeSlot));
        this.magicalSpecialHackyFocus(this.nameEdit);
        this.pvpButton = this.addButton(new Button(this.column2X, row(1), this.columnWidth, 20, this.pvpTitle(), param0 -> {
            this.pvp = !this.pvp;
            param0.setMessage(this.pvpTitle());
        }));
        this.addButton(new Button(this.column1X, row(3), this.columnWidth, 20, this.gameModeTitle(), param0 -> {
            this.gameMode = (this.gameMode + 1) % GAME_MODES.length;
            param0.setMessage(this.gameModeTitle());
        }));
        this.spawnAnimalsButton = this.addButton(new Button(this.column2X, row(3), this.columnWidth, 20, this.spawnAnimalsTitle(), param0 -> {
            this.spawnAnimals = !this.spawnAnimals;
            param0.setMessage(this.spawnAnimalsTitle());
        }));
        this.addButton(new Button(this.column1X, row(5), this.columnWidth, 20, this.difficultyTitle(), param0 -> {
            this.difficulty = (this.difficulty + 1) % DIFFICULTIES.length;
            param0.setMessage(this.difficultyTitle());
            if (this.worldType == RealmsServer.WorldType.NORMAL) {
                this.spawnMonstersButton.active = this.difficulty != 0;
                this.spawnMonstersButton.setMessage(this.spawnMonstersTitle());
            }

        }));
        this.spawnMonstersButton = this.addButton(new Button(this.column2X, row(5), this.columnWidth, 20, this.spawnMonstersTitle(), param0 -> {
            this.spawnMonsters = !this.spawnMonsters;
            param0.setMessage(this.spawnMonstersTitle());
        }));
        this.spawnProtectionButton = this.addButton(
            new RealmsSlotOptionsScreen.SettingsSlider(this.column1X, row(7), this.columnWidth, this.spawnProtection, 0.0F, 16.0F)
        );
        this.spawnNPCsButton = this.addButton(new Button(this.column2X, row(7), this.columnWidth, 20, this.spawnNPCsTitle(), param0 -> {
            this.spawnNPCs = !this.spawnNPCs;
            param0.setMessage(this.spawnNPCsTitle());
        }));
        this.forceGameModeButton = this.addButton(new Button(this.column1X, row(9), this.columnWidth, 20, this.forceGameModeTitle(), param0 -> {
            this.forceGameMode = !this.forceGameMode;
            param0.setMessage(this.forceGameModeTitle());
        }));
        this.commandBlocksButton = this.addButton(new Button(this.column2X, row(9), this.columnWidth, 20, this.commandBlocksTitle(), param0 -> {
            this.commandBlocks = !this.commandBlocks;
            param0.setMessage(this.commandBlocksTitle());
        }));
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            this.pvpButton.active = false;
            this.spawnAnimalsButton.active = false;
            this.spawnNPCsButton.active = false;
            this.spawnMonstersButton.active = false;
            this.spawnProtectionButton.active = false;
            this.commandBlocksButton.active = false;
            this.forceGameModeButton.active = false;
        }

        if (this.difficulty == 0) {
            this.spawnMonstersButton.active = false;
        }

        this.addButton(
            new Button(
                this.column1X, row(13), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.buttons.done"), param0 -> this.saveSettings()
            )
        );
        this.addButton(new Button(this.column2X, row(13), this.columnWidth, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent)));
        this.addWidget(this.nameEdit);
        this.titleLabel = this.addWidget(new RealmsLabel(new TranslatableComponent("mco.configure.world.buttons.options"), this.width / 2, 17, 16777215));
        if (this.warningLabel != null) {
            this.addWidget(this.warningLabel);
        }

        this.narrateLabels();
    }

    private Component difficultyTitle() {
        return new TranslatableComponent("options.difficulty").append(": ").append(DIFFICULTIES[this.difficulty]);
    }

    private Component gameModeTitle() {
        return new TranslatableComponent("selectWorld.gameMode").append(": ").append(GAME_MODES[this.gameMode]);
    }

    private Component pvpTitle() {
        return new TranslatableComponent("mco.configure.world.pvp").append(": ").append(getOnOff(this.pvp));
    }

    private Component spawnAnimalsTitle() {
        return new TranslatableComponent("mco.configure.world.spawnAnimals").append(": ").append(getOnOff(this.spawnAnimals));
    }

    private Component spawnMonstersTitle() {
        return this.difficulty == 0
            ? new TranslatableComponent("mco.configure.world.spawnMonsters").append(": ").append(new TranslatableComponent("mco.configure.world.off"))
            : new TranslatableComponent("mco.configure.world.spawnMonsters").append(": ").append(getOnOff(this.spawnMonsters));
    }

    private Component spawnNPCsTitle() {
        return new TranslatableComponent("mco.configure.world.spawnNPCs").append(": ").append(getOnOff(this.spawnNPCs));
    }

    private Component commandBlocksTitle() {
        return new TranslatableComponent("mco.configure.world.commandBlocks").append(": ").append(getOnOff(this.commandBlocks));
    }

    private Component forceGameModeTitle() {
        return new TranslatableComponent("mco.configure.world.forceGameMode").append(": ").append(getOnOff(this.forceGameMode));
    }

    private static Component getOnOff(boolean param0) {
        return param0 ? TEXT_ON : TEXT_OFF;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        String var0 = I18n.get("mco.configure.world.edit.slot.name");
        this.font.draw(param0, var0, (float)(this.column1X + this.columnWidth / 2 - this.font.width(var0) / 2), (float)(row(0) - 5), 16777215);
        this.titleLabel.render(this, param0);
        if (this.warningLabel != null) {
            this.warningLabel.render(this, param0);
        }

        this.nameEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }

    private String getSlotName() {
        return this.nameEdit.getValue().equals(this.options.getDefaultSlotName(this.activeSlot)) ? "" : this.nameEdit.getValue();
    }

    private void saveSettings() {
        if (this.worldType != RealmsServer.WorldType.ADVENTUREMAP
            && this.worldType != RealmsServer.WorldType.EXPERIENCE
            && this.worldType != RealmsServer.WorldType.INSPIRATION) {
            this.parent
                .saveSlotSettings(
                    new RealmsWorldOptions(
                        this.pvp,
                        this.spawnAnimals,
                        this.spawnMonsters,
                        this.spawnNPCs,
                        this.spawnProtection,
                        this.commandBlocks,
                        this.difficulty,
                        this.gameMode,
                        this.forceGameMode,
                        this.getSlotName()
                    )
                );
        } else {
            this.parent
                .saveSlotSettings(
                    new RealmsWorldOptions(
                        this.options.pvp,
                        this.options.spawnAnimals,
                        this.options.spawnMonsters,
                        this.options.spawnNPCs,
                        this.options.spawnProtection,
                        this.options.commandBlocks,
                        this.difficulty,
                        this.gameMode,
                        this.options.forceGameMode,
                        this.getSlotName()
                    )
                );
        }

    }

    @OnlyIn(Dist.CLIENT)
    class SettingsSlider extends AbstractSliderButton {
        private final double minValue;
        private final double maxValue;

        public SettingsSlider(int param0, int param1, int param2, int param3, float param4, float param5) {
            super(param0, param1, param2, 20, TextComponent.EMPTY, 0.0);
            this.minValue = (double)param4;
            this.maxValue = (double)param5;
            this.value = (double)((Mth.clamp((float)param3, param4, param5) - param4) / (param5 - param4));
            this.updateMessage();
        }

        @Override
        public void applyValue() {
            if (RealmsSlotOptionsScreen.this.spawnProtectionButton.active) {
                RealmsSlotOptionsScreen.this.spawnProtection = (int)Mth.lerp(Mth.clamp(this.value, 0.0, 1.0), this.minValue, this.maxValue);
            }
        }

        @Override
        protected void updateMessage() {
            this.setMessage(
                new TranslatableComponent("mco.configure.world.spawnProtection")
                    .append(": ")
                    .append(
                        (Component)(RealmsSlotOptionsScreen.this.spawnProtection == 0
                            ? new TranslatableComponent("mco.configure.world.off")
                            : new TextComponent(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection)))
                    )
            );
        }

        @Override
        public void onClick(double param0, double param1) {
        }

        @Override
        public void onRelease(double param0, double param1) {
        }
    }
}
