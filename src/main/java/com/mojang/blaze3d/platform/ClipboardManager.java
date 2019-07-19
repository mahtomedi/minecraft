package com.mojang.blaze3d.platform;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import net.minecraft.SharedConstants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

@OnlyIn(Dist.CLIENT)
public class ClipboardManager {
    private final ByteBuffer clipboardScratchBuffer = ByteBuffer.allocateDirect(1024);

    public String getClipboard(long param0, GLFWErrorCallbackI param1) {
        GLFWErrorCallback var0 = GLFW.glfwSetErrorCallback(param1);
        String var1 = GLFW.glfwGetClipboardString(param0);
        var1 = var1 != null ? SharedConstants.filterUnicodeSupplementary(var1) : "";
        GLFWErrorCallback var2 = GLFW.glfwSetErrorCallback(var0);
        if (var2 != null) {
            var2.free();
        }

        return var1;
    }

    private void setClipboard(long param0, ByteBuffer param1, String param2) {
        MemoryUtil.memUTF8(param2, true, param1);
        GLFW.glfwSetClipboardString(param0, param1);
    }

    public void setClipboard(long param0, String param1) {
        int var0 = MemoryUtil.memLengthUTF8(param1, true);
        if (var0 < this.clipboardScratchBuffer.capacity()) {
            this.setClipboard(param0, this.clipboardScratchBuffer, param1);
            ((Buffer)this.clipboardScratchBuffer).clear();
        } else {
            ByteBuffer var1 = ByteBuffer.allocateDirect(var0);
            this.setClipboard(param0, var1, param1);
        }

    }
}
