package com.mojang.blaze3d.platform;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.system.Pointer;

@OnlyIn(Dist.CLIENT)
public class DebugMemoryUntracker {
    @Nullable
    private static final MethodHandle UNTRACK = GLX.make(() -> {
        try {
            Lookup var0 = MethodHandles.lookup();
            Class<?> var1 = Class.forName("org.lwjgl.system.MemoryManage$DebugAllocator");
            Method var2 = var1.getDeclaredMethod("untrack", Long.TYPE);
            var2.setAccessible(true);
            Field var3 = Class.forName("org.lwjgl.system.MemoryUtil$LazyInit").getDeclaredField("ALLOCATOR");
            var3.setAccessible(true);
            Object var4 = var3.get(null);
            return var1.isInstance(var4) ? var0.unreflect(var2) : null;
        } catch (NoSuchMethodException | NoSuchFieldException | IllegalAccessException | ClassNotFoundException var51) {
            throw new RuntimeException(var51);
        }
    });

    public static void untrack(long param0) {
        if (UNTRACK != null) {
            try {
                UNTRACK.invoke((long)param0);
            } catch (Throwable var3) {
                throw new RuntimeException(var3);
            }
        }
    }

    public static void untrack(Pointer param0) {
        untrack(param0.address());
    }
}
