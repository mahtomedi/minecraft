package com.mojang.blaze3d.platform;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
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
        param0x.send("toggleFullScreen:", new Object[]{Pointer.NULL});
    }

    public static void loadIcon(InputStream param0) throws IOException {
        String var0 = Base64.getEncoder().encodeToString(param0.readAllBytes());
        Client var1 = Client.getInstance();
        Object var2 = var1.sendProxy("NSData", "alloc", new Object[0]).send("initWithBase64Encoding:", new Object[]{var0});
        Object var3 = var1.sendProxy("NSImage", "alloc", new Object[0]).send("initWithData:", new Object[]{var2});
        var1.sendProxy("NSApplication", "sharedApplication", new Object[0]).send("setApplicationIconImage:", new Object[]{var3});
    }
}
