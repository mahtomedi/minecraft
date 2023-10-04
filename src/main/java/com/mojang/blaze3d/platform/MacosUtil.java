package com.mojang.blaze3d.platform;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFWNativeCocoa;

@OnlyIn(Dist.CLIENT)
public class MacosUtil {
    private static final int NS_RESIZABLE_WINDOW_MASK = 8;
    private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

    public static void exitNativeFullscreen(long param0) {
        getNsWindow(param0).filter(MacosUtil::isInNativeFullscreen).ifPresent(MacosUtil::toggleNativeFullscreen);
    }

    public static void clearResizableBit(long param0) {
        getNsWindow(param0).ifPresent(param0x -> {
            long var0x = getStyleMask(param0x);
            param0x.send("setStyleMask:", new Object[]{var0x & -9L});
        });
    }

    private static Optional<NSObject> getNsWindow(long param0) {
        long var0 = GLFWNativeCocoa.glfwGetCocoaWindow(param0);
        return var0 != 0L ? Optional.of(new NSObject(new Pointer(var0))) : Optional.empty();
    }

    private static boolean isInNativeFullscreen(NSObject param0x) {
        return (getStyleMask(param0x) & 16384L) != 0L;
    }

    private static long getStyleMask(NSObject param0) {
        return param0.sendRaw("styleMask", new Object[0]);
    }

    private static void toggleNativeFullscreen(NSObject param0x) {
        param0x.send("toggleFullScreen:", new Object[]{Pointer.NULL});
    }

    public static void loadIcon(IoSupplier<InputStream> param0) throws IOException {
        try (InputStream var0 = param0.get()) {
            String var1 = Base64.getEncoder().encodeToString(var0.readAllBytes());
            Client var2 = Client.getInstance();
            Object var3 = var2.sendProxy("NSData", "alloc", new Object[0]).send("initWithBase64Encoding:", new Object[]{var1});
            Object var4 = var2.sendProxy("NSImage", "alloc", new Object[0]).send("initWithData:", new Object[]{var3});
            var2.sendProxy("NSApplication", "sharedApplication", new Object[0]).send("setApplicationIconImage:", new Object[]{var4});
        }

    }
}
