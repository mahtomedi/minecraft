package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
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
import net.minecraft.world.level.LevelType;
import net.minecraft.world.level.levelgen.ChunkGeneratorProvider;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class CreateWorldScreen extends Screen {
    private final Screen lastScreen;
    private EditBox nameEdit;
    private EditBox seedEdit;
    private String resultFolder;
    private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
    @Nullable
    private CreateWorldScreen.SelectedGameMode oldGameMode;
    private Difficulty selectedDifficulty = Difficulty.NORMAL;
    private Difficulty effectiveDifficulty = Difficulty.NORMAL;
    private boolean features = true;
    private boolean commands;
    private boolean commandsChanged;
    private boolean bonusItems;
    private boolean hardCore;
    private boolean done;
    private boolean displayOptions;
    private Button createButton;
    private Button modeButton;
    private Button difficultyButton;
    private Button moreOptionsButton;
    private Button gameRulesButton;
    private Button featuresButton;
    private Button bonusItemsButton;
    private Button typeButton;
    private Button commandsButton;
    private Button customizeTypeButton;
    private Component gameModeHelp1;
    private Component gameModeHelp2;
    private String initSeed;
    private String initName;
    private GameRules gameRules = new GameRules();
    private int levelTypeIndex;
    public ChunkGeneratorProvider levelTypeOptions = LevelType.NORMAL.getDefaultProvider();

    public CreateWorldScreen(@Nullable Screen param0) {
        super(new TranslatableComponent("selectWorld.create"));
        this.lastScreen = param0;
        this.initSeed = "";
        this.initName = I18n.get("selectWorld.newWorld");
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.seedEdit.tick();
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
        this.seedEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, new TranslatableComponent("selectWorld.enterSeed"));
        this.seedEdit.setValue(this.initSeed);
        this.seedEdit.setResponder(param0 -> this.initSeed = this.seedEdit.getValue());
        this.children.add(this.seedEdit);
        this.featuresButton = this.addButton(new Button(this.width / 2 - 155, 100, 150, 20, new TranslatableComponent("selectWorld.mapFeatures"), param0 -> {
            this.features = !this.features;
            param0.queueNarration(250);
        }) {
            @Override
            public Component getMessage() {
                return super.getMessage().mutableCopy().append(" ").append(CommonComponents.optionStatus(CreateWorldScreen.this.features));
            }

            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.mapFeatures.info"));
            }
        });
        this.featuresButton.visible = false;
        this.typeButton = this.addButton(new Button(this.width / 2 + 5, 100, 150, 20, new TranslatableComponent("selectWorld.mapType"), param0 -> {
            ++this.levelTypeIndex;
            if (this.levelTypeIndex >= LevelType.LEVEL_TYPES.length) {
                this.levelTypeIndex = 0;
            }

            while(!this.isValidLevelType()) {
                ++this.levelTypeIndex;
                if (this.levelTypeIndex >= LevelType.LEVEL_TYPES.length) {
                    this.levelTypeIndex = 0;
                }
            }

            this.levelTypeOptions = this.getLevelType().getDefaultProvider();
            this.setDisplayOptions(this.displayOptions);
            param0.queueNarration(250);
        }) {
            @Override
            public Component getMessage() {
                return super.getMessage().mutableCopy().append(" ").append(CreateWorldScreen.this.getLevelType().getDescription());
            }

            @Override
            protected MutableComponent createNarrationMessage() {
                LevelType var0 = CreateWorldScreen.this.getLevelType();
                return var0.hasHelpText() ? super.createNarrationMessage().append(". ").append(var0.getHelpText()) : super.createNarrationMessage();
            }
        });
        this.typeButton.visible = false;
        this.customizeTypeButton = this.addButton(
            new Button(this.width / 2 + 5, 120, 150, 20, new TranslatableComponent("selectWorld.customizeType"), param0 -> {
                if (this.getLevelType() == LevelType.FLAT) {
                    this.minecraft.setScreen(new CreateFlatWorldScreen(this, this.levelTypeOptions));
                }
    
                if (this.getLevelType() == LevelType.BUFFET) {
                    this.minecraft.setScreen(new CreateBuffetWorldScreen(this, this.levelTypeOptions));
                }
    
            })
        );
        this.customizeTypeButton.visible = false;
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
        this.bonusItemsButton = this.addButton(
            new Button(this.width / 2 + 5, 151, 150, 20, new TranslatableComponent("selectWorld.bonusItems"), param0 -> {
                this.bonusItems = !this.bonusItems;
                param0.queueNarration(250);
            }) {
                @Override
                public Component getMessage() {
                    return super.getMessage()
                        .mutableCopy()
                        .append(" ")
                        .append(CommonComponents.optionStatus(CreateWorldScreen.this.bonusItems && !CreateWorldScreen.this.hardCore));
                }
            }
        );
        this.bonusItemsButton.visible = false;
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
        this.setDisplayOptions(this.displayOptions);
        this.setInitialFocus(this.nameEdit);
        this.setGameMode(this.gameMode);
        this.updateResultFolder();
    }

    private LevelType getLevelType() {
        return LevelType.LEVEL_TYPES[this.levelTypeIndex];
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
            long var0 = new Random().nextLong();
            String var1 = this.seedEdit.getValue();
            if (!StringUtils.isEmpty(var1)) {
                try {
                    long var2 = Long.parseLong(var1);
                    if (var2 != 0L) {
                        var0 = var2;
                    }
                } catch (NumberFormatException var61) {
                    var0 = (long)var1.hashCode();
                }
            }

            LevelSettings var5;
            if (this.getLevelType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
                GameRules var4 = new GameRules();
                var4.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
                var5 = new LevelSettings(
                        this.nameEdit.getValue().trim(), var0, GameType.SPECTATOR, false, false, Difficulty.PEACEFUL, this.levelTypeOptions, var4
                    )
                    .enableSinglePlayerCommands();
            } else {
                var5 = new LevelSettings(
                    this.nameEdit.getValue().trim(),
                    var0,
                    this.gameMode.gameType,
                    this.features,
                    this.hardCore,
                    this.effectiveDifficulty,
                    this.levelTypeOptions,
                    this.gameRules
                );
                if (this.bonusItems && !this.hardCore) {
                    var5.enableStartingBonusItems();
                }

                if (this.commands && !this.hardCore) {
                    var5.enableSinglePlayerCommands();
                }
            }

            this.minecraft.selectLevel(this.resultFolder, var5);
        }
    }

    private boolean isValidLevelType() {
        LevelType var0 = this.getLevelType();
        if (var0 == null || !var0.isSelectable()) {
            return false;
        } else {
            return var0 == LevelType.DEBUG_ALL_BLOCK_STATES ? hasShiftDown() : true;
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
            this.bonusItemsButton.active = false;
            this.effectiveDifficulty = Difficulty.HARD;
            this.difficultyButton.active = false;
        } else {
            this.hardCore = false;
            this.commandsButton.active = true;
            this.bonusItemsButton.active = true;
            this.effectiveDifficulty = this.selectedDifficulty;
            this.difficultyButton.active = true;
        }

        this.gameMode = param0;
        this.updateGameModeHelp();
    }

    private void setDisplayOptions(boolean param0) {
        this.displayOptions = param0;
        this.modeButton.visible = !this.displayOptions;
        this.difficultyButton.visible = !this.displayOptions;
        this.typeButton.visible = this.displayOptions;
        if (this.getLevelType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
            this.modeButton.active = false;
            if (this.oldGameMode == null) {
                this.oldGameMode = this.gameMode;
            }

            this.setGameMode(CreateWorldScreen.SelectedGameMode.DEBUG);
            this.featuresButton.visible = false;
            this.bonusItemsButton.visible = false;
            this.commandsButton.visible = false;
            this.customizeTypeButton.visible = false;
        } else {
            this.modeButton.active = true;
            if (this.oldGameMode != null) {
                this.setGameMode(this.oldGameMode);
            }

            this.featuresButton.visible = this.displayOptions && this.getLevelType() != LevelType.CUSTOMIZED;
            this.bonusItemsButton.visible = this.displayOptions;
            this.commandsButton.visible = this.displayOptions;
            this.customizeTypeButton.visible = this.displayOptions && this.getLevelType().hasCustomOptions();
        }

        this.seedEdit.setVisible(this.displayOptions);
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
            if (this.featuresButton.visible) {
                this.drawString(param0, this.font, I18n.get("selectWorld.mapFeatures.info"), this.width / 2 - 150, 122, -6250336);
            }

            if (this.commandsButton.visible) {
                this.drawString(param0, this.font, I18n.get("selectWorld.allowCommands.info"), this.width / 2 - 150, 172, -6250336);
            }

            this.seedEdit.render(param0, param1, param2, param3);
            if (LevelType.LEVEL_TYPES[this.levelTypeIndex].hasHelpText()) {
                this.font.drawWordWrap(this.getLevelType().getHelpText(), this.typeButton.x + 2, this.typeButton.y + 22, this.typeButton.getWidth(), 10526880);
            }
        } else {
            this.drawString(param0, this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 47, -6250336);
            this.drawString(param0, this.font, I18n.get("selectWorld.resultFolder") + " " + this.resultFolder, this.width / 2 - 100, 85, -6250336);
            this.nameEdit.render(param0, param1, param2, param3);
            this.drawCenteredString(param0, this.font, this.gameModeHelp1, this.width / 2 - 155 + 75, 137, -6250336);
            this.drawCenteredString(param0, this.font, this.gameModeHelp2, this.width / 2 - 155 + 75, 149, -6250336);
        }

        super.render(param0, param1, param2, param3);
    }

    public void copyFromWorld(WorldData param0) {
        LevelSettings var0 = param0.getLevelSettings();
        this.initName = var0.getLevelName();
        this.initSeed = Long.toString(var0.getSeed());
        this.levelTypeOptions = var0.getGeneratorProvider();
        LevelType var1 = this.levelTypeOptions.getType() == LevelType.CUSTOMIZED ? LevelType.NORMAL : var0.getGeneratorProvider().getType();
        this.levelTypeIndex = var1.getId();
        this.features = var0.shouldGenerateMapFeatures();
        this.commands = var0.getAllowCommands();
        this.commandsChanged = true;
        this.bonusItems = var0.hasStartingBonusItems();
        this.selectedDifficulty = var0.getDifficulty();
        this.effectiveDifficulty = this.selectedDifficulty;
        this.gameRules.assignFrom(param0.getGameRules(), null);
        if (var0.isHardcore()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
        } else if (var0.getGameType().isSurvival()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
        } else if (var0.getGameType().isCreative()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
        }

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
