package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.util.List;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsSlotOptionsScreen extends RealmsScreen {
    private static final int DEFAULT_DIFFICULTY = 2;
    public static final List<Difficulty> DIFFICULTIES = ImmutableList.of(Difficulty.PEACEFUL, Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD);
    private static final int DEFAULT_GAME_MODE = 0;
    public static final List<GameType> GAME_MODES = ImmutableList.of(GameType.SURVIVAL, GameType.CREATIVE, GameType.ADVENTURE);
    private static final Component NAME_LABEL = new TranslatableComponent("mco.configure.world.edit.slot.name");
    static final Component SPAWN_PROTECTION_TEXT = new TranslatableComponent("mco.configure.world.spawnProtection");
    private EditBox nameEdit;
    protected final RealmsConfigureWorldScreen parent;
    private int column1X;
    private int columnWidth;
    private final RealmsWorldOptions options;
    private final RealmsServer.WorldType worldType;
    private final int activeSlot;
    private Difficulty difficulty;
    private GameType gameMode;
    private boolean pvp;
    private boolean spawnNPCs;
    private boolean spawnAnimals;
    private boolean spawnMonsters;
    int spawnProtection;
    private boolean commandBlocks;
    private boolean forceGameMode;
    RealmsSlotOptionsScreen.SettingsSlider spawnProtectionButton;
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

    private static <T> T findByIndex(List<T> param0, int param1, int param2) {
        try {
            return param0.get(param1);
        } catch (IndexOutOfBoundsException var4) {
            return param0.get(param2);
        }
    }

    private static <T> int findIndex(List<T> param0, T param1, int param2) {
        int var0 = param0.indexOf(param1);
        return var0 == -1 ? param2 : var0;
    }

    @Override
    public void init() {
        this.columnWidth = 170;
        this.column1X = this.width / 2 - this.columnWidth;
        int var0 = this.width / 2 + 10;
        this.difficulty = findByIndex(DIFFICULTIES, this.options.difficulty, 2);
        this.gameMode = findByIndex(GAME_MODES, this.options.gameMode, 0);
        if (this.worldType == RealmsServer.WorldType.NORMAL) {
            this.pvp = this.options.pvp;
            this.spawnProtection = this.options.spawnProtection;
            this.forceGameMode = this.options.forceGameMode;
            this.spawnAnimals = this.options.spawnAnimals;
            this.spawnMonsters = this.options.spawnMonsters;
            this.spawnNPCs = this.options.spawnNPCs;
            this.commandBlocks = this.options.commandBlocks;
        } else {
            Component var1;
            if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
                var1 = new TranslatableComponent("mco.configure.world.edit.subscreen.adventuremap");
            } else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
                var1 = new TranslatableComponent("mco.configure.world.edit.subscreen.inspiration");
            } else {
                var1 = new TranslatableComponent("mco.configure.world.edit.subscreen.experience");
            }

            this.warningLabel = new RealmsLabel(var1, this.width / 2, 26, 16711680);
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
        CycleButton<Boolean> var4 = this.addButton(
            CycleButton.onOffBuilder(this.pvp)
                .create(var0, row(1), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.pvp"), (param0, param1) -> this.pvp = param1)
        );
        this.addButton(
            CycleButton.builder(GameType::getShortDisplayName)
                .withValues(GAME_MODES)
                .withInitialValue(this.gameMode)
                .create(
                    this.column1X, row(3), this.columnWidth, 20, new TranslatableComponent("selectWorld.gameMode"), (param0, param1) -> this.gameMode = param1
                )
        );
        CycleButton<Boolean> var5 = this.addButton(
            CycleButton.onOffBuilder(this.spawnAnimals)
                .create(
                    var0,
                    row(3),
                    this.columnWidth,
                    20,
                    new TranslatableComponent("mco.configure.world.spawnAnimals"),
                    (param0, param1) -> this.spawnAnimals = param1
                )
        );
        CycleButton<Boolean> var6 = CycleButton.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters)
            .create(
                var0,
                row(5),
                this.columnWidth,
                20,
                new TranslatableComponent("mco.configure.world.spawnMonsters"),
                (param0, param1) -> this.spawnMonsters = param1
            );
        this.addButton(
            CycleButton.builder(Difficulty::getDisplayName)
                .withValues(DIFFICULTIES)
                .withInitialValue(this.difficulty)
                .create(this.column1X, row(5), this.columnWidth, 20, new TranslatableComponent("options.difficulty"), (param1, param2) -> {
                    this.difficulty = param2;
                    if (this.worldType == RealmsServer.WorldType.NORMAL) {
                        boolean var0x = this.difficulty != Difficulty.PEACEFUL;
                        var6.active = var0x;
                        var6.setValue(var0x && this.spawnMonsters);
                    }
        
                })
        );
        this.addButton(var6);
        this.spawnProtectionButton = this.addButton(
            new RealmsSlotOptionsScreen.SettingsSlider(this.column1X, row(7), this.columnWidth, this.spawnProtection, 0.0F, 16.0F)
        );
        CycleButton<Boolean> var7 = this.addButton(
            CycleButton.onOffBuilder(this.spawnNPCs)
                .create(
                    var0, row(7), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.spawnNPCs"), (param0, param1) -> this.spawnNPCs = param1
                )
        );
        CycleButton<Boolean> var8 = this.addButton(
            CycleButton.onOffBuilder(this.forceGameMode)
                .create(
                    this.column1X,
                    row(9),
                    this.columnWidth,
                    20,
                    new TranslatableComponent("mco.configure.world.forceGameMode"),
                    (param0, param1) -> this.forceGameMode = param1
                )
        );
        CycleButton<Boolean> var9 = this.addButton(
            CycleButton.onOffBuilder(this.commandBlocks)
                .create(
                    var0,
                    row(9),
                    this.columnWidth,
                    20,
                    new TranslatableComponent("mco.configure.world.commandBlocks"),
                    (param0, param1) -> this.commandBlocks = param1
                )
        );
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            var4.active = false;
            var5.active = false;
            var7.active = false;
            var6.active = false;
            this.spawnProtectionButton.active = false;
            var9.active = false;
            var8.active = false;
        }

        if (this.difficulty == Difficulty.PEACEFUL) {
            var6.active = false;
        }

        this.addButton(
            new Button(
                this.column1X, row(13), this.columnWidth, 20, new TranslatableComponent("mco.configure.world.buttons.done"), param0 -> this.saveSettings()
            )
        );
        this.addButton(new Button(var0, row(13), this.columnWidth, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent)));
        this.addWidget(this.nameEdit);
        this.titleLabel = this.addWidget(new RealmsLabel(new TranslatableComponent("mco.configure.world.buttons.options"), this.width / 2, 17, 16777215));
        if (this.warningLabel != null) {
            this.addWidget(this.warningLabel);
        }

        this.narrateLabels();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.font.draw(param0, NAME_LABEL, (float)(this.column1X + this.columnWidth / 2 - this.font.width(NAME_LABEL) / 2), (float)(row(0) - 5), 16777215);
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
        int var0 = findIndex(DIFFICULTIES, this.difficulty, 2);
        int var1 = findIndex(GAME_MODES, this.gameMode, 0);
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
                        var0,
                        var1,
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
                        var0,
                        var1,
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
                CommonComponents.optionNameValue(
                    RealmsSlotOptionsScreen.SPAWN_PROTECTION_TEXT,
                    (Component)(RealmsSlotOptionsScreen.this.spawnProtection == 0
                        ? CommonComponents.OPTION_OFF
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
