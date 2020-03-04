package net.minecraft.client;

import com.google.common.collect.Queues;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GlUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.DataFixer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
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
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.VirtualScreen;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.LegacyResourcePackAdapter;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PackAdapterV4;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.UnopenedResourcePack;
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
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
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
import net.minecraft.server.level.progress.ProcessorChunkProgressListener;
import net.minecraft.server.level.progress.StoringChunkProgressListener;
import net.minecraft.server.packs.Pack;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.FolderRepositorySource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.UnopenedPack;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadableResourceManager;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.util.profiling.SingleTickProfiler;
import net.minecraft.util.thread.ReentrantBlockableEventLoop;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Snooper;
import net.minecraft.world.SnooperPopulator;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PlayerHeadItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.NetherDimension;
import net.minecraft.world.level.dimension.end.TheEndDimension;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Minecraft extends ReentrantBlockableEventLoop<Runnable> implements SnooperPopulator, WindowEventHandler {
    private static Minecraft instance;
    private static final Logger LOGGER = LogManager.getLogger();
    public static final boolean ON_OSX = Util.getPlatform() == Util.OS.OSX;
    public static final ResourceLocation DEFAULT_FONT = new ResourceLocation("default");
    public static final ResourceLocation ALT_FONT = new ResourceLocation("alt");
    private static final CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK = CompletableFuture.completedFuture(Unit.INSTANCE);
    private final File resourcePackDirectory;
    private final PropertyMap profileProperties;
    private final TextureManager textureManager;
    private final DataFixer fixerUpper;
    private final VirtualScreen virtualScreen;
    private final Window window;
    private final Timer timer = new Timer(20.0F, 0L);
    private final Snooper snooper = new Snooper("client", this, Util.getMillis());
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
    private final ReloadableResourceManager resourceManager;
    private final ClientPackSource clientPackSource;
    private final PackRepository<UnopenedResourcePack> resourcePackRepository;
    private final LanguageManager languageManager;
    private final BlockColors blockColors;
    private final ItemColors itemColors;
    private final RenderTarget mainRenderTarget;
    private final SoundManager soundManager;
    private final MusicManager musicManager;
    private final FontManager fontManager;
    private final SplashManager splashManager;
    private final MinecraftSessionService minecraftSessionService;
    private final SkinManager skinManager;
    private final ModelManager modelManager;
    private final BlockRenderDispatcher blockRenderer;
    private final PaintingTextureManager paintingTextures;
    private final MobEffectTextureManager mobEffectTextures;
    private final ToastComponent toast;
    private final Game game = new Game(this);
    private final Tutorial tutorial;
    public static byte[] reserve = new byte[10485760];
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
    private boolean pause;
    private float pausePartialTick;
    private long lastNanoTime = Util.getNanos();
    private long lastTime;
    private int frames;
    public boolean noRender;
    @Nullable
    public Screen screen;
    @Nullable
    public Overlay overlay;
    private boolean connectedToRealms;
    private Thread gameThread;
    private volatile boolean running = true;
    @Nullable
    private CrashReport delayedCrash;
    private static int fps;
    public String fpsString = "";
    public boolean chunkPath;
    public boolean chunkVisibility;
    public boolean smartCull = true;
    private boolean windowActive;
    private final Queue<Runnable> progressTasks = Queues.newConcurrentLinkedQueue();
    @Nullable
    private CompletableFuture<Void> pendingReload;
    private ProfilerFiller profiler = InactiveProfiler.INSTANCE;
    private int fpsPieRenderTicks;
    private final ContinuousProfiler fpsPieProfiler = new ContinuousProfiler(Util.timeSource, () -> this.fpsPieRenderTicks);
    @Nullable
    private ProfileResults fpsPieResults;
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
        this.resourcePackRepository = new PackRepository<>(Minecraft::createClientPackAdapter);
        this.resourcePackRepository.addSource(this.clientPackSource);
        this.resourcePackRepository.addSource(new FolderRepositorySource(this.resourcePackDirectory));
        this.proxy = param0.user.proxy;
        this.minecraftSessionService = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString()).createMinecraftSessionService();
        this.user = param0.user.user;
        LOGGER.info("Setting user: {}", this.user.getName());
        LOGGER.debug("(Session ID is {})", this.user.getSessionId());
        this.demo = param0.game.demo;
        this.is64bit = checkIs64Bit();
        this.singleplayerServer = null;
        String var1;
        int var2;
        if (param0.server.hostname != null) {
            var1 = param0.server.hostname;
            var2 = param0.server.port;
        } else {
            var1 = null;
            var2 = 0;
        }

        Bootstrap.bootStrap();
        Bootstrap.validate();
        KeybindComponent.keyResolver = KeyMapping::createNameSupplier;
        this.fixerUpper = DataFixers.getDataFixer();
        this.toast = new ToastComponent(this);
        this.tutorial = new Tutorial(this);
        this.gameThread = Thread.currentThread();
        this.options = new Options(this, this.gameDirectory);
        this.hotbarManager = new HotbarManager(this.gameDirectory, this.fixerUpper);
        this.startTimerHackThread();
        LOGGER.info("Backend library: {}", RenderSystem.getBackendDescription());
        DisplayData var5;
        if (this.options.overrideHeight > 0 && this.options.overrideWidth > 0) {
            var5 = new DisplayData(
                this.options.overrideWidth,
                this.options.overrideHeight,
                param0.display.fullscreenWidth,
                param0.display.fullscreenHeight,
                param0.display.isFullscreen
            );
        } else {
            var5 = param0.display;
        }

        Util.timeSource = RenderSystem.initBackendSystem();
        this.virtualScreen = new VirtualScreen(this);
        this.window = this.virtualScreen.newWindow(var5, this.options.fullscreenVideoModeString, this.createTitle());
        this.setWindowActive(true);

        try {
            InputStream var7 = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_16x16.png"));
            InputStream var8 = this.getClientPackSource().getVanillaPack().getResource(PackType.CLIENT_RESOURCES, new ResourceLocation("icons/icon_32x32.png"));
            this.window.setIcon(var7, var8);
        } catch (IOException var8) {
            LOGGER.error("Couldn't set icon", (Throwable)var8);
        }

        this.window.setFramerateLimit(this.options.framerateLimit);
        this.mouseHandler = new MouseHandler(this);
        this.mouseHandler.setup(this.window.getWindow());
        this.keyboardHandler = new KeyboardHandler(this);
        this.keyboardHandler.setup(this.window.getWindow());
        RenderSystem.initRenderer(this.options.glDebugVerbosity, false);
        this.mainRenderTarget = new RenderTarget(this.window.getWidth(), this.window.getHeight(), true, ON_OSX);
        this.mainRenderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.resourceManager = new SimpleReloadableResourceManager(PackType.CLIENT_RESOURCES, this.gameThread);
        this.options.loadResourcePacks(this.resourcePackRepository);
        this.resourcePackRepository.reload();
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
        this.fontManager = new FontManager(this.textureManager, this.isEnforceUnicode());
        this.resourceManager.registerReloadListener(this.fontManager.getReloadListener());
        Font var10 = this.fontManager.get(DEFAULT_FONT);
        if (var10 == null) {
            throw new IllegalStateException("Default font is null");
        } else {
            this.font = var10;
            this.font.setBidirectional(this.languageManager.isBidirectional());
            this.resourceManager.registerReloadListener(new GrassColorReloadListener());
            this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
            this.window.setErrorSection("Startup");
            RenderSystem.setupDefaultState(0, 0, this.window.getWidth(), this.window.getHeight());
            this.window.setErrorSection("Post startup");
            this.blockColors = BlockColors.createDefault();
            this.itemColors = ItemColors.createDefault(this.blockColors);
            this.modelManager = new ModelManager(this.textureManager, this.blockColors, this.options.mipmapLevels);
            this.resourceManager.registerReloadListener(this.modelManager);
            this.itemRenderer = new ItemRenderer(this.textureManager, this.modelManager, this.itemColors);
            this.entityRenderDispatcher = new EntityRenderDispatcher(this.textureManager, this.itemRenderer, this.resourceManager, this.font, this.options);
            this.itemInHandRenderer = new ItemInHandRenderer(this);
            this.resourceManager.registerReloadListener(this.itemRenderer);
            this.renderBuffers = new RenderBuffers();
            this.gameRenderer = new GameRenderer(this, this.resourceManager, this.renderBuffers);
            this.resourceManager.registerReloadListener(this.gameRenderer);
            this.blockRenderer = new BlockRenderDispatcher(this.modelManager.getBlockModelShaper(), this.blockColors);
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
            this.gui = new Gui(this);
            this.debugRenderer = new DebugRenderer(this);
            RenderSystem.setErrorCallback(this::onFullscreenError);
            if (this.options.fullscreen && !this.window.isFullscreen()) {
                this.window.toggleFullScreen();
                this.options.fullscreen = this.window.isFullscreen();
            }

            this.window.updateVsync(this.options.enableVsync);
            this.window.updateRawMouseInput(this.options.rawMouseInput);
            this.window.setDefaultErrorCallback();
            this.resizeDisplay();
            if (var1 != null) {
                this.setScreen(new ConnectScreen(new TitleScreen(), this, var1, var2));
            } else {
                this.setScreen(new TitleScreen(true));
            }

            LoadingOverlay.registerTextures(this);
            List<Pack> var11 = this.resourcePackRepository.getSelected().stream().map(UnopenedPack::open).collect(Collectors.toList());
            this.setOverlay(
                new LoadingOverlay(
                    this,
                    this.resourceManager.createFullReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, var11),
                    param1 -> Util.ifElse(param1, this::rollbackResourcePacks, () -> {
                            this.languageManager.reload(var11);
                            if (SharedConstants.IS_RUNNING_IN_IDE) {
                                this.selfTest();
                            }
        
                        }),
                    false
                )
            );
        }
    }

    public void updateTitle() {
        this.window.setTitle(this.createTitle());
    }

    private String createTitle() {
        StringBuilder var0 = new StringBuilder("Minecraft");
        if (this.isProbablyModded()) {
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

    public boolean isProbablyModded() {
        return !"vanilla".equals(ClientBrandRetriever.getClientModName()) || Minecraft.class.getSigners() == null;
    }

    private void rollbackResourcePacks(Throwable param0) {
        if (this.resourcePackRepository.getSelected().size() > 1) {
            Component var0;
            if (param0 instanceof SimpleReloadableResourceManager.ResourcePackLoadingFailure) {
                var0 = new TextComponent(((SimpleReloadableResourceManager.ResourcePackLoadingFailure)param0).getPack().getName());
            } else {
                var0 = null;
            }

            LOGGER.info("Caught error loading resourcepacks, removing all selected resourcepacks", param0);
            this.resourcePackRepository.setSelected(Collections.emptyList());
            this.options.resourcePacks.clear();
            this.options.incompatibleResourcePacks.clear();
            this.options.save();
            this.reloadResourcePacks().thenRun(() -> {
                ToastComponent var0x = this.getToasts();
                SystemToast.addOrUpdate(var0x, SystemToast.SystemToastIds.PACK_LOAD_FAILURE, new TranslatableComponent("resourcePack.load_fail"), var0);
            });
        } else {
            Util.throwAsRuntime(param0);
        }

    }

    public void run() {
        this.gameThread = Thread.currentThread();

        try {
            boolean var0 = false;

            while(this.running) {
                if (this.delayedCrash != null) {
                    crash(this.delayedCrash);
                    return;
                }

                try {
                    SingleTickProfiler var1 = SingleTickProfiler.createTickProfiler("Renderer");
                    boolean var2 = this.shouldRenderFpsPie();
                    this.startProfilers(var2, var1);
                    this.profiler.startTick();
                    this.runTick(!var0);
                    this.profiler.endTick();
                    this.finishProfilers(var2, var1);
                } catch (OutOfMemoryError var4) {
                    if (var0) {
                        throw var4;
                    }

                    this.emergencySave();
                    this.setScreen(new OutOfMemoryScreen());
                    System.gc();
                    LOGGER.fatal("Out of memory", (Throwable)var4);
                    var0 = true;
                }
            }
        } catch (ReportedException var51) {
            this.fillReport(var51.getReport());
            this.emergencySave();
            LOGGER.fatal("Reported exception thrown!", (Throwable)var51);
            crash(var51.getReport());
        } catch (Throwable var61) {
            CrashReport var6 = this.fillReport(new CrashReport("Unexpected error", var61));
            LOGGER.fatal("Unreported exception thrown!", var61);
            this.emergencySave();
            crash(var6);
        }

    }

    private void createSearchTrees() {
        ReloadableSearchTree<ItemStack> var0 = new ReloadableSearchTree<>(
            param0 -> param0.getTooltipLines(null, TooltipFlag.Default.NORMAL)
                    .stream()
                    .map(param0x -> ChatFormatting.stripFormatting(param0x.getString()).trim())
                    .filter(param0x -> !param0x.isEmpty()),
            param0 -> Stream.of(Registry.ITEM.getKey(param0.getItem()))
        );
        ReloadableIdSearchTree<ItemStack> var1 = new ReloadableIdSearchTree<>(param0 -> ItemTags.getAllTags().getMatchingTags(param0.getItem()).stream());
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

    private void startTimerHackThread() {
        Thread var0 = new Thread("Timer hack thread") {
            @Override
            public void run() {
                while(Minecraft.this.running) {
                    try {
                        Thread.sleep(2147483647L);
                    } catch (InterruptedException var2) {
                    }
                }

            }
        };
        var0.setDaemon(true);
        var0.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        var0.start();
    }

    public void delayCrash(CrashReport param0) {
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
        if (this.pendingReload != null) {
            return this.pendingReload;
        } else {
            CompletableFuture<Void> var0 = new CompletableFuture<>();
            if (this.overlay instanceof LoadingOverlay) {
                this.pendingReload = var0;
                return var0;
            } else {
                this.resourcePackRepository.reload();
                List<Pack> var1 = this.resourcePackRepository.getSelected().stream().map(UnopenedPack::open).collect(Collectors.toList());
                this.setOverlay(
                    new LoadingOverlay(
                        this,
                        this.resourceManager.createFullReload(Util.backgroundExecutor(), this, RESOURCE_RELOAD_INITIAL_TASK, var1),
                        param2 -> Util.ifElse(param2, this::rollbackResourcePacks, () -> {
                                this.languageManager.reload(var1);
                                this.levelRenderer.allChanged();
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
        if (var0) {
            throw new IllegalStateException("Your game data is foobar, fix the errors above!");
        }
    }

    public LevelStorageSource getLevelSource() {
        return this.levelSource;
    }

    public void setScreen(@Nullable Screen param0) {
        if (this.screen != null) {
            this.screen.removed();
        }

        if (param0 == null && this.level == null) {
            param0 = new TitleScreen();
        } else if (param0 == null && this.player.getHealth() <= 0.0F) {
            if (this.player.shouldShowDeathScreen()) {
                param0 = new DeathScreen(null, this.level.getLevelData().isHardcore());
            } else {
                this.player.respawn();
            }
        }

        if (param0 instanceof TitleScreen || param0 instanceof JoinMultiplayerScreen) {
            this.options.renderDebug = false;
            this.gui.getChat().clearMessages(true);
        }

        this.screen = param0;
        if (param0 != null) {
            this.mouseHandler.releaseMouse();
            KeyMapping.releaseAll();
            param0.init(this, this.window.getGuiScaledWidth(), this.window.getGuiScaledHeight());
            this.noRender = false;
            NarratorChatListener.INSTANCE.sayNow(param0.getNarrationMessage());
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
        try {
            this.modelManager.close();
            this.fontManager.close();
            this.gameRenderer.close();
            this.levelRenderer.close();
            this.soundManager.destroy();
            this.resourcePackRepository.close();
            this.particleEngine.close();
            this.mobEffectTextures.close();
            this.paintingTextures.close();
            this.textureManager.close();
            Util.shutdownBackgroundExecutor();
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
        RenderSystem.pushMatrix();
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
            this.toast.render();
            this.profiler.pop();
        }

        if (this.fpsPieResults != null) {
            this.profiler.push("fpsPie");
            this.renderFpsMeter(this.fpsPieResults);
            this.profiler.pop();
        }

        this.profiler.push("blit");
        this.mainRenderTarget.unbindWrite();
        RenderSystem.popMatrix();
        RenderSystem.pushMatrix();
        this.mainRenderTarget.blitToScreen(this.window.getWidth(), this.window.getHeight());
        RenderSystem.popMatrix();
        this.profiler.popPush("updateDisplay");
        this.window.updateDisplay();
        int var5 = this.getFramerateLimit();
        if ((double)var5 < Option.FRAMERATE_LIMIT.getMaxValue()) {
            RenderSystem.limitDisplayFPS(var5);
        }

        this.profiler.popPush("yield");
        Thread.yield();
        this.profiler.pop();
        this.window.setErrorSection("Post render");
        ++this.frames;
        boolean var6 = this.hasSingleplayerServer()
            && (this.screen != null && this.screen.isPauseScreen() || this.overlay != null && this.overlay.isPauseScreen())
            && !this.singleplayerServer.isPublished();
        if (this.pause != var6) {
            if (this.pause) {
                this.pausePartialTick = this.timer.partialTick;
            } else {
                this.timer.partialTick = this.pausePartialTick;
            }

            this.pause = var6;
        }

        long var7 = Util.getNanos();
        this.frameTimer.logFrameDuration(var7 - this.lastNanoTime);
        this.lastNanoTime = var7;
        this.profiler.push("fpsUpdate");

        while(Util.getMillis() >= this.lastTime + 1000L) {
            fps = this.frames;
            this.fpsString = String.format(
                "%d fps T: %s%s%s%s B: %d",
                fps,
                (double)this.options.framerateLimit == Option.FRAMERATE_LIMIT.getMaxValue() ? "inf" : this.options.framerateLimit,
                this.options.enableVsync ? " vsync" : "",
                this.options.fancyGraphics ? "" : " fast",
                this.options.renderClouds == CloudStatus.OFF ? "" : (this.options.renderClouds == CloudStatus.FAST ? " fast-clouds" : " fancy-clouds"),
                this.options.biomeBlendRadius
            );
            this.lastTime += 1000L;
            this.frames = 0;
            this.snooper.prepare();
            if (!this.snooper.isStarted()) {
                this.snooper.start();
            }
        }

        this.profiler.pop();
    }

    private boolean shouldRenderFpsPie() {
        return this.options.renderDebug && this.options.renderDebugCharts && !this.options.hideGui;
    }

    private void startProfilers(boolean param0, @Nullable SingleTickProfiler param1) {
        if (param0) {
            if (!this.fpsPieProfiler.isEnabled()) {
                this.fpsPieRenderTicks = 0;
                this.fpsPieProfiler.enable();
            }

            ++this.fpsPieRenderTicks;
        } else {
            this.fpsPieProfiler.disable();
        }

        this.profiler = SingleTickProfiler.decorateFiller(this.fpsPieProfiler.getFiller(), param1);
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

    private int getFramerateLimit() {
        return this.level != null || this.screen == null && this.overlay == null ? this.window.getFramerateLimit() : 60;
    }

    public void emergencySave() {
        try {
            reserve = new byte[0];
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

    void debugFpsMeterKeyPress(int param0) {
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
                            this.debugPath = this.debugPath + '\u001e';
                        }

                        this.debugPath = this.debugPath + var0.get(param0).name;
                    }
                }

            }
        }
    }

    private void renderFpsMeter(ProfileResults param0) {
        List<ResultField> var0 = param0.getTimes(this.debugPath);
        ResultField var1 = var0.remove(0);
        RenderSystem.clear(256, ON_OSX);
        RenderSystem.matrixMode(5889);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0, (double)this.window.getWidth(), (double)this.window.getHeight(), 0.0, 1000.0, 3000.0);
        RenderSystem.matrixMode(5888);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
        RenderSystem.lineWidth(1.0F);
        RenderSystem.disableTexture();
        Tesselator var2 = Tesselator.getInstance();
        BufferBuilder var3 = var2.getBuilder();
        int var4 = 160;
        int var5 = this.window.getWidth() - 160 - 10;
        int var6 = this.window.getHeight() - 320;
        RenderSystem.enableBlend();
        var3.begin(7, DefaultVertexFormat.POSITION_COLOR);
        var3.vertex((double)((float)var5 - 176.0F), (double)((float)var6 - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
        var3.vertex((double)((float)var5 - 176.0F), (double)(var6 + 320), 0.0).color(200, 0, 0, 0).endVertex();
        var3.vertex((double)((float)var5 + 176.0F), (double)(var6 + 320), 0.0).color(200, 0, 0, 0).endVertex();
        var3.vertex((double)((float)var5 + 176.0F), (double)((float)var6 - 96.0F - 16.0F), 0.0).color(200, 0, 0, 0).endVertex();
        var2.end();
        RenderSystem.disableBlend();
        double var7 = 0.0;

        for(ResultField var8 : var0) {
            int var9 = Mth.floor(var8.percentage / 4.0) + 1;
            var3.begin(6, DefaultVertexFormat.POSITION_COLOR);
            int var10 = var8.getColor();
            int var11 = var10 >> 16 & 0xFF;
            int var12 = var10 >> 8 & 0xFF;
            int var13 = var10 & 0xFF;
            var3.vertex((double)var5, (double)var6, 0.0).color(var11, var12, var13, 255).endVertex();

            for(int var14 = var9; var14 >= 0; --var14) {
                float var15 = (float)((var7 + var8.percentage * (double)var14 / (double)var9) * (float) (Math.PI * 2) / 100.0);
                float var16 = Mth.sin(var15) * 160.0F;
                float var17 = Mth.cos(var15) * 160.0F * 0.5F;
                var3.vertex((double)((float)var5 + var16), (double)((float)var6 - var17), 0.0).color(var11, var12, var13, 255).endVertex();
            }

            var2.end();
            var3.begin(5, DefaultVertexFormat.POSITION_COLOR);

            for(int var18 = var9; var18 >= 0; --var18) {
                float var19 = (float)((var7 + var8.percentage * (double)var18 / (double)var9) * (float) (Math.PI * 2) / 100.0);
                float var20 = Mth.sin(var19) * 160.0F;
                float var21 = Mth.cos(var19) * 160.0F * 0.5F;
                if (!(var21 > 0.0F)) {
                    var3.vertex((double)((float)var5 + var20), (double)((float)var6 - var21), 0.0).color(var11 >> 1, var12 >> 1, var13 >> 1, 255).endVertex();
                    var3.vertex((double)((float)var5 + var20), (double)((float)var6 - var21 + 10.0F), 0.0)
                        .color(var11 >> 1, var12 >> 1, var13 >> 1, 255)
                        .endVertex();
                }
            }

            var2.end();
            var7 += var8.percentage;
        }

        DecimalFormat var22 = new DecimalFormat("##0.00");
        var22.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        RenderSystem.enableTexture();
        String var23 = ProfileResults.demanglePath(var1.name);
        String var24 = "";
        if (!"unspecified".equals(var23)) {
            var24 = var24 + "[0] ";
        }

        if (var23.isEmpty()) {
            var24 = var24 + "ROOT ";
        } else {
            var24 = var24 + var23 + ' ';
        }

        int var25 = 16777215;
        this.font.drawShadow(var24, (float)(var5 - 160), (float)(var6 - 80 - 16), 16777215);
        var24 = var22.format(var1.globalPercentage) + "%";
        this.font.drawShadow(var24, (float)(var5 + 160 - this.font.width(var24)), (float)(var6 - 80 - 16), 16777215);

        for(int var26 = 0; var26 < var0.size(); ++var26) {
            ResultField var27 = var0.get(var26);
            StringBuilder var28 = new StringBuilder();
            if ("unspecified".equals(var27.name)) {
                var28.append("[?] ");
            } else {
                var28.append("[").append(var26 + 1).append("] ");
            }

            String var29 = var28.append(var27.name).toString();
            this.font.drawShadow(var29, (float)(var5 - 160), (float)(var6 + 80 + var26 * 8 + 20), var27.getColor());
            var29 = var22.format(var27.percentage) + "%";
            this.font.drawShadow(var29, (float)(var5 + 160 - 50 - this.font.width(var29)), (float)(var6 + 80 + var26 * 8 + 20), var27.getColor());
            var29 = var22.format(var27.globalPercentage) + "%";
            this.font.drawShadow(var29, (float)(var5 + 160 - this.font.width(var29)), (float)(var6 + 80 + var26 * 8 + 20), var27.getColor());
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

    private void startAttack() {
        if (this.missTime <= 0) {
            if (this.hitResult == null) {
                LOGGER.error("Null returned as 'hitResult', this shouldn't happen!");
                if (this.gameMode.hasMissTime()) {
                    this.missTime = 10;
                }

            } else if (!this.player.isHandsBusy()) {
                switch(this.hitResult.getType()) {
                    case ENTITY:
                        this.gameMode.attack(this.player, ((EntityHitResult)this.hitResult).getEntity());
                        break;
                    case BLOCK:
                        BlockHitResult var0 = (BlockHitResult)this.hitResult;
                        BlockPos var1 = var0.getBlockPos();
                        if (!this.level.getBlockState(var1).isAir()) {
                            this.gameMode.startDestroyBlock(var1, var0.getDirection());
                            break;
                        }
                    case MISS:
                        if (this.gameMode.hasMissTime()) {
                            this.missTime = 10;
                        }

                        this.player.resetAttackStrengthTicker();
                }

                this.player.swing(InteractionHand.MAIN_HAND);
            }
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
                                InteractionResult var7 = this.gameMode.useItemOn(this.player, this.level, var0, var5);
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
                        InteractionResult var8 = this.gameMode.useItem(this.player, this.level, var0);
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
        if (!this.pause) {
            this.gui.tick();
        }

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

        if (this.screen == null && this.player != null) {
            if (this.player.getHealth() <= 0.0F && !(this.screen instanceof DeathScreen)) {
                this.setScreen(null);
            } else if (this.player.isSleeping() && this.level != null) {
                this.setScreen(new InBedChatScreen());
            }
        } else if (this.screen != null && this.screen instanceof InBedChatScreen && !this.player.isSleeping()) {
            this.setScreen(null);
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
                this.level.setSpawnSettings(this.level.getDifficulty() != Difficulty.PEACEFUL, true);
                this.tutorial.tick();

                try {
                    this.level.tick(() -> true);
                } catch (Throwable var4) {
                    CrashReport var1 = CrashReport.forThrowable(var4, "Exception in world tick");
                    if (this.level == null) {
                        CrashReportCategory var2 = var1.addCategory("Affected level");
                        var2.setDetail("Problem", "Level is null!");
                    } else {
                        this.level.fillReportDetails(var1);
                    }

                    throw new ReportedException(var1);
                }
            }

            this.profiler.popPush("animateTick");
            if (!this.pause && this.level != null) {
                this.level.animateTick(Mth.floor(this.player.getX()), Mth.floor(this.player.getY()), Mth.floor(this.player.getZ()));
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

    private void handleKeybinds() {
        for(; this.options.keyTogglePerspective.consumeClick(); this.levelRenderer.needsUpdate()) {
            ++this.options.thirdPersonView;
            if (this.options.thirdPersonView > 2) {
                this.options.thirdPersonView = 0;
            }

            if (this.options.thirdPersonView == 0) {
                this.gameRenderer.checkEntityPostEffect(this.getCameraEntity());
            } else if (this.options.thirdPersonView == 1) {
                this.gameRenderer.checkEntityPostEffect(null);
            }
        }

        while(this.options.keySmoothCamera.consumeClick()) {
            this.options.smoothCamera = !this.options.smoothCamera;
        }

        for(int var0 = 0; var0 < 9; ++var0) {
            boolean var1 = this.options.keySaveHotbarActivator.isDown();
            boolean var2 = this.options.keyLoadHotbarActivator.isDown();
            if (this.options.keyHotbarSlots[var0].consumeClick()) {
                if (this.player.isSpectator()) {
                    this.gui.getSpectatorGui().onHotbarSelected(var0);
                } else if (!this.player.isCreative() || this.screen != null || !var2 && !var1) {
                    this.player.inventory.selected = var0;
                } else {
                    CreativeModeInventoryScreen.handleHotbarLoadOrSave(this, var0, var2, var1);
                }
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

        while(this.options.keySwapHands.consumeClick()) {
            if (!this.player.isSpectator()) {
                this.getConnection()
                    .send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.SWAP_HELD_ITEMS, BlockPos.ZERO, Direction.DOWN));
            }
        }

        while(this.options.keyDrop.consumeClick()) {
            if (!this.player.isSpectator() && this.player.drop(Screen.hasControlDown())) {
                this.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        boolean var3 = this.options.chatVisibility != ChatVisiblity.HIDDEN;
        if (var3) {
            while(this.options.keyChat.consumeClick()) {
                this.setScreen(new ChatScreen(""));
            }

            if (this.screen == null && this.overlay == null && this.options.keyCommand.consumeClick()) {
                this.setScreen(new ChatScreen("/"));
            }
        }

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
                this.startAttack();
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

        this.continueAttack(this.screen == null && this.options.keyAttack.isDown() && this.mouseHandler.isMouseGrabbed());
    }

    public void selectLevel(String param0, String param1, @Nullable LevelSettings param2) {
        this.clearLevel();
        LevelStorage var0 = this.levelSource.selectLevel(param0, null);
        LevelData var1 = var0.prepareLevel();
        if (var1 == null && param2 != null) {
            var1 = new LevelData(param2, param0);
            var0.saveLevelData(var1);
        }

        if (param2 == null) {
            param2 = new LevelSettings(var1);
        }

        this.progressListener.set(null);

        try {
            YggdrasilAuthenticationService var2 = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString());
            MinecraftSessionService var3 = var2.createMinecraftSessionService();
            GameProfileRepository var4 = var2.createProfileRepository();
            GameProfileCache var5 = new GameProfileCache(var4, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            SkullBlockEntity.setProfileCache(var5);
            SkullBlockEntity.setSessionService(var3);
            GameProfileCache.setUsesAuthentication(false);
            this.singleplayerServer = new IntegratedServer(this, param0, param1, param2, var2, var3, var4, var5, param0x -> {
                StoringChunkProgressListener var0x = new StoringChunkProgressListener(param0x + 0);
                var0x.start();
                this.progressListener.set(var0x);
                return new ProcessorChunkProgressListener(var0x, this.progressTasks::add);
            });
            this.singleplayerServer.forkAndRun();
            this.isLocalServer = true;
        } catch (Throwable var111) {
            CrashReport var7 = CrashReport.forThrowable(var111, "Starting integrated server");
            CrashReportCategory var8 = var7.addCategory("Starting integrated server");
            var8.setDetail("Level ID", param0);
            var8.setDetail("Level Name", param1);
            throw new ReportedException(var7);
        }

        while(this.progressListener.get() == null) {
            Thread.yield();
        }

        LevelLoadingScreen var9 = new LevelLoadingScreen(this.progressListener.get());
        this.setScreen(var9);
        this.profiler.push("waitForServer");

        while(!this.singleplayerServer.isReady()) {
            var9.tick();
            this.runTick(false);

            try {
                Thread.sleep(16L);
            } catch (InterruptedException var101) {
            }

            if (this.delayedCrash != null) {
                crash(this.delayedCrash);
                return;
            }
        }

        this.profiler.pop();
        SocketAddress var10 = this.singleplayerServer.getConnection().startMemoryChannel();
        Connection var11 = Connection.connectToLocalServer(var10);
        var11.setListener(new ClientHandshakePacketListenerImpl(var11, this, null, param0x -> {
        }));
        var11.send(new ClientIntentionPacket(var10.toString(), 0, ConnectionProtocol.LOGIN));
        var11.send(new ServerboundHelloPacket(this.getUser().getGameProfile()));
        this.pendingConnection = var11;
    }

    public void setLevel(ClientLevel param0) {
        ProgressScreen var0 = new ProgressScreen();
        var0.progressStartNoAbort(new TranslatableComponent("connect.joining"));
        this.updateScreenAndTick(var0);
        this.level = param0;
        this.updateLevelInEngines(param0);
        if (!this.isLocalServer) {
            AuthenticationService var1 = new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString());
            MinecraftSessionService var2 = var1.createMinecraftSessionService();
            GameProfileRepository var3 = var1.createProfileRepository();
            GameProfileCache var4 = new GameProfileCache(var3, new File(this.gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
            SkullBlockEntity.setProfileCache(var4);
            SkullBlockEntity.setSessionService(var2);
            GameProfileCache.setUsesAuthentication(false);
        }

    }

    public void clearLevel() {
        this.clearLevel(new ProgressScreen());
    }

    public void clearLevel(Screen param0) {
        ClientPacketListener var0 = this.getConnection();
        if (var0 != null) {
            this.dropAllTasks();
            var0.cleanup();
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
    }

    private void updateScreenAndTick(Screen param0) {
        this.profiler.push("forcedTick");
        this.musicManager.stopPlaying();
        this.soundManager.stop();
        this.cameraEntity = null;
        this.pendingConnection = null;
        this.setScreen(param0);
        this.runTick(false);
        this.profiler.pop();
    }

    private void updateLevelInEngines(@Nullable ClientLevel param0) {
        this.levelRenderer.setLevel(param0);
        this.particleEngine.setLevel(param0);
        BlockEntityRenderDispatcher.instance.setLevel(param0);
        this.updateTitle();
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
        return instance.options.fancyGraphics;
    }

    public static boolean useAmbientOcclusion() {
        return instance.options.ambientOcclusion != AmbientOcclusionStatus.OFF;
    }

    private void pickBlock() {
        if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
            boolean var0 = this.player.abilities.instabuild;
            BlockEntity var1 = null;
            HitResult.Type var2 = this.hitResult.getType();
            ItemStack var6;
            if (var2 == HitResult.Type.BLOCK) {
                BlockPos var3 = ((BlockHitResult)this.hitResult).getBlockPos();
                BlockState var4 = this.level.getBlockState(var3);
                Block var5 = var4.getBlock();
                if (var4.isAir()) {
                    return;
                }

                var6 = var5.getCloneItemStack(this.level, var3, var4);
                if (var6.isEmpty()) {
                    return;
                }

                if (var0 && Screen.hasControlDown() && var5.isEntityBlock()) {
                    var1 = this.level.getBlockEntity(var3);
                }
            } else {
                if (var2 != HitResult.Type.ENTITY || !var0) {
                    return;
                }

                Entity var7 = ((EntityHitResult)this.hitResult).getEntity();
                if (var7 instanceof Painting) {
                    var6 = new ItemStack(Items.PAINTING);
                } else if (var7 instanceof LeashFenceKnotEntity) {
                    var6 = new ItemStack(Items.LEAD);
                } else if (var7 instanceof ItemFrame) {
                    ItemFrame var10 = (ItemFrame)var7;
                    ItemStack var11 = var10.getItem();
                    if (var11.isEmpty()) {
                        var6 = new ItemStack(Items.ITEM_FRAME);
                    } else {
                        var6 = var11.copy();
                    }
                } else if (var7 instanceof AbstractMinecart) {
                    AbstractMinecart var14 = (AbstractMinecart)var7;
                    Item var15;
                    switch(var14.getMinecartType()) {
                        case FURNACE:
                            var15 = Items.FURNACE_MINECART;
                            break;
                        case CHEST:
                            var15 = Items.CHEST_MINECART;
                            break;
                        case TNT:
                            var15 = Items.TNT_MINECART;
                            break;
                        case HOPPER:
                            var15 = Items.HOPPER_MINECART;
                            break;
                        case COMMAND_BLOCK:
                            var15 = Items.COMMAND_BLOCK_MINECART;
                            break;
                        default:
                            var15 = Items.MINECART;
                    }

                    var6 = new ItemStack(var15);
                } else if (var7 instanceof Boat) {
                    var6 = new ItemStack(((Boat)var7).getDropItem());
                } else if (var7 instanceof ArmorStand) {
                    var6 = new ItemStack(Items.ARMOR_STAND);
                } else if (var7 instanceof EndCrystal) {
                    var6 = new ItemStack(Items.END_CRYSTAL);
                } else {
                    SpawnEggItem var25 = SpawnEggItem.byId(var7.getType());
                    if (var25 == null) {
                        return;
                    }

                    var6 = new ItemStack(var25);
                }
            }

            if (var6.isEmpty()) {
                String var28 = "";
                if (var2 == HitResult.Type.BLOCK) {
                    var28 = Registry.BLOCK.getKey(this.level.getBlockState(((BlockHitResult)this.hitResult).getBlockPos()).getBlock()).toString();
                } else if (var2 == HitResult.Type.ENTITY) {
                    var28 = Registry.ENTITY_TYPE.getKey(((EntityHitResult)this.hitResult).getEntity().getType()).toString();
                }

                LOGGER.warn("Picking on: [{}] {} gave null item", var2, var28);
            } else {
                Inventory var29 = this.player.inventory;
                if (var1 != null) {
                    this.addCustomNbtData(var6, var1);
                }

                int var30 = var29.findSlotMatchingItem(var6);
                if (var0) {
                    var29.setPickedItem(var6);
                    this.gameMode.handleCreativeModeItemAdd(this.player.getItemInHand(InteractionHand.MAIN_HAND), 36 + var29.selected);
                } else if (var30 != -1) {
                    if (Inventory.isHotbarSlot(var30)) {
                        var29.selected = var30;
                    } else {
                        this.gameMode.handlePickItem(var30);
                    }
                }

            }
        }
    }

    private ItemStack addCustomNbtData(ItemStack param0, BlockEntity param1) {
        CompoundTag var0 = param1.save(new CompoundTag());
        if (param0.getItem() instanceof PlayerHeadItem && var0.contains("Owner")) {
            CompoundTag var1 = var0.getCompound("Owner");
            param0.getOrCreateTag().put("SkullOwner", var1);
            return param0;
        } else {
            param0.addTagElement("BlockEntityTag", var0);
            CompoundTag var2 = new CompoundTag();
            ListTag var3 = new ListTag();
            var3.add(StringTag.valueOf("\"(+NBT)\""));
            var2.put("Lore", var3);
            param0.addTagElement("display", var2);
            return param0;
        }
    }

    public CrashReport fillReport(CrashReport param0) {
        fillReport(this.languageManager, this.launchedVersion, this.options, param0);
        if (this.level != null) {
            this.level.fillReportDetails(param0);
        }

        return param0;
    }

    public static void fillReport(@Nullable LanguageManager param0, String param1, @Nullable Options param2, CrashReport param3) {
        CrashReportCategory var0 = param3.getSystemDetails();
        var0.setDetail("Launched Version", () -> param1);
        var0.setDetail("Backend library", RenderSystem::getBackendDescription);
        var0.setDetail("Backend API", RenderSystem::getApiDescription);
        var0.setDetail("GL Caps", RenderSystem::getCapsString);
        var0.setDetail("Using VBOs", () -> "Yes");
        var0.setDetail(
            "Is Modded",
            () -> {
                String var0x = ClientBrandRetriever.getClientModName();
                if (!"vanilla".equals(var0x)) {
                    return "Definitely; Client brand changed to '" + var0x + "'";
                } else {
                    return Minecraft.class.getSigners() == null
                        ? "Very likely; Jar signature invalidated"
                        : "Probably not. Jar signature remains and client brand is untouched.";
                }
            }
        );
        var0.setDetail("Type", "Client (map_client.txt)");
        if (param2 != null) {
            var0.setDetail("Resource Packs", () -> {
                StringBuilder var0x = new StringBuilder();

                for(String var1x : param2.resourcePacks) {
                    if (var0x.length() > 0) {
                        var0x.append(", ");
                    }

                    var0x.append(var1x);
                    if (param2.incompatibleResourcePacks.contains(var1x)) {
                        var0x.append(" (incompatible)");
                    }
                }

                return var0x.toString();
            });
        }

        if (param0 != null) {
            var0.setDetail("Current Language", () -> param0.getSelected().toString());
        }

        var0.setDetail("CPU", GlUtil::getCpuInfo);
    }

    public static Minecraft getInstance() {
        return instance;
    }

    public CompletableFuture<Void> delayTextureReload() {
        return this.submit(this::reloadResourcePacks).thenCompose(param0 -> param0);
    }

    @Override
    public void populateSnooper(Snooper param0) {
        param0.setDynamicData("fps", fps);
        param0.setDynamicData("vsync_enabled", this.options.enableVsync);
        param0.setDynamicData("display_frequency", this.window.getRefreshRate());
        param0.setDynamicData("display_type", this.window.isFullscreen() ? "fullscreen" : "windowed");
        param0.setDynamicData("run_time", (Util.getMillis() - param0.getStartupTime()) / 60L * 1000L);
        param0.setDynamicData("current_action", this.getCurrentSnooperAction());
        param0.setDynamicData("language", this.options.languageCode == null ? "en_us" : this.options.languageCode);
        String var0 = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "little" : "big";
        param0.setDynamicData("endianness", var0);
        param0.setDynamicData("subtitles", this.options.showSubtitles);
        param0.setDynamicData("touch", this.options.touchscreen ? "touch" : "mouse");
        int var1 = 0;

        for(UnopenedResourcePack var2 : this.resourcePackRepository.getSelected()) {
            if (!var2.isRequired() && !var2.isFixedPosition()) {
                param0.setDynamicData("resource_pack[" + var1++ + "]", var2.getId());
            }
        }

        param0.setDynamicData("resource_packs", var1);
        if (this.singleplayerServer != null) {
            param0.setDynamicData("snooper_partner", this.singleplayerServer.getSnooper().getToken());
        }

    }

    private String getCurrentSnooperAction() {
        if (this.singleplayerServer != null) {
            return this.singleplayerServer.isPublished() ? "hosting_lan" : "singleplayer";
        } else if (this.currentServer != null) {
            return this.currentServer.isLan() ? "playing_lan" : "multiplayer";
        } else {
            return "out_of_game";
        }
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

    public Snooper getSnooper() {
        return this.snooper;
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

    public PackRepository<UnopenedResourcePack> getResourcePackRepository() {
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

    public SoundManager getSoundManager() {
        return this.soundManager;
    }

    public MusicManager.Music getSituationalMusic() {
        if (this.screen instanceof WinScreen) {
            return MusicManager.Music.CREDITS;
        } else if (this.player == null) {
            return MusicManager.Music.MENU;
        } else if (this.player.level.dimension instanceof NetherDimension) {
            return MusicManager.Music.NETHER;
        } else if (this.player.level.dimension instanceof TheEndDimension) {
            return this.gui.getBossOverlay().shouldPlayMusic() ? MusicManager.Music.END_BOSS : MusicManager.Music.END;
        } else {
            Biome.BiomeCategory var0 = this.player.level.getBiome(this.player.blockPosition()).getBiomeCategory();
            if (!this.musicManager.isPlayingMusic(MusicManager.Music.UNDER_WATER)
                && (
                    !this.player.isUnderWater()
                        || this.musicManager.isPlayingMusic(MusicManager.Music.GAME)
                        || var0 != Biome.BiomeCategory.OCEAN && var0 != Biome.BiomeCategory.RIVER
                )) {
                return this.player.abilities.instabuild && this.player.abilities.mayfly ? MusicManager.Music.CREATIVE : MusicManager.Music.GAME;
            } else {
                return MusicManager.Music.UNDER_WATER;
            }
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

    public FontManager getFontManager() {
        return this.fontManager;
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

    public ProfilerFiller getProfiler() {
        return this.profiler;
    }

    public Game getGame() {
        return this.game;
    }

    public SplashManager getSplashManager() {
        return this.splashManager;
    }

    @Nullable
    public Overlay getOverlay() {
        return this.overlay;
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

    private static UnopenedResourcePack createClientPackAdapter(
        String param0x, boolean param1, Supplier<Pack> param2, Pack param3, PackMetadataSection param4, UnopenedPack.Position param5
    ) {
        int var0x = param4.getPackFormat();
        Supplier<Pack> var1x = param2;
        if (var0x <= 3) {
            var1x = adaptV3(param2);
        }

        if (var0x <= 4) {
            var1x = adaptV4(var1x);
        }

        return new UnopenedResourcePack(param0x, param1, var1x, param3, param4, param5);
    }

    private static Supplier<Pack> adaptV3(Supplier<Pack> param0) {
        return () -> new LegacyResourcePackAdapter(param0.get(), LegacyResourcePackAdapter.V3);
    }

    private static Supplier<Pack> adaptV4(Supplier<Pack> param0) {
        return () -> new PackAdapterV4(param0.get());
    }

    public void updateMaxMipLevel(int param0) {
        this.modelManager.updateMaxMipLevel(param0);
    }
}
