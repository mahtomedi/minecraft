package net.minecraft.client;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class Option {
    public static final ProgressOption BIOME_BLEND_RADIUS = new ProgressOption(
        "options.biomeBlendRadius", 0.0, 7.0, 1.0F, param0 -> (double)param0.biomeBlendRadius, (param0, param1) -> {
            param0.biomeBlendRadius = Mth.clamp((int)param1.doubleValue(), 0, 7);
            Minecraft.getInstance().levelRenderer.allChanged();
        }, (param0, param1) -> {
            double var0 = param1.get(param0);
            MutableComponent var1 = param1.createCaption();
            int var2 = (int)var0 * 2 + 1;
            return var1.append(new TranslatableComponent("options.biomeBlendRadius." + var2));
        }
    );
    public static final ProgressOption CHAT_HEIGHT_FOCUSED = new ProgressOption(
        "options.chat.height.focused", 0.0, 1.0, 0.0F, param0 -> param0.chatHeightFocused, (param0, param1) -> {
            param0.chatHeightFocused = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.createCaption().append(ChatComponent.getHeight(var0) + "px");
        }
    );
    public static final ProgressOption CHAT_HEIGHT_UNFOCUSED = new ProgressOption(
        "options.chat.height.unfocused", 0.0, 1.0, 0.0F, param0 -> param0.chatHeightUnfocused, (param0, param1) -> {
            param0.chatHeightUnfocused = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.createCaption().append(ChatComponent.getHeight(var0) + "px");
        }
    );
    public static final ProgressOption CHAT_OPACITY = new ProgressOption(
        "options.chat.opacity", 0.0, 1.0, 0.0F, param0 -> param0.chatOpacity, (param0, param1) -> {
            param0.chatOpacity = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.createCaption().append((int)(var0 * 90.0 + 10.0) + "%");
        }
    );
    public static final ProgressOption CHAT_SCALE = new ProgressOption("options.chat.scale", 0.0, 1.0, 0.0F, param0 -> param0.chatScale, (param0, param1) -> {
        param0.chatScale = param1;
        Minecraft.getInstance().gui.getChat().rescaleChat();
    }, (param0, param1) -> {
        double var0 = param1.toPct(param1.get(param0));
        MutableComponent var1 = param1.createCaption();
        return var0 == 0.0 ? var1.append(CommonComponents.OPTION_OFF) : var1.append((int)(var0 * 100.0) + "%");
    });
    public static final ProgressOption CHAT_WIDTH = new ProgressOption("options.chat.width", 0.0, 1.0, 0.0F, param0 -> param0.chatWidth, (param0, param1) -> {
        param0.chatWidth = param1;
        Minecraft.getInstance().gui.getChat().rescaleChat();
    }, (param0, param1) -> {
        double var0 = param1.toPct(param1.get(param0));
        return param1.createCaption().append(ChatComponent.getWidth(var0) + "px");
    });
    public static final ProgressOption CHAT_LINE_SPACING = new ProgressOption(
        "options.chat.line_spacing",
        0.0,
        1.0,
        0.0F,
        param0 -> param0.chatLineSpacing,
        (param0, param1) -> param0.chatLineSpacing = param1,
        (param0, param1) -> param1.createCaption().append((int)(param1.toPct(param1.get(param0)) * 100.0) + "%")
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
    public static final ProgressOption FOV = new ProgressOption(
        "options.fov", 30.0, 110.0, 1.0F, param0 -> param0.fov, (param0, param1) -> param0.fov = param1, (param0, param1) -> {
            double var0 = param1.get(param0);
            MutableComponent var1 = param1.createCaption();
            if (var0 == 70.0) {
                return var1.append(new TranslatableComponent("options.fov.min"));
            } else {
                return var0 == param1.getMaxValue() ? var1.append(new TranslatableComponent("options.fov.max")) : var1.append(Integer.toString((int)var0));
            }
        }
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
            MutableComponent var1 = param1.createCaption();
            return var0 == param1.getMaxValue()
                ? var1.append(new TranslatableComponent("options.framerateLimit.max"))
                : var1.append(new TranslatableComponent("options.framerate", (int)var0));
        }
    );
    public static final ProgressOption GAMMA = new ProgressOption(
        "options.gamma", 0.0, 1.0, 0.0F, param0 -> param0.gamma, (param0, param1) -> param0.gamma = param1, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            MutableComponent var1 = param1.createCaption();
            if (var0 == 0.0) {
                return var1.append(new TranslatableComponent("options.gamma.min"));
            } else {
                return var0 == 1.0 ? var1.append(new TranslatableComponent("options.gamma.max")) : var1.append("+" + (int)(var0 * 100.0) + "%");
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
            MutableComponent var1 = param1.createCaption();
            return var0 == 0.0 ? var1.append(CommonComponents.OPTION_OFF) : var1.append(Integer.toString((int)var0));
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
            return param1.createCaption().append(String.format("%.2f", param1.toValue(var0)));
        }
    );
    public static final BooleanOption RAW_MOUSE_INPUT = new BooleanOption("options.rawMouseInput", param0 -> param0.rawMouseInput, (param0, param1) -> {
        param0.rawMouseInput = param1;
        Window var0 = Minecraft.getInstance().getWindow();
        if (var0 != null) {
            var0.updateRawMouseInput(param1);
        }

    });
    public static final ProgressOption RENDER_DISTANCE = new ProgressOption(
        "options.renderDistance", 2.0, 16.0, 1.0F, param0 -> (double)param0.renderDistance, (param0, param1) -> {
            param0.renderDistance = (int)param1.doubleValue();
            Minecraft.getInstance().levelRenderer.needsUpdate();
        }, (param0, param1) -> {
            double var0 = param1.get(param0);
            return param1.createCaption().append(new TranslatableComponent("options.chunks", (int)var0));
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
            return param1.createCaption().append(new TranslatableComponent("options.entityDistancePercent", (int)(var0 * 100.0)));
        }
    );
    public static final ProgressOption SENSITIVITY = new ProgressOption(
        "options.sensitivity", 0.0, 1.0, 0.0F, param0 -> param0.sensitivity, (param0, param1) -> param0.sensitivity = param1, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            MutableComponent var1 = param1.createCaption();
            if (var0 == 0.0) {
                return var1.append(new TranslatableComponent("options.sensitivity.min"));
            } else {
                return var0 == 1.0 ? var1.append(new TranslatableComponent("options.sensitivity.max")) : var1.append((int)(var0 * 200.0) + "%");
            }
        }
    );
    public static final ProgressOption TEXT_BACKGROUND_OPACITY = new ProgressOption(
        "options.accessibility.text_background_opacity", 0.0, 1.0, 0.0F, param0 -> param0.textBackgroundOpacity, (param0, param1) -> {
            param0.textBackgroundOpacity = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> param1.createCaption().append((int)(param1.toPct(param1.get(param0)) * 100.0) + "%")
    );
    public static final CycleOption AMBIENT_OCCLUSION = new CycleOption("options.ao", (param0, param1) -> {
        param0.ambientOcclusion = AmbientOcclusionStatus.byId(param0.ambientOcclusion.getId() + param1);
        Minecraft.getInstance().levelRenderer.allChanged();
    }, (param0, param1) -> param1.createCaption().append(new TranslatableComponent(param0.ambientOcclusion.getKey())));
    public static final CycleOption ATTACK_INDICATOR = new CycleOption(
        "options.attackIndicator",
        (param0, param1) -> param0.attackIndicator = AttackIndicatorStatus.byId(param0.attackIndicator.getId() + param1),
        (param0, param1) -> param1.createCaption().append(new TranslatableComponent(param0.attackIndicator.getKey()))
    );
    public static final CycleOption CHAT_VISIBILITY = new CycleOption(
        "options.chat.visibility",
        (param0, param1) -> param0.chatVisibility = ChatVisiblity.byId((param0.chatVisibility.getId() + param1) % 3),
        (param0, param1) -> param1.createCaption().append(new TranslatableComponent(param0.chatVisibility.getKey()))
    );
    public static final CycleOption GRAPHICS = new CycleOption(
        "options.graphics",
        (param0, param1) -> {
            param0.graphicsMode = param0.graphicsMode.cycleNext();
            if (param0.graphicsMode == GraphicsStatus.FABULOUS && !GlStateManager.supportsFramebufferBlit()) {
                param0.graphicsMode = GraphicsStatus.FAST;
            }
    
            Minecraft.getInstance().levelRenderer.allChanged();
        },
        (param0, param1) -> {
            switch(param0.graphicsMode) {
                case FAST:
                    param1.setTooltip("options.graphics.fast.tooltip");
                    break;
                case FANCY:
                    param1.setTooltip("options.graphics.fancy.tooltip");
                    break;
                case FABULOUS:
                    param1.setTooltip("options.graphics.fabulous.tooltip");
            }
    
            TranslatableComponent var0 = new TranslatableComponent(param0.graphicsMode.getKey());
            return param0.graphicsMode == GraphicsStatus.FABULOUS
                ? param1.createCaption().append(new TextComponent("").append(var0).withStyle(ChatFormatting.ITALIC))
                : param1.createCaption().append(var0);
        }
    );
    public static final CycleOption GUI_SCALE = new CycleOption(
        "options.guiScale",
        (param0, param1) -> param0.guiScale = Integer.remainderUnsigned(
                param0.guiScale + param1, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()) + 1
            ),
        (param0, param1) -> {
            MutableComponent var0 = param1.createCaption();
            return param0.guiScale == 0 ? var0.append(new TranslatableComponent("options.guiScale.auto")) : var0.append(Integer.toString(param0.guiScale));
        }
    );
    public static final CycleOption MAIN_HAND = new CycleOption(
        "options.mainHand",
        (param0, param1) -> param0.mainHand = param0.mainHand.getOpposite(),
        (param0, param1) -> param1.createCaption().append(param0.mainHand.getName())
    );
    public static final CycleOption NARRATOR = new CycleOption(
        "options.narrator",
        (param0, param1) -> {
            if (NarratorChatListener.INSTANCE.isActive()) {
                param0.narratorStatus = NarratorStatus.byId(param0.narratorStatus.getId() + param1);
            } else {
                param0.narratorStatus = NarratorStatus.OFF;
            }
    
            NarratorChatListener.INSTANCE.updateNarratorStatus(param0.narratorStatus);
        },
        (param0, param1) -> NarratorChatListener.INSTANCE.isActive()
                ? param1.createCaption().append(param0.narratorStatus.getName())
                : param1.createCaption().append(new TranslatableComponent("options.narrator.notavailable"))
    );
    public static final CycleOption PARTICLES = new CycleOption(
        "options.particles",
        (param0, param1) -> param0.particles = ParticleStatus.byId(param0.particles.getId() + param1),
        (param0, param1) -> param1.createCaption().append(new TranslatableComponent(param0.particles.getKey()))
    );
    public static final CycleOption RENDER_CLOUDS = new CycleOption("options.renderClouds", (param0, param1) -> {
        param0.renderClouds = CloudStatus.byId(param0.renderClouds.getId() + param1);
        if (Minecraft.useShaderTransparency()) {
            RenderTarget var0 = Minecraft.getInstance().levelRenderer.getCloudsTarget();
            if (var0 != null) {
                var0.clear(Minecraft.ON_OSX);
            }
        }

    }, (param0, param1) -> param1.createCaption().append(new TranslatableComponent(param0.renderClouds.getKey())));
    public static final CycleOption TEXT_BACKGROUND = new CycleOption(
        "options.accessibility.text_background",
        (param0, param1) -> param0.backgroundForChatOnly = !param0.backgroundForChatOnly,
        (param0, param1) -> param1.createCaption()
                .append(
                    new TranslatableComponent(
                        param0.backgroundForChatOnly ? "options.accessibility.text_background.chat" : "options.accessibility.text_background.everywhere"
                    )
                )
    );
    public static final BooleanOption AUTO_JUMP = new BooleanOption("options.autoJump", param0 -> param0.autoJump, (param0, param1) -> param0.autoJump = param1);
    public static final BooleanOption AUTO_SUGGESTIONS = new BooleanOption(
        "options.autoSuggestCommands", param0 -> param0.autoSuggestions, (param0, param1) -> param0.autoSuggestions = param1
    );
    public static final BooleanOption CHAT_COLOR = new BooleanOption(
        "options.chat.color", param0 -> param0.chatColors, (param0, param1) -> param0.chatColors = param1
    );
    public static final BooleanOption CHAT_LINKS = new BooleanOption(
        "options.chat.links", param0 -> param0.chatLinks, (param0, param1) -> param0.chatLinks = param1
    );
    public static final BooleanOption CHAT_LINKS_PROMPT = new BooleanOption(
        "options.chat.links.prompt", param0 -> param0.chatLinksPrompt, (param0, param1) -> param0.chatLinksPrompt = param1
    );
    public static final BooleanOption DISCRETE_MOUSE_SCROLL = new BooleanOption(
        "options.discrete_mouse_scroll", param0 -> param0.discreteMouseScroll, (param0, param1) -> param0.discreteMouseScroll = param1
    );
    public static final BooleanOption ENABLE_VSYNC = new BooleanOption("options.vsync", param0 -> param0.enableVsync, (param0, param1) -> {
        param0.enableVsync = param1;
        if (Minecraft.getInstance().getWindow() != null) {
            Minecraft.getInstance().getWindow().updateVsync(param0.enableVsync);
        }

    });
    public static final BooleanOption ENTITY_SHADOWS = new BooleanOption(
        "options.entityShadows", param0 -> param0.entityShadows, (param0, param1) -> param0.entityShadows = param1
    );
    public static final BooleanOption FORCE_UNICODE_FONT = new BooleanOption(
        "options.forceUnicodeFont", param0 -> param0.forceUnicodeFont, (param0, param1) -> {
            param0.forceUnicodeFont = param1;
            Minecraft var0 = Minecraft.getInstance();
            if (var0.getWindow() != null) {
                var0.selectMainFont(param1);
            }
    
        }
    );
    public static final BooleanOption INVERT_MOUSE = new BooleanOption(
        "options.invertMouse", param0 -> param0.invertYMouse, (param0, param1) -> param0.invertYMouse = param1
    );
    public static final BooleanOption REALMS_NOTIFICATIONS = new BooleanOption(
        "options.realmsNotifications", param0 -> param0.realmsNotifications, (param0, param1) -> param0.realmsNotifications = param1
    );
    public static final BooleanOption REDUCED_DEBUG_INFO = new BooleanOption(
        "options.reducedDebugInfo", param0 -> param0.reducedDebugInfo, (param0, param1) -> param0.reducedDebugInfo = param1
    );
    public static final BooleanOption SHOW_SUBTITLES = new BooleanOption(
        "options.showSubtitles", param0 -> param0.showSubtitles, (param0, param1) -> param0.showSubtitles = param1
    );
    public static final BooleanOption SNOOPER_ENABLED = new BooleanOption("options.snooper", param0 -> {
        if (param0.snooperEnabled) {
        }

        return false;
    }, (param0, param1) -> param0.snooperEnabled = param1);
    public static final CycleOption TOGGLE_CROUCH = new CycleOption(
        "key.sneak",
        (param0, param1) -> param0.toggleCrouch = !param0.toggleCrouch,
        (param0, param1) -> param1.createCaption().append(new TranslatableComponent(param0.toggleCrouch ? "options.key.toggle" : "options.key.hold"))
    );
    public static final CycleOption TOGGLE_SPRINT = new CycleOption(
        "key.sprint",
        (param0, param1) -> param0.toggleSprint = !param0.toggleSprint,
        (param0, param1) -> param1.createCaption().append(new TranslatableComponent(param0.toggleSprint ? "options.key.toggle" : "options.key.hold"))
    );
    public static final BooleanOption TOUCHSCREEN = new BooleanOption(
        "options.touchscreen", param0 -> param0.touchscreen, (param0, param1) -> param0.touchscreen = param1
    );
    public static final BooleanOption USE_FULLSCREEN = new BooleanOption("options.fullscreen", param0 -> param0.fullscreen, (param0, param1) -> {
        param0.fullscreen = param1;
        Minecraft var0 = Minecraft.getInstance();
        if (var0.getWindow() != null && var0.getWindow().isFullscreen() != param0.fullscreen) {
            var0.getWindow().toggleFullScreen();
            param0.fullscreen = var0.getWindow().isFullscreen();
        }

    });
    public static final BooleanOption VIEW_BOBBING = new BooleanOption(
        "options.viewBobbing", param0 -> param0.bobView, (param0, param1) -> param0.bobView = param1
    );
    private final String captionId;
    private Optional<TranslatableComponent> toolTip;

    public Option(String param0) {
        this.captionId = param0;
        this.toolTip = Optional.empty();
    }

    public abstract AbstractWidget createButton(Options var1, int var2, int var3, int var4);

    public MutableComponent createCaption() {
        return new TranslatableComponent(this.captionId).append(": ");
    }

    public void setTooltip(String param0) {
        this.toolTip = Optional.of(new TranslatableComponent(param0));
    }

    public Optional<TranslatableComponent> getTooltip() {
        return this.toolTip;
    }
}
