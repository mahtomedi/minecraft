package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Options {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>() {
    };
    public static final int RENDER_DISTANCE_TINY = 2;
    public static final int RENDER_DISTANCE_SHORT = 4;
    public static final int RENDER_DISTANCE_NORMAL = 8;
    public static final int RENDER_DISTANCE_FAR = 12;
    public static final int RENDER_DISTANCE_REALLY_FAR = 16;
    public static final int RENDER_DISTANCE_EXTREME = 32;
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    private static final float DEFAULT_VOLUME = 1.0F;
    public static final String DEFAULT_SOUND_DEVICE = "";
    public boolean darkMojangStudiosBackground;
    public boolean hideLightningFlashes;
    public double sensitivity = 0.5;
    public int renderDistance;
    public int simulationDistance;
    private int serverRenderDistance = 0;
    public float entityDistanceScaling = 1.0F;
    public int framerateLimit = 120;
    public CloudStatus renderClouds = CloudStatus.FANCY;
    public GraphicsStatus graphicsMode = GraphicsStatus.FANCY;
    public AmbientOcclusionStatus ambientOcclusion = AmbientOcclusionStatus.MAX;
    public PrioritizeChunkUpdates prioritizeChunkUpdates = PrioritizeChunkUpdates.NONE;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public ChatVisiblity chatVisibility = ChatVisiblity.FULL;
    public double chatOpacity = 1.0;
    public double chatLineSpacing;
    public double textBackgroundOpacity = 0.5;
    @Nullable
    public String fullscreenVideoModeString;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
    public HumanoidArm mainHand = HumanoidArm.RIGHT;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public double chatScale = 1.0;
    public double chatWidth = 1.0;
    public double chatHeightUnfocused = 0.44366196F;
    public double chatHeightFocused = 1.0;
    public double chatDelay;
    public int mipmapLevels = 4;
    private final Object2FloatMap<SoundSource> sourceVolumes = Util.make(new Object2FloatOpenHashMap<>(), param0x -> param0x.defaultReturnValue(1.0F));
    public boolean useNativeTransport = true;
    public AttackIndicatorStatus attackIndicator = AttackIndicatorStatus.CROSSHAIR;
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public boolean joinedFirstServer = false;
    public boolean hideBundleTutorial = false;
    public int biomeBlendRadius = 2;
    public double mouseWheelSensitivity = 1.0;
    public boolean rawMouseInput = true;
    public int glDebugVerbosity = 1;
    public boolean autoJump = true;
    public boolean autoSuggestions = true;
    public boolean chatColors = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public boolean enableVsync = true;
    public boolean entityShadows = true;
    public boolean forceUnicodeFont;
    public boolean invertYMouse;
    public boolean discreteMouseScroll;
    public boolean realmsNotifications = true;
    public boolean allowServerListing = true;
    public boolean reducedDebugInfo;
    public boolean showSubtitles;
    public boolean backgroundForChatOnly = true;
    public boolean touchscreen;
    public boolean fullscreen;
    public boolean bobView = true;
    public boolean toggleCrouch;
    public boolean toggleSprint;
    public boolean skipMultiplayerWarning;
    public boolean hideMatchedNames = true;
    public boolean showAutosaveIndicator = true;
    public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
    public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
    public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
    public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
    public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
    public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", () -> this.toggleCrouch);
    public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", () -> this.toggleSprint);
    public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
    public final KeyMapping keySwapOffhand = new KeyMapping("key.swapOffhand", 70, "key.categories.inventory");
    public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
    public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
    public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
    public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
    public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
    public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
    public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
    public final KeyMapping keySocialInteractions = new KeyMapping("key.socialInteractions", 80, "key.categories.multiplayer");
    public final KeyMapping keyScreenshot = new KeyMapping("key.screenshot", 291, "key.categories.misc");
    public final KeyMapping keyTogglePerspective = new KeyMapping("key.togglePerspective", 294, "key.categories.misc");
    public final KeyMapping keySmoothCamera = new KeyMapping("key.smoothCamera", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyFullscreen = new KeyMapping("key.fullscreen", 300, "key.categories.misc");
    public final KeyMapping keySpectatorOutlines = new KeyMapping("key.spectatorOutlines", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyAdvancements = new KeyMapping("key.advancements", 76, "key.categories.misc");
    public final KeyMapping[] keyHotbarSlots = new KeyMapping[]{
        new KeyMapping("key.hotbar.1", 49, "key.categories.inventory"),
        new KeyMapping("key.hotbar.2", 50, "key.categories.inventory"),
        new KeyMapping("key.hotbar.3", 51, "key.categories.inventory"),
        new KeyMapping("key.hotbar.4", 52, "key.categories.inventory"),
        new KeyMapping("key.hotbar.5", 53, "key.categories.inventory"),
        new KeyMapping("key.hotbar.6", 54, "key.categories.inventory"),
        new KeyMapping("key.hotbar.7", 55, "key.categories.inventory"),
        new KeyMapping("key.hotbar.8", 56, "key.categories.inventory"),
        new KeyMapping("key.hotbar.9", 57, "key.categories.inventory")
    };
    public final KeyMapping keySaveHotbarActivator = new KeyMapping("key.saveToolbarActivator", 67, "key.categories.creative");
    public final KeyMapping keyLoadHotbarActivator = new KeyMapping("key.loadToolbarActivator", 88, "key.categories.creative");
    public final KeyMapping[] keyMappings = ArrayUtils.addAll(
        (KeyMapping[])(new KeyMapping[]{
            this.keyAttack,
            this.keyUse,
            this.keyUp,
            this.keyLeft,
            this.keyDown,
            this.keyRight,
            this.keyJump,
            this.keyShift,
            this.keySprint,
            this.keyDrop,
            this.keyInventory,
            this.keyChat,
            this.keyPlayerList,
            this.keyPickItem,
            this.keyCommand,
            this.keySocialInteractions,
            this.keyScreenshot,
            this.keyTogglePerspective,
            this.keySmoothCamera,
            this.keyFullscreen,
            this.keySpectatorOutlines,
            this.keySwapOffhand,
            this.keySaveHotbarActivator,
            this.keyLoadHotbarActivator,
            this.keyAdvancements
        }),
        (KeyMapping[])this.keyHotbarSlots
    );
    protected Minecraft minecraft;
    private final File optionsFile;
    public Difficulty difficulty = Difficulty.NORMAL;
    public boolean hideGui;
    private CameraType cameraType = CameraType.FIRST_PERSON;
    public boolean renderDebug;
    public boolean renderDebugCharts;
    public boolean renderFpsChart;
    public String lastMpIp = "";
    public boolean smoothCamera;
    public double fov = 70.0;
    public float screenEffectScale = 1.0F;
    public float fovEffectScale = 1.0F;
    public double gamma;
    public int guiScale;
    public ParticleStatus particles = ParticleStatus.ALL;
    public NarratorStatus narratorStatus = NarratorStatus.OFF;
    public String languageCode = "en_us";
    public String soundDevice = "";
    public boolean syncWrites;

    public Options(Minecraft param0, File param1) {
        this.minecraft = param0;
        this.optionsFile = new File(param1, "options.txt");
        if (param0.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            Option.RENDER_DISTANCE.setMaxValue(32.0F);
            Option.SIMULATION_DISTANCE.setMaxValue(32.0F);
        } else {
            Option.RENDER_DISTANCE.setMaxValue(16.0F);
            Option.SIMULATION_DISTANCE.setMaxValue(16.0F);
        }

        this.renderDistance = param0.is64Bit() ? 12 : 8;
        this.simulationDistance = param0.is64Bit() ? 12 : 8;
        this.gamma = 0.5;
        this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
        this.load();
    }

    public float getBackgroundOpacity(float param0) {
        return this.backgroundForChatOnly ? param0 : (float)this.textBackgroundOpacity;
    }

    public int getBackgroundColor(float param0) {
        return (int)(this.getBackgroundOpacity(param0) * 255.0F) << 24 & 0xFF000000;
    }

    public int getBackgroundColor(int param0) {
        return this.backgroundForChatOnly ? param0 : (int)(this.textBackgroundOpacity * 255.0) << 24 & 0xFF000000;
    }

    public void setKey(KeyMapping param0, InputConstants.Key param1) {
        param0.setKey(param1);
        this.save();
    }

    private void processOptions(Options.FieldAccess param0) {
        this.autoJump = param0.process("autoJump", this.autoJump);
        this.autoSuggestions = param0.process("autoSuggestions", this.autoSuggestions);
        this.chatColors = param0.process("chatColors", this.chatColors);
        this.chatLinks = param0.process("chatLinks", this.chatLinks);
        this.chatLinksPrompt = param0.process("chatLinksPrompt", this.chatLinksPrompt);
        this.enableVsync = param0.process("enableVsync", this.enableVsync);
        this.entityShadows = param0.process("entityShadows", this.entityShadows);
        this.forceUnicodeFont = param0.process("forceUnicodeFont", this.forceUnicodeFont);
        this.discreteMouseScroll = param0.process("discrete_mouse_scroll", this.discreteMouseScroll);
        this.invertYMouse = param0.process("invertYMouse", this.invertYMouse);
        this.realmsNotifications = param0.process("realmsNotifications", this.realmsNotifications);
        this.reducedDebugInfo = param0.process("reducedDebugInfo", this.reducedDebugInfo);
        this.showSubtitles = param0.process("showSubtitles", this.showSubtitles);
        this.touchscreen = param0.process("touchscreen", this.touchscreen);
        this.fullscreen = param0.process("fullscreen", this.fullscreen);
        this.bobView = param0.process("bobView", this.bobView);
        this.toggleCrouch = param0.process("toggleCrouch", this.toggleCrouch);
        this.toggleSprint = param0.process("toggleSprint", this.toggleSprint);
        this.darkMojangStudiosBackground = param0.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
        this.hideLightningFlashes = param0.process("hideLightningFlashes", this.hideLightningFlashes);
        this.sensitivity = param0.process("mouseSensitivity", this.sensitivity);
        this.fov = param0.process("fov", (this.fov - 70.0) / 40.0) * 40.0 + 70.0;
        this.screenEffectScale = param0.process("screenEffectScale", this.screenEffectScale);
        this.fovEffectScale = param0.process("fovEffectScale", this.fovEffectScale);
        this.gamma = param0.process("gamma", this.gamma);
        this.renderDistance = (int)Mth.clamp(
            (double)param0.process("renderDistance", this.renderDistance), Option.RENDER_DISTANCE.getMinValue(), Option.RENDER_DISTANCE.getMaxValue()
        );
        this.simulationDistance = (int)Mth.clamp(
            (double)param0.process("simulationDistance", this.simulationDistance),
            Option.SIMULATION_DISTANCE.getMinValue(),
            Option.SIMULATION_DISTANCE.getMaxValue()
        );
        this.entityDistanceScaling = param0.process("entityDistanceScaling", this.entityDistanceScaling);
        this.guiScale = param0.process("guiScale", this.guiScale);
        this.particles = param0.process("particles", this.particles, ParticleStatus::byId, ParticleStatus::getId);
        this.framerateLimit = param0.process("maxFps", this.framerateLimit);
        this.difficulty = param0.process("difficulty", this.difficulty, Difficulty::byId, Difficulty::getId);
        this.graphicsMode = param0.process("graphicsMode", this.graphicsMode, GraphicsStatus::byId, GraphicsStatus::getId);
        this.ambientOcclusion = param0.process("ao", this.ambientOcclusion, Options::readAmbientOcclusion, param0x -> Integer.toString(param0x.getId()));
        this.prioritizeChunkUpdates = param0.process(
            "prioritizeChunkUpdates", this.prioritizeChunkUpdates, PrioritizeChunkUpdates::byId, PrioritizeChunkUpdates::getId
        );
        this.biomeBlendRadius = param0.process("biomeBlendRadius", this.biomeBlendRadius);
        this.renderClouds = param0.process("renderClouds", this.renderClouds, Options::readCloudStatus, Options::writeCloudStatus);
        this.resourcePacks = param0.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
        this.incompatibleResourcePacks = param0.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
        this.lastMpIp = param0.process("lastServer", this.lastMpIp);
        this.languageCode = param0.process("lang", this.languageCode);
        this.soundDevice = param0.process("soundDevice", this.soundDevice);
        this.chatVisibility = param0.process("chatVisibility", this.chatVisibility, ChatVisiblity::byId, ChatVisiblity::getId);
        this.chatOpacity = param0.process("chatOpacity", this.chatOpacity);
        this.chatLineSpacing = param0.process("chatLineSpacing", this.chatLineSpacing);
        this.textBackgroundOpacity = param0.process("textBackgroundOpacity", this.textBackgroundOpacity);
        this.backgroundForChatOnly = param0.process("backgroundForChatOnly", this.backgroundForChatOnly);
        this.hideServerAddress = param0.process("hideServerAddress", this.hideServerAddress);
        this.advancedItemTooltips = param0.process("advancedItemTooltips", this.advancedItemTooltips);
        this.pauseOnLostFocus = param0.process("pauseOnLostFocus", this.pauseOnLostFocus);
        this.overrideWidth = param0.process("overrideWidth", this.overrideWidth);
        this.overrideHeight = param0.process("overrideHeight", this.overrideHeight);
        this.heldItemTooltips = param0.process("heldItemTooltips", this.heldItemTooltips);
        this.chatHeightFocused = param0.process("chatHeightFocused", this.chatHeightFocused);
        this.chatDelay = param0.process("chatDelay", this.chatDelay);
        this.chatHeightUnfocused = param0.process("chatHeightUnfocused", this.chatHeightUnfocused);
        this.chatScale = param0.process("chatScale", this.chatScale);
        this.chatWidth = param0.process("chatWidth", this.chatWidth);
        this.mipmapLevels = param0.process("mipmapLevels", this.mipmapLevels);
        this.useNativeTransport = param0.process("useNativeTransport", this.useNativeTransport);
        this.mainHand = param0.process("mainHand", this.mainHand, Options::readMainHand, Options::writeMainHand);
        this.attackIndicator = param0.process("attackIndicator", this.attackIndicator, AttackIndicatorStatus::byId, AttackIndicatorStatus::getId);
        this.narratorStatus = param0.process("narrator", this.narratorStatus, NarratorStatus::byId, NarratorStatus::getId);
        this.tutorialStep = param0.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
        this.mouseWheelSensitivity = param0.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
        this.rawMouseInput = param0.process("rawMouseInput", this.rawMouseInput);
        this.glDebugVerbosity = param0.process("glDebugVerbosity", this.glDebugVerbosity);
        this.skipMultiplayerWarning = param0.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
        this.hideMatchedNames = param0.process("hideMatchedNames", this.hideMatchedNames);
        this.joinedFirstServer = param0.process("joinedFirstServer", this.joinedFirstServer);
        this.hideBundleTutorial = param0.process("hideBundleTutorial", this.hideBundleTutorial);
        this.syncWrites = param0.process("syncChunkWrites", this.syncWrites);
        this.showAutosaveIndicator = param0.process("showAutosaveIndicator", this.showAutosaveIndicator);
        this.allowServerListing = param0.process("allowServerListing", this.allowServerListing);

        for(KeyMapping var0 : this.keyMappings) {
            String var1 = var0.saveString();
            String var2 = param0.process("key_" + var0.getName(), var1);
            if (!var1.equals(var2)) {
                var0.setKey(InputConstants.getKey(var2));
            }
        }

        for(SoundSource var3 : SoundSource.values()) {
            this.sourceVolumes.computeFloat(var3, (param1, param2) -> param0.process("soundCategory_" + param1.getName(), param2 != null ? param2 : 1.0F));
        }

        for(PlayerModelPart var4 : PlayerModelPart.values()) {
            boolean var5 = this.modelParts.contains(var4);
            boolean var6 = param0.process("modelPart_" + var4.getId(), var5);
            if (var6 != var5) {
                this.setModelPart(var4, var6);
            }
        }

    }

    public void load() {
        try {
            if (!this.optionsFile.exists()) {
                return;
            }

            this.sourceVolumes.clear();
            CompoundTag var0 = new CompoundTag();

            try (BufferedReader var1 = Files.newReader(this.optionsFile, Charsets.UTF_8)) {
                var1.lines().forEach(param1 -> {
                    try {
                        Iterator<String> var0x = OPTION_SPLITTER.split(param1).iterator();
                        var0.putString(var0x.next(), var0x.next());
                    } catch (Exception var3x) {
                        LOGGER.warn("Skipping bad option: {}", param1);
                    }

                });
            }

            final CompoundTag var2 = this.dataFix(var0);
            if (!var2.contains("graphicsMode") && var2.contains("fancyGraphics")) {
                if (isTrue(var2.getString("fancyGraphics"))) {
                    this.graphicsMode = GraphicsStatus.FANCY;
                } else {
                    this.graphicsMode = GraphicsStatus.FAST;
                }
            }

            this.processOptions(new Options.FieldAccess() {
                @Nullable
                private String getValueOrNull(String param0) {
                    return var2.contains(param0) ? var2.getString(param0) : null;
                }

                @Override
                public int process(String param0, int param1) {
                    String var0 = this.getValueOrNull(param0);
                    if (var0 != null) {
                        try {
                            return Integer.parseInt(var0);
                        } catch (NumberFormatException var5) {
                            Options.LOGGER.warn("Invalid integer value for option {} = {}", param0, var0, var5);
                        }
                    }

                    return param1;
                }

                @Override
                public boolean process(String param0, boolean param1) {
                    String var0 = this.getValueOrNull(param0);
                    return var0 != null ? Options.isTrue(var0) : param1;
                }

                @Override
                public String process(String param0, String param1) {
                    return MoreObjects.firstNonNull(this.getValueOrNull(param0), param1);
                }

                @Override
                public double process(String param0, double param1) {
                    String var0 = this.getValueOrNull(param0);
                    if (var0 == null) {
                        return param1;
                    } else if (Options.isTrue(var0)) {
                        return 1.0;
                    } else if (Options.isFalse(var0)) {
                        return 0.0;
                    } else {
                        try {
                            return Double.parseDouble(var0);
                        } catch (NumberFormatException var6) {
                            Options.LOGGER.warn("Invalid floating point value for option {} = {}", param0, var0, var6);
                            return param1;
                        }
                    }
                }

                @Override
                public float process(String param0, float param1) {
                    String var0 = this.getValueOrNull(param0);
                    if (var0 == null) {
                        return param1;
                    } else if (Options.isTrue(var0)) {
                        return 1.0F;
                    } else if (Options.isFalse(var0)) {
                        return 0.0F;
                    } else {
                        try {
                            return Float.parseFloat(var0);
                        } catch (NumberFormatException var5) {
                            Options.LOGGER.warn("Invalid floating point value for option {} = {}", param0, var0, var5);
                            return param1;
                        }
                    }
                }

                @Override
                public <T> T process(String param0, T param1, Function<String, T> param2, Function<T, String> param3) {
                    String var0 = this.getValueOrNull(param0);
                    return (T)(var0 == null ? param1 : param2.apply(var0));
                }

                @Override
                public <T> T process(String param0, T param1, IntFunction<T> param2, ToIntFunction<T> param3) {
                    String var0 = this.getValueOrNull(param0);
                    if (var0 != null) {
                        try {
                            return param2.apply(Integer.parseInt(var0));
                        } catch (Exception var7) {
                            Options.LOGGER.warn("Invalid integer value for option {} = {}", param0, var0, var7);
                        }
                    }

                    return param1;
                }
            });
            if (var2.contains("fullscreenResolution")) {
                this.fullscreenVideoModeString = var2.getString("fullscreenResolution");
            }

            if (this.minecraft.getWindow() != null) {
                this.minecraft.getWindow().setFramerateLimit(this.framerateLimit);
            }

            KeyMapping.resetMapping();
        } catch (Exception var7) {
            LOGGER.error("Failed to load options", (Throwable)var7);
        }

    }

    static boolean isTrue(String param0) {
        return "true".equals(param0);
    }

    static boolean isFalse(String param0) {
        return "false".equals(param0);
    }

    private CompoundTag dataFix(CompoundTag param0) {
        int var0 = 0;

        try {
            var0 = Integer.parseInt(param0.getString("version"));
        } catch (RuntimeException var4) {
        }

        return NbtUtils.update(this.minecraft.getFixerUpper(), DataFixTypes.OPTIONS, param0, var0);
    }

    public void save() {
        try (final PrintWriter var0 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8))) {
            var0.println("version:" + SharedConstants.getCurrentVersion().getWorldVersion());
            this.processOptions(new Options.FieldAccess() {
                public void writePrefix(String param0) {
                    var0.print(param0);
                    var0.print(':');
                }

                @Override
                public int process(String param0, int param1) {
                    this.writePrefix(param0);
                    var0.println(param1);
                    return param1;
                }

                @Override
                public boolean process(String param0, boolean param1) {
                    this.writePrefix(param0);
                    var0.println(param1);
                    return param1;
                }

                @Override
                public String process(String param0, String param1) {
                    this.writePrefix(param0);
                    var0.println(param1);
                    return param1;
                }

                @Override
                public double process(String param0, double param1) {
                    this.writePrefix(param0);
                    var0.println(param1);
                    return param1;
                }

                @Override
                public float process(String param0, float param1) {
                    this.writePrefix(param0);
                    var0.println(param1);
                    return param1;
                }

                @Override
                public <T> T process(String param0, T param1, Function<String, T> param2, Function<T, String> param3) {
                    this.writePrefix(param0);
                    var0.println(param3.apply(param1));
                    return param1;
                }

                @Override
                public <T> T process(String param0, T param1, IntFunction<T> param2, ToIntFunction<T> param3) {
                    this.writePrefix(param0);
                    var0.println(param3.applyAsInt(param1));
                    return param1;
                }
            });
            if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent()) {
                var0.println("fullscreenResolution:" + this.minecraft.getWindow().getPreferredFullscreenVideoMode().get().write());
            }
        } catch (Exception var6) {
            LOGGER.error("Failed to save options", (Throwable)var6);
        }

        this.broadcastOptions();
    }

    public float getSoundSourceVolume(SoundSource param0) {
        return this.sourceVolumes.getFloat(param0);
    }

    public void setSoundCategoryVolume(SoundSource param0, float param1) {
        this.sourceVolumes.put(param0, param1);
        this.minecraft.getSoundManager().updateSourceVolume(param0, param1);
    }

    public void broadcastOptions() {
        if (this.minecraft.player != null) {
            int var0 = 0;

            for(PlayerModelPart var1 : this.modelParts) {
                var0 |= var1.getMask();
            }

            this.minecraft
                .player
                .connection
                .send(
                    new ServerboundClientInformationPacket(
                        this.languageCode,
                        this.renderDistance,
                        this.chatVisibility,
                        this.chatColors,
                        var0,
                        this.mainHand,
                        this.minecraft.isTextFilteringEnabled(),
                        this.allowServerListing
                    )
                );
        }

    }

    private void setModelPart(PlayerModelPart param0, boolean param1) {
        if (param1) {
            this.modelParts.add(param0);
        } else {
            this.modelParts.remove(param0);
        }

    }

    public boolean isModelPartEnabled(PlayerModelPart param0) {
        return this.modelParts.contains(param0);
    }

    public void toggleModelPart(PlayerModelPart param0, boolean param1) {
        this.setModelPart(param0, param1);
        this.broadcastOptions();
    }

    public CloudStatus getCloudsType() {
        return this.getEffectiveRenderDistance() >= 4 ? this.renderClouds : CloudStatus.OFF;
    }

    public boolean useNativeTransport() {
        return this.useNativeTransport;
    }

    public void loadSelectedResourcePacks(PackRepository param0) {
        Set<String> var0 = Sets.newLinkedHashSet();
        Iterator<String> var1 = this.resourcePacks.iterator();

        while(var1.hasNext()) {
            String var2 = var1.next();
            Pack var3 = param0.getPack(var2);
            if (var3 == null && !var2.startsWith("file/")) {
                var3 = param0.getPack("file/" + var2);
            }

            if (var3 == null) {
                LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", var2);
                var1.remove();
            } else if (!var3.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(var2)) {
                LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", var2);
                var1.remove();
            } else if (var3.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(var2)) {
                LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", var2);
                this.incompatibleResourcePacks.remove(var2);
            } else {
                var0.add(var3.getId());
            }
        }

        param0.setSelected(var0);
    }

    public CameraType getCameraType() {
        return this.cameraType;
    }

    public void setCameraType(CameraType param0) {
        this.cameraType = param0;
    }

    private static List<String> readPackList(String param0x) {
        List<String> var0x = GsonHelper.fromJson(GSON, param0x, RESOURCE_PACK_TYPE);
        return (List<String>)(var0x != null ? var0x : Lists.newArrayList());
    }

    private static CloudStatus readCloudStatus(String param0x) {
        switch(param0x) {
            case "true":
                return CloudStatus.FANCY;
            case "fast":
                return CloudStatus.FAST;
            case "false":
            default:
                return CloudStatus.OFF;
        }
    }

    private static String writeCloudStatus(CloudStatus param0x) {
        switch(param0x) {
            case FANCY:
                return "true";
            case FAST:
                return "fast";
            case OFF:
            default:
                return "false";
        }
    }

    private static AmbientOcclusionStatus readAmbientOcclusion(String param0x) {
        if (isTrue(param0x)) {
            return AmbientOcclusionStatus.MAX;
        } else {
            return isFalse(param0x) ? AmbientOcclusionStatus.OFF : AmbientOcclusionStatus.byId(Integer.parseInt(param0x));
        }
    }

    private static HumanoidArm readMainHand(String param0x) {
        return "left".equals(param0x) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    private static String writeMainHand(HumanoidArm param0x) {
        return param0x == HumanoidArm.LEFT ? "left" : "right";
    }

    public File getFile() {
        return this.optionsFile;
    }

    public String dumpOptionsForReport() {
        ImmutableList<Pair<String, String>> var0 = ImmutableList.<Pair<String, String>>builder()
            .add(Pair.of("ao", String.valueOf(this.ambientOcclusion)))
            .add(Pair.of("biomeBlendRadius", String.valueOf(this.biomeBlendRadius)))
            .add(Pair.of("enableVsync", String.valueOf(this.enableVsync)))
            .add(Pair.of("entityDistanceScaling", String.valueOf(this.entityDistanceScaling)))
            .add(Pair.of("entityShadows", String.valueOf(this.entityShadows)))
            .add(Pair.of("forceUnicodeFont", String.valueOf(this.forceUnicodeFont)))
            .add(Pair.of("fov", String.valueOf(this.fov)))
            .add(Pair.of("fovEffectScale", String.valueOf(this.fovEffectScale)))
            .add(Pair.of("prioritizeChunkUpdates", String.valueOf(this.prioritizeChunkUpdates)))
            .add(Pair.of("fullscreen", String.valueOf(this.fullscreen)))
            .add(Pair.of("fullscreenResolution", String.valueOf(this.fullscreenVideoModeString)))
            .add(Pair.of("gamma", String.valueOf(this.gamma)))
            .add(Pair.of("glDebugVerbosity", String.valueOf(this.glDebugVerbosity)))
            .add(Pair.of("graphicsMode", String.valueOf(this.graphicsMode)))
            .add(Pair.of("guiScale", String.valueOf(this.guiScale)))
            .add(Pair.of("maxFps", String.valueOf(this.framerateLimit)))
            .add(Pair.of("mipmapLevels", String.valueOf(this.mipmapLevels)))
            .add(Pair.of("narrator", String.valueOf(this.narratorStatus)))
            .add(Pair.of("overrideHeight", String.valueOf(this.overrideHeight)))
            .add(Pair.of("overrideWidth", String.valueOf(this.overrideWidth)))
            .add(Pair.of("particles", String.valueOf(this.particles)))
            .add(Pair.of("reducedDebugInfo", String.valueOf(this.reducedDebugInfo)))
            .add(Pair.of("renderClouds", String.valueOf(this.renderClouds)))
            .add(Pair.of("renderDistance", String.valueOf(this.renderDistance)))
            .add(Pair.of("simulationDistance", String.valueOf(this.simulationDistance)))
            .add(Pair.of("resourcePacks", String.valueOf(this.resourcePacks)))
            .add(Pair.of("screenEffectScale", String.valueOf(this.screenEffectScale)))
            .add(Pair.of("syncChunkWrites", String.valueOf(this.syncWrites)))
            .add(Pair.of("useNativeTransport", String.valueOf(this.useNativeTransport)))
            .add(Pair.of("soundDevice", String.valueOf(this.soundDevice)))
            .build();
        return var0.stream().map(param0 -> (String)param0.getFirst() + ": " + (String)param0.getSecond()).collect(Collectors.joining(System.lineSeparator()));
    }

    public void setServerRenderDistance(int param0) {
        this.serverRenderDistance = param0;
    }

    public int getEffectiveRenderDistance() {
        return this.serverRenderDistance > 0 ? Math.min(this.renderDistance, this.serverRenderDistance) : this.renderDistance;
    }

    @OnlyIn(Dist.CLIENT)
    interface FieldAccess {
        int process(String var1, int var2);

        boolean process(String var1, boolean var2);

        String process(String var1, String var2);

        double process(String var1, double var2);

        float process(String var1, float var2);

        <T> T process(String var1, T var2, Function<String, T> var3, Function<T, String> var4);

        <T> T process(String var1, T var2, IntFunction<T> var3, ToIntFunction<T> var4);
    }
}
