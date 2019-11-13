package net.minecraft.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.resources.language.I18n;
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
            String var1 = param1.getCaption();
            int var2 = (int)var0 * 2 + 1;
            return var1 + I18n.get("options.biomeBlendRadius." + var2);
        }
    );
    public static final ProgressOption CHAT_HEIGHT_FOCUSED = new ProgressOption(
        "options.chat.height.focused", 0.0, 1.0, 0.0F, param0 -> param0.chatHeightFocused, (param0, param1) -> {
            param0.chatHeightFocused = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.getCaption() + ChatComponent.getHeight(var0) + "px";
        }
    );
    public static final ProgressOption CHAT_HEIGHT_UNFOCUSED = new ProgressOption(
        "options.chat.height.unfocused", 0.0, 1.0, 0.0F, param0 -> param0.chatHeightUnfocused, (param0, param1) -> {
            param0.chatHeightUnfocused = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.getCaption() + ChatComponent.getHeight(var0) + "px";
        }
    );
    public static final ProgressOption CHAT_OPACITY = new ProgressOption(
        "options.chat.opacity", 0.0, 1.0, 0.0F, param0 -> param0.chatOpacity, (param0, param1) -> {
            param0.chatOpacity = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            return param1.getCaption() + (int)(var0 * 90.0 + 10.0) + "%";
        }
    );
    public static final ProgressOption CHAT_SCALE = new ProgressOption("options.chat.scale", 0.0, 1.0, 0.0F, param0 -> param0.chatScale, (param0, param1) -> {
        param0.chatScale = param1;
        Minecraft.getInstance().gui.getChat().rescaleChat();
    }, (param0, param1) -> {
        double var0 = param1.toPct(param1.get(param0));
        String var1 = param1.getCaption();
        return var0 == 0.0 ? var1 + I18n.get("options.off") : var1 + (int)(var0 * 100.0) + "%";
    });
    public static final ProgressOption CHAT_WIDTH = new ProgressOption("options.chat.width", 0.0, 1.0, 0.0F, param0 -> param0.chatWidth, (param0, param1) -> {
        param0.chatWidth = param1;
        Minecraft.getInstance().gui.getChat().rescaleChat();
    }, (param0, param1) -> {
        double var0 = param1.toPct(param1.get(param0));
        return param1.getCaption() + ChatComponent.getWidth(var0) + "px";
    });
    public static final ProgressOption FOV = new ProgressOption(
        "options.fov", 30.0, 110.0, 1.0F, param0 -> param0.fov, (param0, param1) -> param0.fov = param1, (param0, param1) -> {
            double var0 = param1.get(param0);
            String var1 = param1.getCaption();
            if (var0 == 70.0) {
                return var1 + I18n.get("options.fov.min");
            } else {
                return var0 == param1.getMaxValue() ? var1 + I18n.get("options.fov.max") : var1 + (int)var0;
            }
        }
    );
    public static final ProgressOption FRAMERATE_LIMIT = new ProgressOption(
        "options.framerateLimit", 10.0, 260.0, 10.0F, param0 -> (double)param0.framerateLimit, (param0, param1) -> {
            param0.framerateLimit = (int)param1.doubleValue();
            Minecraft.getInstance().getWindow().setFramerateLimit(param0.framerateLimit);
        }, (param0, param1) -> {
            double var0 = param1.get(param0);
            String var1 = param1.getCaption();
            return var0 == param1.getMaxValue() ? var1 + I18n.get("options.framerateLimit.max") : var1 + I18n.get("options.framerate", (int)var0);
        }
    );
    public static final ProgressOption GAMMA = new ProgressOption(
        "options.gamma", 0.0, 1.0, 0.0F, param0 -> param0.gamma, (param0, param1) -> param0.gamma = param1, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            String var1 = param1.getCaption();
            if (var0 == 0.0) {
                return var1 + I18n.get("options.gamma.min");
            } else {
                return var0 == 1.0 ? var1 + I18n.get("options.gamma.max") : var1 + "+" + (int)(var0 * 100.0) + "%";
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
            String var1 = param1.getCaption();
            return var0 == 0.0 ? var1 + I18n.get("options.off") : var1 + (int)var0;
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
            return param1.getCaption() + String.format("%.2f", param1.toValue(var0));
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
            return param1.getCaption() + I18n.get("options.chunks", (int)var0);
        }
    );
    public static final ProgressOption SENSITIVITY = new ProgressOption(
        "options.sensitivity", 0.0, 1.0, 0.0F, param0 -> param0.sensitivity, (param0, param1) -> param0.sensitivity = param1, (param0, param1) -> {
            double var0 = param1.toPct(param1.get(param0));
            String var1 = param1.getCaption();
            if (var0 == 0.0) {
                return var1 + I18n.get("options.sensitivity.min");
            } else {
                return var0 == 1.0 ? var1 + I18n.get("options.sensitivity.max") : var1 + (int)(var0 * 200.0) + "%";
            }
        }
    );
    public static final ProgressOption TEXT_BACKGROUND_OPACITY = new ProgressOption(
        "options.accessibility.text_background_opacity", 0.0, 1.0, 0.0F, param0 -> param0.textBackgroundOpacity, (param0, param1) -> {
            param0.textBackgroundOpacity = param1;
            Minecraft.getInstance().gui.getChat().rescaleChat();
        }, (param0, param1) -> param1.getCaption() + (int)(param1.toPct(param1.get(param0)) * 100.0) + "%"
    );
    public static final CycleOption AMBIENT_OCCLUSION = new CycleOption("options.ao", (param0, param1) -> {
        param0.ambientOcclusion = AmbientOcclusionStatus.byId(param0.ambientOcclusion.getId() + param1);
        Minecraft.getInstance().levelRenderer.allChanged();
    }, (param0, param1) -> param1.getCaption() + I18n.get(param0.ambientOcclusion.getKey()));
    public static final CycleOption ATTACK_INDICATOR = new CycleOption(
        "options.attackIndicator",
        (param0, param1) -> param0.attackIndicator = AttackIndicatorStatus.byId(param0.attackIndicator.getId() + param1),
        (param0, param1) -> param1.getCaption() + I18n.get(param0.attackIndicator.getKey())
    );
    public static final CycleOption CHAT_VISIBILITY = new CycleOption(
        "options.chat.visibility",
        (param0, param1) -> param0.chatVisibility = ChatVisiblity.byId((param0.chatVisibility.getId() + param1) % 3),
        (param0, param1) -> param1.getCaption() + I18n.get(param0.chatVisibility.getKey())
    );
    public static final CycleOption GRAPHICS = new CycleOption(
        "options.graphics",
        (param0, param1) -> {
            param0.fancyGraphics = !param0.fancyGraphics;
            Minecraft.getInstance().levelRenderer.allChanged();
        },
        (param0, param1) -> param0.fancyGraphics
                ? param1.getCaption() + I18n.get("options.graphics.fancy")
                : param1.getCaption() + I18n.get("options.graphics.fast")
    );
    public static final CycleOption GUI_SCALE = new CycleOption(
        "options.guiScale",
        (param0, param1) -> param0.guiScale = Integer.remainderUnsigned(
                param0.guiScale + param1, Minecraft.getInstance().getWindow().calculateScale(0, Minecraft.getInstance().isEnforceUnicode()) + 1
            ),
        (param0, param1) -> param1.getCaption() + (param0.guiScale == 0 ? I18n.get("options.guiScale.auto") : param0.guiScale)
    );
    public static final CycleOption MAIN_HAND = new CycleOption(
        "options.mainHand", (param0, param1) -> param0.mainHand = param0.mainHand.getOpposite(), (param0, param1) -> param1.getCaption() + param0.mainHand
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
                ? param1.getCaption() + I18n.get(param0.narratorStatus.getKey())
                : param1.getCaption() + I18n.get("options.narrator.notavailable")
    );
    public static final CycleOption PARTICLES = new CycleOption(
        "options.particles",
        (param0, param1) -> param0.particles = ParticleStatus.byId(param0.particles.getId() + param1),
        (param0, param1) -> param1.getCaption() + I18n.get(param0.particles.getKey())
    );
    public static final CycleOption RENDER_CLOUDS = new CycleOption(
        "options.renderClouds",
        (param0, param1) -> param0.renderClouds = CloudStatus.byId(param0.renderClouds.getId() + param1),
        (param0, param1) -> param1.getCaption() + I18n.get(param0.renderClouds.getKey())
    );
    public static final CycleOption TEXT_BACKGROUND = new CycleOption(
        "options.accessibility.text_background",
        (param0, param1) -> param0.backgroundForChatOnly = !param0.backgroundForChatOnly,
        (param0, param1) -> param1.getCaption()
                + I18n.get(param0.backgroundForChatOnly ? "options.accessibility.text_background.chat" : "options.accessibility.text_background.everywhere")
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
            if (var0.getFontManager() != null) {
                var0.getFontManager().setForceUnicode(param0.forceUnicodeFont, Util.backgroundExecutor(), var0);
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
        (param0, param1) -> param1.getCaption() + I18n.get(param0.toggleCrouch ? "options.key.toggle" : "options.key.hold")
    );
    public static final CycleOption TOGGLE_SPRINT = new CycleOption(
        "key.sprint",
        (param0, param1) -> param0.toggleSprint = !param0.toggleSprint,
        (param0, param1) -> param1.getCaption() + I18n.get(param0.toggleSprint ? "options.key.toggle" : "options.key.hold")
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

    public Option(String param0) {
        this.captionId = param0;
    }

    public abstract AbstractWidget createButton(Options var1, int var2, int var3, int var4);

    public String getCaption() {
        return I18n.get(this.captionId) + ": ";
    }
}
