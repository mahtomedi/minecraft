package net.minecraft.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFWDropCallback;

@OnlyIn(Dist.CLIENT)
public class MouseHandler {
    private final Minecraft minecraft;
    private boolean isLeftPressed;
    private boolean isMiddlePressed;
    private boolean isRightPressed;
    private double xpos;
    private double ypos;
    private int fakeRightMouse;
    private int activeButton = -1;
    private boolean ignoreFirstMove = true;
    private int clickDepth;
    private double mousePressedTime;
    private final SmoothDouble smoothTurnX = new SmoothDouble();
    private final SmoothDouble smoothTurnY = new SmoothDouble();
    private double accumulatedDX;
    private double accumulatedDY;
    private double accumulatedScroll;
    private double lastMouseEventTime = Double.MIN_VALUE;
    private boolean mouseGrabbed;

    public MouseHandler(Minecraft param0) {
        this.minecraft = param0;
    }

    private void onPress(long param0, int param1, int param2, int param3) {
        if (param0 == this.minecraft.getWindow().getWindow()) {
            boolean var0 = param2 == 1;
            if (Minecraft.ON_OSX && param1 == 0) {
                if (var0) {
                    if ((param3 & 2) == 2) {
                        param1 = 1;
                        ++this.fakeRightMouse;
                    }
                } else if (this.fakeRightMouse > 0) {
                    param1 = 1;
                    --this.fakeRightMouse;
                }
            }

            int var1 = param1;
            if (var0) {
                if (this.minecraft.options.touchscreen && this.clickDepth++ > 0) {
                    return;
                }

                this.activeButton = var1;
                this.mousePressedTime = Blaze3D.getTime();
            } else if (this.activeButton != -1) {
                if (this.minecraft.options.touchscreen && --this.clickDepth > 0) {
                    return;
                }

                this.activeButton = -1;
            }

            boolean[] var2 = new boolean[]{false};
            if (this.minecraft.getOverlay() == null) {
                if (this.minecraft.screen == null) {
                    if (!this.mouseGrabbed && var0) {
                        this.grabMouse();
                    }
                } else {
                    double var3 = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
                    double var4 = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
                    if (var0) {
                        Screen.wrapScreenError(
                            () -> var2[0] = this.minecraft.screen.mouseClicked(var3, var4, var1),
                            "mouseClicked event handler",
                            this.minecraft.screen.getClass().getCanonicalName()
                        );
                    } else {
                        Screen.wrapScreenError(
                            () -> var2[0] = this.minecraft.screen.mouseReleased(var3, var4, var1),
                            "mouseReleased event handler",
                            this.minecraft.screen.getClass().getCanonicalName()
                        );
                    }
                }
            }

            if (!var2[0] && (this.minecraft.screen == null || this.minecraft.screen.passEvents) && this.minecraft.getOverlay() == null) {
                if (var1 == 0) {
                    this.isLeftPressed = var0;
                } else if (var1 == 2) {
                    this.isMiddlePressed = var0;
                } else if (var1 == 1) {
                    this.isRightPressed = var0;
                }

                KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(var1), var0);
                if (var0) {
                    if (this.minecraft.player.isSpectator() && var1 == 2) {
                        this.minecraft.gui.getSpectatorGui().onMouseMiddleClick();
                    } else {
                        KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(var1));
                    }
                }
            }

        }
    }

    private void onScroll(long param0, double param1, double param2) {
        if (param0 == Minecraft.getInstance().getWindow().getWindow()) {
            double var0 = (this.minecraft.options.discreteMouseScroll ? Math.signum(param2) : param2) * this.minecraft.options.mouseWheelSensitivity;
            if (this.minecraft.getOverlay() == null) {
                if (this.minecraft.screen != null) {
                    double var1 = this.xpos * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
                    double var2 = this.ypos * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
                    this.minecraft.screen.mouseScrolled(var1, var2, var0);
                } else if (this.minecraft.player != null) {
                    if (this.accumulatedScroll != 0.0 && Math.signum(var0) != Math.signum(this.accumulatedScroll)) {
                        this.accumulatedScroll = 0.0;
                    }

                    this.accumulatedScroll += var0;
                    float var3 = (float)((int)this.accumulatedScroll);
                    if (var3 == 0.0F) {
                        return;
                    }

                    this.accumulatedScroll -= (double)var3;
                    if (this.minecraft.player.isSpectator()) {
                        if (this.minecraft.gui.getSpectatorGui().isMenuActive()) {
                            this.minecraft.gui.getSpectatorGui().onMouseScrolled((double)(-var3));
                        } else {
                            float var4 = Mth.clamp(this.minecraft.player.getAbilities().getFlyingSpeed() + var3 * 0.005F, 0.0F, 0.2F);
                            this.minecraft.player.getAbilities().setFlyingSpeed(var4);
                        }
                    } else {
                        this.minecraft.player.getInventory().swapPaint((double)var3);
                    }
                }
            }
        }

    }

    private void onDrop(long param0, List<Path> param1) {
        if (this.minecraft.screen != null) {
            this.minecraft.screen.onFilesDrop(param1);
        }

    }

    public void setup(long param0) {
        InputConstants.setupMouseCallbacks(
            param0,
            (param0x, param1, param2) -> this.minecraft.execute(() -> this.onMove(param0x, param1, param2)),
            (param0x, param1, param2, param3) -> this.minecraft.execute(() -> this.onPress(param0x, param1, param2, param3)),
            (param0x, param1, param2) -> this.minecraft.execute(() -> this.onScroll(param0x, param1, param2)),
            (param0x, param1, param2) -> {
                Path[] var0 = new Path[param1];
    
                for(int var1x = 0; var1x < param1; ++var1x) {
                    var0[var1x] = Paths.get(GLFWDropCallback.getName(param2, var1x));
                }
    
                this.minecraft.execute(() -> this.onDrop(param0x, Arrays.asList(var0)));
            }
        );
    }

    private void onMove(long param0, double param1, double param2) {
        if (param0 == Minecraft.getInstance().getWindow().getWindow()) {
            if (this.ignoreFirstMove) {
                this.xpos = param1;
                this.ypos = param2;
                this.ignoreFirstMove = false;
            }

            GuiEventListener var0 = this.minecraft.screen;
            if (var0 != null && this.minecraft.getOverlay() == null) {
                double var1 = param1 * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
                double var2 = param2 * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();
                Screen.wrapScreenError(() -> var0.mouseMoved(var1, var2), "mouseMoved event handler", var0.getClass().getCanonicalName());
                if (this.activeButton != -1 && this.mousePressedTime > 0.0) {
                    double var3 = (param1 - this.xpos)
                        * (double)this.minecraft.getWindow().getGuiScaledWidth()
                        / (double)this.minecraft.getWindow().getScreenWidth();
                    double var4 = (param2 - this.ypos)
                        * (double)this.minecraft.getWindow().getGuiScaledHeight()
                        / (double)this.minecraft.getWindow().getScreenHeight();
                    Screen.wrapScreenError(
                        () -> var0.mouseDragged(var1, var2, this.activeButton, var3, var4), "mouseDragged event handler", var0.getClass().getCanonicalName()
                    );
                }
            }

            this.minecraft.getProfiler().push("mouse");
            if (this.isMouseGrabbed() && this.minecraft.isWindowActive()) {
                this.accumulatedDX += param1 - this.xpos;
                this.accumulatedDY += param2 - this.ypos;
            }

            this.turnPlayer();
            this.xpos = param1;
            this.ypos = param2;
            this.minecraft.getProfiler().pop();
        }
    }

    public void turnPlayer() {
        double var0 = Blaze3D.getTime();
        double var1 = var0 - this.lastMouseEventTime;
        this.lastMouseEventTime = var0;
        if (this.isMouseGrabbed() && this.minecraft.isWindowActive()) {
            double var2 = this.minecraft.options.sensitivity * 0.6F + 0.2F;
            double var3 = var2 * var2 * var2;
            double var4 = var3 * 8.0;
            double var7;
            double var8;
            if (this.minecraft.options.smoothCamera) {
                double var5 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * var4, var1 * var4);
                double var6 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * var4, var1 * var4);
                var7 = var5;
                var8 = var6;
            } else if (this.minecraft.options.getCameraType().isFirstPerson() && this.minecraft.player.isScoping()) {
                this.smoothTurnX.reset();
                this.smoothTurnY.reset();
                var7 = this.accumulatedDX * var3;
                var8 = this.accumulatedDY * var3;
            } else {
                this.smoothTurnX.reset();
                this.smoothTurnY.reset();
                var7 = this.accumulatedDX * var4;
                var8 = this.accumulatedDY * var4;
            }

            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;
            int var13 = 1;
            if (this.minecraft.options.invertYMouse) {
                var13 = -1;
            }

            this.minecraft.getTutorial().onMouse(var7, var8);
            if (this.minecraft.player != null) {
                this.minecraft.player.turn(var7, var8 * (double)var13);
            }

        } else {
            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;
        }
    }

    public boolean isLeftPressed() {
        return this.isLeftPressed;
    }

    public boolean isMiddlePressed() {
        return this.isMiddlePressed;
    }

    public boolean isRightPressed() {
        return this.isRightPressed;
    }

    public double xpos() {
        return this.xpos;
    }

    public double ypos() {
        return this.ypos;
    }

    public void setIgnoreFirstMove() {
        this.ignoreFirstMove = true;
    }

    public boolean isMouseGrabbed() {
        return this.mouseGrabbed;
    }

    public void grabMouse() {
        if (this.minecraft.isWindowActive()) {
            if (!this.mouseGrabbed) {
                if (!Minecraft.ON_OSX) {
                    KeyMapping.setAll();
                }

                this.mouseGrabbed = true;
                this.xpos = (double)(this.minecraft.getWindow().getScreenWidth() / 2);
                this.ypos = (double)(this.minecraft.getWindow().getScreenHeight() / 2);
                InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
                this.minecraft.setScreen(null);
                this.minecraft.missTime = 10000;
                this.ignoreFirstMove = true;
            }
        }
    }

    public void releaseMouse() {
        if (this.mouseGrabbed) {
            this.mouseGrabbed = false;
            this.xpos = (double)(this.minecraft.getWindow().getScreenWidth() / 2);
            this.ypos = (double)(this.minecraft.getWindow().getScreenHeight() / 2);
            InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212993, this.xpos, this.ypos);
        }
    }

    public void cursorEntered() {
        this.ignoreFirstMove = true;
    }
}
