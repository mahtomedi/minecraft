package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
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
    static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> RESOURCE_PACK_TYPE = new TypeToken<List<String>>() {
    };
    public static final int RENDER_DISTANCE_TINY = 2;
    public static final int RENDER_DISTANCE_SHORT = 4;
    public static final int RENDER_DISTANCE_NORMAL = 8;
    public static final int RENDER_DISTANCE_FAR = 12;
    public static final int RENDER_DISTANCE_REALLY_FAR = 16;
    public static final int RENDER_DISTANCE_EXTREME = 32;
    private static final int TOOLTIP_WIDTH = 200;
    private static final Splitter OPTION_SPLITTER = Splitter.on(':').limit(2);
    private static final float DEFAULT_VOLUME = 1.0F;
    public static final String DEFAULT_SOUND_DEVICE = "";
    private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = new TranslatableComponent("options.darkMojangStudiosBackgroundColor.tooltip");
    private final OptionInstance<Boolean> darkMojangStudiosBackground = OptionInstance.createBoolean("options.darkMojangStudiosBackgroundColor", param0x -> {
        List<FormattedCharSequence> var0x = param0x.font.split(ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND, 200);
        return param1x -> var0x;
    }, false);
    private static final Component ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES = new TranslatableComponent("options.hideLightningFlashes.tooltip");
    private final OptionInstance<Boolean> hideLightningFlash = OptionInstance.createBoolean("options.hideLightningFlashes", param0x -> {
        List<FormattedCharSequence> var0x = param0x.font.split(ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES, 200);
        return param1x -> var0x;
    }, false);
    private final OptionInstance<Double> sensitivity = new OptionInstance<>("options.sensitivity", OptionInstance.noTooltip(), param0x -> {
        Component var0x = this.sensitivity().getCaption();
        if (param0x == 0.0) {
            return genericValueLabel(var0x, new TranslatableComponent("options.sensitivity.min"));
        } else {
            return param0x == 1.0 ? genericValueLabel(var0x, new TranslatableComponent("options.sensitivity.max")) : percentValueLabel(var0x, 2.0 * param0x);
        }
    }, OptionInstance.UnitDouble.INSTANCE, 0.5, param0x -> {
    });
    private final OptionInstance<Integer> renderDistance;
    private final OptionInstance<Integer> simulationDistance;
    private int serverRenderDistance = 0;
    private final OptionInstance<Double> entityDistanceScaling = new OptionInstance<>(
        "options.entityDistanceScaling",
        OptionInstance.noTooltip(),
        param0x -> percentValueLabel(this.entityDistanceScaling().getCaption(), param0x),
        new OptionInstance.IntRange(2, 20).xmap(param0x -> (double)param0x / 4.0, param0x -> (int)(param0x * 4.0)),
        Codec.doubleRange(0.5, 5.0),
        1.0,
        param0x -> {
        }
    );
    public static final int UNLIMITED_FRAMERATE_CUTOFF = 260;
    private final OptionInstance<Integer> framerateLimit = new OptionInstance<>(
        "options.framerateLimit",
        OptionInstance.noTooltip(),
        param0x -> {
            Component var0x = this.framerateLimit().getCaption();
            return param0x == 260
                ? genericValueLabel(var0x, new TranslatableComponent("options.framerateLimit.max"))
                : genericValueLabel(var0x, new TranslatableComponent("options.framerate", param0x));
        },
        new OptionInstance.IntRange(1, 26).xmap(param0x -> param0x * 10, param0x -> param0x / 10),
        Codec.intRange(10, 260),
        120,
        param0x -> Minecraft.getInstance().getWindow().setFramerateLimit(param0x)
    );
    private final OptionInstance<CloudStatus> cloudStatus = new OptionInstance<>(
        "options.renderClouds",
        OptionInstance.noTooltip(),
        param0x -> new TranslatableComponent(param0x.getKey()),
        new OptionInstance.Enum<>(
            Arrays.asList(CloudStatus.values()),
            Codec.either(Codec.BOOL, Codec.STRING).xmap(param0x -> param0x.map(param0xx -> param0xx ? CloudStatus.FANCY : CloudStatus.OFF, param0xx -> {
                    return switch(param0xx) {
                        case "true" -> CloudStatus.FANCY;
                        case "fast" -> CloudStatus.FAST;
                        default -> CloudStatus.OFF;
                    };
                }), param0x -> {
                return Either.right(switch(param0x) {
                    case FANCY -> "true";
                    case FAST -> "fast";
                    case OFF -> "false";
                });
            })
        ),
        CloudStatus.FANCY,
        param0x -> {
            if (Minecraft.useShaderTransparency()) {
                RenderTarget var0x = Minecraft.getInstance().levelRenderer.getCloudsTarget();
                if (var0x != null) {
                    var0x.clear(Minecraft.ON_OSX);
                }
            }
    
        }
    );
    private static final Component GRAPHICS_TOOLTIP_FAST = new TranslatableComponent("options.graphics.fast.tooltip");
    private static final Component GRAPHICS_TOOLTIP_FABULOUS = new TranslatableComponent(
        "options.graphics.fabulous.tooltip", new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC)
    );
    private static final Component GRAPHICS_TOOLTIP_FANCY = new TranslatableComponent("options.graphics.fancy.tooltip");
    private final OptionInstance<GraphicsStatus> graphicsMode = new OptionInstance<>(
        "options.graphics",
        param0x -> {
            List<FormattedCharSequence> var0x = param0x.font.split(GRAPHICS_TOOLTIP_FAST, 200);
            List<FormattedCharSequence> var1x = param0x.font.split(GRAPHICS_TOOLTIP_FANCY, 200);
            List<FormattedCharSequence> var2x = param0x.font.split(GRAPHICS_TOOLTIP_FABULOUS, 200);
            return param3 -> {
                return switch(param3) {
                    case FANCY -> var1x;
                    case FAST -> var0x;
                    case FABULOUS -> var2x;
                };
            };
        },
        param0x -> {
            MutableComponent var0x = new TranslatableComponent(param0x.getKey());
            return param0x == GraphicsStatus.FABULOUS ? var0x.withStyle(ChatFormatting.ITALIC) : var0x;
        },
        new OptionInstance.AltEnum<>(
            Arrays.asList(GraphicsStatus.values()),
            Stream.of(GraphicsStatus.values()).filter(param0x -> param0x != GraphicsStatus.FABULOUS).collect(Collectors.toList()),
            () -> Minecraft.getInstance().isRunning() && Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(),
            (param0x, param1x) -> {
                Minecraft var0x = Minecraft.getInstance();
                GpuWarnlistManager var1x = var0x.getGpuWarnlistManager();
                if (param1x == GraphicsStatus.FABULOUS && var1x.willShowWarning()) {
                    var1x.showWarning();
                } else {
                    param0x.set(param1x);
                    var0x.levelRenderer.allChanged();
                }
            },
            Codec.INT.xmap(GraphicsStatus::byId, GraphicsStatus::getId)
        ),
        GraphicsStatus.FANCY,
        param0x -> {
        }
    );
    private final OptionInstance<AmbientOcclusionStatus> ambientOcclusion = new OptionInstance<>(
        "options.ao",
        OptionInstance.noTooltip(),
        param0x -> new TranslatableComponent(param0x.getKey()),
        new OptionInstance.Enum<>(
            Arrays.asList(AmbientOcclusionStatus.values()),
            Codec.either(
                    Codec.BOOL
                        .xmap(
                            param0x -> param0x ? AmbientOcclusionStatus.MAX.getId() : AmbientOcclusionStatus.OFF.getId(),
                            param0x -> param0x == AmbientOcclusionStatus.MAX.getId()
                        ),
                    Codec.INT
                )
                .xmap(param0x -> param0x.map(param0xx -> param0xx, param0xx -> param0xx), Either::right)
                .xmap(AmbientOcclusionStatus::byId, AmbientOcclusionStatus::getId)
        ),
        AmbientOcclusionStatus.MAX,
        param0x -> Minecraft.getInstance().levelRenderer.allChanged()
    );
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_NONE = new TranslatableComponent("options.prioritizeChunkUpdates.none.tooltip");
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED = new TranslatableComponent("options.prioritizeChunkUpdates.byPlayer.tooltip");
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_NEARBY = new TranslatableComponent("options.prioritizeChunkUpdates.nearby.tooltip");
    private final OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates = new OptionInstance<>(
        "options.prioritizeChunkUpdates",
        param0x -> param1x -> {
                return switch(param1x) {
                    case NONE -> param0x.font.split(PRIORITIZE_CHUNK_TOOLTIP_NONE, 200);
                    case PLAYER_AFFECTED -> param0x.font.split(PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED, 200);
                    case NEARBY -> param0x.font.split(PRIORITIZE_CHUNK_TOOLTIP_NEARBY, 200);
                };
            },
        param0x -> new TranslatableComponent(param0x.getKey()),
        new OptionInstance.Enum<>(Arrays.asList(PrioritizeChunkUpdates.values()), Codec.INT.xmap(PrioritizeChunkUpdates::byId, PrioritizeChunkUpdates::getId)),
        PrioritizeChunkUpdates.NONE,
        param0x -> {
        }
    );
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    private final OptionInstance<ChatVisiblity> chatVisibility = new OptionInstance<>(
        "options.chat.visibility",
        OptionInstance.noTooltip(),
        param0x -> new TranslatableComponent(param0x.getKey()),
        new OptionInstance.Enum<>(Arrays.asList(ChatVisiblity.values()), Codec.INT.xmap(ChatVisiblity::byId, ChatVisiblity::getId)),
        ChatVisiblity.FULL,
        param0x -> {
        }
    );
    private final OptionInstance<Double> chatOpacity = new OptionInstance<>(
        "options.chat.opacity",
        OptionInstance.noTooltip(),
        param0x -> percentValueLabel(this.chatOpacity().getCaption(), param0x * 0.9 + 0.1),
        OptionInstance.UnitDouble.INSTANCE,
        1.0,
        param0x -> Minecraft.getInstance().gui.getChat().rescaleChat()
    );
    private final OptionInstance<Double> chatLineSpacing = new OptionInstance<>(
        "options.chat.line_spacing",
        OptionInstance.noTooltip(),
        param0x -> percentValueLabel(this.chatLineSpacing().getCaption(), param0x),
        OptionInstance.UnitDouble.INSTANCE,
        0.0,
        param0x -> {
        }
    );
    private final OptionInstance<Double> textBackgroundOpacity = new OptionInstance<>(
        "options.accessibility.text_background_opacity",
        OptionInstance.noTooltip(),
        param0x -> percentValueLabel(this.textBackgroundOpacity().getCaption(), param0x),
        OptionInstance.UnitDouble.INSTANCE,
        0.5,
        param0x -> Minecraft.getInstance().gui.getChat().rescaleChat()
    );
    @Nullable
    public String fullscreenVideoModeString;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
    private final OptionInstance<HumanoidArm> mainHand = new OptionInstance<>(
        "options.mainHand",
        OptionInstance.noTooltip(),
        HumanoidArm::getName,
        new OptionInstance.Enum<>(
            Arrays.asList(HumanoidArm.values()),
            Codec.STRING
                .xmap(param0x -> "left".equals(param0x) ? HumanoidArm.LEFT : HumanoidArm.RIGHT, param0x -> param0x == HumanoidArm.LEFT ? "left" : "right")
        ),
        HumanoidArm.RIGHT,
        param0x -> this.broadcastOptions()
    );
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    private final OptionInstance<Double> chatScale = new OptionInstance<>(
        "options.chat.scale",
        OptionInstance.noTooltip(),
        param0x -> (Component)(param0x == 0.0
                ? CommonComponents.optionStatus(this.chatScale().getCaption(), false)
                : percentValueLabel(this.chatScale().getCaption(), param0x)),
        OptionInstance.UnitDouble.INSTANCE,
        0.0,
        param0x -> Minecraft.getInstance().gui.getChat().rescaleChat()
    );
    private final OptionInstance<Double> chatWidth = new OptionInstance<>(
        "options.chat.width",
        OptionInstance.noTooltip(),
        param0x -> pixelValueLabel(this.chatWidth().getCaption(), ChatComponent.getWidth(param0x)),
        OptionInstance.UnitDouble.INSTANCE,
        1.0,
        param0x -> Minecraft.getInstance().gui.getChat().rescaleChat()
    );
    private final OptionInstance<Double> chatHeightUnfocused = new OptionInstance<>(
        "options.chat.height.unfocused",
        OptionInstance.noTooltip(),
        param0x -> pixelValueLabel(this.chatHeightUnfocused().getCaption(), ChatComponent.getHeight(param0x)),
        OptionInstance.UnitDouble.INSTANCE,
        ChatComponent.defaultUnfocusedPct(),
        param0x -> Minecraft.getInstance().gui.getChat().rescaleChat()
    );
    private final OptionInstance<Double> chatHeightFocused = new OptionInstance<>(
        "options.chat.height.focused",
        OptionInstance.noTooltip(),
        param0x -> pixelValueLabel(this.chatHeightFocused().getCaption(), ChatComponent.getHeight(param0x)),
        OptionInstance.UnitDouble.INSTANCE,
        1.0,
        param0x -> Minecraft.getInstance().gui.getChat().rescaleChat()
    );
    private final OptionInstance<Double> chatDelay = new OptionInstance<>(
        "options.chat.delay_instant",
        OptionInstance.noTooltip(),
        param0x -> param0x <= 0.0
                ? new TranslatableComponent("options.chat.delay_none")
                : new TranslatableComponent("options.chat.delay", String.format("%.1f", param0x)),
        new OptionInstance.IntRange(0, 60).xmap(param0x -> (double)param0x / 10.0, param0x -> (int)(param0x * 10.0)),
        Codec.doubleRange(0.0, 6.0),
        0.0,
        param0x -> {
        }
    );
    private final OptionInstance<Integer> mipmapLevels = new OptionInstance<>("options.mipmapLevels", OptionInstance.noTooltip(), param0x -> {
        Component var0x = this.mipmapLevels().getCaption();
        return (Component)(param0x == 0 ? CommonComponents.optionStatus(var0x, false) : genericValueLabel(var0x, param0x));
    }, new OptionInstance.IntRange(0, 4), 4, param0x -> {
    });
    private final Object2FloatMap<SoundSource> sourceVolumes = Util.make(new Object2FloatOpenHashMap<>(), param0x -> param0x.defaultReturnValue(1.0F));
    public boolean useNativeTransport = true;
    private final OptionInstance<AttackIndicatorStatus> attackIndicator = new OptionInstance<>(
        "options.attackIndicator",
        OptionInstance.noTooltip(),
        param0x -> new TranslatableComponent(param0x.getKey()),
        new OptionInstance.Enum<>(Arrays.asList(AttackIndicatorStatus.values()), Codec.INT.xmap(AttackIndicatorStatus::byId, AttackIndicatorStatus::getId)),
        AttackIndicatorStatus.CROSSHAIR,
        param0x -> {
        }
    );
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public boolean joinedFirstServer = false;
    public boolean hideBundleTutorial = false;
    private final OptionInstance<Integer> biomeBlendRadius = new OptionInstance<>("options.biomeBlendRadius", OptionInstance.noTooltip(), param0x -> {
        int var0x = param0x * 2 + 1;
        return genericValueLabel(this.biomeBlendRadius().getCaption(), new TranslatableComponent("options.biomeBlendRadius." + var0x));
    }, new OptionInstance.IntRange(0, 7), 2, param0x -> Minecraft.getInstance().levelRenderer.allChanged());
    private final OptionInstance<Double> mouseWheelSensitivity = new OptionInstance<>(
        "options.mouseWheelSensitivity",
        OptionInstance.noTooltip(),
        param0x -> genericValueLabel(this.mouseWheelSensitivity().getCaption(), new TextComponent(String.format("%.2f", param0x))),
        new OptionInstance.IntRange(-200, 100).xmap(Options::logMouse, Options::unlogMouse),
        Codec.doubleRange(logMouse(-200), logMouse(100)),
        logMouse(0),
        param0x -> {
        }
    );
    private final OptionInstance<Boolean> rawMouseInput = OptionInstance.createBoolean("options.rawMouseInput", true, param0x -> {
        Window var0x = Minecraft.getInstance().getWindow();
        if (var0x != null) {
            var0x.updateRawMouseInput(param0x);
        }

    });
    public int glDebugVerbosity = 1;
    private final OptionInstance<Boolean> autoJump = OptionInstance.createBoolean("options.autoJump", true);
    private final OptionInstance<Boolean> autoSuggestions = OptionInstance.createBoolean("options.autoSuggestCommand", true);
    private final OptionInstance<Boolean> chatColors = OptionInstance.createBoolean("options.chat.color", true);
    private final OptionInstance<Boolean> chatLinks = OptionInstance.createBoolean("options.chat.links", true);
    private final OptionInstance<Boolean> chatLinksPrompt = OptionInstance.createBoolean("options.chat.links.prompt", true);
    private final OptionInstance<Boolean> enableVsync = OptionInstance.createBoolean("options.vsync", true, param0x -> {
        if (Minecraft.getInstance().getWindow() != null) {
            Minecraft.getInstance().getWindow().updateVsync(param0x);
        }

    });
    private final OptionInstance<Boolean> entityShadows = OptionInstance.createBoolean("options.entityShadows", true);
    private final OptionInstance<Boolean> forceUnicodeFont = OptionInstance.createBoolean("options.forceUnicodeFont", false, param0x -> {
        Minecraft var0x = Minecraft.getInstance();
        if (var0x.getWindow() != null) {
            var0x.selectMainFont(param0x);
            var0x.resizeDisplay();
        }

    });
    private final OptionInstance<Boolean> invertYMouse = OptionInstance.createBoolean("options.invertMouse", false);
    private final OptionInstance<Boolean> discreteMouseScroll = OptionInstance.createBoolean("options.discrete_mouse_scroll", false);
    private final OptionInstance<Boolean> realmsNotifications = OptionInstance.createBoolean("optionss.realmsNotifications", true);
    private static final Component ALLOW_SERVER_LISTING_TOOLTIP = new TranslatableComponent("options.allowServerListing.tooltip");
    private final OptionInstance<Boolean> allowServerListing = OptionInstance.createBoolean("options.allowServerListing", param0x -> {
        List<FormattedCharSequence> var0x = param0x.font.split(ALLOW_SERVER_LISTING_TOOLTIP, 200);
        return param1x -> var0x;
    }, true, param0x -> this.broadcastOptions());
    private final OptionInstance<Boolean> reducedDebugInfo = OptionInstance.createBoolean("options.reducedDebugInfo", false);
    private final OptionInstance<Boolean> showSubtitles = OptionInstance.createBoolean("options.showSubtitles", false);
    private static final Component DIRECTIONAL_AUDIO_TOOLTIP_ON = new TranslatableComponent("options.directionalAudio.on.tooltip");
    private static final Component DIRECTIONAL_AUDIO_TOOLTIP_OFF = new TranslatableComponent("options.directionalAudio.off.tooltip");
    private final OptionInstance<Boolean> directionalAudio = OptionInstance.createBoolean("options.directionalAudio", param0x -> {
        List<FormattedCharSequence> var0x = param0x.font.split(DIRECTIONAL_AUDIO_TOOLTIP_ON, 200);
        List<FormattedCharSequence> var1x = param0x.font.split(DIRECTIONAL_AUDIO_TOOLTIP_OFF, 200);
        return param2 -> param2 ? var0x : var1x;
    }, false, param0x -> {
        SoundManager var0x = Minecraft.getInstance().getSoundManager();
        var0x.reload();
        var0x.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    });
    private final OptionInstance<Boolean> backgroundForChatOnly = new OptionInstance<>(
        "options.accessibility.text_background",
        OptionInstance.noTooltip(),
        param0x -> param0x
                ? new TranslatableComponent("options.accessibility.text_background.chat")
                : new TranslatableComponent("options.accessibility.text_background.everywhere"),
        OptionInstance.BOOLEAN_VALUES,
        true,
        param0x -> {
        }
    );
    private final OptionInstance<Boolean> touchscreen = OptionInstance.createBoolean("options.touchscreen", false);
    private final OptionInstance<Boolean> fullscreen = OptionInstance.createBoolean("options.fullscreen", false, param0x -> {
        Minecraft var0x = Minecraft.getInstance();
        if (var0x.getWindow() != null && var0x.getWindow().isFullscreen() != param0x) {
            var0x.getWindow().toggleFullScreen();
            this.fullscreen().set(var0x.getWindow().isFullscreen());
        }

    });
    private final OptionInstance<Boolean> bobView = OptionInstance.createBoolean("options.viewBobbing", true);
    private static final Component MOVEMENT_TOGGLE = new TranslatableComponent("options.key.toggle");
    private static final Component MOVEMENT_HOLD = new TranslatableComponent("options.key.hold");
    private final OptionInstance<Boolean> toggleCrouch = new OptionInstance<>(
        "key.sneak", OptionInstance.noTooltip(), param0x -> param0x ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, param0x -> {
        }
    );
    private final OptionInstance<Boolean> toggleSprint = new OptionInstance<>(
        "key.sprint", OptionInstance.noTooltip(), param0x -> param0x ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, param0x -> {
        }
    );
    public boolean skipMultiplayerWarning;
    public boolean skipRealms32bitWarning;
    private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = new TranslatableComponent("options.hideMatchedNames.tooltip");
    private final OptionInstance<Boolean> hideMatchedNames = OptionInstance.createBoolean("options.hideMatchedNames", param0x -> {
        List<FormattedCharSequence> var0x = param0x.font.split(CHAT_TOOLTIP_HIDE_MATCHED_NAMES, 200);
        return param1x -> var0x;
    }, true);
    private final OptionInstance<Boolean> showAutosaveIndicator = OptionInstance.createBoolean("options.autosaveIndicator", true);
    public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
    public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
    public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
    public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
    public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
    public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", this.toggleCrouch::get);
    public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", this.toggleSprint::get);
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
    public boolean hideGui;
    private CameraType cameraType = CameraType.FIRST_PERSON;
    public boolean renderDebug;
    public boolean renderDebugCharts;
    public boolean renderFpsChart;
    public String lastMpIp = "";
    public boolean smoothCamera;
    private final OptionInstance<Integer> fov = new OptionInstance<>(
        "options.fov",
        OptionInstance.noTooltip(),
        param0x -> {
            Component var0x = this.fov().getCaption();
    
            return switch(param0x) {
                case 70 -> genericValueLabel(var0x, new TranslatableComponent("options.fov.min"));
                case 110 -> genericValueLabel(var0x, new TranslatableComponent("options.fov.max"));
                default -> genericValueLabel(var0x, param0x);
            };
        },
        new OptionInstance.IntRange(30, 110),
        Codec.DOUBLE.xmap(param0x -> (int)(param0x * 40.0 + 70.0), param0x -> ((double)param0x.intValue() - 70.0) / 40.0),
        70,
        param0x -> Minecraft.getInstance().levelRenderer.needsUpdate()
    );
    private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = new TranslatableComponent("options.screenEffectScale.tooltip");
    private final OptionInstance<Double> screenEffectScale = new OptionInstance<>(
        "options.screenEffectScale", param0x -> param1x -> param0x.font.split(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT, 200), param0x -> {
            Component var0x = this.screenEffectScale().getCaption();
            return param0x == 0.0 ? genericValueLabel(var0x, CommonComponents.OPTION_OFF) : percentValueLabel(var0x, param0x);
        }, OptionInstance.UnitDouble.INSTANCE, 1.0, param0x -> {
        }
    );
    private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = new TranslatableComponent("options.fovEffectScale.tooltip");
    private final OptionInstance<Double> fovEffectScale = new OptionInstance<>(
        "options.fovEffectScale", param0x -> param1x -> param0x.font.split(ACCESSIBILITY_TOOLTIP_FOV_EFFECT, 200), param0x -> {
            Component var0x = this.fovEffectScale().getCaption();
            return param0x == 0.0 ? genericValueLabel(var0x, CommonComponents.OPTION_OFF) : percentValueLabel(var0x, param0x);
        }, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), Codec.doubleRange(0.0, 1.0), 1.0, param0x -> {
        }
    );
    private static final Component ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT = new TranslatableComponent("options.darknessEffectScale.tooltip");
    private final OptionInstance<Double> darknessEffectScale = new OptionInstance<>(
        "options.darknessEffectScale", param0x -> param1x -> param0x.font.split(ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT, 200), param0x -> {
            Component var0x = this.darknessEffectScale().getCaption();
            return param0x == 0.0 ? genericValueLabel(var0x, CommonComponents.OPTION_OFF) : percentValueLabel(var0x, param0x);
        }, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), 1.0, param0x -> {
        }
    );
    private final OptionInstance<Double> gamma = new OptionInstance<>("options.gamma", OptionInstance.noTooltip(), param0x -> {
        Component var0x = this.gamma().getCaption();
        int var1x = (int)(param0x * 100.0);
        if (var1x == 0) {
            return genericValueLabel(var0x, new TranslatableComponent("options.gamma.min"));
        } else if (var1x == 50) {
            return genericValueLabel(var0x, new TranslatableComponent("options.gamma.default"));
        } else {
            return var1x == 100 ? genericValueLabel(var0x, new TranslatableComponent("options.gamma.max")) : genericValueLabel(var0x, var1x);
        }
    }, OptionInstance.UnitDouble.INSTANCE, 0.5, param0x -> {
    });
    private final OptionInstance<Integer> guiScale = new OptionInstance<>(
        "options.guiScale",
        OptionInstance.noTooltip(),
        param0x -> (Component)(param0x == 0 ? new TranslatableComponent("options.guiScale.auto") : new TextComponent(Integer.toString(param0x))),
        OptionInstance.clampingLazyMax(0, () -> {
            Minecraft var0x = Minecraft.getInstance();
            return !var0x.isRunning() ? 2147483646 : var0x.getWindow().calculateScale(0, var0x.isEnforceUnicode());
        }),
        0,
        param0x -> {
        }
    );
    private final OptionInstance<ParticleStatus> particles = new OptionInstance<>(
        "options.particles",
        OptionInstance.noTooltip(),
        param0x -> new TranslatableComponent(param0x.getKey()),
        new OptionInstance.Enum<>(Arrays.asList(ParticleStatus.values()), Codec.INT.xmap(ParticleStatus::byId, ParticleStatus::getId)),
        ParticleStatus.ALL,
        param0x -> {
        }
    );
    private final OptionInstance<NarratorStatus> narrator = new OptionInstance<>(
        "options.narrator",
        OptionInstance.noTooltip(),
        param0x -> (Component)(NarratorChatListener.INSTANCE.isActive() ? param0x.getName() : new TranslatableComponent("options.narrator.notavailable")),
        new OptionInstance.Enum<>(Arrays.asList(NarratorStatus.values()), Codec.INT.xmap(NarratorStatus::byId, NarratorStatus::getId)),
        NarratorStatus.OFF,
        param0x -> NarratorChatListener.INSTANCE.updateNarratorStatus(param0x)
    );
    public String languageCode = "en_us";
    private final OptionInstance<String> soundDevice = new OptionInstance<>(
        "options.audioDevice",
        OptionInstance.noTooltip(),
        param0x -> {
            if ("".equals(param0x)) {
                return new TranslatableComponent("options.audioDevice.default");
            } else {
                return param0x.startsWith("OpenAL Soft on ")
                    ? new TextComponent(param0x.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH))
                    : new TextComponent(param0x);
            }
        },
        new OptionInstance.LazyEnum<>(
            () -> Stream.concat(Stream.of(""), Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().stream()).toList(),
            param0x -> Minecraft.getInstance().isRunning()
                        && param0x != ""
                        && !Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().contains(param0x)
                    ? Optional.empty()
                    : Optional.of(param0x),
            Codec.STRING
        ),
        "",
        param0x -> {
            SoundManager var0x = Minecraft.getInstance().getSoundManager();
            var0x.reload();
            var0x.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }
    );
    public boolean syncWrites;

    public OptionInstance<Boolean> darkMojangStudiosBackground() {
        return this.darkMojangStudiosBackground;
    }

    public OptionInstance<Boolean> hideLightningFlash() {
        return this.hideLightningFlash;
    }

    public OptionInstance<Double> sensitivity() {
        return this.sensitivity;
    }

    public OptionInstance<Integer> renderDistance() {
        return this.renderDistance;
    }

    public OptionInstance<Integer> simulationDistance() {
        return this.simulationDistance;
    }

    public OptionInstance<Double> entityDistanceScaling() {
        return this.entityDistanceScaling;
    }

    public OptionInstance<Integer> framerateLimit() {
        return this.framerateLimit;
    }

    public OptionInstance<CloudStatus> cloudStatus() {
        return this.cloudStatus;
    }

    public OptionInstance<GraphicsStatus> graphicsMode() {
        return this.graphicsMode;
    }

    public OptionInstance<AmbientOcclusionStatus> ambientOcclusion() {
        return this.ambientOcclusion;
    }

    public OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates() {
        return this.prioritizeChunkUpdates;
    }

    public OptionInstance<ChatVisiblity> chatVisibility() {
        return this.chatVisibility;
    }

    public OptionInstance<Double> chatOpacity() {
        return this.chatOpacity;
    }

    public OptionInstance<Double> chatLineSpacing() {
        return this.chatLineSpacing;
    }

    public OptionInstance<Double> textBackgroundOpacity() {
        return this.textBackgroundOpacity;
    }

    public OptionInstance<HumanoidArm> mainHand() {
        return this.mainHand;
    }

    public OptionInstance<Double> chatScale() {
        return this.chatScale;
    }

    public OptionInstance<Double> chatWidth() {
        return this.chatWidth;
    }

    public OptionInstance<Double> chatHeightUnfocused() {
        return this.chatHeightUnfocused;
    }

    public OptionInstance<Double> chatHeightFocused() {
        return this.chatHeightFocused;
    }

    public OptionInstance<Double> chatDelay() {
        return this.chatDelay;
    }

    public OptionInstance<Integer> mipmapLevels() {
        return this.mipmapLevels;
    }

    public OptionInstance<AttackIndicatorStatus> attackIndicator() {
        return this.attackIndicator;
    }

    public OptionInstance<Integer> biomeBlendRadius() {
        return this.biomeBlendRadius;
    }

    private static double logMouse(int param0) {
        return Math.pow(10.0, (double)param0 / 100.0);
    }

    private static int unlogMouse(double param0) {
        return Mth.floor(Math.log10(param0) * 100.0);
    }

    public OptionInstance<Double> mouseWheelSensitivity() {
        return this.mouseWheelSensitivity;
    }

    public OptionInstance<Boolean> rawMouseInput() {
        return this.rawMouseInput;
    }

    public OptionInstance<Boolean> autoJump() {
        return this.autoJump;
    }

    public OptionInstance<Boolean> autoSuggestions() {
        return this.autoSuggestions;
    }

    public OptionInstance<Boolean> chatColors() {
        return this.chatColors;
    }

    public OptionInstance<Boolean> chatLinks() {
        return this.chatLinks;
    }

    public OptionInstance<Boolean> chatLinksPrompt() {
        return this.chatLinksPrompt;
    }

    public OptionInstance<Boolean> enableVsync() {
        return this.enableVsync;
    }

    public OptionInstance<Boolean> entityShadows() {
        return this.entityShadows;
    }

    public OptionInstance<Boolean> forceUnicodeFont() {
        return this.forceUnicodeFont;
    }

    public OptionInstance<Boolean> invertYMouse() {
        return this.invertYMouse;
    }

    public OptionInstance<Boolean> discreteMouseScroll() {
        return this.discreteMouseScroll;
    }

    public OptionInstance<Boolean> realmsNotifications() {
        return this.realmsNotifications;
    }

    public OptionInstance<Boolean> allowServerListing() {
        return this.allowServerListing;
    }

    public OptionInstance<Boolean> reducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public OptionInstance<Boolean> showSubtitles() {
        return this.showSubtitles;
    }

    public OptionInstance<Boolean> directionalAudio() {
        return this.directionalAudio;
    }

    public OptionInstance<Boolean> backgroundForChatOnly() {
        return this.backgroundForChatOnly;
    }

    public OptionInstance<Boolean> touchscreen() {
        return this.touchscreen;
    }

    public OptionInstance<Boolean> fullscreen() {
        return this.fullscreen;
    }

    public OptionInstance<Boolean> bobView() {
        return this.bobView;
    }

    public OptionInstance<Boolean> toggleCrouch() {
        return this.toggleCrouch;
    }

    public OptionInstance<Boolean> toggleSprint() {
        return this.toggleSprint;
    }

    public OptionInstance<Boolean> hideMatchedNames() {
        return this.hideMatchedNames;
    }

    public OptionInstance<Boolean> showAutosaveIndicator() {
        return this.showAutosaveIndicator;
    }

    public OptionInstance<Integer> fov() {
        return this.fov;
    }

    public OptionInstance<Double> screenEffectScale() {
        return this.screenEffectScale;
    }

    public OptionInstance<Double> fovEffectScale() {
        return this.fovEffectScale;
    }

    public OptionInstance<Double> darknessEffectScale() {
        return this.darknessEffectScale;
    }

    public OptionInstance<Double> gamma() {
        return this.gamma;
    }

    public OptionInstance<Integer> guiScale() {
        return this.guiScale;
    }

    public OptionInstance<ParticleStatus> particles() {
        return this.particles;
    }

    public OptionInstance<NarratorStatus> narrator() {
        return this.narrator;
    }

    public OptionInstance<String> soundDevice() {
        return this.soundDevice;
    }

    public Options(Minecraft param0, File param1) {
        this.minecraft = param0;
        this.optionsFile = new File(param1, "options.txt");
        boolean var0 = param0.is64Bit();
        boolean var1 = var0 && Runtime.getRuntime().maxMemory() >= 1000000000L;
        this.renderDistance = new OptionInstance<>(
            "options.renderDistance",
            OptionInstance.noTooltip(),
            param0x -> genericValueLabel(this.renderDistance().getCaption(), new TranslatableComponent("options.chunks", param0x)),
            new OptionInstance.IntRange(2, var1 ? 32 : 16),
            var0 ? 12 : 8,
            param0x -> Minecraft.getInstance().levelRenderer.needsUpdate()
        );
        this.simulationDistance = new OptionInstance<>(
            "options.simulationDistance",
            OptionInstance.noTooltip(),
            param0x -> genericValueLabel(this.simulationDistance().getCaption(), new TranslatableComponent("options.chunks", param0x)),
            new OptionInstance.IntRange(5, var1 ? 32 : 16),
            var0 ? 12 : 8,
            param0x -> {
            }
        );
        this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
        this.load();
    }

    public float getBackgroundOpacity(float param0) {
        return this.backgroundForChatOnly.get() ? param0 : this.textBackgroundOpacity().get().floatValue();
    }

    public int getBackgroundColor(float param0) {
        return (int)(this.getBackgroundOpacity(param0) * 255.0F) << 24 & 0xFF000000;
    }

    public int getBackgroundColor(int param0) {
        return this.backgroundForChatOnly.get() ? param0 : (int)(this.textBackgroundOpacity.get() * 255.0) << 24 & 0xFF000000;
    }

    public void setKey(KeyMapping param0, InputConstants.Key param1) {
        param0.setKey(param1);
        this.save();
    }

    private void processOptions(Options.FieldAccess param0) {
        param0.process("autoJump", this.autoJump);
        param0.process("autoSuggestions", this.autoSuggestions);
        param0.process("chatColors", this.chatColors);
        param0.process("chatLinks", this.chatLinks);
        param0.process("chatLinksPrompt", this.chatLinksPrompt);
        param0.process("enableVsync", this.enableVsync);
        param0.process("entityShadows", this.entityShadows);
        param0.process("forceUnicodeFont", this.forceUnicodeFont);
        param0.process("discrete_mouse_scroll", this.discreteMouseScroll);
        param0.process("invertYMouse", this.invertYMouse);
        param0.process("realmsNotifications", this.realmsNotifications);
        param0.process("reducedDebugInfo", this.reducedDebugInfo);
        param0.process("showSubtitles", this.showSubtitles);
        param0.process("directionalAudio", this.directionalAudio);
        param0.process("touchscreen", this.touchscreen);
        param0.process("fullscreen", this.fullscreen);
        param0.process("bobView", this.bobView);
        param0.process("toggleCrouch", this.toggleCrouch);
        param0.process("toggleSprint", this.toggleSprint);
        param0.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
        param0.process("hideLightningFlashes", this.hideLightningFlash);
        param0.process("mouseSensitivity", this.sensitivity);
        param0.process("fov", this.fov);
        param0.process("screenEffectScale", this.screenEffectScale);
        param0.process("fovEffectScale", this.fovEffectScale);
        param0.process("gamma", this.gamma);
        param0.process("renderDistance", this.renderDistance);
        param0.process("simulationDistance", this.simulationDistance);
        param0.process("entityDistanceScaling", this.entityDistanceScaling);
        param0.process("guiScale", this.guiScale);
        param0.process("particles", this.particles);
        param0.process("maxFps", this.framerateLimit);
        param0.process("graphicsMode", this.graphicsMode);
        param0.process("ao", this.ambientOcclusion);
        param0.process("prioritizeChunkUpdates", this.prioritizeChunkUpdates);
        param0.process("biomeBlendRadius", this.biomeBlendRadius);
        param0.process("renderClouds", this.cloudStatus);
        this.resourcePacks = param0.process("resourcePacks", this.resourcePacks, Options::readPackList, GSON::toJson);
        this.incompatibleResourcePacks = param0.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readPackList, GSON::toJson);
        this.lastMpIp = param0.process("lastServer", this.lastMpIp);
        this.languageCode = param0.process("lang", this.languageCode);
        param0.process("soundDevice", this.soundDevice);
        param0.process("chatVisibility", this.chatVisibility);
        param0.process("chatOpacity", this.chatOpacity);
        param0.process("chatLineSpacing", this.chatLineSpacing);
        param0.process("textBackgroundOpacity", this.textBackgroundOpacity);
        param0.process("backgroundForChatOnly", this.backgroundForChatOnly);
        this.hideServerAddress = param0.process("hideServerAddress", this.hideServerAddress);
        this.advancedItemTooltips = param0.process("advancedItemTooltips", this.advancedItemTooltips);
        this.pauseOnLostFocus = param0.process("pauseOnLostFocus", this.pauseOnLostFocus);
        this.overrideWidth = param0.process("overrideWidth", this.overrideWidth);
        this.overrideHeight = param0.process("overrideHeight", this.overrideHeight);
        this.heldItemTooltips = param0.process("heldItemTooltips", this.heldItemTooltips);
        param0.process("chatHeightFocused", this.chatHeightFocused);
        param0.process("chatDelay", this.chatDelay);
        param0.process("chatHeightUnfocused", this.chatHeightUnfocused);
        param0.process("chatScale", this.chatScale);
        param0.process("chatWidth", this.chatWidth);
        param0.process("mipmapLevels", this.mipmapLevels);
        this.useNativeTransport = param0.process("useNativeTransport", this.useNativeTransport);
        param0.process("mainHand", this.mainHand);
        param0.process("attackIndicator", this.attackIndicator);
        param0.process("narrator", this.narrator);
        this.tutorialStep = param0.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
        param0.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
        param0.process("rawMouseInput", this.rawMouseInput);
        this.glDebugVerbosity = param0.process("glDebugVerbosity", this.glDebugVerbosity);
        this.skipMultiplayerWarning = param0.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
        this.skipRealms32bitWarning = param0.process("skipRealms32bitWarning", this.skipRealms32bitWarning);
        param0.process("hideMatchedNames", this.hideMatchedNames);
        this.joinedFirstServer = param0.process("joinedFirstServer", this.joinedFirstServer);
        this.hideBundleTutorial = param0.process("hideBundleTutorial", this.hideBundleTutorial);
        this.syncWrites = param0.process("syncChunkWrites", this.syncWrites);
        param0.process("showAutosaveIndicator", this.showAutosaveIndicator);
        param0.process("allowServerListing", this.allowServerListing);

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
                    this.graphicsMode.set(GraphicsStatus.FANCY);
                } else {
                    this.graphicsMode.set(GraphicsStatus.FAST);
                }
            }

            this.processOptions(
                new Options.FieldAccess() {
                    @Nullable
                    private String getValueOrNull(String param0) {
                        return var2.contains(param0) ? var2.getString(param0) : null;
                    }
    
                    @Override
                    public <T> void process(String param0, OptionInstance<T> param1) {
                        String var0 = this.getValueOrNull(param0);
                        if (var0 != null) {
                            JsonReader var1 = new JsonReader(new StringReader(var0.isEmpty() ? "\"\"" : var0));
                            JsonElement var2 = JsonParser.parseReader(var1);
                            DataResult<T> var3 = param1.codec().parse(JsonOps.INSTANCE, var2);
                            var3.error()
                                .ifPresent(
                                    param2 -> Options.LOGGER.error("Error parsing option value " + var0 + " for option " + param1 + ": " + param2.message())
                                );
                            var3.result().ifPresent(param1::set);
                        }
    
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
                }
            );
            if (var2.contains("fullscreenResolution")) {
                this.fullscreenVideoModeString = var2.getString("fullscreenResolution");
            }

            if (this.minecraft.getWindow() != null) {
                this.minecraft.getWindow().setFramerateLimit(this.framerateLimit.get());
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
                public <T> void process(String param0, OptionInstance<T> param1) {
                    DataResult<JsonElement> var0 = param1.codec().encodeStart(JsonOps.INSTANCE, param1.get());
                    var0.error().ifPresent(param1x -> Options.LOGGER.error("Error saving option " + param1 + ": " + param1x));
                    var0.result().ifPresent(param2 -> {
                        this.writePrefix(param0);
                        var0.println(Options.GSON.toJson(param2));
                    });
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
                        this.renderDistance.get(),
                        this.chatVisibility.get(),
                        this.chatColors.get(),
                        var0,
                        this.mainHand.get(),
                        this.minecraft.isTextFilteringEnabled(),
                        this.allowServerListing.get()
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
        return this.getEffectiveRenderDistance() >= 4 ? this.cloudStatus.get() : CloudStatus.OFF;
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

    public File getFile() {
        return this.optionsFile;
    }

    public String dumpOptionsForReport() {
        Stream<Pair<String, Object>> var0 = Stream.<Pair<String, Object>>builder()
            .add(Pair.of("ao", this.ambientOcclusion.get()))
            .add(Pair.of("biomeBlendRadius", this.biomeBlendRadius.get()))
            .add(Pair.of("enableVsync", this.enableVsync.get()))
            .add(Pair.of("entityDistanceScaling", this.entityDistanceScaling.get()))
            .add(Pair.of("entityShadows", this.entityShadows.get()))
            .add(Pair.of("forceUnicodeFont", this.forceUnicodeFont.get()))
            .add(Pair.of("fov", this.fov.get()))
            .add(Pair.of("fovEffectScale", this.fovEffectScale.get()))
            .add(Pair.of("prioritizeChunkUpdates", this.prioritizeChunkUpdates.get()))
            .add(Pair.of("fullscreen", this.fullscreen.get()))
            .add(Pair.of("fullscreenResolution", String.valueOf(this.fullscreenVideoModeString)))
            .add(Pair.of("gamma", this.gamma.get()))
            .add(Pair.of("glDebugVerbosity", this.glDebugVerbosity))
            .add(Pair.of("graphicsMode", this.graphicsMode.get()))
            .add(Pair.of("guiScale", this.guiScale.get()))
            .add(Pair.of("maxFps", this.framerateLimit.get()))
            .add(Pair.of("mipmapLevels", this.mipmapLevels.get()))
            .add(Pair.of("narrator", this.narrator.get()))
            .add(Pair.of("overrideHeight", this.overrideHeight))
            .add(Pair.of("overrideWidth", this.overrideWidth))
            .add(Pair.of("particles", this.particles.get()))
            .add(Pair.of("reducedDebugInfo", this.reducedDebugInfo.get()))
            .add(Pair.of("renderClouds", this.cloudStatus.get()))
            .add(Pair.of("renderDistance", this.renderDistance.get()))
            .add(Pair.of("simulationDistance", this.simulationDistance.get()))
            .add(Pair.of("resourcePacks", this.resourcePacks))
            .add(Pair.of("screenEffectScale", this.screenEffectScale.get()))
            .add(Pair.of("syncChunkWrites", this.syncWrites))
            .add(Pair.of("useNativeTransport", this.useNativeTransport))
            .add(Pair.of("soundDevice", this.soundDevice.get()))
            .build();
        return var0.<String>map(param0 -> (String)param0.getFirst() + ": " + param0.getSecond()).collect(Collectors.joining(System.lineSeparator()));
    }

    public void setServerRenderDistance(int param0) {
        this.serverRenderDistance = param0;
    }

    public int getEffectiveRenderDistance() {
        return this.serverRenderDistance > 0 ? Math.min(this.renderDistance.get(), this.serverRenderDistance) : this.renderDistance.get();
    }

    private static Component pixelValueLabel(Component param0, int param1) {
        return new TranslatableComponent("options.pixel_value", param0, param1);
    }

    private static Component percentValueLabel(Component param0, double param1) {
        return new TranslatableComponent("options.percent_value", param0, (int)(param1 * 100.0));
    }

    public static Component genericValueLabel(Component param0, Component param1) {
        return new TranslatableComponent("options.generic_value", param0, param1);
    }

    public static Component genericValueLabel(Component param0, int param1) {
        return genericValueLabel(param0, new TextComponent(Integer.toString(param1)));
    }

    @OnlyIn(Dist.CLIENT)
    interface FieldAccess {
        <T> void process(String var1, OptionInstance<T> var2);

        int process(String var1, int var2);

        boolean process(String var1, boolean var2);

        String process(String var1, String var2);

        float process(String var1, float var2);

        <T> T process(String var1, T var2, Function<String, T> var3, Function<T, String> var4);
    }
}
