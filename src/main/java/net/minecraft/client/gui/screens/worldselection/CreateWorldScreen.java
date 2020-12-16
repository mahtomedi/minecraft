package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.ServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CreateWorldScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Component GAME_MODEL_LABEL = new TranslatableComponent("selectWorld.gameMode");
    private static final Component SEED_LABEL = new TranslatableComponent("selectWorld.enterSeed");
    private static final Component SEED_INFO = new TranslatableComponent("selectWorld.seedInfo");
    private static final Component NAME_LABEL = new TranslatableComponent("selectWorld.enterName");
    private static final Component OUTPUT_DIR_INFO = new TranslatableComponent("selectWorld.resultFolder");
    private static final Component COMMANDS_INFO = new TranslatableComponent("selectWorld.allowCommands.info");
    private final Screen lastScreen;
    private EditBox nameEdit;
    private String resultFolder;
    private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
    @Nullable
    private CreateWorldScreen.SelectedGameMode oldGameMode;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean commands;
    private boolean commandsChanged;
    public boolean hardCore;
    protected DataPackConfig dataPacks;
    @Nullable
    private Path tempDataPackDir;
    @Nullable
    private PackRepository tempDataPackRepository;
    private boolean worldGenSettingsVisible;
    private Button createButton;
    private CycleButton<CreateWorldScreen.SelectedGameMode> modeButton;
    private CycleButton<Difficulty> difficultyButton;
    private Button moreOptionsButton;
    private Button gameRulesButton;
    private Button dataPacksButton;
    private CycleButton<Boolean> commandsButton;
    private Component gameModeHelp1;
    private Component gameModeHelp2;
    private String initName;
    private GameRules gameRules = new GameRules();
    public final WorldGenSettingsComponent worldGenSettingsComponent;

    public CreateWorldScreen(
        @Nullable Screen param0,
        LevelSettings param1,
        WorldGenSettings param2,
        @Nullable Path param3,
        DataPackConfig param4,
        RegistryAccess.RegistryHolder param5
    ) {
        this(param0, param4, new WorldGenSettingsComponent(param5, param2, WorldPreset.of(param2), OptionalLong.of(param2.seed())));
        this.initName = param1.levelName();
        this.commands = param1.allowCommands();
        this.commandsChanged = true;
        this.difficulty = param1.difficulty();
        this.gameRules.assignFrom(param1.gameRules(), null);
        if (param1.hardcore()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
        } else if (param1.gameType().isSurvival()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
        } else if (param1.gameType().isCreative()) {
            this.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
        }

        this.tempDataPackDir = param3;
    }

    public static CreateWorldScreen create(@Nullable Screen param0) {
        RegistryAccess.RegistryHolder var0 = RegistryAccess.builtin();
        return new CreateWorldScreen(
            param0,
            DataPackConfig.DEFAULT,
            new WorldGenSettingsComponent(
                var0,
                WorldGenSettings.makeDefault(
                    var0.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY),
                    var0.registryOrThrow(Registry.BIOME_REGISTRY),
                    var0.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
                ),
                Optional.of(WorldPreset.NORMAL),
                OptionalLong.empty()
            )
        );
    }

    private CreateWorldScreen(@Nullable Screen param0, DataPackConfig param1, WorldGenSettingsComponent param2) {
        super(new TranslatableComponent("selectWorld.create"));
        this.lastScreen = param0;
        this.initName = I18n.get("selectWorld.newWorld");
        this.dataPacks = param1;
        this.worldGenSettingsComponent = param2;
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
        int var0 = this.width / 2 - 155;
        int var1 = this.width / 2 + 5;
        this.modeButton = this.addButton(
            CycleButton.builder(CreateWorldScreen.SelectedGameMode::getDisplayName)
                .withValues(
                    CreateWorldScreen.SelectedGameMode.SURVIVAL, CreateWorldScreen.SelectedGameMode.CREATIVE, CreateWorldScreen.SelectedGameMode.HARDCORE
                )
                .withInitialValue(this.gameMode)
                .withCustomNarration(
                    param0 -> AbstractWidget.wrapDefaultNarrationMessage(param0.getMessage())
                            .append(". ")
                            .append(this.gameModeHelp1)
                            .append(" ")
                            .append(this.gameModeHelp2)
                )
                .create(var0, 100, 150, 20, GAME_MODEL_LABEL, (param0, param1) -> this.setGameMode(param1))
        );
        this.difficultyButton = this.addButton(
            CycleButton.builder(Difficulty::getDisplayName)
                .withValues(Difficulty.values())
                .withInitialValue(this.getEffectiveDifficulty())
                .create(var1, 100, 150, 20, new TranslatableComponent("options.difficulty"), (param0, param1) -> this.difficulty = param1)
        );
        this.commandsButton = this.addButton(
            CycleButton.onOffBuilder(this.commands && !this.hardCore)
                .withCustomNarration(
                    param0 -> param0.createDefaultNarrationMessage().append(". ").append(new TranslatableComponent("selectWorld.allowCommands.info"))
                )
                .create(var0, 151, 150, 20, new TranslatableComponent("selectWorld.allowCommands"), (param0, param1) -> {
                    this.commandsChanged = true;
                    this.commands = param1;
                })
        );
        this.dataPacksButton = this.addButton(
            new Button(var1, 151, 150, 20, new TranslatableComponent("selectWorld.dataPacks"), param0 -> this.openDataPackSelectionScreen())
        );
        this.gameRulesButton = this.addButton(
            new Button(
                var0,
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
        this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
        this.moreOptionsButton = this.addButton(
            new Button(var1, 185, 150, 20, new TranslatableComponent("selectWorld.moreWorldOptions"), param0 -> this.toggleWorldGenSettingsVisibility())
        );
        this.createButton = this.addButton(
            new Button(var0, this.height - 28, 150, 20, new TranslatableComponent("selectWorld.create"), param0 -> this.onCreate())
        );
        this.createButton.active = !this.initName.isEmpty();
        this.addButton(new Button(var1, this.height - 28, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.popScreen()));
        this.refreshWorldGenSettingsVisibility();
        this.setInitialFocus(this.nameEdit);
        this.setGameMode(this.gameMode);
        this.updateResultFolder();
    }

    private Difficulty getEffectiveDifficulty() {
        return this.gameMode == CreateWorldScreen.SelectedGameMode.HARDCORE ? Difficulty.HARD : this.difficulty;
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
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(new TranslatableComponent("createWorld.preparing")));
        if (this.copyTempDataPackDirToNewWorld()) {
            this.cleanupTempResources();
            WorldGenSettings var0 = this.worldGenSettingsComponent.makeSettings(this.hardCore);
            LevelSettings var2;
            if (var0.isDebug()) {
                GameRules var1 = new GameRules();
                var1.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
                var2 = new LevelSettings(this.nameEdit.getValue().trim(), GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, var1, DataPackConfig.DEFAULT);
            } else {
                var2 = new LevelSettings(
                    this.nameEdit.getValue().trim(),
                    this.gameMode.gameType,
                    this.hardCore,
                    this.getEffectiveDifficulty(),
                    this.commands && !this.hardCore,
                    this.gameRules,
                    this.dataPacks
                );
            }

            this.minecraft.createLevel(this.resultFolder, var2, this.worldGenSettingsComponent.registryHolder(), var0);
        }
    }

    private void toggleWorldGenSettingsVisibility() {
        this.setWorldGenSettingsVisible(!this.worldGenSettingsVisible);
    }

    private void setGameMode(CreateWorldScreen.SelectedGameMode param0) {
        if (!this.commandsChanged) {
            this.commands = param0 == CreateWorldScreen.SelectedGameMode.CREATIVE;
            this.commandsButton.setValue(this.commands);
        }

        if (param0 == CreateWorldScreen.SelectedGameMode.HARDCORE) {
            this.hardCore = true;
            this.commandsButton.active = false;
            this.commandsButton.setValue(false);
            this.worldGenSettingsComponent.switchToHardcore();
            this.difficultyButton.setValue(Difficulty.HARD);
            this.difficultyButton.active = false;
        } else {
            this.hardCore = false;
            this.commandsButton.active = true;
            this.commandsButton.setValue(this.commands);
            this.worldGenSettingsComponent.switchOutOfHardcode();
            this.difficultyButton.setValue(this.difficulty);
            this.difficultyButton.active = true;
        }

        this.gameMode = param0;
        this.updateGameModeHelp();
    }

    public void refreshWorldGenSettingsVisibility() {
        this.setWorldGenSettingsVisible(this.worldGenSettingsVisible);
    }

    private void setWorldGenSettingsVisible(boolean param0) {
        this.worldGenSettingsVisible = param0;
        this.modeButton.visible = !param0;
        this.difficultyButton.visible = !param0;
        if (this.worldGenSettingsComponent.isDebug()) {
            this.dataPacksButton.visible = false;
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

            this.commandsButton.visible = !param0;
            this.dataPacksButton.visible = !param0;
        }

        this.worldGenSettingsComponent.setVisibility(param0);
        this.nameEdit.setVisible(!param0);
        if (param0) {
            this.moreOptionsButton.setMessage(CommonComponents.GUI_DONE);
        } else {
            this.moreOptionsButton.setMessage(new TranslatableComponent("selectWorld.moreWorldOptions"));
        }

        this.gameRulesButton.visible = !param0;
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
        if (this.worldGenSettingsVisible) {
            this.setWorldGenSettingsVisible(false);
        } else {
            this.popScreen();
        }

    }

    public void popScreen() {
        this.minecraft.setScreen(this.lastScreen);
        this.cleanupTempResources();
    }

    private void cleanupTempResources() {
        if (this.tempDataPackRepository != null) {
            this.tempDataPackRepository.close();
        }

        this.removeTempDataPackDir();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 20, -1);
        if (this.worldGenSettingsVisible) {
            drawString(param0, this.font, SEED_LABEL, this.width / 2 - 100, 47, -6250336);
            drawString(param0, this.font, SEED_INFO, this.width / 2 - 100, 85, -6250336);
            this.worldGenSettingsComponent.render(param0, param1, param2, param3);
        } else {
            drawString(param0, this.font, NAME_LABEL, this.width / 2 - 100, 47, -6250336);
            drawString(
                param0, this.font, new TextComponent("").append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 100, 85, -6250336
            );
            this.nameEdit.render(param0, param1, param2, param3);
            drawString(param0, this.font, this.gameModeHelp1, this.width / 2 - 150, 122, -6250336);
            drawString(param0, this.font, this.gameModeHelp2, this.width / 2 - 150, 134, -6250336);
            if (this.commandsButton.visible) {
                drawString(param0, this.font, COMMANDS_INFO, this.width / 2 - 150, 172, -6250336);
            }
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

    @Nullable
    protected Path getTempDataPackDir() {
        if (this.tempDataPackDir == null) {
            try {
                this.tempDataPackDir = Files.createTempDirectory("mcworld-");
            } catch (IOException var2) {
                LOGGER.warn("Failed to create temporary dir", (Throwable)var2);
                SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
                this.popScreen();
            }
        }

        return this.tempDataPackDir;
    }

    private void openDataPackSelectionScreen() {
        Pair<File, PackRepository> var0 = this.getDataPackSelectionSettings();
        if (var0 != null) {
            this.minecraft
                .setScreen(
                    new PackSelectionScreen(this, var0.getSecond(), this::tryApplyNewDataPacks, var0.getFirst(), new TranslatableComponent("dataPack.title"))
                );
        }

    }

    private void tryApplyNewDataPacks(PackRepository param0) {
        List<String> var0x = ImmutableList.copyOf(param0.getSelectedIds());
        List<String> var1 = param0.getAvailableIds().stream().filter(param1 -> !var0x.contains(param1)).collect(ImmutableList.toImmutableList());
        DataPackConfig var2 = new DataPackConfig(var0x, var1);
        if (var0x.equals(this.dataPacks.getEnabled())) {
            this.dataPacks = var2;
        } else {
            this.minecraft.tell(() -> this.minecraft.setScreen(new GenericDirtMessageScreen(new TranslatableComponent("dataPack.validation.working"))));
            ServerResources.loadResources(
                    param0.openAllSelected(),
                    this.worldGenSettingsComponent.registryHolder(),
                    Commands.CommandSelection.INTEGRATED,
                    2,
                    Util.backgroundExecutor(),
                    this.minecraft
                )
                .handle(
                    (param1, param2) -> {
                        if (param2 != null) {
                            LOGGER.warn("Failed to validate datapack", param2);
                            this.minecraft
                                .tell(
                                    () -> this.minecraft
                                            .setScreen(
                                                new ConfirmScreen(
                                                    param0x -> {
                                                        if (param0x) {
                                                            this.openDataPackSelectionScreen();
                                                        } else {
                                                            this.dataPacks = DataPackConfig.DEFAULT;
                                                            this.minecraft.setScreen(this);
                                                        }
                            
                                                    },
                                                    new TranslatableComponent("dataPack.validation.failed"),
                                                    TextComponent.EMPTY,
                                                    new TranslatableComponent("dataPack.validation.back"),
                                                    new TranslatableComponent("dataPack.validation.reset")
                                                )
                                            )
                                );
                        } else {
                            this.minecraft.tell(() -> {
                                this.dataPacks = var2;
                                this.worldGenSettingsComponent.updateDataPacks(param1);
                                param1.close();
                                this.minecraft.setScreen(this);
                            });
                        }
        
                        return null;
                    }
                );
        }
    }

    private void removeTempDataPackDir() {
        if (this.tempDataPackDir != null) {
            try (Stream<Path> var0 = Files.walk(this.tempDataPackDir)) {
                var0.sorted(Comparator.reverseOrder()).forEach(param0 -> {
                    try {
                        Files.delete(param0);
                    } catch (IOException var2) {
                        LOGGER.warn("Failed to remove temporary file {}", param0, var2);
                    }

                });
            } catch (IOException var14) {
                LOGGER.warn("Failed to list temporary dir {}", this.tempDataPackDir);
            }

            this.tempDataPackDir = null;
        }

    }

    private static void copyBetweenDirs(Path param0, Path param1, Path param2) {
        try {
            Util.copyBetweenDirs(param0, param1, param2);
        } catch (IOException var4) {
            LOGGER.warn("Failed to copy datapack file from {} to {}", param2, param1);
            throw new CreateWorldScreen.OperationFailedException(var4);
        }
    }

    private boolean copyTempDataPackDirToNewWorld() {
        if (this.tempDataPackDir != null) {
            try (
                LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().createAccess(this.resultFolder);
                Stream<Path> var1 = Files.walk(this.tempDataPackDir);
            ) {
                Path var2 = var0.getLevelPath(LevelResource.DATAPACK_DIR);
                Files.createDirectories(var2);
                var1.filter(param0 -> !param0.equals(this.tempDataPackDir)).forEach(param1 -> copyBetweenDirs(this.tempDataPackDir, var2, param1));
            } catch (CreateWorldScreen.OperationFailedException | IOException var33) {
                LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, var33);
                SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
                this.popScreen();
                return false;
            }
        }

        return true;
    }

    @Nullable
    public static Path createTempDataPackDirFromExistingWorld(Path param0, Minecraft param1) {
        MutableObject<Path> var0 = new MutableObject<>();

        try (Stream<Path> var1 = Files.walk(param0)) {
            var1.filter(param1x -> !param1x.equals(param0)).forEach(param2 -> {
                Path var0x = var0.getValue();
                if (var0x == null) {
                    try {
                        var0x = Files.createTempDirectory("mcworld-");
                    } catch (IOException var5) {
                        LOGGER.warn("Failed to create temporary dir");
                        throw new CreateWorldScreen.OperationFailedException(var5);
                    }

                    var0.setValue(var0x);
                }

                copyBetweenDirs(param0, var0x, param2);
            });
        } catch (CreateWorldScreen.OperationFailedException | IOException var16) {
            LOGGER.warn("Failed to copy datapacks from world {}", param0, var16);
            SystemToast.onPackCopyFailure(param1, param0.toString());
            return null;
        }

        return var0.getValue();
    }

    @Nullable
    private Pair<File, PackRepository> getDataPackSelectionSettings() {
        Path var0 = this.getTempDataPackDir();
        if (var0 != null) {
            File var1 = var0.toFile();
            if (this.tempDataPackRepository == null) {
                this.tempDataPackRepository = new PackRepository(
                    PackType.SERVER_DATA, new ServerPacksSource(), new FolderRepositorySource(var1, PackSource.DEFAULT)
                );
                this.tempDataPackRepository.reload();
            }

            this.tempDataPackRepository.setSelected(this.dataPacks.getEnabled());
            return Pair.of(var1, this.tempDataPackRepository);
        } else {
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class OperationFailedException extends RuntimeException {
        public OperationFailedException(Throwable param0) {
            super(param0);
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
        private final Component displayName;

        private SelectedGameMode(String param0, GameType param1) {
            this.name = param0;
            this.gameType = param1;
            this.displayName = new TranslatableComponent("selectWorld.gameMode." + param0);
        }

        public Component getDisplayName() {
            return this.displayName;
        }
    }
}
