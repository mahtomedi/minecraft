package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class CreateWorldScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMP_WORLD_PREFIX = "mcworld-";
    private static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
    private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
    private static final Component SEED_INFO = Component.translatable("selectWorld.seedInfo");
    private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
    private static final Component OUTPUT_DIR_INFO = Component.translatable("selectWorld.resultFolder");
    private static final Component COMMANDS_INFO = Component.translatable("selectWorld.allowCommands.info");
    private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
    @Nullable
    private final Screen lastScreen;
    private EditBox nameEdit;
    String resultFolder;
    private CreateWorldScreen.SelectedGameMode gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
    @Nullable
    private CreateWorldScreen.SelectedGameMode oldGameMode;
    private Difficulty difficulty = Difficulty.NORMAL;
    private boolean commands;
    private boolean commandsChanged;
    public boolean hardCore;
    protected WorldDataConfiguration dataConfiguration;
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

    public static void openFresh(Minecraft param0, @Nullable Screen param1) {
        queueLoadScreen(param0, PREPARING_WORLD_DATA);
        PackRepository var0 = new PackRepository(new ServerPacksSource());
        WorldLoader.InitConfig var1 = createDefaultLoadConfig(var0, WorldDataConfiguration.DEFAULT);
        CompletableFuture<WorldCreationContext> var2 = WorldLoader.load(
            var1,
            param0x -> new WorldLoader.DataLoadOutput<>(
                    new CreateWorldScreen.DataPackReloadCookie(
                        new WorldGenSettings(WorldOptions.defaultWithRandomSeed(), WorldPresets.createNormalWorldDimensions(param0x.datapackWorldgen())),
                        param0x.dataConfiguration()
                    ),
                    param0x.datapackDimensions()
                ),
            (param0x, param1x, param2, param3) -> {
                param0x.close();
                return new WorldCreationContext(param3.worldGenSettings(), param2, param1x, param3.dataConfiguration());
            },
            Util.backgroundExecutor(),
            param0
        );
        param0.managedBlock(var2::isDone);
        param0.setScreen(
            new CreateWorldScreen(param1, WorldDataConfiguration.DEFAULT, new WorldGenSettingsComponent(var2.join(), Optional.of(WorldPresets.NORMAL)))
        );
    }

    public static CreateWorldScreen createFromExisting(@Nullable Screen param0, LevelSettings param1, WorldCreationContext param2, @Nullable Path param3) {
        CreateWorldScreen var0 = new CreateWorldScreen(
            param0,
            param2.dataConfiguration(),
            new WorldGenSettingsComponent(param2, WorldPresets.fromSettings(param2.selectedDimensions().dimensions()), param2.options().seed())
        );
        var0.initName = param1.levelName();
        var0.commands = param1.allowCommands();
        var0.commandsChanged = true;
        var0.difficulty = param1.difficulty();
        var0.gameRules.assignFrom(param1.gameRules(), null);
        if (param1.hardcore()) {
            var0.gameMode = CreateWorldScreen.SelectedGameMode.HARDCORE;
        } else if (param1.gameType().isSurvival()) {
            var0.gameMode = CreateWorldScreen.SelectedGameMode.SURVIVAL;
        } else if (param1.gameType().isCreative()) {
            var0.gameMode = CreateWorldScreen.SelectedGameMode.CREATIVE;
        }

        var0.tempDataPackDir = param3;
        return var0;
    }

    private CreateWorldScreen(@Nullable Screen param0, WorldDataConfiguration param1, WorldGenSettingsComponent param2) {
        super(Component.translatable("selectWorld.create"));
        this.lastScreen = param0;
        this.initName = I18n.get("selectWorld.newWorld");
        this.dataConfiguration = param1;
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
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 60, 200, 20, Component.translatable("selectWorld.enterName")) {
            @Override
            protected MutableComponent createNarrationMessage() {
                return CommonComponents.joinForNarration(super.createNarrationMessage(), Component.translatable("selectWorld.resultFolder"))
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
        this.addWidget(this.nameEdit);
        int var0 = this.width / 2 - 155;
        int var1 = this.width / 2 + 5;
        this.modeButton = this.addRenderableWidget(
            CycleButton.builder(CreateWorldScreen.SelectedGameMode::getDisplayName)
                .withValues(
                    CreateWorldScreen.SelectedGameMode.SURVIVAL, CreateWorldScreen.SelectedGameMode.HARDCORE, CreateWorldScreen.SelectedGameMode.CREATIVE
                )
                .withInitialValue(this.gameMode)
                .withCustomNarration(
                    param0 -> AbstractWidget.wrapDefaultNarrationMessage(param0.getMessage())
                            .append(CommonComponents.NARRATION_SEPARATOR)
                            .append(this.gameModeHelp1)
                            .append(" ")
                            .append(this.gameModeHelp2)
                )
                .create(var0, 100, 150, 20, GAME_MODEL_LABEL, (param0, param1) -> this.setGameMode(param1))
        );
        this.difficultyButton = this.addRenderableWidget(
            CycleButton.builder(Difficulty::getDisplayName)
                .withValues(Difficulty.values())
                .withInitialValue(this.getEffectiveDifficulty())
                .create(var1, 100, 150, 20, Component.translatable("options.difficulty"), (param0, param1) -> this.difficulty = param1)
        );
        this.commandsButton = this.addRenderableWidget(
            CycleButton.onOffBuilder(this.commands && !this.hardCore)
                .withCustomNarration(
                    param0 -> CommonComponents.joinForNarration(
                            param0.createDefaultNarrationMessage(), Component.translatable("selectWorld.allowCommands.info")
                        )
                )
                .create(var0, 151, 150, 20, Component.translatable("selectWorld.allowCommands"), (param0, param1) -> {
                    this.commandsChanged = true;
                    this.commands = param1;
                })
        );
        this.dataPacksButton = this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.dataPacks"), param0 -> this.openDataPackSelectionScreen()).bounds(var1, 151, 150, 20).build()
        );
        this.gameRulesButton = this.addRenderableWidget(
            Button.builder(
                    Component.translatable("selectWorld.gameRules"),
                    param0 -> this.minecraft.setScreen(new EditGameRulesScreen(this.gameRules.copy(), param0x -> {
                            this.minecraft.setScreen(this);
                            param0x.ifPresent(param0xx -> this.gameRules = param0xx);
                        }))
                )
                .bounds(var0, 185, 150, 20)
                .build()
        );
        this.worldGenSettingsComponent.init(this, this.minecraft, this.font);
        this.moreOptionsButton = this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.moreWorldOptions"), param0 -> this.toggleWorldGenSettingsVisibility())
                .bounds(var1, 185, 150, 20)
                .build()
        );
        this.createButton = this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.create"), param0 -> this.onCreate()).bounds(var0, this.height - 28, 150, 20).build()
        );
        this.createButton.active = !this.initName.isEmpty();
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.popScreen()).bounds(var1, this.height - 28, 150, 20).build());
        this.refreshWorldGenSettingsVisibility();
        this.setInitialFocus(this.nameEdit);
        this.setGameMode(this.gameMode);
        this.updateResultFolder();
    }

    private Difficulty getEffectiveDifficulty() {
        return this.gameMode == CreateWorldScreen.SelectedGameMode.HARDCORE ? Difficulty.HARD : this.difficulty;
    }

    private void updateGameModeHelp() {
        this.gameModeHelp1 = Component.translatable("selectWorld.gameMode." + this.gameMode.name + ".line1");
        this.gameModeHelp2 = Component.translatable("selectWorld.gameMode." + this.gameMode.name + ".line2");
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

    private static void queueLoadScreen(Minecraft param0, Component param1) {
        param0.forceSetScreen(new GenericDirtMessageScreen(param1));
    }

    private void onCreate() {
        WorldCreationContext var0 = this.worldGenSettingsComponent.settings();
        WorldDimensions.Complete var1 = var0.selectedDimensions().bake(var0.datapackDimensions());
        LayeredRegistryAccess<RegistryLayer> var2 = var0.worldgenRegistries().replaceFrom(RegistryLayer.DIMENSIONS, var1.dimensionsRegistryAccess());
        Lifecycle var3 = FeatureFlags.isExperimental(var0.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
        Lifecycle var4 = var2.compositeAccess().allElementsLifecycle();
        Lifecycle var5 = var4.add(var3);
        WorldOpenFlows.confirmWorldCreation(this.minecraft, this, var5, () -> this.createNewWorld(var1.specialWorldProperty(), var2, var5));
    }

    private void createNewWorld(PrimaryLevelData.SpecialWorldProperty param0, LayeredRegistryAccess<RegistryLayer> param1, Lifecycle param2) {
        queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
        Optional<LevelStorageSource.LevelStorageAccess> var0 = this.createNewWorldDirectory();
        if (!var0.isEmpty()) {
            this.removeTempDataPackDir();
            boolean var1 = param0 == PrimaryLevelData.SpecialWorldProperty.DEBUG;
            WorldCreationContext var2 = this.worldGenSettingsComponent.settings();
            WorldOptions var3 = this.worldGenSettingsComponent.createFinalOptions(var1, this.hardCore);
            LevelSettings var4 = this.createLevelSettings(var1);
            WorldData var5 = new PrimaryLevelData(var4, var3, param0, param2);
            this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(var0.get(), var2.dataPackResources(), param1, var5);
        }
    }

    private LevelSettings createLevelSettings(boolean param0) {
        String var0 = this.nameEdit.getValue().trim();
        if (param0) {
            GameRules var1 = new GameRules();
            var1.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
            return new LevelSettings(var0, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, var1, WorldDataConfiguration.DEFAULT);
        } else {
            return new LevelSettings(
                var0,
                this.gameMode.gameType,
                this.hardCore,
                this.getEffectiveDifficulty(),
                this.commands && !this.hardCore,
                this.gameRules,
                this.dataConfiguration
            );
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
            this.moreOptionsButton.setMessage(Component.translatable("selectWorld.moreWorldOptions"));
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
            drawString(param0, this.font, Component.empty().append(OUTPUT_DIR_INFO).append(" ").append(this.resultFolder), this.width / 2 - 100, 85, -6250336);
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
    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T param0) {
        return super.addWidget(param0);
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T param0) {
        return super.addRenderableWidget(param0);
    }

    @Nullable
    private Path getTempDataPackDir() {
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
        Pair<Path, PackRepository> var0 = this.getDataPackSelectionSettings();
        if (var0 != null) {
            this.minecraft
                .setScreen(
                    new PackSelectionScreen(this, var0.getSecond(), this::tryApplyNewDataPacks, var0.getFirst(), Component.translatable("dataPack.title"))
                );
        }

    }

    private void tryApplyNewDataPacks(PackRepository param0) {
        List<String> var0x = ImmutableList.copyOf(param0.getSelectedIds());
        List<String> var1 = param0.getAvailableIds().stream().filter(param1 -> !var0x.contains(param1)).collect(ImmutableList.toImmutableList());
        WorldDataConfiguration var2 = new WorldDataConfiguration(new DataPackConfig(var0x, var1), this.dataConfiguration.enabledFeatures());
        if (var0x.equals(this.dataConfiguration.dataPacks().getEnabled())) {
            this.dataConfiguration = var2;
        } else {
            FeatureFlagSet var3 = param0.getRequestedFeatureFlags();
            if (FeatureFlags.isExperimental(var3)) {
                this.minecraft.tell(() -> this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen(param0.getSelectedPacks(), param2 -> {
                        if (param2) {
                            this.applyNewPackConfig(param0, var2);
                        } else {
                            this.openDataPackSelectionScreen();
                        }

                    })));
            } else {
                this.applyNewPackConfig(param0, var2);
            }

        }
    }

    private void applyNewPackConfig(PackRepository param0, WorldDataConfiguration param1) {
        this.minecraft.tell(() -> this.minecraft.setScreen(new GenericDirtMessageScreen(Component.translatable("dataPack.validation.working"))));
        WorldLoader.InitConfig var0 = createDefaultLoadConfig(param0, param1);
        WorldLoader.<CreateWorldScreen.DataPackReloadCookie, WorldCreationContext>load(
                var0,
                param0x -> {
                    if (param0x.datapackWorldgen().registryOrThrow(Registry.WORLD_PRESET_REGISTRY).size() == 0) {
                        throw new IllegalStateException("Needs at least one world preset to continue");
                    } else if (param0x.datapackWorldgen().registryOrThrow(Registry.BIOME_REGISTRY).size() == 0) {
                        throw new IllegalStateException("Needs at least one biome continue");
                    } else {
                        WorldCreationContext var0x = this.worldGenSettingsComponent.settings();
                        DynamicOps<JsonElement> var1x = RegistryOps.create(JsonOps.INSTANCE, var0x.worldgenLoadContext());
                        DataResult<JsonElement> var2x = WorldGenSettings.encode(var1x, var0x.options(), var0x.selectedDimensions())
                            .setLifecycle(Lifecycle.stable());
                        DynamicOps<JsonElement> var3x = RegistryOps.create(JsonOps.INSTANCE, param0x.datapackWorldgen());
                        WorldGenSettings var4 = var2x.<WorldGenSettings>flatMap(param1x -> WorldGenSettings.CODEC.parse(var3x, param1x))
                            .getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error));
                        return new WorldLoader.DataLoadOutput<>(
                            new CreateWorldScreen.DataPackReloadCookie(var4, param0x.dataConfiguration()), param0x.datapackDimensions()
                        );
                    }
                },
                (param0x, param1x, param2, param3) -> {
                    param0x.close();
                    return new WorldCreationContext(param3.worldGenSettings(), param2, param1x, param3.dataConfiguration());
                },
                Util.backgroundExecutor(),
                this.minecraft
            )
            .thenAcceptAsync(param0x -> {
                this.dataConfiguration = param0x.dataConfiguration();
                this.worldGenSettingsComponent.updateSettings(param0x);
                this.rebuildWidgets();
            }, this.minecraft)
            .handle(
                (param0x, param1x) -> {
                    if (param1x != null) {
                        LOGGER.warn("Failed to validate datapack", param1x);
                        this.minecraft
                            .tell(
                                () -> this.minecraft
                                        .setScreen(
                                            new ConfirmScreen(
                                                param0xx -> {
                                                    if (param0xx) {
                                                        this.openDataPackSelectionScreen();
                                                    } else {
                                                        this.dataConfiguration = WorldDataConfiguration.DEFAULT;
                                                        this.minecraft.setScreen(this);
                                                    }
                            
                                                },
                                                Component.translatable("dataPack.validation.failed"),
                                                CommonComponents.EMPTY,
                                                Component.translatable("dataPack.validation.back"),
                                                Component.translatable("dataPack.validation.reset")
                                            )
                                        )
                            );
                    } else {
                        this.minecraft.tell(() -> this.minecraft.setScreen(this));
                    }
        
                    return null;
                }
            );
    }

    private static WorldLoader.InitConfig createDefaultLoadConfig(PackRepository param0, WorldDataConfiguration param1) {
        WorldLoader.PackConfig var0 = new WorldLoader.PackConfig(param0, param1, false, true);
        return new WorldLoader.InitConfig(var0, Commands.CommandSelection.INTEGRATED, 2);
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
            } catch (IOException var6) {
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
            throw new UncheckedIOException(var4);
        }
    }

    private Optional<LevelStorageSource.LevelStorageAccess> createNewWorldDirectory() {
        try {
            LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().createAccess(this.resultFolder);
            if (this.tempDataPackDir == null) {
                return Optional.of(var0);
            }

            try {
                Optional var41;
                try (Stream<Path> var1 = Files.walk(this.tempDataPackDir)) {
                    Path var2 = var0.getLevelPath(LevelResource.DATAPACK_DIR);
                    Files.createDirectories(var2);
                    var1.filter(param0 -> !param0.equals(this.tempDataPackDir)).forEach(param1 -> copyBetweenDirs(this.tempDataPackDir, var2, param1));
                    var41 = Optional.of(var0);
                }

                return var41;
            } catch (UncheckedIOException | IOException var7) {
                LOGGER.warn("Failed to copy datapacks to world {}", this.resultFolder, var7);
                var0.close();
            }
        } catch (UncheckedIOException | IOException var8) {
            LOGGER.warn("Failed to create access for {}", this.resultFolder, var8);
        }

        SystemToast.onPackCopyFailure(this.minecraft, this.resultFolder);
        this.popScreen();
        return Optional.empty();
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
                        throw new UncheckedIOException(var5);
                    }

                    var0.setValue(var0x);
                }

                copyBetweenDirs(param0, var0x, param2);
            });
        } catch (UncheckedIOException | IOException var8) {
            LOGGER.warn("Failed to copy datapacks from world {}", param0, var8);
            SystemToast.onPackCopyFailure(param1, param0.toString());
            return null;
        }

        return var0.getValue();
    }

    @Nullable
    private Pair<Path, PackRepository> getDataPackSelectionSettings() {
        Path var0 = this.getTempDataPackDir();
        if (var0 != null) {
            if (this.tempDataPackRepository == null) {
                this.tempDataPackRepository = ServerPacksSource.createPackRepository(var0);
                this.tempDataPackRepository.reload();
            }

            this.tempDataPackRepository.setSelected(this.dataConfiguration.dataPacks().getEnabled());
            return Pair.of(var0, this.tempDataPackRepository);
        } else {
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record DataPackReloadCookie(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
    }

    @OnlyIn(Dist.CLIENT)
    static enum SelectedGameMode {
        SURVIVAL("survival", GameType.SURVIVAL),
        HARDCORE("hardcore", GameType.SURVIVAL),
        CREATIVE("creative", GameType.CREATIVE),
        DEBUG("spectator", GameType.SPECTATOR);

        final String name;
        final GameType gameType;
        private final Component displayName;

        private SelectedGameMode(String param0, GameType param1) {
            this.name = param0;
            this.gameType = param1;
            this.displayName = Component.translatable("selectWorld.gameMode." + param0);
        }

        public Component getDisplayName() {
            return this.displayName;
        }
    }
}
