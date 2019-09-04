package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import java.io.IOException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ProgramManager {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void glUseProgram(int param0) {
        GlStateManager._glUseProgram(param0);
    }

    public static void releaseProgram(Effect param0) {
        param0.getFragmentProgram().close();
        param0.getVertexProgram().close();
        GlStateManager.glDeleteProgram(param0.getId());
    }

    public static int createProgram() throws IOException {
        int var0 = GlStateManager.glCreateProgram();
        if (var0 <= 0) {
            throw new IOException("Could not create shader program (returned program ID " + var0 + ")");
        } else {
            return var0;
        }
    }

    public static void linkProgram(Effect param0) throws IOException {
        param0.getFragmentProgram().attachToEffect(param0);
        param0.getVertexProgram().attachToEffect(param0);
        GlStateManager.glLinkProgram(param0.getId());
        int var0 = GlStateManager.glGetProgrami(param0.getId(), 35714);
        if (var0 == 0) {
            LOGGER.warn(
                "Error encountered when linking program containing VS {} and FS {}. Log output:",
                param0.getVertexProgram().getName(),
                param0.getFragmentProgram().getName()
            );
            LOGGER.warn(GlStateManager.glGetProgramInfoLog(param0.getId(), 32768));
        }

    }
}
