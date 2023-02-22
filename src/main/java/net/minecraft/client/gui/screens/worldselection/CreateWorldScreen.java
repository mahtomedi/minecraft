package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
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
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.components.tabs.TabNavigationBar;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.levelgen.presets.WorldPreset;
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
    private static final int GROUP_BOTTOM = 1;
    private static final int TAB_COLUMN_WIDTH = 210;
    private static final int FOOTER_HEIGHT = 36;
    private static final int TEXT_INDENT = 1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TEMP_WORLD_PREFIX = "mcworld-";
    static final Component GAME_MODEL_LABEL = Component.translatable("selectWorld.gameMode");
    static final Component NAME_LABEL = Component.translatable("selectWorld.enterName");
    static final Component EXPERIMENTS_LABEL = Component.translatable("selectWorld.experiments");
    static final Component ALLOW_CHEATS_INFO = Component.translatable("selectWorld.allowCommands.info");
    private static final Component PREPARING_WORLD_DATA = Component.translatable("createWorld.preparing");
    private static final int HORIZONTAL_BUTTON_SPACING = 10;
    private static final int VERTICAL_BUTTON_SPACING = 8;
    public static final ResourceLocation HEADER_SEPERATOR = new ResourceLocation("textures/gui/header_separator.png");
    public static final ResourceLocation FOOTER_SEPERATOR = new ResourceLocation("textures/gui/footer_separator.png");
    final WorldCreationUiState uiState;
    private final TabManager tabManager = new TabManager(this::addRenderableWidget, param1x -> this.removeWidget(param1x));
    private boolean recreated;
    @Nullable
    private final Screen lastScreen;
    @Nullable
    private String resultFolder;
    @Nullable
    private Path tempDataPackDir;
    @Nullable
    private PackRepository tempDataPackRepository;
    @Nullable
    private GridLayout bottomButtons;
    @Nullable
    private TabNavigationBar tabNavigationBar;

    public static void openFresh(Minecraft param0, @Nullable Screen param1) {
        queueLoadScreen(param0, PREPARING_WORLD_DATA);
        PackRepository var0 = new PackRepository(new ServerPacksSource());
        WorldLoader.InitConfig var1 = createDefaultLoadConfig(var0, WorldDataConfiguration.DEFAULT);
        CompletableFuture<WorldCreationContext> var2 = WorldLoader.load(
            var1,
            param0x -> new WorldLoader.DataLoadOutput(
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
        param0.setScreen(new CreateWorldScreen(param1, (WorldCreationContext)var2.join(), Optional.of(WorldPresets.NORMAL), OptionalLong.empty()));
    }

    public static CreateWorldScreen createFromExisting(@Nullable Screen param0, LevelSettings param1, WorldCreationContext param2, @Nullable Path param3) {
        CreateWorldScreen var0 = new CreateWorldScreen(
            param0, param2, WorldPresets.fromSettings(param2.selectedDimensions().dimensions()), OptionalLong.of(param2.options().seed())
        );
        var0.recreated = true;
        var0.uiState.setName(param1.levelName());
        var0.uiState.setAllowCheats(param1.allowCommands());
        var0.uiState.setDifficulty(param1.difficulty());
        var0.uiState.getGameRules().assignFrom(param1.gameRules(), null);
        if (param1.hardcore()) {
            var0.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.HARDCORE);
        } else if (param1.gameType().isSurvival()) {
            var0.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.SURVIVAL);
        } else if (param1.gameType().isCreative()) {
            var0.uiState.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE);
        }

        var0.tempDataPackDir = param3;
        return var0;
    }

    private CreateWorldScreen(@Nullable Screen param0, WorldCreationContext param1, Optional<ResourceKey<WorldPreset>> param2, OptionalLong param3) {
        super(Component.translatable("selectWorld.create"));
        this.lastScreen = param0;
        this.uiState = new WorldCreationUiState(param1, param2, param3);
    }

    public WorldCreationUiState getUiState() {
        return this.uiState;
    }

    @Override
    public void tick() {
        this.tabManager.tickCurrent();
    }

    @Override
    protected void init() {
        this.updateResultFolder(this.uiState.getName());
        this.tabNavigationBar = TabNavigationBar.builder(this.tabManager, this.width)
            .addTabs(new CreateWorldScreen.GameTab(), new CreateWorldScreen.WorldTab(), new CreateWorldScreen.MoreTab())
            .build();
        this.addRenderableWidget(this.tabNavigationBar);
        this.uiState.addListener(param0 -> {
            if (param0.nameChanged()) {
                this.updateResultFolder(param0.getName());
            }
        });
        this.bottomButtons = new GridLayout().columnSpacing(10);
        GridLayout.RowHelper var0 = this.bottomButtons.createRowHelper(2);
        Button var1 = var0.addChild(Button.builder(Component.translatable("selectWorld.create"), param0 -> this.onCreate()).build());
        this.uiState.addListener(param1 -> var1.active = !this.uiState.getName().isEmpty());
        var0.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.popScreen()).build());
        this.bottomButtons.visitWidgets(param0 -> {
            param0.setTabOrderGroup(1);
            this.addRenderableWidget(param0);
        });
        this.tabNavigationBar.selectTab(0);
        this.uiState.onChanged();
        this.repositionElements();
    }

    @Override
    public void repositionElements() {
        if (this.tabNavigationBar != null && this.bottomButtons != null) {
            this.tabNavigationBar.setWidth(this.width);
            this.tabNavigationBar.arrangeElements();
            this.bottomButtons.arrangeElements();
            FrameLayout.centerInRectangle(this.bottomButtons, 0, this.height - 36, this.width, 36);
            int var0 = this.tabNavigationBar.getRectangle().bottom();
            ScreenRectangle var1 = new ScreenRectangle(0, var0, this.width, this.bottomButtons.getY() - var0);
            this.tabManager.setTabArea(var1);
        }
    }

    private void updateResultFolder(String param0) {
        this.resultFolder = param0.trim();
        if (this.resultFolder.isEmpty()) {
            this.resultFolder = "World";
        }

        try {
            this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
        } catch (Exception var5) {
            this.resultFolder = "World";

            try {
                this.resultFolder = FileUtil.findAvailableName(this.minecraft.getLevelSource().getBaseDir(), this.resultFolder, "");
            } catch (Exception var4) {
                throw new RuntimeException("Could not create save folder", var4);
            }
        }

    }

    private static void queueLoadScreen(Minecraft param0, Component param1) {
        param0.forceSetScreen(new GenericDirtMessageScreen(param1));
    }

    private void onCreate() {
        WorldCreationContext var0 = this.uiState.getSettings();
        WorldDimensions.Complete var1 = var0.selectedDimensions().bake(var0.datapackDimensions());
        LayeredRegistryAccess<RegistryLayer> var2 = var0.worldgenRegistries().replaceFrom(RegistryLayer.DIMENSIONS, var1.dimensionsRegistryAccess());
        Lifecycle var3 = FeatureFlags.isExperimental(var0.dataConfiguration().enabledFeatures()) ? Lifecycle.experimental() : Lifecycle.stable();
        Lifecycle var4 = var2.compositeAccess().allRegistriesLifecycle();
        Lifecycle var5 = var4.add(var3);
        boolean var6 = !this.recreated && var4 == Lifecycle.stable();
        WorldOpenFlows.confirmWorldCreation(this.minecraft, this, var5, () -> this.createNewWorld(var1.specialWorldProperty(), var2, var5), var6);
    }

    private void createNewWorld(PrimaryLevelData.SpecialWorldProperty param0, LayeredRegistryAccess<RegistryLayer> param1, Lifecycle param2) {
        queueLoadScreen(this.minecraft, PREPARING_WORLD_DATA);
        Optional<LevelStorageSource.LevelStorageAccess> var0 = this.createNewWorldDirectory();
        if (!var0.isEmpty()) {
            this.removeTempDataPackDir();
            boolean var1 = param0 == PrimaryLevelData.SpecialWorldProperty.DEBUG;
            WorldCreationContext var2 = this.uiState.getSettings();
            LevelSettings var3 = this.createLevelSettings(var1);
            WorldData var4 = new PrimaryLevelData(var3, var2.options(), param0, param2);
            this.minecraft.createWorldOpenFlows().createLevelFromExistingSettings(var0.get(), var2.dataPackResources(), param1, var4);
        }
    }

    private LevelSettings createLevelSettings(boolean param0) {
        String var0 = this.uiState.getName().trim();
        if (param0) {
            GameRules var1 = new GameRules();
            var1.getRule(GameRules.RULE_DAYLIGHT).set(false, null);
            return new LevelSettings(var0, GameType.SPECTATOR, false, Difficulty.PEACEFUL, true, var1, WorldDataConfiguration.DEFAULT);
        } else {
            return new LevelSettings(
                var0,
                this.uiState.getGameMode().gameType,
                this.uiState.isHardcore(),
                this.uiState.getDifficulty(),
                this.uiState.isAllowCheats(),
                this.uiState.getGameRules(),
                this.uiState.getSettings().dataConfiguration()
            );
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (this.tabNavigationBar.keyPressed(param0)) {
            return true;
        } else if (super.keyPressed(param0, param1, param2)) {
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
        this.popScreen();
    }

    public void popScreen() {
        this.minecraft.setScreen(this.lastScreen);
        this.removeTempDataPackDir();
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        RenderSystem.setShaderTexture(0, FOOTER_SEPERATOR);
        blit(param0, 0, Mth.roundToward(this.height - 36 - 2, 2), 0.0F, 0.0F, this.width, 2, 32, 2);
        super.render(param0, param1, param2, param3);
    }

    @Override
    public void renderDirtBackground(PoseStack param0) {
        RenderSystem.setShaderTexture(0, LIGHT_DIRT_BACKGROUND);
        int var0 = 32;
        blit(param0, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
    }

    @Override
    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T param0) {
        return super.addWidget(param0);
    }

    @Override
    protected <T extends GuiEventListener & Renderable & NarratableEntry> T addRenderableWidget(T param0x) {
        return super.addRenderableWidget(param0x);
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

    void openExperimentsScreen(WorldDataConfiguration param0) {
        Pair<Path, PackRepository> var0 = this.getDataPackSelectionSettings(param0);
        if (var0 != null) {
            this.minecraft
                .setScreen(new ExperimentsScreen(this, var0.getSecond(), param0x -> this.tryApplyNewDataPacks(param0x, false, this::openExperimentsScreen)));
        }

    }

    void openDataPackSelectionScreen(WorldDataConfiguration param0) {
        Pair<Path, PackRepository> var0 = this.getDataPackSelectionSettings(param0);
        if (var0 != null) {
            this.minecraft
                .setScreen(
                    new PackSelectionScreen(
                        var0.getSecond(),
                        param0x -> this.tryApplyNewDataPacks(param0x, true, this::openDataPackSelectionScreen),
                        var0.getFirst(),
                        Component.translatable("dataPack.title")
                    )
                );
        }

    }

    private void tryApplyNewDataPacks(PackRepository param0, boolean param1, Consumer<WorldDataConfiguration> param2) {
        List<String> var0 = ImmutableList.copyOf(param0.getSelectedIds());
        List<String> var1 = param0.getAvailableIds().stream().filter(param1x -> !var0.contains(param1x)).collect(ImmutableList.toImmutableList());
        WorldDataConfiguration var2 = new WorldDataConfiguration(
            new DataPackConfig(var0, var1), this.uiState.getSettings().dataConfiguration().enabledFeatures()
        );
        if (this.uiState.tryUpdateDataConfiguration(var2)) {
            this.minecraft.setScreen(this);
        } else {
            FeatureFlagSet var3 = param0.getRequestedFeatureFlags();
            if (FeatureFlags.isExperimental(var3) && param1) {
                this.minecraft.setScreen(new ConfirmExperimentalFeaturesScreen(param0.getSelectedPacks(), param3 -> {
                    if (param3) {
                        this.applyNewPackConfig(param0, var2, param2);
                    } else {
                        param2.accept(this.uiState.getSettings().dataConfiguration());
                    }

                }));
            } else {
                this.applyNewPackConfig(param0, var2, param2);
            }

        }
    }

    private void applyNewPackConfig(PackRepository param0, WorldDataConfiguration param1, Consumer<WorldDataConfiguration> param2) {
        this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("dataPack.validation.working")));
        WorldLoader.InitConfig var0 = createDefaultLoadConfig(param0, param1);
        WorldLoader.load(
                var0,
                param0x -> {
                    if (param0x.datapackWorldgen().registryOrThrow(Registries.WORLD_PRESET).size() == 0) {
                        throw new IllegalStateException("Needs at least one world preset to continue");
                    } else if (param0x.datapackWorldgen().registryOrThrow(Registries.BIOME).size() == 0) {
                        throw new IllegalStateException("Needs at least one biome continue");
                    } else {
                        WorldCreationContext var0x = this.uiState.getSettings();
                        DynamicOps<JsonElement> var1x = RegistryOps.create(JsonOps.INSTANCE, var0x.worldgenLoadContext());
                        DataResult<JsonElement> var2x = WorldGenSettings.encode(var1x, var0x.options(), var0x.selectedDimensions())
                            .setLifecycle(Lifecycle.stable());
                        DynamicOps<JsonElement> var3x = RegistryOps.create(JsonOps.INSTANCE, param0x.datapackWorldgen());
                        WorldGenSettings var4x = (WorldGenSettings)var2x.flatMap(param1x -> WorldGenSettings.CODEC.parse(var3x, param1x))
                            .getOrThrow(false, Util.prefix("Error parsing worldgen settings after loading data packs: ", LOGGER::error));
                        return new WorldLoader.DataLoadOutput(
                            new CreateWorldScreen.DataPackReloadCookie(var4x, param0x.dataConfiguration()), param0x.datapackDimensions()
                        );
                    }
                },
                (param0x, param1x, param2x, param3) -> {
                    param0x.close();
                    return new WorldCreationContext(param3.worldGenSettings(), param2x, param1x, param3.dataConfiguration());
                },
                Util.backgroundExecutor(),
                this.minecraft
            )
            .thenAcceptAsync(this.uiState::setSettings, this.minecraft)
            .handle(
                (param1x, param2x) -> {
                    if (param2x != null) {
                        LOGGER.warn("Failed to validate datapack", param2x);
                        this.minecraft
                            .setScreen(
                                new ConfirmScreen(
                                    param1xx -> {
                                        if (param1xx) {
                                            param2.accept(this.uiState.getSettings().dataConfiguration());
                                        } else {
                                            param2.accept(WorldDataConfiguration.DEFAULT);
                                        }
                    
                                    },
                                    Component.translatable("dataPack.validation.failed"),
                                    CommonComponents.EMPTY,
                                    Component.translatable("dataPack.validation.back"),
                                    Component.translatable("dataPack.validation.reset")
                                )
                            );
                    } else {
                        this.minecraft.setScreen(this);
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
                    FileUtil.createDirectoriesSafe(var2);
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
    private Pair<Path, PackRepository> getDataPackSelectionSettings(WorldDataConfiguration param0) {
        Path var0 = this.getTempDataPackDir();
        if (var0 != null) {
            if (this.tempDataPackRepository == null) {
                this.tempDataPackRepository = ServerPacksSource.createPackRepository(var0);
                this.tempDataPackRepository.reload();
            }

            this.tempDataPackRepository.setSelected(param0.dataPacks().getEnabled());
            return Pair.of(var0, this.tempDataPackRepository);
        } else {
            return null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static record DataPackReloadCookie(WorldGenSettings worldGenSettings, WorldDataConfiguration dataConfiguration) {
    }

    @OnlyIn(Dist.CLIENT)
    class GameTab extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("createWorld.tab.game.title");
        private static final Component ALLOW_CHEATS = Component.translatable("selectWorld.allowCommands");
        private final EditBox nameEdit;

        GameTab() {
            super(TITLE);
            GridLayout.RowHelper param0 = this.layout.rowSpacing(8).createRowHelper(1);
            LayoutSettings var0 = param0.newCellSettings();
            GridLayout.RowHelper var1 = new GridLayout().rowSpacing(4).createRowHelper(1);
            var1.addChild(new StringWidget(CreateWorldScreen.NAME_LABEL, CreateWorldScreen.this.minecraft.font), var1.newCellSettings().paddingLeft(1));
            this.nameEdit = var1.addChild(
                new EditBox(CreateWorldScreen.this.font, 0, 0, 208, 20, Component.translatable("selectWorld.enterName")), var1.newCellSettings().padding(1)
            );
            this.nameEdit.setValue(CreateWorldScreen.this.uiState.getName());
            this.nameEdit.setResponder(CreateWorldScreen.this.uiState::setName);
            CreateWorldScreen.this.setInitialFocus(this.nameEdit);
            param0.addChild(var1.getGrid(), param0.newCellSettings().alignHorizontallyCenter());
            CycleButton<WorldCreationUiState.SelectedGameMode> var2 = param0.addChild(
                CycleButton.<WorldCreationUiState.SelectedGameMode>builder(param0x -> param0x.displayName)
                    .withValues(
                        WorldCreationUiState.SelectedGameMode.SURVIVAL,
                        WorldCreationUiState.SelectedGameMode.HARDCORE,
                        WorldCreationUiState.SelectedGameMode.CREATIVE
                    )
                    .create(0, 0, 210, 20, CreateWorldScreen.GAME_MODEL_LABEL, (param0x, param1) -> CreateWorldScreen.this.uiState.setGameMode(param1)),
                var0
            );
            CreateWorldScreen.this.uiState.addListener(param1 -> {
                var2.setValue(param1.getGameMode());
                var2.active = !param1.isDebug();
                var2.setTooltip(Tooltip.create(param1.getGameMode().getInfo()));
            });
            CycleButton<Difficulty> var3 = param0.addChild(
                CycleButton.builder(Difficulty::getDisplayName)
                    .withValues(Difficulty.values())
                    .create(
                        0, 0, 210, 20, Component.translatable("options.difficulty"), (param0x, param1) -> CreateWorldScreen.this.uiState.setDifficulty(param1)
                    ),
                var0
            );
            CreateWorldScreen.this.uiState.addListener(param1 -> {
                var3.setValue(CreateWorldScreen.this.uiState.getDifficulty());
                var3.active = !CreateWorldScreen.this.uiState.isHardcore();
                var3.setTooltip(Tooltip.create(CreateWorldScreen.this.uiState.getDifficulty().getInfo()));
            });
            CycleButton<Boolean> var4 = param0.addChild(
                CycleButton.onOffBuilder()
                    .withTooltip(param0x -> Tooltip.create(CreateWorldScreen.ALLOW_CHEATS_INFO))
                    .create(0, 0, 210, 20, ALLOW_CHEATS, (param0x, param1) -> CreateWorldScreen.this.uiState.setAllowCheats(param1))
            );
            CreateWorldScreen.this.uiState.addListener(param1 -> {
                var4.setValue(CreateWorldScreen.this.uiState.isAllowCheats());
                var4.active = !CreateWorldScreen.this.uiState.isDebug() && !CreateWorldScreen.this.uiState.isHardcore();
            });
            param0.addChild(
                Button.builder(
                        CreateWorldScreen.EXPERIMENTS_LABEL,
                        param0x -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())
                    )
                    .width(210)
                    .build()
            );
        }

        @Override
        public void tick() {
            this.nameEdit.tick();
        }
    }

    @OnlyIn(Dist.CLIENT)
    class MoreTab extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("createWorld.tab.more.title");
        private static final Component GAME_RULES_LABEL = Component.translatable("selectWorld.gameRules");
        private static final Component DATA_PACKS_LABEL = Component.translatable("selectWorld.dataPacks");

        MoreTab() {
            super(TITLE);
            GridLayout.RowHelper param0 = this.layout.rowSpacing(8).createRowHelper(1);
            param0.addChild(Button.builder(GAME_RULES_LABEL, param0x -> this.openGameRulesScreen()).width(210).build());
            param0.addChild(
                Button.builder(
                        CreateWorldScreen.EXPERIMENTS_LABEL,
                        param0x -> CreateWorldScreen.this.openExperimentsScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())
                    )
                    .width(210)
                    .build()
            );
            param0.addChild(
                Button.builder(
                        DATA_PACKS_LABEL,
                        param0x -> CreateWorldScreen.this.openDataPackSelectionScreen(CreateWorldScreen.this.uiState.getSettings().dataConfiguration())
                    )
                    .width(210)
                    .build()
            );
        }

        private void openGameRulesScreen() {
            CreateWorldScreen.this.minecraft.setScreen(new EditGameRulesScreen(CreateWorldScreen.this.uiState.getGameRules().copy(), param0 -> {
                CreateWorldScreen.this.minecraft.setScreen(CreateWorldScreen.this);
                param0.ifPresent(CreateWorldScreen.this.uiState::setGameRules);
            }));
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WorldTab extends GridLayoutTab {
        private static final Component TITLE = Component.translatable("createWorld.tab.world.title");
        private static final Component AMPLIFIED_HELP_TEXT = Component.translatable("generator.minecraft.amplified.info");
        private static final Component GENERATE_STRUCTURES = Component.translatable("selectWorld.mapFeatures");
        private static final Component GENERATE_STRUCTURES_INFO = Component.translatable("selectWorld.mapFeatures.info");
        private static final Component BONUS_CHEST = Component.translatable("selectWorld.bonusItems");
        private static final Component SEED_LABEL = Component.translatable("selectWorld.enterSeed");
        static final Component SEED_EMPTY_HINT = Component.translatable("selectWorld.seedInfo").withStyle(ChatFormatting.DARK_GRAY);
        private static final int WORLD_TAB_WIDTH = 310;
        private final EditBox seedEdit;
        private final Button customizeTypeButton;

        WorldTab() {
            super(TITLE);
            GridLayout.RowHelper param0 = this.layout.columnSpacing(10).rowSpacing(8).createRowHelper(2);
            CycleButton<WorldCreationUiState.WorldTypeEntry> var0 = param0.addChild(
                CycleButton.builder(WorldCreationUiState.WorldTypeEntry::describePreset)
                    .withValues(this.createWorldTypeValueSupplier())
                    .withCustomNarration(CreateWorldScreen.WorldTab::createTypeButtonNarration)
                    .create(
                        0, 0, 150, 20, Component.translatable("selectWorld.mapType"), (param0x, param1) -> CreateWorldScreen.this.uiState.setWorldType(param1)
                    )
            );
            var0.setValue(CreateWorldScreen.this.uiState.getWorldType());
            CreateWorldScreen.this.uiState.addListener(param1 -> {
                WorldCreationUiState.WorldTypeEntry var0x = param1.getWorldType();
                var0.setValue(var0x);
                if (var0x.isAmplified()) {
                    var0.setTooltip(Tooltip.create(AMPLIFIED_HELP_TEXT));
                } else {
                    var0.setTooltip(null);
                }

                var0.active = CreateWorldScreen.this.uiState.getWorldType().preset() != null;
            });
            this.customizeTypeButton = param0.addChild(
                Button.builder(Component.translatable("selectWorld.customizeType"), param0x -> this.openPresetEditor()).build()
            );
            CreateWorldScreen.this.uiState.addListener(param0x -> this.customizeTypeButton.active = !param0x.isDebug() && param0x.getPresetEditor() != null);
            GridLayout.RowHelper var1 = new GridLayout().rowSpacing(4).createRowHelper(1);
            var1.addChild(new StringWidget(SEED_LABEL, CreateWorldScreen.this.font).alignLeft());
            this.seedEdit = var1.addChild(new EditBox(CreateWorldScreen.this.font, 0, 0, 308, 20, Component.translatable("selectWorld.enterSeed")) {
                @Override
                protected MutableComponent createNarrationMessage() {
                    return super.createNarrationMessage().append(CommonComponents.NARRATION_SEPARATOR).append(CreateWorldScreen.WorldTab.SEED_EMPTY_HINT);
                }
            }, param0.newCellSettings().padding(1));
            this.seedEdit.setHint(SEED_EMPTY_HINT);
            this.seedEdit.setValue(CreateWorldScreen.this.uiState.getSeed());
            this.seedEdit.setResponder(param0x -> CreateWorldScreen.this.uiState.setSeed(this.seedEdit.getValue()));
            param0.addChild(var1.getGrid(), 2);
            SwitchGrid.Builder var2 = SwitchGrid.builder(310).withPaddingLeft(1);
            var2.addSwitch(GENERATE_STRUCTURES, CreateWorldScreen.this.uiState::isGenerateStructures, CreateWorldScreen.this.uiState::setGenerateStructures)
                .withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isDebug())
                .withInfo(GENERATE_STRUCTURES_INFO);
            var2.addSwitch(BONUS_CHEST, CreateWorldScreen.this.uiState::isBonusChest, CreateWorldScreen.this.uiState::setBonusChest)
                .withIsActiveCondition(() -> !CreateWorldScreen.this.uiState.isHardcore() && !CreateWorldScreen.this.uiState.isDebug());
            SwitchGrid var3 = var2.build(param1 -> param0.addChild(param1, 2));
            CreateWorldScreen.this.uiState.addListener(param1 -> var3.refreshStates());
        }

        private void openPresetEditor() {
            PresetEditor var0 = CreateWorldScreen.this.uiState.getPresetEditor();
            if (var0 != null) {
                CreateWorldScreen.this.minecraft.setScreen(var0.createEditScreen(CreateWorldScreen.this, CreateWorldScreen.this.uiState.getSettings()));
            }

        }

        private CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry> createWorldTypeValueSupplier() {
            return new CycleButton.ValueListSupplier<WorldCreationUiState.WorldTypeEntry>() {
                @Override
                public List<WorldCreationUiState.WorldTypeEntry> getSelectedList() {
                    return CycleButton.DEFAULT_ALT_LIST_SELECTOR.getAsBoolean()
                        ? CreateWorldScreen.this.uiState.getAltPresetList()
                        : CreateWorldScreen.this.uiState.getNormalPresetList();
                }

                @Override
                public List<WorldCreationUiState.WorldTypeEntry> getDefaultList() {
                    return CreateWorldScreen.this.uiState.getNormalPresetList();
                }
            };
        }

        private static MutableComponent createTypeButtonNarration(CycleButton<WorldCreationUiState.WorldTypeEntry> param0x) {
            return ((WorldCreationUiState.WorldTypeEntry)param0x.getValue()).isAmplified()
                ? CommonComponents.joinForNarration(param0x.createDefaultNarrationMessage(), AMPLIFIED_HELP_TEXT)
                : param0x.createDefaultNarrationMessage();
        }

        @Override
        public void tick() {
            this.seedEdit.tick();
        }
    }
}
