package net.minecraft.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.ChatOptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.controls.ControlsScreen;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyboardHandler {
    private final Minecraft minecraft;
    private boolean sendRepeatsToGui;
    private final ClipboardManager clipboardManager = new ClipboardManager();
    private long debugCrashKeyTime = -1L;
    private long debugCrashKeyReportedTime = -1L;
    private long debugCrashKeyReportedCount = -1L;
    private boolean handledDebugKey;

    public KeyboardHandler(Minecraft param0) {
        this.minecraft = param0;
    }

    private void debugFeedbackTranslated(String param0, Object... param1) {
        this.minecraft
            .gui
            .getChat()
            .addMessage(
                new TextComponent("")
                    .append(new TranslatableComponent("debug.prefix").withStyle(new ChatFormatting[]{ChatFormatting.YELLOW, ChatFormatting.BOLD}))
                    .append(" ")
                    .append(new TranslatableComponent(param0, param1))
            );
    }

    private void debugWarningTranslated(String param0, Object... param1) {
        this.minecraft
            .gui
            .getChat()
            .addMessage(
                new TextComponent("")
                    .append(new TranslatableComponent("debug.prefix").withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}))
                    .append(" ")
                    .append(new TranslatableComponent(param0, param1))
            );
    }

    private boolean handleDebugKeys(int param0) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return true;
        } else {
            switch(param0) {
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
                    }

                    this.debugFeedbackTranslated("debug.copy_location.message");
                    this.setClipboard(
                        String.format(
                            Locale.ROOT,
                            "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f",
                            DimensionType.getName(this.minecraft.player.level.dimension.getType()),
                            this.minecraft.player.x,
                            this.minecraft.player.y,
                            this.minecraft.player.z,
                            this.minecraft.player.yRot,
                            this.minecraft.player.xRot
                        )
                    );
                    return true;
                case 68:
                    if (this.minecraft.gui != null) {
                        this.minecraft.gui.getChat().clearMessages(false);
                    }

                    return true;
                case 69:
                case 74:
                case 75:
                case 76:
                case 77:
                case 79:
                case 82:
                case 83:
                default:
                    return false;
                case 70:
                    Option.RENDER_DISTANCE
                        .set(
                            this.minecraft.options,
                            Mth.clamp(
                                (double)(this.minecraft.options.renderDistance + (Screen.hasShiftDown() ? -1 : 1)),
                                Option.RENDER_DISTANCE.getMinValue(),
                                Option.RENDER_DISTANCE.getMaxValue()
                            )
                        );
                    this.debugFeedbackTranslated("debug.cycle_renderdistance.message", this.minecraft.options.renderDistance);
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
                case 78:
                    if (!this.minecraft.player.hasPermissions(2)) {
                        this.debugFeedbackTranslated("debug.creative_spectator.error");
                    } else if (this.minecraft.player.isCreative()) {
                        this.minecraft.player.chat("/gamemode spectator");
                    } else {
                        this.minecraft.player.chat("/gamemode creative");
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
                    var2.addMessage(new TranslatableComponent("debug.reload_chunks.help"));
                    var2.addMessage(new TranslatableComponent("debug.show_hitboxes.help"));
                    var2.addMessage(new TranslatableComponent("debug.copy_location.help"));
                    var2.addMessage(new TranslatableComponent("debug.clear_chat.help"));
                    var2.addMessage(new TranslatableComponent("debug.cycle_renderdistance.help"));
                    var2.addMessage(new TranslatableComponent("debug.chunk_boundaries.help"));
                    var2.addMessage(new TranslatableComponent("debug.advanced_tooltips.help"));
                    var2.addMessage(new TranslatableComponent("debug.inspect.help"));
                    var2.addMessage(new TranslatableComponent("debug.creative_spectator.help"));
                    var2.addMessage(new TranslatableComponent("debug.pause_focus.help"));
                    var2.addMessage(new TranslatableComponent("debug.help.help"));
                    var2.addMessage(new TranslatableComponent("debug.reload_resourcepacks.help"));
                    var2.addMessage(new TranslatableComponent("debug.pause.help"));
                    return true;
                case 84:
                    this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
                    this.minecraft.reloadResourcePacks();
                    return true;
            }
        }
    }

    private void copyRecreateCommand(boolean param0, boolean param1) {
        HitResult var0 = this.minecraft.hitResult;
        if (var0 != null) {
            switch(var0.getType()) {
                case BLOCK:
                    BlockPos var1 = ((BlockHitResult)var0).getBlockPos();
                    BlockState var2 = this.minecraft.player.level.getBlockState(var1);
                    if (param0) {
                        if (param1) {
                            this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(var1, param2 -> {
                                this.copyCreateBlockCommand(var2, var1, param2);
                                this.debugFeedbackTranslated("debug.inspect.server.block");
                            });
                        } else {
                            BlockEntity var3 = this.minecraft.player.level.getBlockEntity(var1);
                            CompoundTag var4 = var3 != null ? var3.save(new CompoundTag()) : null;
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
                    ResourceLocation var6 = Registry.ENTITY_TYPE.getKey(var5.getType());
                    Vec3 var7 = new Vec3(var5.x, var5.y, var5.z);
                    if (param0) {
                        if (param1) {
                            this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(var5.getId(), param2 -> {
                                this.copyCreateEntityCommand(var6, var7, param2);
                                this.debugFeedbackTranslated("debug.inspect.server.entity");
                            });
                        } else {
                            CompoundTag var8 = var5.saveWithoutId(new CompoundTag());
                            this.copyCreateEntityCommand(var6, var7, var8);
                            this.debugFeedbackTranslated("debug.inspect.client.entity");
                        }
                    } else {
                        this.copyCreateEntityCommand(var6, var7, null);
                        this.debugFeedbackTranslated("debug.inspect.client.entity");
                    }
            }

        }
    }

    private void copyCreateBlockCommand(BlockState param0, BlockPos param1, @Nullable CompoundTag param2) {
        if (param2 != null) {
            param2.remove("x");
            param2.remove("y");
            param2.remove("z");
            param2.remove("id");
        }

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
            param2.remove("UUIDMost");
            param2.remove("UUIDLeast");
            param2.remove("Pos");
            param2.remove("Dimension");
            String var0 = param2.getPrettyDisplay().getString();
            var1 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", param0.toString(), param1.x, param1.y, param1.z, var0);
        } else {
            var1 = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", param0.toString(), param1.x, param1.y, param1.z);
        }

        this.setClipboard(var1);
    }

    public void keyPress(long param0, int param1, int param2, int param3, int param4) {
        if (param0 == this.minecraft.getWindow().getWindow()) {
            if (this.debugCrashKeyTime > 0L) {
                if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67)
                    || !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
                    this.debugCrashKeyTime = -1L;
                }
            } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67)
                && InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292)) {
                this.handledDebugKey = true;
                this.debugCrashKeyTime = Util.getMillis();
                this.debugCrashKeyReportedTime = Util.getMillis();
                this.debugCrashKeyReportedCount = 0L;
            }

            ContainerEventHandler var0 = this.minecraft.screen;
            if (param3 == 1 && (!(this.minecraft.screen instanceof ControlsScreen) || ((ControlsScreen)var0).lastKeySelection <= Util.getMillis() - 20L)) {
                if (this.minecraft.options.keyFullscreen.matches(param1, param2)) {
                    this.minecraft.getWindow().toggleFullScreen();
                    this.minecraft.options.fullscreen = this.minecraft.getWindow().isFullscreen();
                    return;
                }

                if (this.minecraft.options.keyScreenshot.matches(param1, param2)) {
                    if (Screen.hasControlDown()) {
                    }

                    Screenshot.grab(
                        this.minecraft.gameDirectory,
                        this.minecraft.getWindow().getWidth(),
                        this.minecraft.getWindow().getHeight(),
                        this.minecraft.getMainRenderTarget(),
                        param0x -> this.minecraft.execute(() -> this.minecraft.gui.getChat().addMessage(param0x))
                    );
                    return;
                }
            }

            boolean var1 = var0 == null || !(var0.getFocused() instanceof EditBox) || !((EditBox)var0.getFocused()).canConsumeInput();
            if (param3 != 0 && param1 == 66 && Screen.hasControlDown() && var1) {
                Option.NARRATOR.toggle(this.minecraft.options, 1);
                if (var0 instanceof ChatOptionsScreen) {
                    ((ChatOptionsScreen)var0).updateNarratorButton();
                }

                if (var0 instanceof AccessibilityOptionsScreen) {
                    ((AccessibilityOptionsScreen)var0).updateNarratorButton();
                }
            }

            if (var0 != null) {
                boolean[] var2 = new boolean[]{false};
                Screen.wrapScreenError(() -> {
                    if (param3 != 1 && (param3 != 2 || !this.sendRepeatsToGui)) {
                        if (param3 == 0) {
                            var2[0] = var0.keyReleased(param1, param2, param4);
                        }
                    } else {
                        var2[0] = var0.keyPressed(param1, param2, param4);
                    }

                }, "keyPressed event handler", var0.getClass().getCanonicalName());
                if (var2[0]) {
                    return;
                }
            }

            if (this.minecraft.screen == null || this.minecraft.screen.passEvents) {
                InputConstants.Key var3 = InputConstants.getKey(param1, param2);
                if (param3 == 0) {
                    KeyMapping.set(var3, false);
                    if (param1 == 292) {
                        if (this.handledDebugKey) {
                            this.handledDebugKey = false;
                        } else {
                            this.minecraft.options.renderDebug = !this.minecraft.options.renderDebug;
                            this.minecraft.options.renderDebugCharts = this.minecraft.options.renderDebug && Screen.hasShiftDown();
                            this.minecraft.options.renderFpsChart = this.minecraft.options.renderDebug && Screen.hasAltDown();
                        }
                    }
                } else {
                    if (param1 == 293 && this.minecraft.gameRenderer != null) {
                        this.minecraft.gameRenderer.togglePostEffect();
                    }

                    boolean var4 = false;
                    if (this.minecraft.screen == null) {
                        if (param1 == 256) {
                            boolean var5 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
                            this.minecraft.pauseGame(var5);
                        }

                        var4 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292) && this.handleDebugKeys(param1);
                        this.handledDebugKey |= var4;
                        if (param1 == 290) {
                            this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
                        }
                    }

                    if (var4) {
                        KeyMapping.set(var3, false);
                    } else {
                        KeyMapping.set(var3, true);
                        KeyMapping.click(var3);
                    }

                    if (this.minecraft.options.renderDebugCharts) {
                        if (param1 == 48) {
                            this.minecraft.debugFpsMeterKeyPress(0);
                        }

                        for(int var6 = 0; var6 < 9; ++var6) {
                            if (param1 == 49 + var6) {
                                this.minecraft.debugFpsMeterKeyPress(var6 + 1);
                            }
                        }
                    }
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

    public void setSendRepeatsToGui(boolean param0) {
        this.sendRepeatsToGui = param0;
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
        this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), param0);
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

                throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
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
