package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.TextureUtil;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.SimpleOptionsSubScreen;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardHandler {
    public static final int DEBUG_CRASH_TIME = 10000;
    private final Minecraft minecraft;
    private final ClipboardManager clipboardManager = new ClipboardManager();
    private long debugCrashKeyTime = -1L;
    private long debugCrashKeyReportedTime = -1L;
    private long debugCrashKeyReportedCount = -1L;
    private boolean handledDebugKey;

    public KeyboardHandler(Minecraft param0) {
        this.minecraft = param0;
    }

    private boolean handleChunkDebugKeys(int param0) {
        switch(param0) {
            case 69:
                this.minecraft.sectionPath = !this.minecraft.sectionPath;
                this.debugFeedback("SectionPath: {0}", this.minecraft.sectionPath ? "shown" : "hidden");
                return true;
            case 76:
                this.minecraft.smartCull = !this.minecraft.smartCull;
                this.debugFeedback("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
                return true;
            case 85:
                if (Screen.hasShiftDown()) {
                    this.minecraft.levelRenderer.killFrustum();
                    this.debugFeedback("Killed frustum");
                } else {
                    this.minecraft.levelRenderer.captureFrustum();
                    this.debugFeedback("Captured frustum");
                }

                return true;
            case 86:
                this.minecraft.sectionVisibility = !this.minecraft.sectionVisibility;
                this.debugFeedback("SectionVisibility: {0}", this.minecraft.sectionVisibility ? "enabled" : "disabled");
                return true;
            case 87:
                this.minecraft.wireframe = !this.minecraft.wireframe;
                this.debugFeedback("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
                return true;
            default:
                return false;
        }
    }

    private void debugComponent(ChatFormatting param0, Component param1) {
        this.minecraft
            .gui
            .getChat()
            .addMessage(
                Component.empty()
                    .append(Component.translatable("debug.prefix").withStyle(param0, ChatFormatting.BOLD))
                    .append(CommonComponents.SPACE)
                    .append(param1)
            );
    }

    private void debugFeedbackComponent(Component param0) {
        this.debugComponent(ChatFormatting.YELLOW, param0);
    }

    private void debugFeedbackTranslated(String param0, Object... param1) {
        this.debugFeedbackComponent(Component.translatableEscape(param0, param1));
    }

    private void debugWarningTranslated(String param0, Object... param1) {
        this.debugComponent(ChatFormatting.RED, Component.translatableEscape(param0, param1));
    }

    private void debugFeedback(String param0, Object... param1) {
        this.debugFeedbackComponent(Component.literal(MessageFormat.format(param0, param1)));
    }

    private boolean handleDebugKeys(int param0) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return true;
        } else {
            switch(param0) {
                case 49:
                    this.minecraft.getDebugOverlay().toggleProfilerChart();
                    return true;
                case 50:
                    this.minecraft.getDebugOverlay().toggleFpsCharts();
                    return true;
                case 51:
                    this.minecraft.getDebugOverlay().toggleNetworkCharts();
                    return true;
                case 65:
                    this.minecraft.levelRenderer.allChanged();
                    this.debugFeedbackTranslated("debug.reload_chunks.message");
                    return true;
                case 66:
                    boolean var0 = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
                    this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(var0);
                    this.debugFeedbackTranslated(var0 ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
                    return true;
                case 67:
                    if (this.minecraft.player.isReducedDebugInfo()) {
                        return false;
                    } else {
                        ClientPacketListener var6 = this.minecraft.player.connection;
                        if (var6 == null) {
                            return false;
                        }

                        this.debugFeedbackTranslated("debug.copy_location.message");
                        this.setClipboard(
                            String.format(
                                Locale.ROOT,
                                "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
                                this.minecraft.player.level().dimension().location(),
                                this.minecraft.player.getX(),
                                this.minecraft.player.getY(),
                                this.minecraft.player.getZ(),
                                this.minecraft.player.getYRot(),
                                this.minecraft.player.getXRot()
                            )
                        );
                        return true;
                    }
                case 68:
                    if (this.minecraft.gui != null) {
                        this.minecraft.gui.getChat().clearMessages(false);
                    }

                    return true;
                case 71:
                    boolean var1 = this.minecraft.debugRenderer.switchRenderChunkborder();
                    this.debugFeedbackTranslated(var1 ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
                    return true;
                case 72:
                    this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
                    this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
                    this.minecraft.options.save();
                    return true;
                case 73:
                    if (!this.minecraft.player.isReducedDebugInfo()) {
                        this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
                    }

                    return true;
                case 76:
                    if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
                        this.debugFeedbackTranslated("debug.profiling.start", 10);
                    }

                    return true;
                case 78:
                    if (!this.minecraft.player.hasPermissions(2)) {
                        this.debugFeedbackTranslated("debug.creative_spectator.error");
                    } else if (!this.minecraft.player.isSpectator()) {
                        this.minecraft.player.connection.sendUnsignedCommand("gamemode spectator");
                    } else {
                        this.minecraft
                            .player
                            .connection
                            .sendUnsignedCommand(
                                "gamemode " + MoreObjects.firstNonNull(this.minecraft.gameMode.getPreviousPlayerMode(), GameType.CREATIVE).getName()
                            );
                    }

                    return true;
                case 80:
                    this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
                    this.minecraft.options.save();
                    this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
                    return true;
                case 81:
                    this.debugFeedbackTranslated("debug.help.message");
                    ChatComponent var2 = this.minecraft.gui.getChat();
                    var2.addMessage(Component.translatable("debug.reload_chunks.help"));
                    var2.addMessage(Component.translatable("debug.show_hitboxes.help"));
                    var2.addMessage(Component.translatable("debug.copy_location.help"));
                    var2.addMessage(Component.translatable("debug.clear_chat.help"));
                    var2.addMessage(Component.translatable("debug.chunk_boundaries.help"));
                    var2.addMessage(Component.translatable("debug.advanced_tooltips.help"));
                    var2.addMessage(Component.translatable("debug.inspect.help"));
                    var2.addMessage(Component.translatable("debug.profiling.help"));
                    var2.addMessage(Component.translatable("debug.creative_spectator.help"));
                    var2.addMessage(Component.translatable("debug.pause_focus.help"));
                    var2.addMessage(Component.translatable("debug.help.help"));
                    var2.addMessage(Component.translatable("debug.dump_dynamic_textures.help"));
                    var2.addMessage(Component.translatable("debug.reload_resourcepacks.help"));
                    var2.addMessage(Component.translatable("debug.pause.help"));
                    var2.addMessage(Component.translatable("debug.gamemodes.help"));
                    return true;
                case 83:
                    Path var3 = this.minecraft.gameDirectory.toPath().toAbsolutePath();
                    Path var4 = TextureUtil.getDebugTexturePath(var3);
                    this.minecraft.getTextureManager().dumpAllSheets(var4);
                    Component var5 = Component.literal(var3.relativize(var4).toString())
                        .withStyle(ChatFormatting.UNDERLINE)
                        .withStyle(param1 -> param1.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, var4.toFile().toString())));
                    this.debugFeedbackTranslated("debug.dump_dynamic_textures", var5);
                    return true;
                case 84:
                    this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
                    this.minecraft.reloadResourcePacks();
                    return true;
                case 293:
                    if (!this.minecraft.player.hasPermissions(2)) {
                        this.debugFeedbackTranslated("debug.gamemodes.error");
                    } else {
                        this.minecraft.setScreen(new GameModeSwitcherScreen());
                    }

                    return true;
                default:
                    return false;
            }
        }
    }

    private void copyRecreateCommand(boolean param0, boolean param1) {
        HitResult var0 = this.minecraft.hitResult;
        if (var0 != null) {
            switch(var0.getType()) {
                case BLOCK:
                    BlockPos var1 = ((BlockHitResult)var0).getBlockPos();
                    BlockState var2 = this.minecraft.player.level().getBlockState(var1);
                    if (param0) {
                        if (param1) {
                            this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(var1, param2 -> {
                                this.copyCreateBlockCommand(var2, var1, param2);
                                this.debugFeedbackTranslated("debug.inspect.server.block");
                            });
                        } else {
                            BlockEntity var3 = this.minecraft.player.level().getBlockEntity(var1);
                            CompoundTag var4 = var3 != null ? var3.saveWithoutMetadata() : null;
                            this.copyCreateBlockCommand(var2, var1, var4);
                            this.debugFeedbackTranslated("debug.inspect.client.block");
                        }
                    } else {
                        this.copyCreateBlockCommand(var2, var1, null);
                        this.debugFeedbackTranslated("debug.inspect.client.block");
                    }
                    break;
                case ENTITY:
                    Entity var5 = ((EntityHitResult)var0).getEntity();
                    ResourceLocation var6 = BuiltInRegistries.ENTITY_TYPE.getKey(var5.getType());
                    if (param0) {
                        if (param1) {
                            this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(var5.getId(), param2 -> {
                                this.copyCreateEntityCommand(var6, var5.position(), param2);
                                this.debugFeedbackTranslated("debug.inspect.server.entity");
                            });
                        } else {
                            CompoundTag var7 = var5.saveWithoutId(new CompoundTag());
                            this.copyCreateEntityCommand(var6, var5.position(), var7);
                            this.debugFeedbackTranslated("debug.inspect.client.entity");
                        }
                    } else {
                        this.copyCreateEntityCommand(var6, var5.position(), null);
                        this.debugFeedbackTranslated("debug.inspect.client.entity");
                    }
            }

        }
    }

    private void copyCreateBlockCommand(BlockState param0, BlockPos param1, @Nullable CompoundTag param2) {
        StringBuilder var0 = new StringBuilder(BlockStateParser.serialize(param0));
        if (param2 != null) {
            var0.append(param2);
        }

        String var1 = String.format(Locale.ROOT, "/setblock %d %d %d %s", param1.getX(), param1.getY(), param1.getZ(), var0);
        this.setClipboard(var1);
    }

    private void copyCreateEntityCommand(ResourceLocation param0, Vec3 param1, @Nullable CompoundTag param2) {
        String var1;
        if (param2 != null) {
            param2.remove("UUID");
            param2.remove("Pos");
            param2.remove("Dimension");
            String var0 = NbtUtils.toPrettyComponent(param2).getString();
            var1 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", param0, param1.x, param1.y, param1.z, var0);
        } else {
            var1 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", param0, param1.x, param1.y, param1.z);
        }

        this.setClipboard(var1);
    }

    public void keyPress(long param0, int param1, int param2, int param3, int param4) {
        if (param0 == this.minecraft.getWindow().getWindow()) {
            boolean var0 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
            if (this.debugCrashKeyTime > 0L) {
                if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !var0) {
                    this.debugCrashKeyTime = -1L;
                }
            } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && var0) {
                this.handledDebugKey = true;
                this.debugCrashKeyTime = Util.getMillis();
                this.debugCrashKeyReportedTime = Util.getMillis();
                this.debugCrashKeyReportedCount = 0L;
            }

            Screen var1 = this.minecraft.screen;
            if (var1 != null) {
                switch(param1) {
                    case 258:
                        this.minecraft.setLastInputType(InputType.KEYBOARD_TAB);
                    case 259:
                    case 260:
                    case 261:
                    default:
                        break;
                    case 262:
                    case 263:
                    case 264:
                    case 265:
                        this.minecraft.setLastInputType(InputType.KEYBOARD_ARROW);
                }
            }

            if (param3 == 1 && (!(this.minecraft.screen instanceof KeyBindsScreen) || ((KeyBindsScreen)var1).lastKeySelection <= Util.getMillis() - 20L)) {
                if (this.minecraft.options.keyFullscreen.matches(param1, param2)) {
                    this.minecraft.getWindow().toggleFullScreen();
                    this.minecraft.options.fullscreen().set(this.minecraft.getWindow().isFullscreen());
                    return;
                }

                if (this.minecraft.options.keyScreenshot.matches(param1, param2)) {
                    if (Screen.hasControlDown()) {
                    }

                    Screenshot.grab(
                        this.minecraft.gameDirectory,
                        this.minecraft.getMainRenderTarget(),
                        param0x -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(param0x))
                    );
                    return;
                }
            }

            if (this.minecraft.getNarrator().isActive() && this.minecraft.options.narratorHotkey().get()) {
                boolean var10000;
                label136: {
                    if (var1 != null) {
                        GuiEventListener var9 = var1.getFocused();
                        if (var9 instanceof EditBox var2 && var2.canConsumeInput()) {
                            var10000 = false;
                            break label136;
                        }
                    }

                    var10000 = true;
                }

                boolean var4 = var10000;
                if (param3 != 0 && param1 == 66 && Screen.hasControlDown() && var4) {
                    boolean var5 = this.minecraft.options.narrator().get() == NarratorStatus.OFF;
                    this.minecraft.options.narrator().set(NarratorStatus.byId(this.minecraft.options.narrator().get().getId() + 1));
                    this.minecraft.options.save();
                    if (var1 instanceof SimpleOptionsSubScreen) {
                        ((SimpleOptionsSubScreen)var1).updateNarratorButton();
                    }

                    if (var5 && var1 != null) {
                        var1.narrationEnabled();
                    }
                }
            }

            if (var1 != null) {
                boolean[] var6 = new boolean[]{false};
                Screen.wrapScreenError(() -> {
                    if (param3 == 1 || param3 == 2) {
                        var1.afterKeyboardAction();
                        var6[0] = var1.keyPressed(param1, param2, param4);
                    } else if (param3 == 0) {
                        var6[0] = var1.keyReleased(param1, param2, param4);
                    }

                }, "keyPressed event handler", var1.getClass().getCanonicalName());
                if (var6[0]) {
                    return;
                }
            }

            if (this.minecraft.screen != null) {
                Screen var15 = this.minecraft.screen;
                if (!(var15 instanceof PauseScreen)) {
                    return;
                }

                PauseScreen var7 = (PauseScreen)var15;
                if (var7.showsPauseMenu()) {
                    return;
                }
            }

            InputConstants.Key var8 = InputConstants.getKey(param1, param2);
            if (param3 == 0) {
                KeyMapping.set(var8, false);
                if (param1 == 292) {
                    if (this.handledDebugKey) {
                        this.handledDebugKey = false;
                    } else {
                        this.minecraft.getDebugOverlay().toggleOverlay();
                    }
                }
            } else {
                if (param1 == 293 && this.minecraft.gameRenderer != null) {
                    this.minecraft.gameRenderer.togglePostEffect();
                }

                boolean var9 = false;
                if (param1 == 256) {
                    this.minecraft.pauseGame(var0);
                    var9 |= var0;
                }

                var9 |= var0 && this.handleDebugKeys(param1);
                this.handledDebugKey |= var9;
                if (param1 == 290) {
                    this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                }

                if (var9) {
                    KeyMapping.set(var8, false);
                } else {
                    KeyMapping.set(var8, true);
                    KeyMapping.click(var8);
                }

                if (this.minecraft.getDebugOverlay().showProfilerChart() && !var0 && param1 >= 48 && param1 <= 57) {
                    this.minecraft.debugFpsMeterKeyPress(param1 - 48);
                }
            }

        }
    }

    private void charTyped(long param0, int param1, int param2) {
        if (param0 == this.minecraft.getWindow().getWindow()) {
            GuiEventListener var0 = this.minecraft.screen;
            if (var0 != null && this.minecraft.getOverlay() == null) {
                if (Character.charCount(param1) == 1) {
                    Screen.wrapScreenError(() -> var0.charTyped((char)param1, param2), "charTyped event handler", var0.getClass().getCanonicalName());
                } else {
                    for(char var1 : Character.toChars(param1)) {
                        Screen.wrapScreenError(() -> var0.charTyped(var1, param2), "charTyped event handler", var0.getClass().getCanonicalName());
                    }
                }

            }
        }
    }

    public void setup(long param0) {
        InputConstants.setupKeyboardCallbacks(
            param0,
            (param0x, param1, param2, param3, param4) -> this.minecraft.execute(() -> this.keyPress(param0x, param1, param2, param3, param4)),
            (param0x, param1, param2) -> this.minecraft.execute(() -> this.charTyped(param0x, param1, param2))
        );
    }

    public String getClipboard() {
        return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (param0, param1) -> {
            if (param0 != 65545) {
                this.minecraft.getWindow().defaultErrorCallback(param0, param1);
            }

        });
    }

    public void setClipboard(String param0) {
        if (!param0.isEmpty()) {
            this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), param0);
        }

    }

    public void tick() {
        if (this.debugCrashKeyTime > 0L) {
            long var0 = Util.getMillis();
            long var1 = 10000L - (var0 - this.debugCrashKeyTime);
            long var2 = var0 - this.debugCrashKeyReportedTime;
            if (var1 < 0L) {
                if (Screen.hasControlDown()) {
                    Blaze3D.youJustLostTheGame();
                }

                String var3 = "Manually triggered debug crash";
                CrashReport var4 = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
                CrashReportCategory var5 = var4.addCategory("Manual crash details");
                NativeModuleLister.addCrashSection(var5);
                throw new ReportedException(var4);
            }

            if (var2 >= 1000L) {
                if (this.debugCrashKeyReportedCount == 0L) {
                    this.debugFeedbackTranslated("debug.crash.message");
                } else {
                    this.debugWarningTranslated("debug.crash.warning", Mth.ceil((float)var1 / 1000.0F));
                }

                this.debugCrashKeyReportedTime = var0;
                ++this.debugCrashKeyReportedCount;
            }
        }

    }
}
