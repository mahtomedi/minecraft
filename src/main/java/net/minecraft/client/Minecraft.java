package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Queues;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.minecraft.UserApiService.UserFlag;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.MainTarget;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.TimerQuery;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.math.Matrix4f;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.FileUtil;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.SystemReport;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.InBedChatScreen;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.OutOfMemoryScreen;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.profiling.ClientMetricsSamplersProvider;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.LegacyPackResourcesAdapter;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PackResourcesAdapterV4;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.client.searchtree.ReloadableIdSearchTree;
import net.minecraft.client.searchtree.ReloadableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.KeybindComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FileZipper;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.MemoryReserve;
import net.minecraft.util.ModCheck;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.EmptyProfileResults;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.profiling.metrics.profiling.ActiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.InactiveMetricsRecorder;
import net.minecraft.util.profiling.metrics.profiling.MetricsRecorder;
import net.minecraft.util.profiling.metrics.storage.MetricsPersister;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Minecraft extends ReentrantBlockableEventLoop<Runnable> implements WindowEventHandler {
    private static Minecraft instance;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;
    private static final int MAX_TICKS_PER_UPDATE = 10;
    public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("default");
    public static final ResourceLocation UNIFORM_FONT = new ResourceLocation("uniform");
    public static final ResourceLocation ALT_FONT = new ResourceLocation("alt");
    private static final ResourceLocation REGIONAL_COMPLIANCIES = new ResourceLocation("regional_compliancies.json");
    private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private static final Component SOCIAL_INTERACTIONS_NOT_AVAILABLE = new TranslatableComponent("multiplayer.socialInteractions.not_available");
    public static final String UPDATE_DRIVERS_ADVICE = "Please make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions).";
    private final File resourcePackDirectory;
    private final PropertyMap profileProperties;
    private final TextureManager textureManager;
    private final DataFixer fixerUpper;
    private final VirtualScreen virtualScreen;
    private final Window window;
    private final Timer timer = new Timer(20.0F, 0L);
    private final RenderBuffers renderBuffers;
    public final LevelRenderer levelRenderer;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final ItemInHandRenderer itemInHandRenderer;
    public final ParticleEngine particleEngine;
    private final SearchRegistry searchRegistry = new SearchRegistry();
    private final User user;
    public final Font font;
    public final GameRenderer gameRenderer;
    public final DebugRenderer debugRenderer;
    private final AtomicReference<StoringChunkProgressListener> progressListener = new AtomicReference<>();
    public final Gui gui;
    public final Options options;
    private final HotbarManager hotbarManager;
    public final MouseHandler mouseHandler;
    public final KeyboardHandler keyboardHandler;
    public final File gameDirectory;
    private final String launchedVersion;
    private final String versionType;
    private final Proxy proxy;
    private final LevelStorageSource levelSource;
    public final FrameTimer frameTimer = new FrameTimer();
    private final boolean is64bit;
    private final boolean demo;
    private final boolean allowsMultiplayer;
    private final boolean allowsChat;
    private final ReloadableResourceManager resourceManager;
    private final ClientPackSource clientPackSource;
    private final PackRepository resourcePackRepository;
    private final LanguageManager languageManager;
    private final BlockColors blockColors;
    private final ItemColors itemColors;
    private final RenderTarget mainRenderTarget;
    private final SoundManager soundManager;
    private final MusicManager musicManager;
    private final FontManager fontManager;
    private final SplashManager splashManager;
    private final GpuWarnlistManager gpuWarnlistManager;
    private final PeriodicNotificationManager regionalCompliancies = new PeriodicNotificationManager(REGIONAL_COMPLIANCIES, Minecraft::countryEqualsISO3);
    private final MinecraftSessionService minecraftSessionService;
    private final UserApiService userApiService;
    private final SkinManager skinManager;
    private final ModelManager modelManager;
    private final BlockRenderDispatcher blockRenderer;
    private final PaintingTextureManager paintingTextures;
    private final MobEffectTextureManager mobEffectTextures;
    private final ToastComponent toast;
    private final Game game = new Game(this);
    private final Tutorial tutorial;
    private final PlayerSocialManager playerSocialManager;
    private final EntityModelSet entityModels;
    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final UUID deviceSessionId = UUID.randomUUID();
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Nullable
    public ClientLevel level;
    @Nullable
    public LocalPlayer player;
    @Nullable
    private IntegratedServer singleplayerServer;
    @Nullable
    private ServerData currentServer;
    @Nullable
    private Connection pendingConnection;
    private boolean isLocalServer;
    @Nullable
    public Entity cameraEntity;
    @Nullable
    public Entity crosshairPickEntity;
    @Nullable
    public HitResult hitResult;
    private int rightClickDelay;
    protected int missTime;
    private volatile boolean pause;
    private float pausePartialTick;
    private long lastNanoTime = Util.getNanos();
    private long lastTime;
    private int frames;
    public boolean noRender;
    @Nullable
    public Screen screen;
    @Nullable
    private Overlay overlay;
    private boolean connectedToRealms;
    private Thread gameThread;
    private volatile boolean running;
    @Nullable
    private Supplier<CrashReport> delayedCrash;
    private static int fps;
    public String fpsString = "";
    public boolean wireframe;
    public boolean chunkPath;
    public boolean chunkVisibility;
    public boolean smartCull = true;
    private boolean windowActive;
    private final Queue<Runnable> progressTasks = Queues.newConcurrentLinkedQueue();
    @Nullable
    private CompletableFuture<Void> pendingReload;
    @Nullable
    private TutorialToast socialInteractionsToast;
    private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
    private int fpsPieRenderTicks;
    private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
    @Nullable
    private ProfileResults fpsPieResults;
    private MetricsRecorder metricsRecorder = InactiveMetricsRecorder.INSTANCE;
    private final ResourceLoadStateTracker reloadStateTracker = new ResourceLoadStateTracker();
    private long savedCpuDuration;
    private double gpuUtilization;
    @Nullable
    private TimerQuery.FrameProfile currentFrameProfile;
    private String debugPath = "root";

    public Minecraft(GameConfig param0) {
        super("Client");
        instance = this;
        this.gameDirectory = param0.location.gameDirectory;
        File var0 = param0.location.assetDirectory;
        this.resourcePackDirectory = param0.location.resourcePackDirectory;
        this.launchedVersion = param0.game.launchVersion;
        this.versionType = param0.game.versionType;
        this.profileProperties = param0.user.profileProperties;
        this.clientPackSource = new ClientPackSource(new File(this.gameDirectory, "server-resource-packs"), param0.location.getAssetIndex());
        this.resourcePackRepository = new PackRepository(
            Minecraft::createClientPackAdapter, this.clientPackSource, new FolderRepositorySource(this.resourcePackDirectory, PackSource.DEFAULT)
        );
        this.proxy = param0.user.proxy;
        YggdrasilAuthenticationService var1 = new YggdrasilAuthenticationService(this.proxy);
        this.minecraftSessionService = var1.createMinecraftSessionService();
        this.userApiService = this.createUserApiService(var1, param0);
        this.user = param0.user.user;
        LOGGER.info("Setting user: {}", this.user.getName());
        LOGGER.debug("(Session ID is {})", this.user.getSessionId());
        this.demo = param0.game.demo;
        this.allowsMultiplayer = !param0.game.disableMultiplayer;
        this.allowsChat = !param0.game.disableChat;
        this.is64bit = checkIs64Bit();
        this.singleplayerServer = null;
        String var2;
        int var3;
        if (this.allowsMultiplayer() && param0.server.hostname != null) {
            var2 = param0.server.hostname;
            var3 = param0.server.port;
        } else {
            var2 = null;
            var3 = 0;
        }

        KeybindComponent.setKeyResolver(KeyMapping::createNameSupplier);
        this.fixerUpper = DataFixers.getDataFixer();
        this.toast = new ToastComponent(this);
        this.gameThread = Thread.currentThread();
        this.options = new Options(this, this.gameDirectory);
        this.running = true;
        this.tutorial = new Tutorial(this, this.options);
        this.hotbarManager = new HotbarManager(this.gameDirectory, this.fixerUpper);
        LOGGER.info("Backend library: {}", RenderSystem.getBackendDescription());
        DisplayData var6;
        if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
            var6 = new DisplayData(
                this.options.overrideWidth,
                this.options.overrideHeight,
                param0.display.fullscreenWidth,
                param0.display.fullscreenHeight,
                param0.display.isFullscreen
            );
        } else {
            var6 = param0.display;
        }

        Util.timeSource = RenderSystem.initBackendSystem();
        this.virtualScreen = new VirtualScreen(this);
        this.window = this.virtualScreen.newWindow(var6, this.options.fullscreenVideoModeString, this.createTitle());
        this.setWindowActive(true);

        try {
            if (ON_OSX) {
                InputStream var8 = this.getClientPackSource()
                    .getVanillaPack()
                    .getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/minecraft.icns"));
                MacosUtil.loadIcon(var8);
            } else {
                InputStream var9 = this.getClientPackSource()
                    .getVanillaPack()
                    .getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_16x16.png"));
                InputStream var10 = this.getClientPackSource()
                    .getVanillaPack()
                    .getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_32x32.png"));
                this.window.setIcon(var9, var10);
            }
        } catch (IOException var9) {
            LOGGER.error("Couldn't set icon", (Throwable)var9);
        }

        this.window.setFramerateLimit(this.options.framerateLimit);
        this.mouseHandler = new MouseHandler(this);
        this.mouseHandler.setup(this.window.getWindow());
        this.keyboardHandler = new KeyboardHandler(this);
        this.keyboardHandler.setup(this.window.getWindow());
        RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
        this.mainRenderTarget = new MainTarget(this.window.getWidth(), this.window.getHeight());
        this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.mainRenderTarget.clear(ON_OSX);
        this.resourceManager = new ReloadableResourceManager(PackType.CLIENT_RESOURCES);
        this.resourcePackRepository.reload();
        this.options.loadSelectedResourcePacks(this.resourcePackRepository);
        this.languageManager = new LanguageManager(this.options.languageCode);
        this.resourceManager.registerReloadListener(this.languageManager);
        this.textureManager = new TextureManager(this.resourceManager);
        this.resourceManager.registerReloadListener(this.textureManager);
        this.skinManager = new SkinManager(this.textureManager, new File(var0, "skins"), this.minecraftSessionService);
        this.levelSource = new LevelStorageSource(this.gameDirectory.toPath().resolve("saves"), this.gameDirectory.toPath().resolve("backups"), this.fixerUpper);
        this.soundManager = new SoundManager(this.resourceManager, this.options);
        this.resourceManager.registerReloadListener(this.soundManager);
        this.splashManager = new SplashManager(this.user);
        this.resourceManager.registerReloadListener(this.splashManager);
        this.musicManager = new MusicManager(this);
        this.fontManager = new FontManager(this.textureManager);
        this.font = this.fontManager.createFont();
        this.resourceManager.registerReloadListener(this.fontManager.getReloadListener());
        this.selectMainFont(this.isEnforceUnicode());
        this.resourceManager.registerReloadListener(new GrassColorReloadListener());
        this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
        this.window.setErrorSection("Startup");
        RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
        this.window.setErrorSection("Post startup");
        this.blockColors = BlockColors.createDefault();
        this.itemColors = ItemColors.createDefault(this.blockColors);
        this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels);
        this.resourceManager.registerReloadListener(this.modelManager);
        this.entityModels = new EntityModelSet();
        this.resourceManager.registerReloadListener(this.entityModels);
        this.blockEntityRenderDispatcher = new BlockEntityRenderDispatcher(this.font, this.entityModels, this::getBlockRenderer);
        this.resourceManager.registerReloadListener(this.blockEntityRenderDispatcher);
        BlockEntityWithoutLevelRenderer var12 = new BlockEntityWithoutLevelRenderer(this.blockEntityRenderDispatcher, this.entityModels);
        this.resourceManager.registerReloadListener(var12);
        this.itemRenderer = new ItemRenderer(this.textureManager, this.modelManager, this.itemColors, var12);
        this.entityRenderDispatcher = new EntityRenderDispatcher(this.textureManager, this.itemRenderer, this.font, this.options, this.entityModels);
        this.resourceManager.registerReloadListener(this.entityRenderDispatcher);
        this.itemInHandRenderer = new ItemInHandRenderer(this);
        this.resourceManager.registerReloadListener(this.itemRenderer);
        this.renderBuffers = new RenderBuffers();
        this.gameRenderer = new GameRenderer(this, this.resourceManager, this.renderBuffers);
        this.resourceManager.registerReloadListener(this.gameRenderer);
        this.playerSocialManager = new PlayerSocialManager(this, this.userApiService);
        this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), var12, this.blockColors);
        this.resourceManager.registerReloadListener(this.blockRenderer);
        this.levelRenderer = new LevelRenderer(this, this.renderBuffers);
        this.resourceManager.registerReloadListener(this.levelRenderer);
        this.createSearchTrees();
        this.resourceManager.registerReloadListener(this.searchRegistry);
        this.particleEngine = new ParticleEngine(this.level, this.textureManager);
        this.resourceManager.registerReloadListener(this.particleEngine);
        this.paintingTextures = new PaintingTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.paintingTextures);
        this.mobEffectTextures = new MobEffectTextureManager(this.textureManager);
        this.resourceManager.registerReloadListener(this.mobEffectTextures);
        this.gpuWarnlistManager = new GpuWarnlistManager();
        this.resourceManager.registerReloadListener(this.gpuWarnlistManager);
        this.resourceManager.registerReloadListener(this.regionalCompliancies);
        this.gui = new Gui(this);
        this.debugRenderer = new DebugRenderer(this);
        RenderSystem.setErrorCallback(this::onFullscreenError);
        if (this.mainRenderTarget.width != this.window.getWidth() || this.mainRenderTarget.height != this.window.getHeight()) {
            StringBuilder var13 = new StringBuilder(
                "Recovering from unsupported resolution ("
                    + this.window.getWidth()
                    + "x"
                    + this.window.getHeight()
                    + ").\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions)."
            );
            if (GlDebug.isDebugEnabled()) {
                var13.append("\n\nReported GL debug messages:\n").append(String.join("\n", GlDebug.getLastOpenGlDebugMessages()));
            }

            this.window.setWindowed(this.mainRenderTarget.width, this.mainRenderTarget.height);
            TinyFileDialogs.tinyfd_messageBox("Minecraft", var13.toString(), "ok", "error", false);
        } else if (this.options.fullscreen && !this.window.isFullscreen()) {
            this.window.toggleFullScreen();
            this.options.fullscreen = this.window.isFullscreen();
        }

        this.window.updateVsync(this.options.enableVsync);
        this.window.updateRawMouseInput(this.options.rawMouseInput().get());
        this.window.setDefaultErrorCallback();
        this.resizeDisplay();
        this.gameRenderer.preloadUiShader(this.getClientPackSource().getVanillaPack());
        LoadingOverlay.registerTextures(this);
        List<PackResources> var14 = this.resourcePackRepository.openAllSelected();
        this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.INITIAL, var14);
        this.setOverlay(
            new LoadingOverlay(
                this,
                this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, var14),
                param0x -> Util.ifElse(param0x, this::rollbackResourcePacks, () -> {
                        if (SharedConstants.IS_RUNNING_IN_IDE) {
                            this.selfTest();
                        }
        
                        this.reloadStateTracker.finishReload();
                    }),
                false
            )
        );
        if (var2 != null) {
            ConnectScreen.startConnecting(new TitleScreen(), this, new ServerAddress(var2, var3), null);
        } else {
            this.setScreen(new TitleScreen(true));
        }

    }

    private static boolean countryEqualsISO3(Object param0x) {
        try {
            return Locale.getDefault().getISO3Country().equals(param0x);
        } catch (MissingResourceException var2) {
            return false;
        }
    }

    public void updateTitle() {
        this.window.setTitle(this.createTitle());
    }

    private String createTitle() {
        StringBuilder var0 = new StringBuilder("Minecraft");
        if (checkModStatus().shouldReportAsModified()) {
            var0.append("*");
        }

        var0.append(" ");
        var0.append(SharedConstants.getCurrentVersion().getName());
        ClientPacketListener var1 = this.getConnection();
        if (var1 != null && var1.getConnection().isConnected()) {
            var0.append(" - ");
            if (this.singleplayerServer != null && !this.singleplayerServer.isPublished()) {
                var0.append(I18n.get("title.singleplayer"));
            } else if (this.isConnectedToRealms()) {
                var0.append(I18n.get("title.multiplayer.realms"));
            } else if (this.singleplayerServer == null && (this.currentServer == null || !this.currentServer.isLan())) {
                var0.append(I18n.get("title.multiplayer.other"));
            } else {
                var0.append(I18n.get("title.multiplayer.lan"));
            }
        }

        return var0.toString();
    }

    private UserApiService createUserApiService(YggdrasilAuthenticationService param0, GameConfig param1) {
        try {
            return param0.createUserApiService(param1.user.user.getAccessToken());
        } catch (AuthenticationException var4) {
            LOGGER.error("Failed to verify authentication", (Throwable)var4);
            return UserApiService.OFFLINE;
        }
    }

    public static ModCheck checkModStatus() {
        return ModCheck.identify("vanilla", ClientBrandRetriever::getClientModName, "Client", Minecraft.class);
    }

    private void rollbackResourcePacks(Throwable param0) {
        if (this.resourcePackRepository.getSelectedIds().size() > 1) {
            this.clearResourcePacksOnError(param0, null);
        } else {
            Util.throwAsRuntime(param0);
        }

    }

    public void clearResourcePacksOnError(Throwable param0, @Nullable Component param1) {
        LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", param0);
        this.reloadStateTracker.startRecovery(param0);
        this.resourcePackRepository.setSelected(Collections.emptyList());
        this.options.resourcePacks.clear();
        this.options.incompatibleResourcePacks.clear();
        this.options.save();
        this.reloadResourcePacks(true).thenRun(() -> {
            ToastComponent var0 = this.getToasts();
            SystemToast.addOrUpdate(var0, SystemToast.SystemToastIds.PACK_LOAD_FAILURE, new TranslatableComponent("resourcePack.load_fail"), param1);
        });
    }

    public void run() {
        this.gameThread = Thread.currentThread();
        if (Runtime.getRuntime().availableProcessors() > 4) {
            this.gameThread.setPriority(10);
        }

        try {
            boolean var0 = false;

            while(this.running) {
                if (this.delayedCrash != null) {
                    crash(this.delayedCrash.get());
                    return;
                }

                try {
                    SingleTickProfiler var1 = SingleTickProfiler.createTickProfiler("Renderer");
                    boolean var2 = this.shouldRenderFpsPie();
                    this.profiler = this.constructProfiler(var2, var1);
                    this.profiler.startTick();
                    this.metricsRecorder.startTick();
                    this.runTick(!var0);
                    this.metricsRecorder.endTick();
                    this.profiler.endTick();
                    this.finishProfilers(var2, var1);
                } catch (OutOfMemoryError var4) {
                    if (var0) {
                        throw var4;
                    }

                    this.emergencySave();
                    this.setScreen(new OutOfMemoryScreen());
                    System.gc();
                    LOGGER.error(LogUtils.FATAL_MARKER, "Out of memory", (Throwable)var4);
                    var0 = true;
                }
            }
        } catch (ReportedException var51) {
            this.fillReport(var51.getReport());
            this.emergencySave();
            LOGGER.error(LogUtils.FATAL_MARKER, "Reported exception thrown!", (Throwable)var51);
            crash(var51.getReport());
        } catch (Throwable var61) {
            CrashReport var6 = this.fillReport(new CrashReport("Unexpected error", var61));
            LOGGER.error(LogUtils.FATAL_MARKER, "Unreported exception thrown!", var61);
            this.emergencySave();
            crash(var6);
        }

    }

    void selectMainFont(boolean param0) {
        this.fontManager.setRenames(param0 ? ImmutableMap.of(DEFAULT_FONT, UNIFORM_FONT) : ImmutableMap.of());
    }

    private void createSearchTrees() {
        ReloadableSearchTree<ItemStack> var0 = new ReloadableSearchTree<>(
            param0 -> param0.getTooltipLines(null, TooltipFlag.Default.NORMAL)
                    .stream()
                    .map(param0x -> ChatFormatting.stripFormatting(param0x.getString()).trim())
                    .filter(param0x -> !param0x.isEmpty()),
            param0 -> Stream.of(Registry.ITEM.getKey(param0.getItem()))
        );
        ReloadableIdSearchTree<ItemStack> var1 = new ReloadableIdSearchTree<>(param0 -> param0.getTags().map(TagKey::location));
        NonNullList<ItemStack> var2 = NonNullList.create();

        for(Item var3 : Registry.ITEM) {
            var3.fillItemCategory(CreativeModeTab.TAB_SEARCH, var2);
        }

        var2.forEach(param2 -> {
            var0.add(param2);
            var1.add(param2);
        });
        ReloadableSearchTree<RecipeCollection> var4 = new ReloadableSearchTree<>(
            param0 -> param0.getRecipes()
                    .stream()
                    .flatMap(param0x -> param0x.getResultItem().getTooltipLines(null, TooltipFlag.Default.NORMAL).stream())
                    .map(param0x -> ChatFormatting.stripFormatting(param0x.getString()).trim())
                    .filter(param0x -> !param0x.isEmpty()),
            param0 -> param0.getRecipes().stream().map(param0x -> Registry.ITEM.getKey(param0x.getResultItem().getItem()))
        );
        this.searchRegistry.register(SearchRegistry.CREATIVE_NAMES, var0);
        this.searchRegistry.register(SearchRegistry.CREATIVE_TAGS, var1);
        this.searchRegistry.register(SearchRegistry.RECIPE_COLLECTIONS, var4);
    }

    private void onFullscreenError(int param0x, long param1) {
        this.options.enableVsync = false;
        this.options.save();
    }

    private static boolean checkIs64Bit() {
        String[] var0 = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

        for(String var1 : var0) {
            String var2 = System.getProperty(var1);
            if (var2 != null && var2.contains("64")) {
                return true;
            }
        }

        return false;
    }

    public RenderTarget getMainRenderTarget() {
        return this.mainRenderTarget;
    }

    public String getLaunchedVersion() {
        return this.launchedVersion;
    }

    public String getVersionType() {
        return this.versionType;
    }

    public void delayCrash(Supplier<CrashReport> param0) {
        this.delayedCrash = param0;
    }

    public static void crash(CrashReport param0) {
        File var0 = new File(getInstance().gameDirectory, "crash-reports");
        File var1 = new File(var0, "crash-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-client.txt");
        Bootstrap.realStdoutPrintln(param0.getFriendlyReport());
        if (param0.getSaveFile() != null) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + param0.getSaveFile());
            System.exit(-1);
        } else if (param0.saveToFile(var1)) {
            Bootstrap.realStdoutPrintln("#@!@# Game crashed! Crash report saved to: #@!@# " + var1.getAbsolutePath());
            System.exit(-1);
        } else {
            Bootstrap.realStdoutPrintln("#@?@# Game crashed! Crash report could not be saved. #@?@#");
            System.exit(-2);
        }

    }

    public boolean isEnforceUnicode() {
        return this.options.forceUnicodeFont;
    }

    public CompletableFuture<Void> reloadResourcePacks() {
        return this.reloadResourcePacks(false);
    }

    private CompletableFuture<Void> reloadResourcePacks(boolean param0) {
        if (this.pendingReload != null) {
            return this.pendingReload;
        } else {
            CompletableFuture<Void> var0 = new CompletableFuture<>();
            if (!param0 && this.overlay instanceof LoadingOverlay) {
                this.pendingReload = var0;
                return var0;
            } else {
                this.resourcePackRepository.reload();
                List<PackResources> var1 = this.resourcePackRepository.openAllSelected();
                if (!param0) {
                    this.reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.MANUAL, var1);
                }

                this.setOverlay(
                    new LoadingOverlay(
                        this,
                        this.resourceManager.createReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, var1),
                        param1 -> Util.ifElse(param1, this::rollbackResourcePacks, () -> {
                                this.levelRenderer.allChanged();
                                this.reloadStateTracker.finishReload();
                                var0.complete(null);
                            }),
                        true
                    )
                );
                return var0;
            }
        }
    }

    private void selfTest() {
        boolean var0 = false;
        BlockModelShaper var1 = this.getBlockRenderer().getBlockModelShaper();
        BakedModel var2 = var1.getModelManager().getMissingModel();

        for(Block var3 : Registry.BLOCK) {
            for(BlockState var4 : var3.getStateDefinition().getPossibleStates()) {
                if (var4.getRenderShape() == RenderShape.MODEL) {
                    BakedModel var5 = var1.getBlockModel(var4);
                    if (var5 == var2) {
                        LOGGER.debug("Missing model for: {}", var4);
                        var0 = true;
                    }
                }
            }
        }

        TextureAtlasSprite var6 = var2.getParticleIcon();

        for(Block var7 : Registry.BLOCK) {
            for(BlockState var8 : var7.getStateDefinition().getPossibleStates()) {
                TextureAtlasSprite var9 = var1.getParticleIcon(var8);
                if (!var8.isAir() && var9 == var6) {
                    LOGGER.debug("Missing particle icon for: {}", var8);
                    var0 = true;
                }
            }
        }

        NonNullList<ItemStack> var10 = NonNullList.create();

        for(Item var11 : Registry.ITEM) {
            var10.clear();
            var11.fillItemCategory(CreativeModeTab.TAB_SEARCH, var10);

            for(ItemStack var12 : var10) {
                String var13 = var12.getDescriptionId();
                String var14 = new TranslatableComponent(var13).getString();
                if (var14.toLowerCase(Locale.ROOT).equals(var11.getDescriptionId())) {
                    LOGGER.debug("Missing translation for: {} {} {}", var12, var13, var12.getItem());
                }
            }
        }

        var0 |= MenuScreens.selfTest();
        var0 |= EntityRenderers.validateRegistrations();
        if (var0) {
            throw new IllegalStateException("Your game data is foobar, fix the errors above!");
        }
    }

    public LevelStorageSource getLevelSource() {
        return this.levelSource;
    }

    private void openChatScreen(String param0) {
        Minecraft.ChatStatus var0 = this.getChatStatus();
        if (!var0.isChatAllowed(this.isLocalServer())) {
            this.gui.setOverlayMessage(var0.getMessage(), false);
        } else {
            this.setScreen(new ChatScreen(param0));
        }

    }

    public void setScreen(@Nullable Screen param0) {
        if (SharedConstants.IS_RUNNING_IN_IDE && Thread.currentThread() != this.gameThread) {
            LOGGER.error("setScreen called from non-game thread");
        }

        if (this.screen != null) {
            this.screen.removed();
        }

        if (param0 == null && this.level == null) {
            param0 = new TitleScreen();
        } else if (param0 == null && this.player.isDeadOrDying()) {
            if (this.player.shouldShowDeathScreen()) {
                param0 = new DeathScreen(null, this.level.getLevelData().isHardcore());
            } else {
                this.player.respawn();
            }
        }

        this.screen = param0;
        BufferUploader.reset();
        if (param0 != null) {
            this.mouseHandler.releaseMouse();
            KeyMapping.releaseAll();
            param0.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
            this.noRender = false;
        } else {
            this.soundManager.resume();
            this.mouseHandler.grabMouse();
        }

        this.updateTitle();
    }

    public void setOverlay(@Nullable Overlay param0) {
        this.overlay = param0;
    }

    public void destroy() {
        try {
            LOGGER.info("Stopping!");

            try {
                NarratorChatListener.INSTANCE.destroy();
            } catch (Throwable var7) {
            }

            try {
                if (this.level != null) {
                    this.level.disconnect();
                }

                this.clearLevel();
            } catch (Throwable var6) {
            }

            if (this.screen != null) {
                this.screen.removed();
            }

            this.close();
        } finally {
            Util.timeSource = System::nanoTime;
            if (this.delayedCrash == null) {
                System.exit(0);
            }

        }

    }

    @Override
    public void close() {
        if (this.currentFrameProfile != null) {
            this.currentFrameProfile.cancel();
        }

        try {
            this.regionalCompliancies.close();
            this.modelManager.close();
            this.fontManager.close();
            this.gameRenderer.close();
            this.levelRenderer.close();
            this.soundManager.destroy();
            this.particleEngine.close();
            this.mobEffectTextures.close();
            this.paintingTextures.close();
            this.textureManager.close();
            this.resourceManager.close();
            Util.shutdownExecutors();
        } catch (Throwable var5) {
            LOGGER.error("Shutdown failure!", var5);
            throw var5;
        } finally {
            this.virtualScreen.close();
            this.window.close();
        }

    }

    private void runTick(boolean param0) {
        this.window.setErrorSection("Pre render");
        long var0 = Util.getNanos();
        if (this.window.shouldClose()) {
            this.stop();
        }

        if (this.pendingReload != null && !(this.overlay instanceof LoadingOverlay)) {
            CompletableFuture<Void> var1 = this.pendingReload;
            this.pendingReload = null;
            this.reloadResourcePacks().thenRun(() -> var1.complete(null));
        }

        Runnable var2;
        while((var2 = this.progressTasks.poll()) != null) {
            var2.run();
        }

        if (param0) {
            int var3 = this.timer.advanceTime(Util.getMillis());
            this.profiler.push("scheduledExecutables");
            this.runAllTasks();
            this.profiler.pop();
            this.profiler.push("tick");

            for(int var4 = 0; var4 < Math.min(10, var3); ++var4) {
                this.profiler.incrementCounter("clientTick");
                this.tick();
            }

            this.profiler.pop();
        }

        this.mouseHandler.turnPlayer();
        this.window.setErrorSection("Render");
        this.profiler.push("sound");
        this.soundManager.updateSource(this.gameRenderer.getMainCamera());
        this.profiler.pop();
        this.profiler.push("render");
        boolean var6;
        if (!this.options.renderDebug && !this.metricsRecorder.isRecording()) {
            var6 = false;
            this.gpuUtilization = 0.0;
        } else {
            var6 = this.currentFrameProfile == null || this.currentFrameProfile.isDone();
            if (var6) {
                TimerQuery.getInstance().ifPresent(TimerQuery::beginProfile);
            }
        }

        PoseStack var7 = RenderSystem.getModelViewStack();
        var7.pushPose();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.clear(16640, ON_OSX);
        this.mainRenderTarget.bindWrite(true);
        FogRenderer.setupNoFog();
        this.profiler.push("display");
        RenderSystem.enableTexture();
        RenderSystem.enableCull();
        this.profiler.pop();
        if (!this.noRender) {
            this.profiler.popPush("gameRenderer");
            this.gameRenderer.render(this.pause ? this.pausePartialTick : this.timer.partialTick, var0, param0);
            this.profiler.popPush("toasts");
            this.toast.render(new PoseStack());
            this.profiler.pop();
        }

        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            this.renderFpsMeter(new PoseStack(), this.fpsPieResults);
            this.profiler.pop();
        }

        this.profiler.push("blit");
        this.mainRenderTarget.unbindWrite();
        var7.popPose();
        var7.pushPose();
        RenderSystem.applyModelViewMatrix();
        this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
        if (var6) {
            TimerQuery.getInstance().ifPresent(param0x -> this.currentFrameProfile = param0x.endProfile());
        }

        var7.popPose();
        RenderSystem.applyModelViewMatrix();
        this.profiler.popPush("updateDisplay");
        this.window.updateDisplay();
        int var8 = this.getFramerateLimit();
        if ((double)var8 < Option.FRAMERATE_LIMIT.getMaxValue()) {
            RenderSystem.limitDisplayFPS(var8);
        }

        this.profiler.popPush("yield");
        Thread.yield();
        this.profiler.pop();
        this.window.setErrorSection("Post render");
        ++this.frames;
        boolean var9 = this.hasSingleplayerServer()
            && (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen())
            && !this.singleplayerServer.isPublished();
        if (this.pause != var9) {
            if (this.pause) {
                this.pausePartialTick = this.timer.partialTick;
            } else {
                this.timer.partialTick = this.pausePartialTick;
            }

            this.pause = var9;
        }

        long var10 = Util.getNanos();
        long var11 = var10 - this.lastNanoTime;
        if (var6) {
            this.savedCpuDuration = var11;
        }

        this.frameTimer.logFrameDuration(var11);
        this.lastNanoTime = var10;
        this.profiler.push("fpsUpdate");
        if (this.currentFrameProfile != null && this.currentFrameProfile.isDone()) {
            this.gpuUtilization = (double)this.currentFrameProfile.get() * 100.0 / (double)this.savedCpuDuration;
        }

        while(Util.getMillis() >= this.lastTime + 1000L) {
            String var12;
            if (this.gpuUtilization > 0.0) {
                var12 = " GPU: " + (this.gpuUtilization > 100.0 ? ChatFormatting.RED + "100%" : Math.round(this.gpuUtilization) + "%");
            } else {
                var12 = "";
            }

            fps = this.frames;
            this.fpsString = String.format(
                "%d fps T: %s%s%s%s B: %d%s",
                fps,
                (double)this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf" : this.options.framerateLimit,
                this.options.enableVsync ? " vsync" : "",
                this.options.graphicsMode,
                this.options.renderClouds == CloudStatus.OFF ? "" : (this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
                this.options.biomeBlendRadius().get(),
                var12
            );
            this.lastTime += 1000L;
            this.frames = 0;
        }

        this.profiler.pop();
    }

    private boolean shouldRenderFpsPie() {
        return this.options.renderDebug && this.options.renderDebugCharts && !this.options.hideGui;
    }

    private ProfilerFiller constructProfiler(boolean param0, @Nullable SingleTickProfiler param1) {
        if (!param0) {
            this.fpsPieProfiler.disable();
            if (!this.metricsRecorder.isRecording() && param1 == null) {
                return InactiveProfiler.INSTANCE;
            }
        }

        ProfilerFiller var0;
        if (param0) {
            if (!this.fpsPieProfiler.isEnabled()) {
                this.fpsPieRenderTicks = 0;
                this.fpsPieProfiler.enable();
            }

            ++this.fpsPieRenderTicks;
            var0 = this.fpsPieProfiler.getFiller();
        } else {
            var0 = InactiveProfiler.INSTANCE;
        }

        if (this.metricsRecorder.isRecording()) {
            var0 = ProfilerFiller.tee(var0, this.metricsRecorder.getProfiler());
        }

        return SingleTickProfiler.decorateFiller(var0, param1);
    }

    private void finishProfilers(boolean param0, @Nullable SingleTickProfiler param1) {
        if (param1 != null) {
            param1.endTick();
        }

        if (param0) {
            this.fpsPieResults = this.fpsPieProfiler.getResults();
        } else {
            this.fpsPieResults = null;
        }

        this.profiler = this.fpsPieProfiler.getFiller();
    }

    @Override
    public void resizeDisplay() {
        int var0 = this.window.calculateScale(this.options.guiScale, this.isEnforceUnicode());
        this.window.setGuiScale((double)var0);
        if (this.screen != null) {
            this.screen.resize(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
        }

        RenderTarget var1 = this.getMainRenderTarget();
        var1.resize(this.window.getWidth(), this.window.getHeight(), ON_OSX);
        this.gameRenderer.resize(this.window.getWidth(), this.window.getHeight());
        this.mouseHandler.setIgnoreFirstMove();
    }

    @Override
    public void cursorEntered() {
        this.mouseHandler.cursorEntered();
    }

    private int getFramerateLimit() {
        return this.level != null || this.screen == null && this.overlay == null ? this.window.getFramerateLimit() : 60;
    }

    public void emergencySave() {
        try {
            MemoryReserve.release();
            this.levelRenderer.clear();
        } catch (Throwable var3) {
        }

        try {
            System.gc();
            if (this.isLocalServer && this.singleplayerServer != null) {
                this.singleplayerServer.halt(true);
            }

            this.clearLevel(new GenericDirtMessageScreen(new TranslatableComponent("menu.savingLevel")));
        } catch (Throwable var2) {
        }

        System.gc();
    }

    public boolean debugClientMetricsStart(Consumer<TranslatableComponent> param0) {
        if (this.metricsRecorder.isRecording()) {
            this.debugClientMetricsStop();
            return false;
        } else {
            Consumer<ProfileResults> var0 = param1 -> {
                if (param1 != EmptyProfileResults.EMPTY) {
                    int var0x = param1.getTickDuration();
                    double var1x = (double)param1.getNanoDuration() / (double)TimeUtil.NANOSECONDS_PER_SECOND;
                    this.execute(
                        () -> param0.accept(
                                new TranslatableComponent(
                                    "commands.debug.stopped",
                                    String.format(Locale.ROOT, "%.2f", var1x),
                                    var0x,
                                    String.format(Locale.ROOT, "%.2f", (double)var0x / var1x)
                                )
                            )
                    );
                }
            };
            Consumer<Path> var1 = param1 -> {
                Component var0x = new TextComponent(param1.toString())
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(param1x -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, param1.toFile().getParent())));
                this.execute(() -> param0.accept(new TranslatableComponent("debug.profiling.stop", var0x)));
            };
            SystemReport var2 = fillSystemReport(new SystemReport(), this, this.languageManager, this.launchedVersion, this.options);
            Consumer<List<Path>> var3 = param2 -> {
                Path var0x = this.archiveProfilingReport(var2, param2);
                var1.accept(var0x);
            };
            Consumer<Path> var4;
            if (this.singleplayerServer == null) {
                var4 = param1 -> var3.accept(ImmutableList.of(param1));
            } else {
                this.singleplayerServer.fillSystemReport(var2);
                CompletableFuture<Path> var5 = new CompletableFuture<>();
                CompletableFuture<Path> var6 = new CompletableFuture<>();
                CompletableFuture.allOf(var5, var6).thenRunAsync(() -> var3.accept(ImmutableList.of(var5.join(), var6.join())), Util.ioPool());
                this.singleplayerServer.startRecordingMetrics(param0x -> {
                }, var6::complete);
                var4 = var5::complete;
            }

            this.metricsRecorder = ActiveMetricsRecorder.createStarted(
                new ClientMetricsSamplersProvider(Util.timeSource, this.levelRenderer),
                Util.timeSource,
                Util.ioPool(),
                new MetricsPersister("client"),
                param1 -> {
                    this.metricsRecorder = InactiveMetricsRecorder.INSTANCE;
                    var0.accept(param1);
                },
                var4
            );
            return true;
        }
    }

    private void debugClientMetricsStop() {
        this.metricsRecorder.end();
        if (this.singleplayerServer != null) {
            this.singleplayerServer.finishRecordingMetrics();
        }

    }

    private void debugClientMetricsCancel() {
        this.metricsRecorder.cancel();
        if (this.singleplayerServer != null) {
            this.singleplayerServer.cancelRecordingMetrics();
        }

    }

    private Path archiveProfilingReport(SystemReport param0, List<Path> param1) {
        String var0;
        if (this.isLocalServer()) {
            var0 = this.getSingleplayerServer().getWorldData().getLevelName();
        } else {
            var0 = this.getCurrentServer().name;
        }

        Path var4;
        try {
            String var2 = String.format(
                "%s-%s-%s", new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()), var0, SharedConstants.getCurrentVersion().getId()
            );
            String var3 = FileUtil.findAvailableName(MetricsPersister.PROFILING_RESULTS_DIR, var2, ".zip");
            var4 = MetricsPersister.PROFILING_RESULTS_DIR.resolve(var3);
        } catch (IOException var21) {
            throw new UncheckedIOException(var21);
        }

        try (FileZipper var7 = new FileZipper(var4)) {
            var7.add(Paths.get("system.txt"), param0.toLineSeparatedString());
            var7.add(Paths.get("client").resolve(this.options.getFile().getName()), this.options.dumpOptionsForReport());
            param1.forEach(var7::add);
        } finally {
            for(Path var10 : param1) {
                try {
                    FileUtils.forceDelete(var10.toFile());
                } catch (IOException var18) {
                    LOGGER.warn("Failed to delete temporary profiling result {}", var10, var18);
                }
            }

        }

        return var4;
    }

    public void debugFpsMeterKeyPress(int param0) {
        if (this.fpsPieResults != null) {
            List<ResultField> var0 = this.fpsPieResults.getTimes(this.debugPath);
            if (!var0.isEmpty()) {
                ResultField var1 = var0.remove(0);
                if (param0 == 0) {
                    if (!var1.name.isEmpty()) {
                        int var2 = this.debugPath.lastIndexOf(30);
                        if (var2 >= 0) {
                            this.debugPath = this.debugPath.substring(0, var2);
                        }
                    }
                } else {
                    --param0;
                    if (param0 < var0.size() && !"unspecified".equals(var0.get(param0).name)) {
                        if (!this.debugPath.isEmpty()) {
                            this.debugPath = this.debugPath + "\u001e";
                        }

                        this.debugPath = this.debugPath + var0.get(param0).name;
                    }
                }

            }
        }
    }

    private void renderFpsMeter(PoseStack param0, ProfileResults param1) {
        List<ResultField> var0 = param1.getTimes(this.debugPath);
        ResultField var1 = var0.remove(0);
        RenderSystem.clear(256, ON_OSX);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f var2 = Matrix4f.orthographic(0.0F, (float)this.window.getWidth(), 0.0F, (float)this.window.getHeight(), 1000.0F, 3000.0F);
        RenderSystem.setProjectionMatrix(var2);
        PoseStack var3 = RenderSystem.getModelViewStack();
        var3.setIdentity();
        var3.translate(0.0, 0.0, -2000.0);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.lineWidth(1.0F);
        RenderSystem.disableTexture();
        Tesselator var4 = Tesselator.getInstance();
        BufferBuilder var5 = var4.getBuilder();
        int var6 = 160;
        int var7 = this.window.getWidth() - 160 - 10;
        int var8 = this.window.getHeight() - 320;
        RenderSystem.enableBlend();
        var5.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        var5.vertex((double)((float)var7 - 176.0F), (double)((float)var8 - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
        var5.vertex((double)((float)var7 - 176.0F), (double)(var8 + 320), 0.0).color(200, 0, 0, 0).endVertex();
        var5.vertex((double)((float)var7 + 176.0F), (double)(var8 + 320), 0.0).color(200, 0, 0, 0).endVertex();
        var5.vertex((double)((float)var7 + 176.0F), (double)((float)var8 - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
        var4.end();
        RenderSystem.disableBlend();
        double var9 = 0.0;

        for(ResultField var10 : var0) {
            int var11 = Mth.floor(var10.percentage / 4.0) + 1;
            var5.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            int var12 = var10.getColor();
            int var13 = var12 >> 16 & 0xFF;
            int var14 = var12 >> 8 & 0xFF;
            int var15 = var12 & 0xFF;
            var5.vertex((double)var7, (double)var8, 0.0).color(var13, var14, var15, 255).endVertex();

            for(int var16 = var11; var16 >= 0; --var16) {
                float var17 = (float)((var9 + var10.percentage * (double)var16 / (double)var11) * (float) (Math.PI * 2) / 100.0);
                float var18 = Mth.sin(var17) * 160.0F;
                float var19 = Mth.cos(var17) * 160.0F * 0.5F;
                var5.vertex((double)((float)var7 + var18), (double)((float)var8 - var19), 0.0).color(var13, var14, var15, 255).endVertex();
            }

            var4.end();
            var5.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

            for(int var20 = var11; var20 >= 0; --var20) {
                float var21 = (float)((var9 + var10.percentage * (double)var20 / (double)var11) * (float) (Math.PI * 2) / 100.0);
                float var22 = Mth.sin(var21) * 160.0F;
                float var23 = Mth.cos(var21) * 160.0F * 0.5F;
                if (!(var23 > 0.0F)) {
                    var5.vertex((double)((float)var7 + var22), (double)((float)var8 - var23), 0.0).color(var13 >> 1, var14 >> 1, var15 >> 1, 255).endVertex();
                    var5.vertex((double)((float)var7 + var22), (double)((float)var8 - var23 + 10.0F), 0.0)
                        .color(var13 >> 1, var14 >> 1, var15 >> 1, 255)
                        .endVertex();
                }
            }

            var4.end();
            var9 += var10.percentage;
        }

        DecimalFormat var24 = new DecimalFormat("##0.00");
        var24.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        RenderSystem.enableTexture();
        String var25 = ProfileResults.demanglePath(var1.name);
        String var26 = "";
        if (!"unspecified".equals(var25)) {
            var26 = var26 + "[0] ";
        }

        if (var25.isEmpty()) {
            var26 = var26 + "ROOT ";
        } else {
            var26 = var26 + var25 + " ";
        }

        int var27 = 16777215;
        this.font.drawShadow(param0, var26, (float)(var7 - 160), (float)(var8 - 80 - 16), 16777215);
        var26 = var24.format(var1.globalPercentage) + "%";
        this.font.drawShadow(param0, var26, (float)(var7 + 160 - this.font.width(var26)), (float)(var8 - 80 - 16), 16777215);

        for(int var28 = 0; var28 < var0.size(); ++var28) {
            ResultField var29 = var0.get(var28);
            StringBuilder var30 = new StringBuilder();
            if ("unspecified".equals(var29.name)) {
                var30.append("[?] ");
            } else {
                var30.append("[").append(var28 + 1).append("] ");
            }

            String var31 = var30.append(var29.name).toString();
            this.font.drawShadow(param0, var31, (float)(var7 - 160), (float)(var8 + 80 + var28 * 8 + 20), var29.getColor());
            var31 = var24.format(var29.percentage) + "%";
            this.font.drawShadow(param0, var31, (float)(var7 + 160 - 50 - this.font.width(var31)), (float)(var8 + 80 + var28 * 8 + 20), var29.getColor());
            var31 = var24.format(var29.globalPercentage) + "%";
            this.font.drawShadow(param0, var31, (float)(var7 + 160 - this.font.width(var31)), (float)(var8 + 80 + var28 * 8 + 20), var29.getColor());
        }

    }

    public void stop() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void pauseGame(boolean param0) {
        if (this.screen == null) {
            boolean var0 = this.hasSingleplayerServer() && !this.singleplayerServer.isPublished();
            if (var0) {
                this.setScreen(new PauseScreen(!param0));
                this.soundManager.pause();
            } else {
                this.setScreen(new PauseScreen(true));
            }

        }
    }

    private void continueAttack(boolean param0) {
        if (!param0) {
            this.missTime = 0;
        }

        if (this.missTime <= 0 && !this.player.isUsingItem()) {
            if (param0 && this.hitResult != null && this.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult var0 = (BlockHitResult)this.hitResult;
                BlockPos var1 = var0.getBlockPos();
                if (!this.level.getBlockState(var1).isAir()) {
                    Direction var2 = var0.getDirection();
                    if (this.gameMode.continueDestroyBlock(var1, var2)) {
                        this.particleEngine.crack(var1, var2);
                        this.player.swing(InteractionHand.MAIN_HAND);
                    }
                }

            } else {
                this.gameMode.stopDestroyBlock();
            }
        }
    }

    private boolean startAttack() {
        if (this.missTime > 0) {
            return false;
        } else if (this.hitResult == null) {
            LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
            if (this.gameMode.hasMissTime()) {
                this.missTime = 10;
            }

            return false;
        } else if (this.player.isHandsBusy()) {
            return false;
        } else {
            boolean var0 = false;
            switch(this.hitResult.getType()) {
                case ENTITY:
                    this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
                    break;
                case BLOCK:
                    BlockHitResult var1 = (BlockHitResult)this.hitResult;
                    BlockPos var2 = var1.getBlockPos();
                    if (!this.level.getBlockState(var2).isAir()) {
                        this.gameMode.startDestroyBlock(var2, var1.getDirection());
                        if (this.level.getBlockState(var2).isAir()) {
                            var0 = true;
                        }
                        break;
                    }
                case MISS:
                    if (this.gameMode.hasMissTime()) {
                        this.missTime = 10;
                    }

                    this.player.resetAttackStrengthTicker();
            }

            this.player.swing(InteractionHand.MAIN_HAND);
            return var0;
        }
    }

    private void startUseItem() {
        if (!this.gameMode.isDestroying()) {
            this.rightClickDelay = 4;
            if (!this.player.isHandsBusy()) {
                if (this.hitResult == null) {
                    LOGGER.warn("Null returned as 'hitResult', this shouldn't happen!");
                }

                for(InteractionHand var0 : InteractionHand.values()) {
                    ItemStack var1 = this.player.getItemInHand(var0);
                    if (this.hitResult != null) {
                        switch(this.hitResult.getType()) {
                            case ENTITY:
                                EntityHitResult var2 = (EntityHitResult)this.hitResult;
                                Entity var3 = var2.getEntity();
                                if (!this.level.getWorldBorder().isWithinBounds(var3.blockPosition())) {
                                    return;
                                }

                                InteractionResult var4 = this.gameMode.interactAt(this.player, var3, var2, var0);
                                if (!var4.consumesAction()) {
                                    var4 = this.gameMode.interact(this.player, var3, var0);
                                }

                                if (var4.consumesAction()) {
                                    if (var4.shouldSwing()) {
                                        this.player.swing(var0);
                                    }

                                    return;
                                }
                                break;
                            case BLOCK:
                                BlockHitResult var5 = (BlockHitResult)this.hitResult;
                                int var6 = var1.getCount();
                                InteractionResult var7 = this.gameMode.useItemOn(this.player, var0, var5);
                                if (var7.consumesAction()) {
                                    if (var7.shouldSwing()) {
                                        this.player.swing(var0);
                                        if (!var1.isEmpty() && (var1.getCount() != var6 || this.gameMode.hasInfiniteItems())) {
                                            this.gameRenderer.itemInHandRenderer.itemUsed(var0);
                                        }
                                    }

                                    return;
                                }

                                if (var7 == InteractionResult.FAIL) {
                                    return;
                                }
                        }
                    }

                    if (!var1.isEmpty()) {
                        InteractionResult var8 = this.gameMode.useItem(this.player, var0);
                        if (var8.consumesAction()) {
                            if (var8.shouldSwing()) {
                                this.player.swing(var0);
                            }

                            this.gameRenderer.itemInHandRenderer.itemUsed(var0);
                            return;
                        }
                    }
                }

            }
        }
    }

    public MusicManager getMusicManager() {
        return this.musicManager;
    }

    public void tick() {
        if (this.rightClickDelay > 0) {
            --this.rightClickDelay;
        }

        this.profiler.push("gui");
        this.gui.tick(this.pause);
        this.profiler.pop();
        this.gameRenderer.pick(1.0F);
        this.tutorial.onLookAt(this.level, this.hitResult);
        this.profiler.push("gameMode");
        if (!this.pause && this.level != null) {
            this.gameMode.tick();
        }

        this.profiler.popPush("textures");
        if (this.level != null) {
            this.textureManager.tick();
        }

        if (this.screen != null || this.player == null) {
            Screen var4 = this.screen;
            if (var4 instanceof InBedChatScreen var0 && !this.player.isSleeping()) {
                var0.onPlayerWokeUp();
            }
        } else if (this.player.isDeadOrDying() && !(this.screen instanceof DeathScreen)) {
            this.setScreen(null);
        } else if (this.player.isSleeping() && this.level != null) {
            this.setScreen(new InBedChatScreen());
        }

        if (this.screen != null) {
            this.missTime = 10000;
        }

        if (this.screen != null) {
            Screen.wrapScreenError(() -> this.screen.tick(), "Ticking screen", this.screen.getClass().getCanonicalName());
        }

        if (!this.options.renderDebug) {
            this.gui.clearCache();
        }

        if (this.overlay == null && (this.screen == null || this.screen.passEvents)) {
            this.profiler.popPush("Keybindings");
            this.handleKeybinds();
            if (this.missTime > 0) {
                --this.missTime;
            }
        }

        if (this.level != null) {
            this.profiler.popPush("gameRenderer");
            if (!this.pause) {
                this.gameRenderer.tick();
            }

            this.profiler.popPush("levelRenderer");
            if (!this.pause) {
                this.levelRenderer.tick();
            }

            this.profiler.popPush("level");
            if (!this.pause) {
                if (this.level.getSkyFlashTime() > 0) {
                    this.level.setSkyFlashTime(this.level.getSkyFlashTime() - 1);
                }

                this.level.tickEntities();
            }
        } else if (this.gameRenderer.currentEffect() != null) {
            this.gameRenderer.shutdownEffect();
        }

        if (!this.pause) {
            this.musicManager.tick();
        }

        this.soundManager.tick(this.pause);
        if (this.level != null) {
            if (!this.pause) {
                if (!this.options.joinedFirstServer && this.isMultiplayerServer()) {
                    Component var1 = new TranslatableComponent("tutorial.socialInteractions.title");
                    Component var2 = new TranslatableComponent("tutorial.socialInteractions.description", Tutorial.key("socialInteractions"));
                    this.socialInteractionsToast = new TutorialToast(TutorialToast.Icons.SOCIAL_INTERACTIONS, var1, var2, true);
                    this.tutorial.addTimedToast(this.socialInteractionsToast, 160);
                    this.options.joinedFirstServer = true;
                    this.options.save();
                }

                this.tutorial.tick();

                try {
                    this.level.tick(() -> true);
                } catch (Throwable var41) {
                    CrashReport var4 = CrashReport.forThrowable(var41, "Exception in world tick");
                    if (this.level == null) {
                        CrashReportCategory var5 = var4.addCategory("Affected level");
                        var5.setDetail("Problem", "Level is null!");
                    } else {
                        this.level.fillReportDetails(var4);
                    }

                    throw new ReportedException(var4);
                }
            }

            this.profiler.popPush("animateTick");
            if (!this.pause && this.level != null) {
                this.level.animateTick(this.player.getBlockX(), this.player.getBlockY(), this.player.getBlockZ());
            }

            this.profiler.popPush("particles");
            if (!this.pause) {
                this.particleEngine.tick();
            }
        } else if (this.pendingConnection != null) {
            this.profiler.popPush("pendingConnection");
            this.pendingConnection.tick();
        }

        this.profiler.popPush("keyboard");
        this.keyboardHandler.tick();
        this.profiler.pop();
    }

    private boolean isMultiplayerServer() {
        return !this.isLocalServer || this.singleplayerServer != null && this.singleplayerServer.isPublished();
    }

    private void handleKeybinds() {
        for(; this.options.keyTogglePerspective.consumeClick(); this.levelRenderer.needsUpdate()) {
            CameraType var0 = this.options.getCameraType();
            this.options.setCameraType(this.options.getCameraType().cycle());
            if (var0.isFirstPerson() != this.options.getCameraType().isFirstPerson()) {
                this.gameRenderer.checkEntityPostEffect(this.options.getCameraType().isFirstPerson() ? this.getCameraEntity() : null);
            }
        }

        while(this.options.keySmoothCamera.consumeClick()) {
            this.options.smoothCamera = !this.options.smoothCamera;
        }

        for(int var1 = 0; var1 < 9; ++var1) {
            boolean var2 = this.options.keySaveHotbarActivator.isDown();
            boolean var3 = this.options.keyLoadHotbarActivator.isDown();
            if (this.options.keyHotbarSlots[var1].consumeClick()) {
                if (this.player.isSpectator()) {
                    this.gui.getSpectatorGui().onHotbarSelected(var1);
                } else if (!this.player.isCreative() || this.screen != null || !var3 && !var2) {
                    this.player.getInventory().selected = var1;
                } else {
                    CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, var1, var3, var2);
                }
            }
        }

        while(this.options.keySocialInteractions.consumeClick()) {
            if (!this.isMultiplayerServer()) {
                this.player.displayClientMessage(SOCIAL_INTERACTIONS_NOT_AVAILABLE, true);
                NarratorChatListener.INSTANCE.sayNow(SOCIAL_INTERACTIONS_NOT_AVAILABLE);
            } else {
                if (this.socialInteractionsToast != null) {
                    this.tutorial.removeTimedToast(this.socialInteractionsToast);
                    this.socialInteractionsToast = null;
                }

                this.setScreen(new SocialInteractionsScreen());
            }
        }

        while(this.options.keyInventory.consumeClick()) {
            if (this.gameMode.isServerControlledInventory()) {
                this.player.sendOpenInventory();
            } else {
                this.tutorial.onOpenInventory();
                this.setScreen(new InventoryScreen(this.player));
            }
        }

        while(this.options.keyAdvancements.consumeClick()) {
            this.setScreen(new AdvancementsScreen(this.player.connection.getAdvancements()));
        }

        while(this.options.keySwapOffhand.consumeClick()) {
            if (!this.player.isSpectator()) {
                this.getConnection()
                    .send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ZERO, Direction.DOWN));
            }
        }

        while(this.options.keyDrop.consumeClick()) {
            if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
                this.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        while(this.options.keyChat.consumeClick()) {
            this.openChatScreen("");
        }

        if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
            this.openChatScreen("/");
        }

        boolean var4 = false;
        if (this.player.isUsingItem()) {
            if (!this.options.keyUse.isDown()) {
                this.gameMode.releaseUsingItem(this.player);
            }

            while(this.options.keyAttack.consumeClick()) {
            }

            while(this.options.keyUse.consumeClick()) {
            }

            while(this.options.keyPickItem.consumeClick()) {
            }
        } else {
            while(this.options.keyAttack.consumeClick()) {
                var4 |= this.startAttack();
            }

            while(this.options.keyUse.consumeClick()) {
                this.startUseItem();
            }

            while(this.options.keyPickItem.consumeClick()) {
                this.pickBlock();
            }
        }

        if (this.options.keyUse.isDown() && this.rightClickDelay == 0 && !this.player.isUsingItem()) {
            this.startUseItem();
        }

        this.continueAttack(this.screen == null && !var4 && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
    }

    public ClientTelemetryManager createTelemetryManager() {
        return new ClientTelemetryManager(this, this.userApiService, this.user.getXuid(), this.user.getClientId(), this.deviceSessionId);
    }

    public double getGpuUtilization() {
        return this.gpuUtilization;
    }

    public WorldOpenFlows createWorldOpenFlows() {
        return new WorldOpenFlows(this, this.levelSource);
    }

    public void doWorldLoad(String param0, LevelStorageSource.LevelStorageAccess param1, PackRepository param2, WorldStem param3) {
        this.clearLevel();
        this.progressListener.set(null);

        try {
            param1.saveDataTag(param3.registryAccess(), param3.worldData());
            YggdrasilAuthenticationService var0 = new YggdrasilAuthenticationService(this.proxy);
            MinecraftSessionService var1 = var0.createMinecraftSessionService();
            GameProfileRepository var2 = var0.createProfileRepository();
            GameProfileCache var3 = new GameProfileCache(var2, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            var3.setExecutor(this);
            SkullBlockEntity.setup(var3, var1, this);
            GameProfileCache.setUsesAuthentication(false);
            this.singleplayerServer = MinecraftServer.spin(param6 -> new IntegratedServer(param6, this, param1, param2, param3, var1, var2, var3, param0x -> {
                    StoringChunkProgressListener var0x = new StoringChunkProgressListener(param0x + 0);
                    this.progressListener.set(var0x);
                    return ProcessorChunkProgressListener.createStarted(var0x, this.progressTasks::add);
                }));
            this.isLocalServer = true;
        } catch (Throwable var10) {
            CrashReport var5 = CrashReport.forThrowable(var10, "Starting integrated server");
            CrashReportCategory var6 = var5.addCategory("Starting integrated server");
            var6.setDetail("Level ID", param0);
            var6.setDetail("Level Name", () -> param3.worldData().getLevelName());
            throw new ReportedException(var5);
        }

        while(this.progressListener.get() == null) {
            Thread.yield();
        }

        LevelLoadingScreen var7 = new LevelLoadingScreen(this.progressListener.get());
        this.setScreen(var7);
        this.profiler.push("waitForServer");

        while(!this.singleplayerServer.isReady()) {
            var7.tick();
            this.runTick(false);

            try {
                Thread.sleep(16L);
            } catch (InterruptedException var91) {
            }

            if (this.delayedCrash != null) {
                crash(this.delayedCrash.get());
                return;
            }
        }

        this.profiler.pop();
        SocketAddress var8 = this.singleplayerServer.getConnection().startMemoryChannel();
        Connection var9 = Connection.connectToLocalServer(var8);
        var9.setListener(new ClientHandshakePacketListenerImpl(var9, this, null, param0x -> {
        }));
        var9.send(new ClientIntentionPacket(var8.toString(), 0, ConnectionProtocol.LOGIN));
        var9.send(new ServerboundHelloPacket(this.getUser().getGameProfile()));
        this.pendingConnection = var9;
    }

    public void setLevel(ClientLevel param0) {
        ProgressScreen var0 = new ProgressScreen(true);
        var0.progressStartNoAbort(new TranslatableComponent("connect.joining"));
        this.updateScreenAndTick(var0);
        this.level = param0;
        this.updateLevelInEngines(param0);
        if (!this.isLocalServer) {
            AuthenticationService var1 = new YggdrasilAuthenticationService(this.proxy);
            MinecraftSessionService var2 = var1.createMinecraftSessionService();
            GameProfileRepository var3 = var1.createProfileRepository();
            GameProfileCache var4 = new GameProfileCache(var3, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            var4.setExecutor(this);
            SkullBlockEntity.setup(var4, var2, this);
            GameProfileCache.setUsesAuthentication(false);
        }

    }

    public void clearLevel() {
        this.clearLevel(new ProgressScreen(true));
    }

    public void clearLevel(Screen param0) {
        ClientPacketListener var0 = this.getConnection();
        if (var0 != null) {
            this.dropAllTasks();
            var0.cleanup();
        }

        this.playerSocialManager.stopOnlineMode();
        if (this.metricsRecorder.isRecording()) {
            this.debugClientMetricsCancel();
        }

        IntegratedServer var1 = this.singleplayerServer;
        this.singleplayerServer = null;
        this.gameRenderer.resetData();
        this.gameMode = null;
        NarratorChatListener.INSTANCE.clear();
        this.updateScreenAndTick(param0);
        if (this.level != null) {
            if (var1 != null) {
                this.profiler.push("waitForServer");

                while(!var1.isShutdown()) {
                    this.runTick(false);
                }

                this.profiler.pop();
            }

            this.clientPackSource.clearServerPack();
            this.gui.onDisconnected();
            this.currentServer = null;
            this.isLocalServer = false;
            this.game.onLeaveGameSession();
        }

        this.level = null;
        this.updateLevelInEngines(null);
        this.player = null;
        SkullBlockEntity.clear();
    }

    private void updateScreenAndTick(Screen param0) {
        this.profiler.push("forcedTick");
        this.soundManager.stop();
        this.cameraEntity = null;
        this.pendingConnection = null;
        this.setScreen(param0);
        this.runTick(false);
        this.profiler.pop();
    }

    public void forceSetScreen(Screen param0) {
        this.profiler.push("forcedTick");
        this.setScreen(param0);
        this.runTick(false);
        this.profiler.pop();
    }

    private void updateLevelInEngines(@Nullable ClientLevel param0) {
        this.levelRenderer.setLevel(param0);
        this.particleEngine.setLevel(param0);
        this.blockEntityRenderDispatcher.setLevel(param0);
        this.updateTitle();
    }

    public boolean allowsMultiplayer() {
        return this.allowsMultiplayer && this.userApiService.properties().flag(UserFlag.SERVERS_ALLOWED);
    }

    public boolean allowsRealms() {
        return this.userApiService.properties().flag(UserFlag.REALMS_ALLOWED);
    }

    public boolean isBlocked(UUID param0) {
        if (this.getChatStatus().isChatAllowed(false)) {
            return this.playerSocialManager.shouldHideMessageFrom(param0);
        } else {
            return (this.player == null || !param0.equals(this.player.getUUID())) && !param0.equals(Util.NIL_UUID);
        }
    }

    public Minecraft.ChatStatus getChatStatus() {
        if (this.options.chatVisibility == ChatVisiblity.HIDDEN) {
            return Minecraft.ChatStatus.DISABLED_BY_OPTIONS;
        } else if (!this.allowsChat) {
            return Minecraft.ChatStatus.DISABLED_BY_LAUNCHER;
        } else {
            return !this.userApiService.properties().flag(UserFlag.CHAT_ALLOWED) ? Minecraft.ChatStatus.DISABLED_BY_PROFILE : Minecraft.ChatStatus.ENABLED;
        }
    }

    public final boolean isDemo() {
        return this.demo;
    }

    @Nullable
    public ClientPacketListener getConnection() {
        return this.player == null ? null : this.player.connection;
    }

    public static boolean renderNames() {
        return !instance.options.hideGui;
    }

    public static boolean useFancyGraphics() {
        return instance.options.graphicsMode.getId() >= GraphicsStatus.FANCY.getId();
    }

    public static boolean useShaderTransparency() {
        return !instance.gameRenderer.isPanoramicMode() && instance.options.graphicsMode.getId() >= GraphicsStatus.FABULOUS.getId();
    }

    public static boolean useAmbientOcclusion() {
        return instance.options.ambientOcclusion().get() != AmbientOcclusionStatus.OFF;
    }

    private void pickBlock() {
        if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
            boolean var0 = this.player.getAbilities().instabuild;
            BlockEntity var1 = null;
            HitResult.Type var2 = this.hitResult.getType();
            ItemStack var6;
            if (var2 == HitResult.Type.BLOCK) {
                BlockPos var3 = ((BlockHitResult)this.hitResult).getBlockPos();
                BlockState var4 = this.level.getBlockState(var3);
                if (var4.isAir()) {
                    return;
                }

                Block var5 = var4.getBlock();
                var6 = var5.getCloneItemStack(this.level, var3, var4);
                if (var6.isEmpty()) {
                    return;
                }

                if (var0 && Screen.hasControlDown() && var4.hasBlockEntity()) {
                    var1 = this.level.getBlockEntity(var3);
                }
            } else {
                if (var2 != HitResult.Type.ENTITY || !var0) {
                    return;
                }

                Entity var7 = ((EntityHitResult)this.hitResult).getEntity();
                var6 = var7.getPickResult();
                if (var6 == null) {
                    return;
                }
            }

            if (var6.isEmpty()) {
                String var10 = "";
                if (var2 == HitResult.Type.BLOCK) {
                    var10 = Registry.BLOCK.getKey(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock()).toString();
                } else if (var2 == HitResult.Type.ENTITY) {
                    var10 = Registry.ENTITY_TYPE.getKey(((EntityHitResult)this.hitResult).getEntity().getType()).toString();
                }

                LOGGER.warn("Picking on: [{}] {} gave null item", var2, var10);
            } else {
                Inventory var11 = this.player.getInventory();
                if (var1 != null) {
                    this.addCustomNbtData(var6, var1);
                }

                int var12 = var11.findSlotMatchingItem(var6);
                if (var0) {
                    var11.setPickedItem(var6);
                    this.gameMode.handleCreativeModeItemAdd(this.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + var11.selected);
                } else if (var12 != -1) {
                    if (Inventory.isHotbarSlot(var12)) {
                        var11.selected = var12;
                    } else {
                        this.gameMode.handlePickItem(var12);
                    }
                }

            }
        }
    }

    private ItemStack addCustomNbtData(ItemStack param0, BlockEntity param1) {
        CompoundTag var0 = param1.saveWithFullMetadata();
        if (param0.getItem() instanceof PlayerHeadItem && var0.contains("SkullOwner")) {
            CompoundTag var1 = var0.getCompound("SkullOwner");
            param0.getOrCreateTag().put("SkullOwner", var1);
            return param0;
        } else {
            BlockItem.setBlockEntityData(param0, param1.getType(), var0);
            CompoundTag var2 = new CompoundTag();
            ListTag var3 = new ListTag();
            var3.add(StringTag.valueOf("\"(+NBT)\""));
            var2.put("Lore", var3);
            param0.addTagElement("display", var2);
            return param0;
        }
    }

    public CrashReport fillReport(CrashReport param0) {
        SystemReport var0 = param0.getSystemReport();
        fillSystemReport(var0, this, this.languageManager, this.launchedVersion, this.options);
        if (this.level != null) {
            this.level.fillReportDetails(param0);
        }

        if (this.singleplayerServer != null) {
            this.singleplayerServer.fillSystemReport(var0);
        }

        this.reloadStateTracker.fillCrashReport(param0);
        return param0;
    }

    public static void fillReport(@Nullable Minecraft param0, @Nullable LanguageManager param1, String param2, @Nullable Options param3, CrashReport param4) {
        SystemReport var0 = param4.getSystemReport();
        fillSystemReport(var0, param0, param1, param2, param3);
    }

    private static SystemReport fillSystemReport(
        SystemReport param0, @Nullable Minecraft param1, @Nullable LanguageManager param2, String param3, Options param4
    ) {
        param0.setDetail("Launched Version", () -> param3);
        param0.setDetail("Backend library", RenderSystem::getBackendDescription);
        param0.setDetail("Backend API", RenderSystem::getApiDescription);
        param0.setDetail("Window size", () -> param1 != null ? param1.window.getWidth() + "x" + param1.window.getHeight() : "<not initialized>");
        param0.setDetail("GL Caps", RenderSystem::getCapsString);
        param0.setDetail("GL debug messages", () -> GlDebug.isDebugEnabled() ? String.join("\n", GlDebug.getLastOpenGlDebugMessages()) : "<disabled>");
        param0.setDetail("Using VBOs", () -> "Yes");
        param0.setDetail("Is Modded", () -> checkModStatus().fullDescription());
        param0.setDetail("Type", "Client (map_client.txt)");
        if (param4 != null) {
            if (instance != null) {
                String var0 = instance.getGpuWarnlistManager().getAllWarnings();
                if (var0 != null) {
                    param0.setDetail("GPU Warnings", var0);
                }
            }

            param0.setDetail("Graphics mode", param4.graphicsMode.toString());
            param0.setDetail("Resource Packs", () -> {
                StringBuilder var0x = new StringBuilder();

                for(String var1x : param4.resourcePacks) {
                    if (var0x.length() > 0) {
                        var0x.append(", ");
                    }

                    var0x.append(var1x);
                    if (param4.incompatibleResourcePacks.contains(var1x)) {
                        var0x.append(" (incompatible)");
                    }
                }

                return var0x.toString();
            });
        }

        if (param2 != null) {
            param0.setDetail("Current Language", () -> param2.getSelected().toString());
        }

        param0.setDetail("CPU", GlUtil::getCpuInfo);
        return param0;
    }

    public static Minecraft getInstance() {
        return instance;
    }

    public CompletableFuture<Void> delayTextureReload() {
        return this.<CompletableFuture<Void>>submit(this::reloadResourcePacks).thenCompose(param0 -> param0);
    }

    public void setCurrentServer(@Nullable ServerData param0) {
        this.currentServer = param0;
    }

    @Nullable
    public ServerData getCurrentServer() {
        return this.currentServer;
    }

    public boolean isLocalServer() {
        return this.isLocalServer;
    }

    public boolean hasSingleplayerServer() {
        return this.isLocalServer && this.singleplayerServer != null;
    }

    @Nullable
    public IntegratedServer getSingleplayerServer() {
        return this.singleplayerServer;
    }

    public User getUser() {
        return this.user;
    }

    public PropertyMap getProfileProperties() {
        if (this.profileProperties.isEmpty()) {
            GameProfile var0 = this.getMinecraftSessionService().fillProfileProperties(this.user.getGameProfile(), false);
            this.profileProperties.putAll(var0.getProperties());
        }

        return this.profileProperties;
    }

    public Proxy getProxy() {
        return this.proxy;
    }

    public TextureManager getTextureManager() {
        return this.textureManager;
    }

    public ResourceManager getResourceManager() {
        return this.resourceManager;
    }

    public PackRepository getResourcePackRepository() {
        return this.resourcePackRepository;
    }

    public ClientPackSource getClientPackSource() {
        return this.clientPackSource;
    }

    public File getResourcePackDirectory() {
        return this.resourcePackDirectory;
    }

    public LanguageManager getLanguageManager() {
        return this.languageManager;
    }

    public Function<ResourceLocation, TextureAtlasSprite> getTextureAtlas(ResourceLocation param0) {
        return this.modelManager.getAtlas(param0)::getSprite;
    }

    public boolean is64Bit() {
        return this.is64bit;
    }

    public boolean isPaused() {
        return this.pause;
    }

    public GpuWarnlistManager getGpuWarnlistManager() {
        return this.gpuWarnlistManager;
    }

    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    public Music getSituationalMusic() {
        if (this.screen instanceof WinScreen) {
            return Musics.CREDITS;
        } else if (this.player != null) {
            if (this.player.level.dimension() == Level.END) {
                return this.gui.getBossOverlay().shouldPlayMusic() ? Musics.END_BOSS : Musics.END;
            } else {
                Holder<Biome> var0 = this.player.level.getBiome(this.player.blockPosition());
                if (!this.musicManager.isPlayingMusic(Musics.UNDER_WATER) && (!this.player.isUnderWater() || !var0.is(BiomeTags.PLAYS_UNDERWATER_MUSIC))) {
                    return this.player.level.dimension() != Level.NETHER && this.player.getAbilities().instabuild && this.player.getAbilities().mayfly
                        ? Musics.CREATIVE
                        : var0.value().getBackgroundMusic().orElse(Musics.GAME);
                } else {
                    return Musics.UNDER_WATER;
                }
            }
        } else {
            return Musics.MENU;
        }
    }

    public MinecraftSessionService getMinecraftSessionService() {
        return this.minecraftSessionService;
    }

    public SkinManager getSkinManager() {
        return this.skinManager;
    }

    @Nullable
    public Entity getCameraEntity() {
        return this.cameraEntity;
    }

    public void setCameraEntity(Entity param0) {
        this.cameraEntity = param0;
        this.gameRenderer.checkEntityPostEffect(param0);
    }

    public boolean shouldEntityAppearGlowing(Entity param0) {
        return param0.isCurrentlyGlowing()
            || this.player != null && this.player.isSpectator() && this.options.keySpectatorOutlines.isDown() && param0.getType() == EntityType.PLAYER;
    }

    @Override
    protected Thread getRunningThread() {
        return this.gameThread;
    }

    @Override
    protected Runnable wrapRunnable(Runnable param0) {
        return param0;
    }

    @Override
    protected boolean shouldRun(Runnable param0) {
        return true;
    }

    public BlockRenderDispatcher getBlockRenderer() {
        return this.blockRenderer;
    }

    public EntityRenderDispatcher getEntityRenderDispatcher() {
        return this.entityRenderDispatcher;
    }

    public BlockEntityRenderDispatcher getBlockEntityRenderDispatcher() {
        return this.blockEntityRenderDispatcher;
    }

    public ItemRenderer getItemRenderer() {
        return this.itemRenderer;
    }

    public ItemInHandRenderer getItemInHandRenderer() {
        return this.itemInHandRenderer;
    }

    public <T> MutableSearchTree<T> getSearchTree(SearchRegistry.Key<T> param0) {
        return this.searchRegistry.getTree(param0);
    }

    public FrameTimer getFrameTimer() {
        return this.frameTimer;
    }

    public boolean isConnectedToRealms() {
        return this.connectedToRealms;
    }

    public void setConnectedToRealms(boolean param0) {
        this.connectedToRealms = param0;
    }

    public DataFixer getFixerUpper() {
        return this.fixerUpper;
    }

    public float getFrameTime() {
        return this.timer.partialTick;
    }

    public float getDeltaFrameTime() {
        return this.timer.tickDelta;
    }

    public BlockColors getBlockColors() {
        return this.blockColors;
    }

    public boolean showOnlyReducedInfo() {
        return this.player != null && this.player.isReducedDebugInfo() || this.options.reducedDebugInfo;
    }

    public ToastComponent getToasts() {
        return this.toast;
    }

    public Tutorial getTutorial() {
        return this.tutorial;
    }

    public boolean isWindowActive() {
        return this.windowActive;
    }

    public HotbarManager getHotbarManager() {
        return this.hotbarManager;
    }

    public ModelManager getModelManager() {
        return this.modelManager;
    }

    public PaintingTextureManager getPaintingTextures() {
        return this.paintingTextures;
    }

    public MobEffectTextureManager getMobEffectTextures() {
        return this.mobEffectTextures;
    }

    @Override
    public void setWindowActive(boolean param0) {
        this.windowActive = param0;
    }

    public Component grabPanoramixScreenshot(File param0, int param1, int param2) {
        int var0 = this.window.getWidth();
        int var1 = this.window.getHeight();
        RenderTarget var2 = new TextureTarget(param1, param2, true, ON_OSX);
        float var3 = this.player.getXRot();
        float var4 = this.player.getYRot();
        float var5 = this.player.xRotO;
        float var6 = this.player.yRotO;
        this.gameRenderer.setRenderBlockOutline(false);

        TranslatableComponent var12;
        try {
            this.gameRenderer.setPanoramicMode(true);
            this.levelRenderer.graphicsChanged();
            this.window.setWidth(param1);
            this.window.setHeight(param2);

            for(int var7 = 0; var7 < 6; ++var7) {
                switch(var7) {
                    case 0:
                        this.player.setYRot(var4);
                        this.player.setXRot(0.0F);
                        break;
                    case 1:
                        this.player.setYRot((var4 + 90.0F) % 360.0F);
                        this.player.setXRot(0.0F);
                        break;
                    case 2:
                        this.player.setYRot((var4 + 180.0F) % 360.0F);
                        this.player.setXRot(0.0F);
                        break;
                    case 3:
                        this.player.setYRot((var4 - 90.0F) % 360.0F);
                        this.player.setXRot(0.0F);
                        break;
                    case 4:
                        this.player.setYRot(var4);
                        this.player.setXRot(-90.0F);
                        break;
                    case 5:
                    default:
                        this.player.setYRot(var4);
                        this.player.setXRot(90.0F);
                }

                this.player.yRotO = this.player.getYRot();
                this.player.xRotO = this.player.getXRot();
                var2.bindWrite(true);
                this.gameRenderer.renderLevel(1.0F, 0L, new PoseStack());

                try {
                    Thread.sleep(10L);
                } catch (InterruptedException var17) {
                }

                Screenshot.grab(param0, "panorama_" + var7 + ".png", var2, param0x -> {
                });
            }

            Component var8 = new TextComponent(param0.getName())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(param1x -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, param0.getAbsolutePath())));
            return new TranslatableComponent("screenshot.success", var8);
        } catch (Exception var18) {
            LOGGER.error("Couldn't save image", (Throwable)var18);
            var12 = new TranslatableComponent("screenshot.failure", var18.getMessage());
        } finally {
            this.player.setXRot(var3);
            this.player.setYRot(var4);
            this.player.xRotO = var5;
            this.player.yRotO = var6;
            this.gameRenderer.setRenderBlockOutline(true);
            this.window.setWidth(var0);
            this.window.setHeight(var1);
            var2.destroyBuffers();
            this.gameRenderer.setPanoramicMode(false);
            this.levelRenderer.graphicsChanged();
            this.getMainRenderTarget().bindWrite(true);
        }

        return var12;
    }

    private Component grabHugeScreenshot(File param0, int param1, int param2, int param3, int param4) {
        try {
            ByteBuffer var0 = GlUtil.allocateMemory(param1 * param2 * 3);
            Screenshot var1 = new Screenshot(param0, param3, param4, param2);
            float var2 = (float)param3 / (float)param1;
            float var3 = (float)param4 / (float)param2;
            float var4 = var2 > var3 ? var2 : var3;

            for(int var5 = (param4 - 1) / param2 * param2; var5 >= 0; var5 -= param2) {
                for(int var6 = 0; var6 < param3; var6 += param1) {
                    RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
                    float var7 = (float)(param3 - param1) / 2.0F * 2.0F - (float)(var6 * 2);
                    float var8 = (float)(param4 - param2) / 2.0F * 2.0F - (float)(var5 * 2);
                    var7 /= (float)param1;
                    var8 /= (float)param2;
                    this.gameRenderer.renderZoomed(var4, var7, var8);
                    var0.clear();
                    RenderSystem.pixelStore(3333, 1);
                    RenderSystem.pixelStore(3317, 1);
                    RenderSystem.readPixels(0, 0, param1, param2, 32992, 5121, var0);
                    var1.addRegion(var0, var6, var5, param1, param2);
                }

                var1.saveRow();
            }

            File var9 = var1.close();
            GlUtil.freeMemory(var0);
            Component var10 = new TextComponent(var9.getName())
                .withStyle(ChatFormatting.UNDERLINE)
                .withStyle(param1x -> param1x.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, var9.getAbsolutePath())));
            return new TranslatableComponent("screenshot.success", var10);
        } catch (Exception var15) {
            LOGGER.warn("Couldn't save screenshot", (Throwable)var15);
            return new TranslatableComponent("screenshot.failure", var15.getMessage());
        }
    }

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public Game getGame() {
        return this.game;
    }

    @Nullable
    public StoringChunkProgressListener getProgressListener() {
        return this.progressListener.get();
    }

    public SplashManager getSplashManager() {
        return this.splashManager;
    }

    @Nullable
    public Overlay getOverlay() {
        return this.overlay;
    }

    public PlayerSocialManager getPlayerSocialManager() {
        return this.playerSocialManager;
    }

    public boolean renderOnThread() {
        return false;
    }

    public Window getWindow() {
        return this.window;
    }

    public RenderBuffers renderBuffers() {
        return this.renderBuffers;
    }

    private static Pack createClientPackAdapter(
        String param0x, Component param1, boolean param2, Supplier<PackResources> param3, PackMetadataSection param4, Pack.Position param5, PackSource param6
    ) {
        int var0x = param4.getPackFormat();
        Supplier<PackResources> var1x = param3;
        if (var0x <= 3) {
            var1x = adaptV3(param3);
        }

        if (var0x <= 4) {
            var1x = adaptV4(var1x);
        }

        return new Pack(param0x, param1, param2, var1x, param4, PackType.CLIENT_RESOURCES, param5, param6);
    }

    private static Supplier<PackResources> adaptV3(Supplier<PackResources> param0) {
        return () -> new LegacyPackResourcesAdapter(param0.get(), LegacyPackResourcesAdapter.V3);
    }

    private static Supplier<PackResources> adaptV4(Supplier<PackResources> param0) {
        return () -> new PackResourcesAdapterV4(param0.get());
    }

    public void updateMaxMipLevel(int param0) {
        this.modelManager.updateMaxMipLevel(param0);
    }

    public EntityModelSet getEntityModels() {
        return this.entityModels;
    }

    public boolean isTextFilteringEnabled() {
        return this.userApiService.properties().flag(UserFlag.PROFANITY_FILTER_ENABLED);
    }

    public void prepareForMultiplayer() {
        this.playerSocialManager.startOnlineMode();
    }

    @OnlyIn(Dist.CLIENT)
    public static enum ChatStatus {
        ENABLED(TextComponent.EMPTY) {
            @Override
            public boolean isChatAllowed(boolean param0) {
                return true;
            }
        },
        DISABLED_BY_OPTIONS(new TranslatableComponent("chat.disabled.options").withStyle(ChatFormatting.RED)) {
            @Override
            public boolean isChatAllowed(boolean param0) {
                return false;
            }
        },
        DISABLED_BY_LAUNCHER(new TranslatableComponent("chat.disabled.launcher").withStyle(ChatFormatting.RED)) {
            @Override
            public boolean isChatAllowed(boolean param0) {
                return param0;
            }
        },
        DISABLED_BY_PROFILE(new TranslatableComponent("chat.disabled.profile").withStyle(ChatFormatting.RED)) {
            @Override
            public boolean isChatAllowed(boolean param0) {
                return param0;
            }
        };

        private final Component message;

        ChatStatus(Component param0) {
            this.message = param0;
        }

        public Component getMessage() {
            return this.message;
        }

        public abstract boolean isChatAllowed(boolean var1);
    }
}
