package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Options {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>() {
    };
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    public double sensitivity = 0.5;
    public int renderDistance = -1;
    public float entityDistanceScaling = 1.0F;
    public int framerateLimit = 120;
    public CloudStatus renderClouds = CloudStatus.FANCY;
    public GraphicsStatus graphicsMode = GraphicsStatus.FANCY;
    public AmbientOcclusionStatus ambientOcclusion = AmbientOcclusionStatus.MAX;
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
    private final Set<PlayerModelPart> modelParts = Sets.newHashSet(PlayerModelPart.values());
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
    private final Map<SoundSource, Float> sourceVolumes = Maps.newEnumMap(SoundSource.class);
    public boolean useNativeTransport = true;
    public AttackIndicatorStatus attackIndicator = AttackIndicatorStatus.CROSSHAIR;
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public boolean joinedFirstServer = false;
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
    public boolean reducedDebugInfo;
    public boolean snooperEnabled = true;
    public boolean showSubtitles;
    public boolean backgroundForChatOnly = true;
    public boolean touchscreen;
    public boolean fullscreen;
    public boolean bobView = true;
    public boolean toggleCrouch;
    public boolean toggleSprint;
    public boolean skipMultiplayerWarning;
    public boolean hideMatchedNames = true;
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
        new KeyMapping[]{
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
        },
        this.keyHotbarSlots
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
    public boolean syncWrites;

    public Options(Minecraft param0, File param1) {
        this.minecraft = param0;
        this.optionsFile = new File(param1, "options.txt");
        if (param0.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            Option.RENDER_DISTANCE.setMaxValue(32.0F);
        } else {
            Option.RENDER_DISTANCE.setMaxValue(16.0F);
        }

        this.renderDistance = param0.is64Bit() ? 12 : 8;
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

            CompoundTag var2 = this.dataFix(var0);
            if (!var2.contains("graphicsMode") && var2.contains("fancyGraphics")) {
                if ("true".equals(var2.getString("fancyGraphics"))) {
                    this.graphicsMode = GraphicsStatus.FANCY;
                } else {
                    this.graphicsMode = GraphicsStatus.FAST;
                }
            }

            for(String var3 : var2.getAllKeys()) {
                String var4 = var2.getString(var3);

                try {
                    if ("autoJump".equals(var3)) {
                        Option.AUTO_JUMP.set(this, var4);
                    }

                    if ("autoSuggestions".equals(var3)) {
                        Option.AUTO_SUGGESTIONS.set(this, var4);
                    }

                    if ("chatColors".equals(var3)) {
                        Option.CHAT_COLOR.set(this, var4);
                    }

                    if ("chatLinks".equals(var3)) {
                        Option.CHAT_LINKS.set(this, var4);
                    }

                    if ("chatLinksPrompt".equals(var3)) {
                        Option.CHAT_LINKS_PROMPT.set(this, var4);
                    }

                    if ("enableVsync".equals(var3)) {
                        Option.ENABLE_VSYNC.set(this, var4);
                    }

                    if ("entityShadows".equals(var3)) {
                        Option.ENTITY_SHADOWS.set(this, var4);
                    }

                    if ("forceUnicodeFont".equals(var3)) {
                        Option.FORCE_UNICODE_FONT.set(this, var4);
                    }

                    if ("discrete_mouse_scroll".equals(var3)) {
                        Option.DISCRETE_MOUSE_SCROLL.set(this, var4);
                    }

                    if ("invertYMouse".equals(var3)) {
                        Option.INVERT_MOUSE.set(this, var4);
                    }

                    if ("realmsNotifications".equals(var3)) {
                        Option.REALMS_NOTIFICATIONS.set(this, var4);
                    }

                    if ("reducedDebugInfo".equals(var3)) {
                        Option.REDUCED_DEBUG_INFO.set(this, var4);
                    }

                    if ("showSubtitles".equals(var3)) {
                        Option.SHOW_SUBTITLES.set(this, var4);
                    }

                    if ("snooperEnabled".equals(var3)) {
                        Option.SNOOPER_ENABLED.set(this, var4);
                    }

                    if ("touchscreen".equals(var3)) {
                        Option.TOUCHSCREEN.set(this, var4);
                    }

                    if ("fullscreen".equals(var3)) {
                        Option.USE_FULLSCREEN.set(this, var4);
                    }

                    if ("bobView".equals(var3)) {
                        Option.VIEW_BOBBING.set(this, var4);
                    }

                    if ("toggleCrouch".equals(var3)) {
                        this.toggleCrouch = "true".equals(var4);
                    }

                    if ("toggleSprint".equals(var3)) {
                        this.toggleSprint = "true".equals(var4);
                    }

                    if ("mouseSensitivity".equals(var3)) {
                        this.sensitivity = (double)readFloat(var4);
                    }

                    if ("fov".equals(var3)) {
                        this.fov = (double)(readFloat(var4) * 40.0F + 70.0F);
                    }

                    if ("screenEffectScale".equals(var3)) {
                        this.screenEffectScale = readFloat(var4);
                    }

                    if ("fovEffectScale".equals(var3)) {
                        this.fovEffectScale = readFloat(var4);
                    }

                    if ("gamma".equals(var3)) {
                        this.gamma = (double)readFloat(var4);
                    }

                    if ("renderDistance".equals(var3)) {
                        this.renderDistance = Integer.parseInt(var4);
                    }

                    if ("entityDistanceScaling".equals(var3)) {
                        this.entityDistanceScaling = Float.parseFloat(var4);
                    }

                    if ("guiScale".equals(var3)) {
                        this.guiScale = Integer.parseInt(var4);
                    }

                    if ("particles".equals(var3)) {
                        this.particles = ParticleStatus.byId(Integer.parseInt(var4));
                    }

                    if ("maxFps".equals(var3)) {
                        this.framerateLimit = Integer.parseInt(var4);
                        if (this.minecraft.getWindow() != null) {
                            this.minecraft.getWindow().setFramerateLimit(this.framerateLimit);
                        }
                    }

                    if ("difficulty".equals(var3)) {
                        this.difficulty = Difficulty.byId(Integer.parseInt(var4));
                    }

                    if ("graphicsMode".equals(var3)) {
                        this.graphicsMode = GraphicsStatus.byId(Integer.parseInt(var4));
                    }

                    if ("tutorialStep".equals(var3)) {
                        this.tutorialStep = TutorialSteps.getByName(var4);
                    }

                    if ("ao".equals(var3)) {
                        if ("true".equals(var4)) {
                            this.ambientOcclusion = AmbientOcclusionStatus.MAX;
                        } else if ("false".equals(var4)) {
                            this.ambientOcclusion = AmbientOcclusionStatus.OFF;
                        } else {
                            this.ambientOcclusion = AmbientOcclusionStatus.byId(Integer.parseInt(var4));
                        }
                    }

                    if ("renderClouds".equals(var3)) {
                        if ("true".equals(var4)) {
                            this.renderClouds = CloudStatus.FANCY;
                        } else if ("false".equals(var4)) {
                            this.renderClouds = CloudStatus.OFF;
                        } else if ("fast".equals(var4)) {
                            this.renderClouds = CloudStatus.FAST;
                        }
                    }

                    if ("attackIndicator".equals(var3)) {
                        this.attackIndicator = AttackIndicatorStatus.byId(Integer.parseInt(var4));
                    }

                    if ("resourcePacks".equals(var3)) {
                        this.resourcePacks = GsonHelper.fromJson(GSON, var4, RESOURCE_PACK_TYPE);
                        if (this.resourcePacks == null) {
                            this.resourcePacks = Lists.newArrayList();
                        }
                    }

                    if ("incompatibleResourcePacks".equals(var3)) {
                        this.incompatibleResourcePacks = GsonHelper.fromJson(GSON, var4, RESOURCE_PACK_TYPE);
                        if (this.incompatibleResourcePacks == null) {
                            this.incompatibleResourcePacks = Lists.newArrayList();
                        }
                    }

                    if ("lastServer".equals(var3)) {
                        this.lastMpIp = var4;
                    }

                    if ("lang".equals(var3)) {
                        this.languageCode = var4;
                    }

                    if ("chatVisibility".equals(var3)) {
                        this.chatVisibility = ChatVisiblity.byId(Integer.parseInt(var4));
                    }

                    if ("chatOpacity".equals(var3)) {
                        this.chatOpacity = (double)readFloat(var4);
                    }

                    if ("chatLineSpacing".equals(var3)) {
                        this.chatLineSpacing = (double)readFloat(var4);
                    }

                    if ("textBackgroundOpacity".equals(var3)) {
                        this.textBackgroundOpacity = (double)readFloat(var4);
                    }

                    if ("backgroundForChatOnly".equals(var3)) {
                        this.backgroundForChatOnly = "true".equals(var4);
                    }

                    if ("fullscreenResolution".equals(var3)) {
                        this.fullscreenVideoModeString = var4;
                    }

                    if ("hideServerAddress".equals(var3)) {
                        this.hideServerAddress = "true".equals(var4);
                    }

                    if ("advancedItemTooltips".equals(var3)) {
                        this.advancedItemTooltips = "true".equals(var4);
                    }

                    if ("pauseOnLostFocus".equals(var3)) {
                        this.pauseOnLostFocus = "true".equals(var4);
                    }

                    if ("overrideHeight".equals(var3)) {
                        this.overrideHeight = Integer.parseInt(var4);
                    }

                    if ("overrideWidth".equals(var3)) {
                        this.overrideWidth = Integer.parseInt(var4);
                    }

                    if ("heldItemTooltips".equals(var3)) {
                        this.heldItemTooltips = "true".equals(var4);
                    }

                    if ("chatHeightFocused".equals(var3)) {
                        this.chatHeightFocused = (double)readFloat(var4);
                    }

                    if ("chatDelay".equals(var3)) {
                        this.chatDelay = (double)readFloat(var4);
                    }

                    if ("chatHeightUnfocused".equals(var3)) {
                        this.chatHeightUnfocused = (double)readFloat(var4);
                    }

                    if ("chatScale".equals(var3)) {
                        this.chatScale = (double)readFloat(var4);
                    }

                    if ("chatWidth".equals(var3)) {
                        this.chatWidth = (double)readFloat(var4);
                    }

                    if ("mipmapLevels".equals(var3)) {
                        this.mipmapLevels = Integer.parseInt(var4);
                    }

                    if ("useNativeTransport".equals(var3)) {
                        this.useNativeTransport = "true".equals(var4);
                    }

                    if ("mainHand".equals(var3)) {
                        this.mainHand = "left".equals(var4) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
                    }

                    if ("narrator".equals(var3)) {
                        this.narratorStatus = NarratorStatus.byId(Integer.parseInt(var4));
                    }

                    if ("biomeBlendRadius".equals(var3)) {
                        this.biomeBlendRadius = Integer.parseInt(var4);
                    }

                    if ("mouseWheelSensitivity".equals(var3)) {
                        this.mouseWheelSensitivity = (double)readFloat(var4);
                    }

                    if ("rawMouseInput".equals(var3)) {
                        this.rawMouseInput = "true".equals(var4);
                    }

                    if ("glDebugVerbosity".equals(var3)) {
                        this.glDebugVerbosity = Integer.parseInt(var4);
                    }

                    if ("skipMultiplayerWarning".equals(var3)) {
                        this.skipMultiplayerWarning = "true".equals(var4);
                    }

                    if ("hideMatchedNames".equals(var3)) {
                        this.hideMatchedNames = "true".equals(var4);
                    }

                    if ("joinedFirstServer".equals(var3)) {
                        this.joinedFirstServer = "true".equals(var4);
                    }

                    if ("syncChunkWrites".equals(var3)) {
                        this.syncWrites = "true".equals(var4);
                    }

                    for(KeyMapping var5 : this.keyMappings) {
                        if (var3.equals("key_" + var5.getName())) {
                            var5.setKey(InputConstants.getKey(var4));
                        }
                    }

                    for(SoundSource var6 : SoundSource.values()) {
                        if (var3.equals("soundCategory_" + var6.getName())) {
                            this.sourceVolumes.put(var6, readFloat(var4));
                        }
                    }

                    for(PlayerModelPart var7 : PlayerModelPart.values()) {
                        if (var3.equals("modelPart_" + var7.getId())) {
                            this.setModelPart(var7, "true".equals(var4));
                        }
                    }
                } catch (Exception var19) {
                    LOGGER.warn("Skipping bad option: {}:{}", var3, var4);
                }
            }

            KeyMapping.resetMapping();
        } catch (Exception var20) {
            LOGGER.error("Failed to load options", (Throwable)var20);
        }

    }

    private CompoundTag dataFix(CompoundTag param0) {
        int var0 = 0;

        try {
            var0 = Integer.parseInt(param0.getString("version"));
        } catch (RuntimeException var4) {
        }

        return NbtUtils.update(this.minecraft.getFixerUpper(), DataFixTypes.OPTIONS, param0, var0);
    }

    private static float readFloat(String param0) {
        if ("true".equals(param0)) {
            return 1.0F;
        } else {
            return "false".equals(param0) ? 0.0F : Float.parseFloat(param0);
        }
    }

    public void save() {
        try (PrintWriter var0 = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8))) {
            var0.println("version:" + SharedConstants.getCurrentVersion().getWorldVersion());
            var0.println("autoJump:" + Option.AUTO_JUMP.get(this));
            var0.println("autoSuggestions:" + Option.AUTO_SUGGESTIONS.get(this));
            var0.println("chatColors:" + Option.CHAT_COLOR.get(this));
            var0.println("chatLinks:" + Option.CHAT_LINKS.get(this));
            var0.println("chatLinksPrompt:" + Option.CHAT_LINKS_PROMPT.get(this));
            var0.println("enableVsync:" + Option.ENABLE_VSYNC.get(this));
            var0.println("entityShadows:" + Option.ENTITY_SHADOWS.get(this));
            var0.println("forceUnicodeFont:" + Option.FORCE_UNICODE_FONT.get(this));
            var0.println("discrete_mouse_scroll:" + Option.DISCRETE_MOUSE_SCROLL.get(this));
            var0.println("invertYMouse:" + Option.INVERT_MOUSE.get(this));
            var0.println("realmsNotifications:" + Option.REALMS_NOTIFICATIONS.get(this));
            var0.println("reducedDebugInfo:" + Option.REDUCED_DEBUG_INFO.get(this));
            var0.println("snooperEnabled:" + Option.SNOOPER_ENABLED.get(this));
            var0.println("showSubtitles:" + Option.SHOW_SUBTITLES.get(this));
            var0.println("touchscreen:" + Option.TOUCHSCREEN.get(this));
            var0.println("fullscreen:" + Option.USE_FULLSCREEN.get(this));
            var0.println("bobView:" + Option.VIEW_BOBBING.get(this));
            var0.println("toggleCrouch:" + this.toggleCrouch);
            var0.println("toggleSprint:" + this.toggleSprint);
            var0.println("mouseSensitivity:" + this.sensitivity);
            var0.println("fov:" + (this.fov - 70.0) / 40.0);
            var0.println("screenEffectScale:" + this.screenEffectScale);
            var0.println("fovEffectScale:" + this.fovEffectScale);
            var0.println("gamma:" + this.gamma);
            var0.println("renderDistance:" + this.renderDistance);
            var0.println("entityDistanceScaling:" + this.entityDistanceScaling);
            var0.println("guiScale:" + this.guiScale);
            var0.println("particles:" + this.particles.getId());
            var0.println("maxFps:" + this.framerateLimit);
            var0.println("difficulty:" + this.difficulty.getId());
            var0.println("graphicsMode:" + this.graphicsMode.getId());
            var0.println("ao:" + this.ambientOcclusion.getId());
            var0.println("biomeBlendRadius:" + this.biomeBlendRadius);
            switch(this.renderClouds) {
                case FANCY:
                    var0.println("renderClouds:true");
                    break;
                case FAST:
                    var0.println("renderClouds:fast");
                    break;
                case OFF:
                    var0.println("renderClouds:false");
            }

            var0.println("resourcePacks:" + GSON.toJson(this.resourcePacks));
            var0.println("incompatibleResourcePacks:" + GSON.toJson(this.incompatibleResourcePacks));
            var0.println("lastServer:" + this.lastMpIp);
            var0.println("lang:" + this.languageCode);
            var0.println("chatVisibility:" + this.chatVisibility.getId());
            var0.println("chatOpacity:" + this.chatOpacity);
            var0.println("chatLineSpacing:" + this.chatLineSpacing);
            var0.println("textBackgroundOpacity:" + this.textBackgroundOpacity);
            var0.println("backgroundForChatOnly:" + this.backgroundForChatOnly);
            if (this.minecraft.getWindow().getPreferredFullscreenVideoMode().isPresent()) {
                var0.println("fullscreenResolution:" + this.minecraft.getWindow().getPreferredFullscreenVideoMode().get().write());
            }

            var0.println("hideServerAddress:" + this.hideServerAddress);
            var0.println("advancedItemTooltips:" + this.advancedItemTooltips);
            var0.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            var0.println("overrideWidth:" + this.overrideWidth);
            var0.println("overrideHeight:" + this.overrideHeight);
            var0.println("heldItemTooltips:" + this.heldItemTooltips);
            var0.println("chatHeightFocused:" + this.chatHeightFocused);
            var0.println("chatDelay: " + this.chatDelay);
            var0.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            var0.println("chatScale:" + this.chatScale);
            var0.println("chatWidth:" + this.chatWidth);
            var0.println("mipmapLevels:" + this.mipmapLevels);
            var0.println("useNativeTransport:" + this.useNativeTransport);
            var0.println("mainHand:" + (this.mainHand == HumanoidArm.LEFT ? "left" : "right"));
            var0.println("attackIndicator:" + this.attackIndicator.getId());
            var0.println("narrator:" + this.narratorStatus.getId());
            var0.println("tutorialStep:" + this.tutorialStep.getName());
            var0.println("mouseWheelSensitivity:" + this.mouseWheelSensitivity);
            var0.println("rawMouseInput:" + Option.RAW_MOUSE_INPUT.get(this));
            var0.println("glDebugVerbosity:" + this.glDebugVerbosity);
            var0.println("skipMultiplayerWarning:" + this.skipMultiplayerWarning);
            var0.println("hideMatchedNames:" + this.hideMatchedNames);
            var0.println("joinedFirstServer:" + this.joinedFirstServer);
            var0.println("syncChunkWrites:" + this.syncWrites);

            for(KeyMapping var1 : this.keyMappings) {
                var0.println("key_" + var1.getName() + ":" + var1.saveString());
            }

            for(SoundSource var2 : SoundSource.values()) {
                var0.println("soundCategory_" + var2.getName() + ":" + this.getSoundSourceVolume(var2));
            }

            for(PlayerModelPart var3 : PlayerModelPart.values()) {
                var0.println("modelPart_" + var3.getId() + ":" + this.modelParts.contains(var3));
            }
        } catch (Exception var17) {
            LOGGER.error("Failed to save options", (Throwable)var17);
        }

        this.broadcastOptions();
    }

    public float getSoundSourceVolume(SoundSource param0) {
        return this.sourceVolumes.containsKey(param0) ? this.sourceVolumes.get(param0) : 1.0F;
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
                .send(new ServerboundClientInformationPacket(this.languageCode, this.renderDistance, this.chatVisibility, this.chatColors, var0, this.mainHand));
        }

    }

    public Set<PlayerModelPart> getModelParts() {
        return ImmutableSet.copyOf(this.modelParts);
    }

    public void setModelPart(PlayerModelPart param0, boolean param1) {
        if (param1) {
            this.modelParts.add(param0);
        } else {
            this.modelParts.remove(param0);
        }

        this.broadcastOptions();
    }

    public void toggleModelPart(PlayerModelPart param0) {
        if (this.getModelParts().contains(param0)) {
            this.modelParts.remove(param0);
        } else {
            this.modelParts.add(param0);
        }

        this.broadcastOptions();
    }

    public CloudStatus getCloudsType() {
        return this.renderDistance >= 4 ? this.renderClouds : CloudStatus.OFF;
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
}
