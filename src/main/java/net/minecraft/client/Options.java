package net.minecraft.client;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.mojang.blaze3d.platform.InputConstants;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.UnopenedResourcePack;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Options {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();
    private static final Type RESOURCE_PACK_TYPE = new ParameterizedType() {
        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{String.class};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    };
    public static final Splitter COLON_SPLITTER = Splitter.on(':');
    public double sensitivity = 0.5;
    public int renderDistance = -1;
    public int framerateLimit = 120;
    public CloudStatus renderClouds = CloudStatus.FANCY;
    public boolean fancyGraphics = true;
    public AmbientOcclusionStatus ambientOcclusion = AmbientOcclusionStatus.MAX;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public ChatVisiblity chatVisibility = ChatVisiblity.FULL;
    public double chatOpacity = 1.0;
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
    public int mipmapLevels = 4;
    private final Map<SoundSource, Float> sourceVolumes = Maps.newEnumMap(SoundSource.class);
    public boolean useNativeTransport = true;
    public AttackIndicatorStatus attackIndicator = AttackIndicatorStatus.CROSSHAIR;
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
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
    public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
    public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
    public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
    public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
    public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
    public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", () -> this.toggleCrouch);
    public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", () -> this.toggleSprint);
    public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
    public final KeyMapping keySwapHands = new KeyMapping("key.swapHands", 70, "key.categories.inventory");
    public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
    public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
    public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
    public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
    public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
    public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
    public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
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
            this.keyScreenshot,
            this.keyTogglePerspective,
            this.keySmoothCamera,
            this.keyFullscreen,
            this.keySpectatorOutlines,
            this.keySwapHands,
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
    public int thirdPersonView;
    public boolean renderDebug;
    public boolean renderDebugCharts;
    public boolean renderFpsChart;
    public String lastMpIp = "";
    public boolean smoothCamera;
    public double fov = 70.0;
    public double gamma;
    public int guiScale;
    public ParticleStatus particles = ParticleStatus.ALL;
    public NarratorStatus narratorStatus = NarratorStatus.OFF;
    public String languageCode = "en_us";

    public Options(Minecraft param0, File param1) {
        this.minecraft = param0;
        this.optionsFile = new File(param1, "options.txt");
        if (param0.is64Bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            Option.RENDER_DISTANCE.setMaxValue(32.0F);
        } else {
            Option.RENDER_DISTANCE.setMaxValue(16.0F);
        }

        this.renderDistance = param0.is64Bit() ? 12 : 8;
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
            List<String> var0 = IOUtils.readLines(new FileInputStream(this.optionsFile));
            CompoundTag var1 = new CompoundTag();

            for(String var2 : var0) {
                try {
                    Iterator<String> var3 = COLON_SPLITTER.omitEmptyStrings().limit(2).split(var2).iterator();
                    var1.putString(var3.next(), var3.next());
                } catch (Exception var10) {
                    LOGGER.warn("Skipping bad option: {}", var2);
                }
            }

            var1 = this.dataFix(var1);

            for(String var5 : var1.getAllKeys()) {
                String var6 = var1.getString(var5);

                try {
                    if ("autoJump".equals(var5)) {
                        Option.AUTO_JUMP.set(this, var6);
                    }

                    if ("autoSuggestions".equals(var5)) {
                        Option.AUTO_SUGGESTIONS.set(this, var6);
                    }

                    if ("chatColors".equals(var5)) {
                        Option.CHAT_COLOR.set(this, var6);
                    }

                    if ("chatLinks".equals(var5)) {
                        Option.CHAT_LINKS.set(this, var6);
                    }

                    if ("chatLinksPrompt".equals(var5)) {
                        Option.CHAT_LINKS_PROMPT.set(this, var6);
                    }

                    if ("enableVsync".equals(var5)) {
                        Option.ENABLE_VSYNC.set(this, var6);
                    }

                    if ("entityShadows".equals(var5)) {
                        Option.ENTITY_SHADOWS.set(this, var6);
                    }

                    if ("forceUnicodeFont".equals(var5)) {
                        Option.FORCE_UNICODE_FONT.set(this, var6);
                    }

                    if ("discrete_mouse_scroll".equals(var5)) {
                        Option.DISCRETE_MOUSE_SCROLL.set(this, var6);
                    }

                    if ("invertYMouse".equals(var5)) {
                        Option.INVERT_MOUSE.set(this, var6);
                    }

                    if ("realmsNotifications".equals(var5)) {
                        Option.REALMS_NOTIFICATIONS.set(this, var6);
                    }

                    if ("reducedDebugInfo".equals(var5)) {
                        Option.REDUCED_DEBUG_INFO.set(this, var6);
                    }

                    if ("showSubtitles".equals(var5)) {
                        Option.SHOW_SUBTITLES.set(this, var6);
                    }

                    if ("snooperEnabled".equals(var5)) {
                        Option.SNOOPER_ENABLED.set(this, var6);
                    }

                    if ("touchscreen".equals(var5)) {
                        Option.TOUCHSCREEN.set(this, var6);
                    }

                    if ("fullscreen".equals(var5)) {
                        Option.USE_FULLSCREEN.set(this, var6);
                    }

                    if ("bobView".equals(var5)) {
                        Option.VIEW_BOBBING.set(this, var6);
                    }

                    if ("toggleCrouch".equals(var5)) {
                        this.toggleCrouch = "true".equals(var6);
                    }

                    if ("toggleSprint".equals(var5)) {
                        this.toggleSprint = "true".equals(var6);
                    }

                    if ("mouseSensitivity".equals(var5)) {
                        this.sensitivity = (double)readFloat(var6);
                    }

                    if ("fov".equals(var5)) {
                        this.fov = (double)(readFloat(var6) * 40.0F + 70.0F);
                    }

                    if ("gamma".equals(var5)) {
                        this.gamma = (double)readFloat(var6);
                    }

                    if ("renderDistance".equals(var5)) {
                        this.renderDistance = Integer.parseInt(var6);
                    }

                    if ("guiScale".equals(var5)) {
                        this.guiScale = Integer.parseInt(var6);
                    }

                    if ("particles".equals(var5)) {
                        this.particles = ParticleStatus.byId(Integer.parseInt(var6));
                    }

                    if ("maxFps".equals(var5)) {
                        this.framerateLimit = Integer.parseInt(var6);
                        if (this.minecraft.getWindow() != null) {
                            this.minecraft.getWindow().setFramerateLimit(this.framerateLimit);
                        }
                    }

                    if ("difficulty".equals(var5)) {
                        this.difficulty = Difficulty.byId(Integer.parseInt(var6));
                    }

                    if ("fancyGraphics".equals(var5)) {
                        this.fancyGraphics = "true".equals(var6);
                    }

                    if ("tutorialStep".equals(var5)) {
                        this.tutorialStep = TutorialSteps.getByName(var6);
                    }

                    if ("ao".equals(var5)) {
                        if ("true".equals(var6)) {
                            this.ambientOcclusion = AmbientOcclusionStatus.MAX;
                        } else if ("false".equals(var6)) {
                            this.ambientOcclusion = AmbientOcclusionStatus.OFF;
                        } else {
                            this.ambientOcclusion = AmbientOcclusionStatus.byId(Integer.parseInt(var6));
                        }
                    }

                    if ("renderClouds".equals(var5)) {
                        if ("true".equals(var6)) {
                            this.renderClouds = CloudStatus.FANCY;
                        } else if ("false".equals(var6)) {
                            this.renderClouds = CloudStatus.OFF;
                        } else if ("fast".equals(var6)) {
                            this.renderClouds = CloudStatus.FAST;
                        }
                    }

                    if ("attackIndicator".equals(var5)) {
                        this.attackIndicator = AttackIndicatorStatus.byId(Integer.parseInt(var6));
                    }

                    if ("resourcePacks".equals(var5)) {
                        this.resourcePacks = GsonHelper.fromJson(GSON, var6, RESOURCE_PACK_TYPE);
                        if (this.resourcePacks == null) {
                            this.resourcePacks = Lists.newArrayList();
                        }
                    }

                    if ("incompatibleResourcePacks".equals(var5)) {
                        this.incompatibleResourcePacks = GsonHelper.fromJson(GSON, var6, RESOURCE_PACK_TYPE);
                        if (this.incompatibleResourcePacks == null) {
                            this.incompatibleResourcePacks = Lists.newArrayList();
                        }
                    }

                    if ("lastServer".equals(var5)) {
                        this.lastMpIp = var6;
                    }

                    if ("lang".equals(var5)) {
                        this.languageCode = var6;
                    }

                    if ("chatVisibility".equals(var5)) {
                        this.chatVisibility = ChatVisiblity.byId(Integer.parseInt(var6));
                    }

                    if ("chatOpacity".equals(var5)) {
                        this.chatOpacity = (double)readFloat(var6);
                    }

                    if ("textBackgroundOpacity".equals(var5)) {
                        this.textBackgroundOpacity = (double)readFloat(var6);
                    }

                    if ("backgroundForChatOnly".equals(var5)) {
                        this.backgroundForChatOnly = "true".equals(var6);
                    }

                    if ("fullscreenResolution".equals(var5)) {
                        this.fullscreenVideoModeString = var6;
                    }

                    if ("hideServerAddress".equals(var5)) {
                        this.hideServerAddress = "true".equals(var6);
                    }

                    if ("advancedItemTooltips".equals(var5)) {
                        this.advancedItemTooltips = "true".equals(var6);
                    }

                    if ("pauseOnLostFocus".equals(var5)) {
                        this.pauseOnLostFocus = "true".equals(var6);
                    }

                    if ("overrideHeight".equals(var5)) {
                        this.overrideHeight = Integer.parseInt(var6);
                    }

                    if ("overrideWidth".equals(var5)) {
                        this.overrideWidth = Integer.parseInt(var6);
                    }

                    if ("heldItemTooltips".equals(var5)) {
                        this.heldItemTooltips = "true".equals(var6);
                    }

                    if ("chatHeightFocused".equals(var5)) {
                        this.chatHeightFocused = (double)readFloat(var6);
                    }

                    if ("chatHeightUnfocused".equals(var5)) {
                        this.chatHeightUnfocused = (double)readFloat(var6);
                    }

                    if ("chatScale".equals(var5)) {
                        this.chatScale = (double)readFloat(var6);
                    }

                    if ("chatWidth".equals(var5)) {
                        this.chatWidth = (double)readFloat(var6);
                    }

                    if ("mipmapLevels".equals(var5)) {
                        this.mipmapLevels = Integer.parseInt(var6);
                    }

                    if ("useNativeTransport".equals(var5)) {
                        this.useNativeTransport = "true".equals(var6);
                    }

                    if ("mainHand".equals(var5)) {
                        this.mainHand = "left".equals(var6) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
                    }

                    if ("narrator".equals(var5)) {
                        this.narratorStatus = NarratorStatus.byId(Integer.parseInt(var6));
                    }

                    if ("biomeBlendRadius".equals(var5)) {
                        this.biomeBlendRadius = Integer.parseInt(var6);
                    }

                    if ("mouseWheelSensitivity".equals(var5)) {
                        this.mouseWheelSensitivity = (double)readFloat(var6);
                    }

                    if ("rawMouseInput".equals(var5)) {
                        this.rawMouseInput = "true".equals(var6);
                    }

                    if ("glDebugVerbosity".equals(var5)) {
                        this.glDebugVerbosity = Integer.parseInt(var6);
                    }

                    for(KeyMapping var7 : this.keyMappings) {
                        if (var5.equals("key_" + var7.getName())) {
                            var7.setKey(InputConstants.getKey(var6));
                        }
                    }

                    for(SoundSource var8 : SoundSource.values()) {
                        if (var5.equals("soundCategory_" + var8.getName())) {
                            this.sourceVolumes.put(var8, readFloat(var6));
                        }
                    }

                    for(PlayerModelPart var9 : PlayerModelPart.values()) {
                        if (var5.equals("modelPart_" + var9.getId())) {
                            this.setModelPart(var9, "true".equals(var6));
                        }
                    }
                } catch (Exception var111) {
                    LOGGER.warn("Skipping bad option: {}:{}", var5, var6);
                }
            }

            KeyMapping.resetMapping();
        } catch (Exception var12) {
            LOGGER.error("Failed to load options", (Throwable)var12);
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
            var0.println("gamma:" + this.gamma);
            var0.println("renderDistance:" + this.renderDistance);
            var0.println("guiScale:" + this.guiScale);
            var0.println("particles:" + this.particles.getId());
            var0.println("maxFps:" + this.framerateLimit);
            var0.println("difficulty:" + this.difficulty.getId());
            var0.println("fancyGraphics:" + this.fancyGraphics);
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

    public void loadResourcePacks(PackRepository<UnopenedResourcePack> param0) {
        param0.reload();
        Set<UnopenedResourcePack> var0 = Sets.newLinkedHashSet();
        Iterator<String> var1 = this.resourcePacks.iterator();

        while(var1.hasNext()) {
            String var2 = var1.next();
            UnopenedResourcePack var3 = param0.getPack(var2);
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
                var0.add(var3);
            }
        }

        param0.setSelected(var0);
    }
}
