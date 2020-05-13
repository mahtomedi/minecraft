package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CreateWorldScreen extends Screen {
    private final Screen lastScreen;
    private EditBox nameEdit;
    private String resultFolder;
    private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
    @Nullable
    private CreateWorldScreen.SelectedGameMode oldGameMode;
    private Difficulty selectedDifficulty = Difficulty.NORMAL;
    private Difficulty effectiveDifficulty = Difficulty.NORMAL;
    private boolean commands;
    private boolean commandsChanged;
    public boolean hardCore;
    private boolean done;
    private boolean displayOptions;
    private Button createButton;
    private Button modeButton;
    private Button difficultyButton;
    private Button moreOptionsButton;
    private Button gameRulesButton;
    private Button commandsButton;
    private Component gameModeHelp1;
    private Component gameModeHelp2;
    private String initName;
    private GameRules gameRules = new GameRules();
    public final WorldGenSettingsComponent worldGenSettingsComponent;

    public CreateWorldScreen(@Nullable Screen param0, WorldData param1) {
        this(param0, new WorldGenSettingsComponent(param1.getLevelSettings().worldGenSettings()));
        LevelSettings var0 = param1.getLevelSettings();
        this.initName = var0.getLevelName();
        this.commands = var0.getAllowCommands();
        this.commandsChanged = true;
        this.selectedDifficulty = var0.getDifficulty();
        this.effectiveDifficulty = this.selectedDifficulty;
        this.gameRules.assignFrom(param1.getGameRules(), null);
        if (var0.isHardcore()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
        } else if (var0.getGameType().isSurvival()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
        } else if (var0.getGameType().isCreative()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
        }

    }

    public CreateWorldScreen(@Nullable Screen param0) {
        this(param0, new WorldGenSettingsComponent());
    }

    private CreateWorldScreen(@Nullable Screen param0, WorldGenSettingsComponent param1) {
        super(new TranslatableComponent("selectWorld.create"));
        this.lastScreen = param0;
        this.initName = I18n.get("selectWorld.newWorld");
        this.worldGenSettingsComponent = param1;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.worldGenSettingsComponent.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterName")) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage()
                    .append(". ")
                    .append(new TranslatableComponent("selectWorld.resultFolder"))
                    .append(" ")
                    .append(CreateWorldScreen.this.resultFolder);
            }
        };
        this.nameEdit.setValue(this.initName);
        this.nameEdit.setResponder(param0 -> {
            this.initName = param0;
            this.createButton.active = !this.nameEdit.getValue().isEmpty();
            this.updateResultFolder();
        });
        this.children.add(this.nameEdit);
        this.modeButton = this.addButton(
            new Button(this.width / 2 - 155, 115, 150, 20, new TranslatableComponent("selectWorld.gameMode"), param0 -> {
                switch(this.gameMode) {
                    case SURVIVAL:
                        this.setGameMode(CreateWorldScreen.SelectedGameMode.HARDCORE);
                        break;
                    case HARDCORE:
                        this.setGameMode(CreateWorldScreen.SelectedGameMode.CREATIVE);
                        break;
                    case CREATIVE:
                        this.setGameMode(CreateWorldScreen.SelectedGameMode.SURVIVAL);
                }
    
                param0.queueNarration(250);
            }) {
                @Override
                public Component getMessage() {
                    return super.getMessage()
                        .mutableCopy()
                        .append(": ")
                        .append(new TranslatableComponent("selectWorld.gameMode." + CreateWorldScreen.this.gameMode.name));
                }
    
                @Override
                protected MutableComponent createNarrationMessage() {
                    return super.createNarrationMessage()
                        .append(". ")
                        .append(CreateWorldScreen.this.gameModeHelp1)
                        .append(" ")
                        .append(CreateWorldScreen.this.gameModeHelp2);
                }
            }
        );
        this.difficultyButton = this.addButton(new Button(this.width / 2 + 5, 115, 150, 20, new TranslatableComponent("options.difficulty"), param0 -> {
            this.selectedDifficulty = this.selectedDifficulty.nextById();
            this.effectiveDifficulty = this.selectedDifficulty;
            param0.queueNarration(250);
        }) {
            @Override
            public Component getMessage() {
                return new TranslatableComponent("options.difficulty").append(": ").append(CreateWorldScreen.this.effectiveDifficulty.getDisplayName());
            }
        });
        this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
        this.commandsButton = this.addButton(
            new Button(this.width / 2 - 155, 151, 150, 20, new TranslatableComponent("selectWorld.allowCommands"), param0 -> {
                this.commandsChanged = true;
                this.commands = !this.commands;
                param0.queueNarration(250);
            }) {
                @Override
                public Component getMessage() {
                    return super.getMessage()
                        .mutableCopy()
                        .append(" ")
                        .append(CommonComponents.optionStatus(CreateWorldScreen.this.commands && !CreateWorldScreen.this.hardCore));
                }
    
                @Override
                protected MutableComponent createNarrationMessage() {
                    return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.allowCommands.info"));
                }
            }
        );
        this.commandsButton.visible = false;
        this.createButton = this.addButton(
            new Button(this.width / 2 - 155, this.height - 28, 150, 20, new TranslatableComponent("selectWorld.create"), param0 -> this.onCreate())
        );
        this.createButton.active = !this.initName.isEmpty();
        this.addButton(
            new Button(this.width / 2 + 5, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.moreOptionsButton = this.addButton(
            new Button(this.width / 2 + 5, 185, 150, 20, new TranslatableComponent("selectWorld.moreWorldOptions"), param0 -> this.toggleDisplayOptions())
        );
        this.gameRulesButton = this.addButton(
            new Button(
                this.width / 2 - 155,
                185,
                150,
                20,
                new TranslatableComponent("selectWorld.gameRules"),
                param0 -> this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), param0x -> {
                        this.minecraft.setScreen(this);
                        param0x.ifPresent(param0xx -> this.gameRules = param0xx);
                    }))
            )
        );
        this.updateDisplayOptions();
        this.setInitialFocus(this.nameEdit);
        this.setGameMode(this.gameMode);
        this.updateResultFolder();
    }

    private void updateGameModeHelp() {
        this.gameModeHelp1 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line1");
        this.gameModeHelp2 = new TranslatableComponent("selectWorld.gameMode." + this.gameMode.name + ".line2");
    }

    private void updateResultFolder() {
        this.resultFolder = this.nameEdit.getValue().trim();
        if (this.resultFolder.isEmpty()) {
            this.resultFolder = "World";
        }

        try {
            this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
        } catch (Exception var4) {
            this.resultFolder = "World";

            try {
                this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
            } catch (Exception var3) {
                throw new RuntimeException("Could not create save folder", var3);
            }
        }

    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onCreate() {
        this.minecraft.setScreen(null);
        if (!this.done) {
            this.done = true;
            WorldGenSettings var0 = this.worldGenSettingsComponent.makeSettings(this.hardCore);
            LevelSettings var2;
            if (var0.isDebug()) {
                GameRules var1 = new GameRules();
                var1.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
                var2 = new LevelSettings(this.nameEdit.getValue().trim(), GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, var1, var0);
            } else {
                var2 = new LevelSettings(
                    this.nameEdit.getValue().trim(),
                    this.gameMode.gameType,
                    this.hardCore,
                    this.effectiveDifficulty,
                    this.commands && !this.hardCore,
                    this.gameRules,
                    var0
                );
            }

            this.minecraft.selectLevel(this.resultFolder, var2);
        }
    }

    private void toggleDisplayOptions() {
        this.setDisplayOptions(!this.displayOptions);
    }

    private void setGameMode(CreateWorldScreen.SelectedGameMode param0) {
        if (!this.commandsChanged) {
            this.commands = param0 == CreateWorldScreen.SelectedGameMode.CREATIVE;
        }

        if (param0 == CreateWorldScreen.SelectedGameMode.HARDCORE) {
            this.hardCore = true;
            this.commandsButton.active = false;
            this.worldGenSettingsComponent.bonusItemsButton.active = false;
            this.effectiveDifficulty = Difficulty.HARD;
            this.difficultyButton.active = false;
        } else {
            this.hardCore = false;
            this.commandsButton.active = true;
            this.worldGenSettingsComponent.bonusItemsButton.active = true;
            this.effectiveDifficulty = this.selectedDifficulty;
            this.difficultyButton.active = true;
        }

        this.gameMode = param0;
        this.updateGameModeHelp();
    }

    public void updateDisplayOptions() {
        this.setDisplayOptions(this.displayOptions);
    }

    private void setDisplayOptions(boolean param0) {
        this.displayOptions = param0;
        this.modeButton.visible = !this.displayOptions;
        this.difficultyButton.visible = !this.displayOptions;
        if (this.worldGenSettingsComponent.isDebug()) {
            this.modeButton.active = false;
            if (this.oldGameMode == null) {
                this.oldGameMode = this.gameMode;
            }

            this.setGameMode(CreateWorldScreen.SelectedGameMode.DEBUG);
            this.commandsButton.visible = false;
        } else {
            this.modeButton.active = true;
            if (this.oldGameMode != null) {
                this.setGameMode(this.oldGameMode);
            }

            this.commandsButton.visible = this.displayOptions;
        }

        this.worldGenSettingsComponent.setDisplayOptions(this.displayOptions);
        this.nameEdit.setVisible(!this.displayOptions);
        if (this.displayOptions) {
            this.moreOptionsButton.setMessage(CommonComponents.GUI_DONE);
        } else {
            this.moreOptionsButton.setMessage(new TranslatableComponent("selectWorld.moreWorldOptions"));
        }

        this.gameRulesButton.visible = !this.displayOptions;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (super.keyPressed(param0, param1, param2)) {
            return true;
        } else if (param0 != 257 && param0 != 335) {
            return false;
        } else {
            this.onCreate();
            return true;
        }
    }

    @Override
    public void onClose() {
        if (this.displayOptions) {
            this.setDisplayOptions(false);
        } else {
            this.minecraft.setScreen(this.lastScreen);
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 20, -1);
        if (this.displayOptions) {
            this.drawString(param0, this.font, I18n.get("selectWorld.enterSeed"), this.width / 2 - 100, 47, -6250336);
            this.drawString(param0, this.font, I18n.get("selectWorld.seedInfo"), this.width / 2 - 100, 85, -6250336);
            if (this.commandsButton.visible) {
                this.drawString(param0, this.font, I18n.get("selectWorld.allowCommands.info"), this.width / 2 - 150, 172, -6250336);
            }

            this.worldGenSettingsComponent.render(param0, param1, param2, param3);
        } else {
            this.drawString(param0, this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 47, -6250336);
            this.drawString(param0, this.font, I18n.get("selectWorld.resultFolder") + " " + this.resultFolder, this.width / 2 - 100, 85, -6250336);
            this.nameEdit.render(param0, param1, param2, param3);
            this.drawCenteredString(param0, this.font, this.gameModeHelp1, this.width / 2 - 155 + 75, 137, -6250336);
            this.drawCenteredString(param0, this.font, this.gameModeHelp2, this.width / 2 - 155 + 75, 149, -6250336);
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    protected <T extends GuiEventListener> T addWidget(T param0) {
        return super.addWidget(param0);
    }

    @Override
    protected <T extends AbstractWidget> T addButton(T param0) {
        return super.addButton(param0);
    }

    @OnlyIn(Dist.CLIENT)
    static enum SelectedGameMode {
        SURVIVAL("survival", GameType.SURVIVAL),
        HARDCORE("hardcore", GameType.SURVIVAL),
        CREATIVE("creative", GameType.CREATIVE),
        DEBUG("spectator", GameType.SPECTATOR);

        private final String name;
        private final GameType gameType;

        private SelectedGameMode(String param0, GameType param1) {
            this.name = param0;
            this.gameType = param1;
        }
    }
}
