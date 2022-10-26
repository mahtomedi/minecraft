package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
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
    private static final Component NAME_LABEL = Component.translatable("mco.configure.world.edit.slot.name");
    static final Component SPAWN_PROTECTION_TEXT = Component.translatable("mco.configure.world.spawnProtection");
    private static final Component SPAWN_WARNING_TITLE = Component.translatable("mco.configure.world.spawn_toggle.title")
        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
    private EditBox nameEdit;
    protected final RealmsConfigureWorldScreen parent;
    private int column1X;
    private int columnWidth;
    private final RealmsWorldOptions options;
    private final RealmsServer.WorldType worldType;
    private Difficulty difficulty;
    private GameType gameMode;
    private final String defaultSlotName;
    private String worldName;
    private boolean pvp;
    private boolean spawnNPCs;
    private boolean spawnAnimals;
    private boolean spawnMonsters;
    int spawnProtection;
    private boolean commandBlocks;
    private boolean forceGameMode;
    RealmsSlotOptionsScreen.SettingsSlider spawnProtectionButton;

    public RealmsSlotOptionsScreen(RealmsConfigureWorldScreen param0, RealmsWorldOptions param1, RealmsServer.WorldType param2, int param3) {
        super(Component.translatable("mco.configure.world.buttons.options"));
        this.parent = param0;
        this.options = param1;
        this.worldType = param2;
        this.difficulty = findByIndex(DIFFICULTIES, param1.difficulty, 2);
        this.gameMode = findByIndex(GAME_MODES, param1.gameMode, 0);
        this.defaultSlotName = param1.getDefaultSlotName(param3);
        this.setWorldName(param1.getSlotName(param3));
        if (param2 == RealmsServer.WorldType.NORMAL) {
            this.pvp = param1.pvp;
            this.spawnProtection = param1.spawnProtection;
            this.forceGameMode = param1.forceGameMode;
            this.spawnAnimals = param1.spawnAnimals;
            this.spawnMonsters = param1.spawnMonsters;
            this.spawnNPCs = param1.spawnNPCs;
            this.commandBlocks = param1.commandBlocks;
        } else {
            this.pvp = true;
            this.spawnProtection = 0;
            this.forceGameMode = false;
            this.spawnAnimals = true;
            this.spawnMonsters = true;
            this.spawnNPCs = true;
            this.commandBlocks = true;
        }

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
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            Component var1;
            if (this.worldType == RealmsServer.WorldType.ADVENTUREMAP) {
                var1 = Component.translatable("mco.configure.world.edit.subscreen.adventuremap");
            } else if (this.worldType == RealmsServer.WorldType.INSPIRATION) {
                var1 = Component.translatable("mco.configure.world.edit.subscreen.inspiration");
            } else {
                var1 = Component.translatable("mco.configure.world.edit.subscreen.experience");
            }

            this.addLabel(new RealmsLabel(var1, this.width / 2, 26, 16711680));
        }

        this.nameEdit = new EditBox(
            this.minecraft.font, this.column1X + 2, row(1), this.columnWidth - 4, 20, null, Component.translatable("mco.configure.world.edit.slot.name")
        );
        this.nameEdit.setMaxLength(10);
        this.nameEdit.setValue(this.worldName);
        this.nameEdit.setResponder(this::setWorldName);
        this.magicalSpecialHackyFocus(this.nameEdit);
        CycleButton<Boolean> var4 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.pvp)
                .create(var0, row(1), this.columnWidth, 20, Component.translatable("mco.configure.world.pvp"), (param0, param1) -> this.pvp = param1)
        );
        this.addRenderableWidget(
            CycleButton.builder(GameType::getShortDisplayName)
                .withValues(GAME_MODES)
                .withInitialValue(this.gameMode)
                .create(this.column1X, row(3), this.columnWidth, 20, Component.translatable("selectWorld.gameMode"), (param0, param1) -> this.gameMode = param1)
        );
        Component var5 = Component.translatable("mco.configure.world.spawn_toggle.message");
        CycleButton<Boolean> var6 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.spawnAnimals)
                .create(
                    var0,
                    row(3),
                    this.columnWidth,
                    20,
                    Component.translatable("mco.configure.world.spawnAnimals"),
                    this.confirmDangerousOption(var5, param0 -> this.spawnAnimals = param0)
                )
        );
        CycleButton<Boolean> var7 = CycleButton.onOffBuilder(this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters)
            .create(
                var0,
                row(5),
                this.columnWidth,
                20,
                Component.translatable("mco.configure.world.spawnMonsters"),
                this.confirmDangerousOption(var5, param0 -> this.spawnMonsters = param0)
            );
        this.addRenderableWidget(
            CycleButton.builder(Difficulty::getDisplayName)
                .withValues(DIFFICULTIES)
                .withInitialValue(this.difficulty)
                .create(this.column1X, row(5), this.columnWidth, 20, Component.translatable("options.difficulty"), (param1, param2) -> {
                    this.difficulty = param2;
                    if (this.worldType == RealmsServer.WorldType.NORMAL) {
                        boolean var0x = this.difficulty != Difficulty.PEACEFUL;
                        var7.active = var0x;
                        var7.setValue(var0x && this.spawnMonsters);
                    }
        
                })
        );
        this.addRenderableWidget(var7);
        this.spawnProtectionButton = this.addRenderableWidget(
            new RealmsSlotOptionsScreen.SettingsSlider(this.column1X, row(7), this.columnWidth, this.spawnProtection, 0.0F, 16.0F)
        );
        CycleButton<Boolean> var8 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.spawnNPCs)
                .create(
                    var0,
                    row(7),
                    this.columnWidth,
                    20,
                    Component.translatable("mco.configure.world.spawnNPCs"),
                    this.confirmDangerousOption(Component.translatable("mco.configure.world.spawn_toggle.message.npc"), param0 -> this.spawnNPCs = param0)
                )
        );
        CycleButton<Boolean> var9 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.forceGameMode)
                .create(
                    this.column1X,
                    row(9),
                    this.columnWidth,
                    20,
                    Component.translatable("mco.configure.world.forceGameMode"),
                    (param0, param1) -> this.forceGameMode = param1
                )
        );
        CycleButton<Boolean> var10 = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.commandBlocks)
                .create(
                    var0,
                    row(9),
                    this.columnWidth,
                    20,
                    Component.translatable("mco.configure.world.commandBlocks"),
                    (param0, param1) -> this.commandBlocks = param1
                )
        );
        if (this.worldType != RealmsServer.WorldType.NORMAL) {
            var4.active = false;
            var6.active = false;
            var8.active = false;
            var7.active = false;
            this.spawnProtectionButton.active = false;
            var10.active = false;
            var9.active = false;
        }

        if (this.difficulty == Difficulty.PEACEFUL) {
            var7.active = false;
        }

        this.addRenderableWidget(
            Button.builder(Component.translatable("mco.configure.world.buttons.done"), param0 -> this.saveSettings())
                .bounds(this.column1X, row(13), this.columnWidth, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.parent)).bounds(var0, row(13), this.columnWidth, 20).build()
        );
        this.addWidget(this.nameEdit);
    }

    private CycleButton.OnValueChange<Boolean> confirmDangerousOption(Component param0, Consumer<Boolean> param1) {
        return (param2, param3) -> {
            if (param3) {
                param1.accept(true);
            } else {
                this.minecraft.setScreen(new ConfirmScreen(param1x -> {
                    if (param1x) {
                        param1.accept(false);
                    }

                    this.minecraft.setScreen(this);
                }, SPAWN_WARNING_TITLE, param0, CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
            }

        };
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(this.getTitle(), this.createLabelNarration());
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 17, 16777215);
        this.font.draw(param0, NAME_LABEL, (float)(this.column1X + this.columnWidth / 2 - this.font.width(NAME_LABEL) / 2), (float)(row(0) - 5), 16777215);
        this.nameEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }

    private void setWorldName(String param0) {
        if (param0.equals(this.defaultSlotName)) {
            this.worldName = "";
        } else {
            this.worldName = param0;
        }

    }

    private void saveSettings() {
        int var0 = findIndex(DIFFICULTIES, this.difficulty, 2);
        int var1 = findIndex(GAME_MODES, this.gameMode, 0);
        if (this.worldType != RealmsServer.WorldType.ADVENTUREMAP
            && this.worldType != RealmsServer.WorldType.EXPERIENCE
            && this.worldType != RealmsServer.WorldType.INSPIRATION) {
            boolean var2 = this.worldType == RealmsServer.WorldType.NORMAL && this.difficulty != Difficulty.PEACEFUL && this.spawnMonsters;
            this.parent
                .saveSlotSettings(
                    new RealmsWorldOptions(
                        this.pvp,
                        this.spawnAnimals,
                        var2,
                        this.spawnNPCs,
                        this.spawnProtection,
                        this.commandBlocks,
                        var0,
                        var1,
                        this.forceGameMode,
                        this.worldName
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
                        this.worldName
                    )
                );
        }

    }

    @OnlyIn(Dist.CLIENT)
    class SettingsSlider extends AbstractSliderButton {
        private final double minValue;
        private final double maxValue;

        public SettingsSlider(int param0, int param1, int param2, int param3, float param4, float param5) {
            super(param0, param1, param2, 20, CommonComponents.EMPTY, 0.0);
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
                        : Component.literal(String.valueOf(RealmsSlotOptionsScreen.this.spawnProtection)))
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
