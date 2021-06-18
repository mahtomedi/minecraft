package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Option {
    protected static final int OPTIONS_TOOLTIP_WIDTH = 200;
    public static final ProgressOption BIOME_BLEND_RADIUS = new ProgressOption(
        "options.biomeBlendRadius", 0.0, 7.0, 1.0F, param0 -> (double)param0.biomeBlendRadius, (param0, param1) -> {
            param0.biomeBlendRadius = Mth.clamp((int)param1.doubleValue(), 0, 7);
            Minecraft.getInstance().levelRenderer.allChanged();
        }, (param0, param1) -> {
            double var0 = param1.get(param0);
            int var1 = (int)var0 * 2 + 1;
            return param1.genericValueLabel(new TranslatableComponent("options.biomeBlendRadius." + var1));
        }
    );
    public static final ProgressOption CHAT_HEIGHT_FOCUSED = new ProgressOption(
        "options.chat.height.focused", 0.0, 1.0, 0.0F, param0 -> param0.chatHeightFocused, (param0, param1) -> {
            param0.chatHeightFocused = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.pixelValueLabel(ChatComponent.getHeight(var0));
        }
    );
    public static final ProgressOption CHAT_HEIGHT_UNFOCUSED = new ProgressOption(
        "options.chat.height.unfocused", 0.0, 1.0, 0.0F, param0 -> param0.chatHeightUnfocused, (param0, param1) -> {
            param0.chatHeightUnfocused = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.pixelValueLabel(ChatComponent.getHeight(var0));
        }
    );
    public static final ProgressOption CHAT_OPACITY = new ProgressOption(
        "options.chat.opacity", 0.0, 1.0, 0.0F, param0 -> param0.chatOpacity, (param0, param1) -> {
            param0.chatOpacity = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.percentValueLabel(var0 * 0.9 + 0.1);
        }
    );
    public static final ProgressOption CHAT_SCALE = new ProgressOption("options.chat.scale", 0.0, 1.0, 0.0F, param0 -> param0.chatScale, (param0, param1) -> {
        param0.chatScale = param1;
        Minecraft.getInstance().gui.getChat().rescaleChat();
    }, (param0, param1) -> {
        double var0 = param1.toPct(param1.get(param0));
        return (Component)(var0 == 0.0 ? CommonComponents.optionStatus(param1.getCaption(), false) : param1.percentValueLabel(var0));
    });
    public static final ProgressOption CHAT_WIDTH = new ProgressOption("options.chat.width", 0.0, 1.0, 0.0F, param0 -> param0.chatWidth, (param0, param1) -> {
        param0.chatWidth = param1;
        Minecraft.getInstance().gui.getChat().rescaleChat();
    }, (param0, param1) -> {
        double var0 = param1.toPct(param1.get(param0));
        return param1.pixelValueLabel(ChatComponent.getWidth(var0));
    });
    public static final ProgressOption CHAT_LINE_SPACING = new ProgressOption(
        "options.chat.line_spacing",
        0.0,
        1.0,
        0.0F,
        param0 -> param0.chatLineSpacing,
        (param0, param1) -> param0.chatLineSpacing = param1,
        (param0, param1) -> param1.percentValueLabel(param1.toPct(param1.get(param0)))
    );
    public static final ProgressOption CHAT_DELAY = new ProgressOption(
        "options.chat.delay_instant",
        0.0,
        6.0,
        0.1F,
        param0 -> param0.chatDelay,
        (param0, param1) -> param0.chatDelay = param1,
        (param0, param1) -> {
            double var0 = param1.get(param0);
            return var0 <= 0.0
                ? new TranslatableComponent("options.chat.delay_none")
                : new TranslatableComponent("options.chat.delay", String.format("%.1f", var0));
        }
    );
    public static final ProgressOption FOV = new ProgressOption("options.fov", 30.0, 110.0, 1.0F, param0 -> param0.fov, (param0, param1) -> {
        param0.fov = param1;
        Minecraft.getInstance().levelRenderer.needsUpdate();
    }, (param0, param1) -> {
        double var0 = param1.get(param0);
        if (var0 == 70.0) {
            return param1.genericValueLabel(new TranslatableComponent("options.fov.min"));
        } else {
            return var0 == param1.getMaxValue() ? param1.genericValueLabel(new TranslatableComponent("options.fov.max")) : param1.genericValueLabel((int)var0);
        }
    });
    private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = new TranslatableComponent("options.fovEffectScale.tooltip");
    public static final ProgressOption FOV_EFFECTS_SCALE = new ProgressOption(
        "options.fovEffectScale",
        0.0,
        1.0,
        0.0F,
        param0 -> Math.pow((double)param0.fovEffectScale, 2.0),
        (param0, param1) -> param0.fovEffectScale = (float)Math.sqrt(param1),
        (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return var0 == 0.0 ? param1.genericValueLabel(CommonComponents.OPTION_OFF) : param1.percentValueLabel(var0);
        },
        param0 -> param0.font.split(ACCESSIBILITY_TOOLTIP_FOV_EFFECT, 200)
    );
    private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = new TranslatableComponent("options.screenEffectScale.tooltip");
    public static final ProgressOption SCREEN_EFFECTS_SCALE = new ProgressOption(
        "options.screenEffectScale",
        0.0,
        1.0,
        0.0F,
        param0 -> (double)param0.screenEffectScale,
        (param0, param1) -> param0.screenEffectScale = param1.floatValue(),
        (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return var0 == 0.0 ? param1.genericValueLabel(CommonComponents.OPTION_OFF) : param1.percentValueLabel(var0);
        },
        param0 -> param0.font.split(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT, 200)
    );
    public static final ProgressOption FRAMERATE_LIMIT = new ProgressOption(
        "options.framerateLimit",
        10.0,
        260.0,
        10.0F,
        param0 -> (double)param0.framerateLimit,
        (param0, param1) -> {
            param0.framerateLimit = (int)param1.doubleValue();
            Minecraft.getInstance().getWindow().setFramerateLimit(param0.framerateLimit);
        },
        (param0, param1) -> {
            double var0 = param1.get(param0);
            return var0 == param1.getMaxValue()
                ? param1.genericValueLabel(new TranslatableComponent("options.framerateLimit.max"))
                : param1.genericValueLabel(new TranslatableComponent("options.framerate", (int)var0));
        }
    );
    public static final ProgressOption GAMMA = new ProgressOption(
        "options.gamma", 0.0, 1.0, 0.0F, param0 -> param0.gamma, (param0, param1) -> param0.gamma = param1, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            if (var0 == 0.0) {
                return param1.genericValueLabel(new TranslatableComponent("options.gamma.min"));
            } else {
                return var0 == 1.0
                    ? param1.genericValueLabel(new TranslatableComponent("options.gamma.max"))
                    : param1.percentAddValueLabel((int)(var0 * 100.0));
            }
        }
    );
    public static final ProgressOption MIPMAP_LEVELS = new ProgressOption(
        "options.mipmapLevels",
        0.0,
        4.0,
        1.0F,
        param0 -> (double)param0.mipmapLevels,
        (param0, param1) -> param0.mipmapLevels = (int)param1.doubleValue(),
        (param0, param1) -> {
            double var0 = param1.get(param0);
            return (Component)(var0 == 0.0 ? CommonComponents.optionStatus(param1.getCaption(), false) : param1.genericValueLabel((int)var0));
        }
    );
    public static final ProgressOption MOUSE_WHEEL_SENSITIVITY = new LogaritmicProgressOption(
        "options.mouseWheelSensitivity",
        0.01,
        10.0,
        0.01F,
        param0 -> param0.mouseWheelSensitivity,
        (param0, param1) -> param0.mouseWheelSensitivity = param1,
        (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.genericValueLabel(new TextComponent(String.format("%.2f", param1.toValue(var0))));
        }
    );
    public static final CycleOption<Boolean> RAW_MOUSE_INPUT = CycleOption.createOnOff(
        "options.rawMouseInput", param0 -> param0.rawMouseInput, (param0, param1, param2) -> {
            param0.rawMouseInput = param2;
            Window var0 = Minecraft.getInstance().getWindow();
            if (var0 != null) {
                var0.updateRawMouseInput(param2);
            }
    
        }
    );
    public static final ProgressOption RENDER_DISTANCE = new ProgressOption(
        "options.renderDistance", 2.0, 16.0, 1.0F, param0 -> (double)param0.renderDistance, (param0, param1) -> {
            param0.renderDistance = (int)param1.doubleValue();
            Minecraft.getInstance().levelRenderer.needsUpdate();
        }, (param0, param1) -> {
            double var0 = param1.get(param0);
            return param1.genericValueLabel(new TranslatableComponent("options.chunks", (int)var0));
        }
    );
    public static final ProgressOption ENTITY_DISTANCE_SCALING = new ProgressOption(
        "options.entityDistanceScaling",
        0.5,
        5.0,
        0.25F,
        param0 -> (double)param0.entityDistanceScaling,
        (param0, param1) -> param0.entityDistanceScaling = (float)param1.doubleValue(),
        (param0, param1) -> {
            double var0 = param1.get(param0);
            return param1.percentValueLabel(var0);
        }
    );
    public static final ProgressOption SENSITIVITY = new ProgressOption(
        "options.sensitivity", 0.0, 1.0, 0.0F, param0 -> param0.sensitivity, (param0, param1) -> param0.sensitivity = param1, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            if (var0 == 0.0) {
                return param1.genericValueLabel(new TranslatableComponent("options.sensitivity.min"));
            } else {
                return var0 == 1.0 ? param1.genericValueLabel(new TranslatableComponent("options.sensitivity.max")) : param1.percentValueLabel(2.0 * var0);
            }
        }
    );
    public static final ProgressOption TEXT_BACKGROUND_OPACITY = new ProgressOption(
        "options.accessibility.text_background_opacity", 0.0, 1.0, 0.0F, param0 -> param0.textBackgroundOpacity, (param0, param1) -> {
            param0.textBackgroundOpacity = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> param1.percentValueLabel(param1.toPct(param1.get(param0)))
    );
    public static final CycleOption<AmbientOcclusionStatus> AMBIENT_OCCLUSION = CycleOption.create(
        "options.ao",
        AmbientOcclusionStatus.values(),
        param0 -> new TranslatableComponent(param0.getKey()),
        param0 -> param0.ambientOcclusion,
        (param0, param1, param2) -> {
            param0.ambientOcclusion = param2;
            Minecraft.getInstance().levelRenderer.allChanged();
        }
    );
    public static final CycleOption<AttackIndicatorStatus> ATTACK_INDICATOR = CycleOption.create(
        "options.attackIndicator",
        AttackIndicatorStatus.values(),
        param0 -> new TranslatableComponent(param0.getKey()),
        param0 -> param0.attackIndicator,
        (param0, param1, param2) -> param0.attackIndicator = param2
    );
    public static final CycleOption<ChatVisiblity> CHAT_VISIBILITY = CycleOption.create(
        "options.chat.visibility",
        ChatVisiblity.values(),
        param0 -> new TranslatableComponent(param0.getKey()),
        param0 -> param0.chatVisibility,
        (param0, param1, param2) -> param0.chatVisibility = param2
    );
    private static final Component GRAPHICS_TOOLTIP_FAST = new TranslatableComponent("options.graphics.fast.tooltip");
    private static final Component GRAPHICS_TOOLTIP_FABULOUS = new TranslatableComponent(
        "options.graphics.fabulous.tooltip", new TranslatableComponent("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC)
    );
    private static final Component GRAPHICS_TOOLTIP_FANCY = new TranslatableComponent("options.graphics.fancy.tooltip");
    public static final CycleOption<GraphicsStatus> GRAPHICS = CycleOption.<GraphicsStatus>create(
            "options.graphics",
            Arrays.asList(GraphicsStatus.values()),
            Stream.of(GraphicsStatus.values()).filter(param0 -> param0 != GraphicsStatus.FABULOUS).collect(Collectors.toList()),
            () -> Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(),
            param0 -> {
                MutableComponent var0 = new TranslatableComponent(param0.getKey());
                return param0 == GraphicsStatus.FABULOUS ? var0.withStyle(ChatFormatting.ITALIC) : var0;
            },
            param0 -> param0.graphicsMode,
            (param0, param1, param2) -> {
                Minecraft var0 = Minecraft.getInstance();
                GpuWarnlistManager var1 = var0.getGpuWarnlistManager();
                if (param2 == GraphicsStatus.FABULOUS && var1.willShowWarning()) {
                    var1.showWarning();
                } else {
                    param0.graphicsMode = param2;
                    var0.levelRenderer.allChanged();
                }
            }
        )
        .setTooltip(param0 -> {
            List<FormattedCharSequence> var0 = param0.font.split(GRAPHICS_TOOLTIP_FAST, 200);
            List<FormattedCharSequence> var1 = param0.font.split(GRAPHICS_TOOLTIP_FANCY, 200);
            List<FormattedCharSequence> var2 = param0.font.split(GRAPHICS_TOOLTIP_FABULOUS, 200);
            return param3 -> {
                switch(param3) {
                    case FANCY:
                        return var1;
                    case FAST:
                        return var0;
                    case FABULOUS:
                        return var2;
                    default:
                        return ImmutableList.of();
                }
            };
        });
    public static final CycleOption GUI_SCALE = CycleOption.create(
        "options.guiScale",
        () -> IntStream.rangeClosed(0, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()))
                .boxed()
                .collect(Collectors.toList()),
        param0 -> (Component)(param0 == 0 ? new TranslatableComponent("options.guiScale.auto") : new TextComponent(Integer.toString(param0))),
        param0 -> param0.guiScale,
        (param0, param1, param2) -> param0.guiScale = param2
    );
    public static final CycleOption<HumanoidArm> MAIN_HAND = CycleOption.create(
        "options.mainHand", HumanoidArm.values(), HumanoidArm::getName, param0 -> param0.mainHand, (param0, param1, param2) -> {
            param0.mainHand = param2;
            param0.broadcastOptions();
        }
    );
    public static final CycleOption<NarratorStatus> NARRATOR = CycleOption.create(
        "options.narrator",
        NarratorStatus.values(),
        param0 -> (Component)(NarratorChatListener.INSTANCE.isActive() ? param0.getName() : new TranslatableComponent("options.narrator.notavailable")),
        param0 -> param0.narratorStatus,
        (param0, param1, param2) -> {
            param0.narratorStatus = param2;
            NarratorChatListener.INSTANCE.updateNarratorStatus(param2);
        }
    );
    public static final CycleOption<ParticleStatus> PARTICLES = CycleOption.create(
        "options.particles",
        ParticleStatus.values(),
        param0 -> new TranslatableComponent(param0.getKey()),
        param0 -> param0.particles,
        (param0, param1, param2) -> param0.particles = param2
    );
    public static final CycleOption<CloudStatus> RENDER_CLOUDS = CycleOption.create(
        "options.renderClouds",
        CloudStatus.values(),
        param0 -> new TranslatableComponent(param0.getKey()),
        param0 -> param0.renderClouds,
        (param0, param1, param2) -> {
            param0.renderClouds = param2;
            if (Minecraft.useShaderTransparency()) {
                RenderTarget var0 = Minecraft.getInstance().levelRenderer.getCloudsTarget();
                if (var0 != null) {
                    var0.clear(Minecraft.ON_OSX);
                }
            }
    
        }
    );
    public static final CycleOption<Boolean> TEXT_BACKGROUND = CycleOption.createBinaryOption(
        "options.accessibility.text_background",
        new TranslatableComponent("options.accessibility.text_background.chat"),
        new TranslatableComponent("options.accessibility.text_background.everywhere"),
        param0 -> param0.backgroundForChatOnly,
        (param0, param1, param2) -> param0.backgroundForChatOnly = param2
    );
    private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = new TranslatableComponent("options.hideMatchedNames.tooltip");
    public static final CycleOption<Boolean> AUTO_JUMP = CycleOption.createOnOff(
        "options.autoJump", param0 -> param0.autoJump, (param0, param1, param2) -> param0.autoJump = param2
    );
    public static final CycleOption<Boolean> AUTO_SUGGESTIONS = CycleOption.createOnOff(
        "options.autoSuggestCommands", param0 -> param0.autoSuggestions, (param0, param1, param2) -> param0.autoSuggestions = param2
    );
    public static final CycleOption<Boolean> CHAT_COLOR = CycleOption.createOnOff(
        "options.chat.color", param0 -> param0.chatColors, (param0, param1, param2) -> param0.chatColors = param2
    );
    public static final CycleOption<Boolean> HIDE_MATCHED_NAMES = CycleOption.createOnOff(
        "options.hideMatchedNames",
        CHAT_TOOLTIP_HIDE_MATCHED_NAMES,
        param0 -> param0.hideMatchedNames,
        (param0, param1, param2) -> param0.hideMatchedNames = param2
    );
    public static final CycleOption<Boolean> CHAT_LINKS = CycleOption.createOnOff(
        "options.chat.links", param0 -> param0.chatLinks, (param0, param1, param2) -> param0.chatLinks = param2
    );
    public static final CycleOption<Boolean> CHAT_LINKS_PROMPT = CycleOption.createOnOff(
        "options.chat.links.prompt", param0 -> param0.chatLinksPrompt, (param0, param1, param2) -> param0.chatLinksPrompt = param2
    );
    public static final CycleOption<Boolean> DISCRETE_MOUSE_SCROLL = CycleOption.createOnOff(
        "options.discrete_mouse_scroll", param0 -> param0.discreteMouseScroll, (param0, param1, param2) -> param0.discreteMouseScroll = param2
    );
    public static final CycleOption<Boolean> ENABLE_VSYNC = CycleOption.createOnOff(
        "options.vsync", param0 -> param0.enableVsync, (param0, param1, param2) -> {
            param0.enableVsync = param2;
            if (Minecraft.getInstance().getWindow() != null) {
                Minecraft.getInstance().getWindow().updateVsync(param0.enableVsync);
            }
    
        }
    );
    public static final CycleOption<Boolean> ENTITY_SHADOWS = CycleOption.createOnOff(
        "options.entityShadows", param0 -> param0.entityShadows, (param0, param1, param2) -> param0.entityShadows = param2
    );
    public static final CycleOption<Boolean> FORCE_UNICODE_FONT = CycleOption.createOnOff(
        "options.forceUnicodeFont", param0 -> param0.forceUnicodeFont, (param0, param1, param2) -> {
            param0.forceUnicodeFont = param2;
            Minecraft var0 = Minecraft.getInstance();
            if (var0.getWindow() != null) {
                var0.selectMainFont(param2);
            }
    
        }
    );
    public static final CycleOption<Boolean> INVERT_MOUSE = CycleOption.createOnOff(
        "options.invertMouse", param0 -> param0.invertYMouse, (param0, param1, param2) -> param0.invertYMouse = param2
    );
    public static final CycleOption<Boolean> REALMS_NOTIFICATIONS = CycleOption.createOnOff(
        "options.realmsNotifications", param0 -> param0.realmsNotifications, (param0, param1, param2) -> param0.realmsNotifications = param2
    );
    public static final CycleOption<Boolean> REDUCED_DEBUG_INFO = CycleOption.createOnOff(
        "options.reducedDebugInfo", param0 -> param0.reducedDebugInfo, (param0, param1, param2) -> param0.reducedDebugInfo = param2
    );
    public static final CycleOption<Boolean> SHOW_SUBTITLES = CycleOption.createOnOff(
        "options.showSubtitles", param0 -> param0.showSubtitles, (param0, param1, param2) -> param0.showSubtitles = param2
    );
    public static final CycleOption<Boolean> SNOOPER_ENABLED = CycleOption.createOnOff("options.snooper", param0 -> {
        if (param0.snooperEnabled) {
        }

        return false;
    }, (param0, param1, param2) -> param0.snooperEnabled = param2);
    private static final Component MOVEMENT_TOGGLE = new TranslatableComponent("options.key.toggle");
    private static final Component MOVEMENT_HOLD = new TranslatableComponent("options.key.hold");
    public static final CycleOption<Boolean> TOGGLE_CROUCH = CycleOption.createBinaryOption(
        "key.sneak", MOVEMENT_TOGGLE, MOVEMENT_HOLD, param0 -> param0.toggleCrouch, (param0, param1, param2) -> param0.toggleCrouch = param2
    );
    public static final CycleOption<Boolean> TOGGLE_SPRINT = CycleOption.createBinaryOption(
        "key.sprint", MOVEMENT_TOGGLE, MOVEMENT_HOLD, param0 -> param0.toggleSprint, (param0, param1, param2) -> param0.toggleSprint = param2
    );
    public static final CycleOption<Boolean> TOUCHSCREEN = CycleOption.createOnOff(
        "options.touchscreen", param0 -> param0.touchscreen, (param0, param1, param2) -> param0.touchscreen = param2
    );
    public static final CycleOption<Boolean> USE_FULLSCREEN = CycleOption.createOnOff(
        "options.fullscreen", param0 -> param0.fullscreen, (param0, param1, param2) -> {
            param0.fullscreen = param2;
            Minecraft var0 = Minecraft.getInstance();
            if (var0.getWindow() != null && var0.getWindow().isFullscreen() != param0.fullscreen) {
                var0.getWindow().toggleFullScreen();
                param0.fullscreen = var0.getWindow().isFullscreen();
            }
    
        }
    );
    public static final CycleOption<Boolean> VIEW_BOBBING = CycleOption.createOnOff(
        "options.viewBobbing", param0 -> param0.bobView, (param0, param1, param2) -> param0.bobView = param2
    );
    private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = new TranslatableComponent("options.darkMojangStudiosBackgroundColor.tooltip");
    public static final CycleOption<Boolean> DARK_MOJANG_STUDIOS_BACKGROUND_COLOR = CycleOption.createOnOff(
        "options.darkMojangStudiosBackgroundColor",
        ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND,
        param0 -> param0.darkMojangStudiosBackground,
        (param0, param1, param2) -> param0.darkMojangStudiosBackground = param2
    );
    private final Component caption;

    public Option(String param0) {
        this.caption = new TranslatableComponent(param0);
    }

    public abstract AbstractWidget createButton(Options var1, int var2, int var3, int var4);

    protected Component getCaption() {
        return this.caption;
    }

    protected Component pixelValueLabel(int param0) {
        return new TranslatableComponent("options.pixel_value", this.getCaption(), param0);
    }

    protected Component percentValueLabel(double param0) {
        return new TranslatableComponent("options.percent_value", this.getCaption(), (int)(param0 * 100.0));
    }

    protected Component percentAddValueLabel(int param0) {
        return new TranslatableComponent("options.percent_add_value", this.getCaption(), param0);
    }

    protected Component genericValueLabel(Component param0) {
        return new TranslatableComponent("options.generic_value", this.getCaption(), param0);
    }

    protected Component genericValueLabel(int param0) {
        return this.genericValueLabel(new TextComponent(Integer.toString(param0)));
    }
}
