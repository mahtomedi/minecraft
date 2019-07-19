package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GLX;
import java.io.IOException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ProgramManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static ProgramManager instance;

    public static void createInstance() {
        instance = new ProgramManager();
    }

    public static ProgramManager getInstance() {
        return instance;
    }

    private ProgramManager() {
    }

    public void releaseProgram(Effect param0) {
        param0.getFragmentProgram().close();
        param0.getVertexProgram().close();
        GLX.glDeleteProgram(param0.getId());
    }

    public int createProgram() throws IOException {
        int var0 = GLX.glCreateProgram();
        if (var0 <= 0) {
            throw new IOException("Could not create shader program (returned program ID " + var0 + ")");
        } else {
            return var0;
        }
    }

    public void linkProgram(Effect param0) throws IOException {
        param0.getFragmentProgram().attachToEffect(param0);
        param0.getVertexProgram().attachToEffect(param0);
        GLX.glLinkProgram(param0.getId());
        int var0 = GLX.glGetProgrami(param0.getId(), GLX.GL_LINK_STATUS);
        if (var0 == 0) {
            LOGGER.warn(
                "Error encountered when linking program containing VS {} and FS {}. Log output:",
                param0.getVertexProgram().getName(),
                param0.getFragmentProgram().getName()
            );
            LOGGER.warn(GLX.glGetProgramInfoLog(param0.getId(), 32768));
        }

    }
}
