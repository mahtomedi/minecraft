package com.mojang.blaze3d.platform;

import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.util.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFWNativeCocoa;

@OnlyIn(Dist.CLIENT)
public class MacosUtil {
    private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

    public static void toggleFullscreen(long param0) {
        getNsWindow(param0).filter(MacosUtil::isInKioskMode).ifPresent(MacosUtil::toggleFullscreen);
    }

    private static Optional<NSObject> getNsWindow(long param0) {
        long var0 = GLFWNativeCocoa.glfwGetCocoaWindow(param0);
        return var0 != 0L ? Optional.of(new NSObject(new Pointer(var0))) : Optional.empty();
    }

    private static boolean isInKioskMode(NSObject param0x) {
        return (param0x.sendRaw("styleMask", new Object[0]) & 16384L) == 16384L;
    }

    private static void toggleFullscreen(NSObject param0x) {
        param0x.send("toggleFullScreen:", new Object[0]);
    }
}
