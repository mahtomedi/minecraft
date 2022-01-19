package com.mojang.blaze3d.shaders;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class Program {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_LOG_LENGTH = 32768;
    private final Program.Type type;
    private final String name;
    private int id;

    protected Program(Program.Type param0, int param1, String param2) {
        this.type = param0;
        this.id = param1;
        this.name = param2;
    }

    public void attachToShader(Shader param0) {
        RenderSystem.assertOnRenderThread();
        GlStateManager.glAttachShader(param0.getId(), this.getId());
    }

    public void close() {
        if (this.id != -1) {
            RenderSystem.assertOnRenderThread();
            GlStateManager.glDeleteShader(this.id);
            this.id = -1;
            this.type.getPrograms().remove(this.name);
        }
    }

    public String getName() {
        return this.name;
    }

    public static Program compileShader(Program.Type param0, String param1, InputStream param2, String param3, GlslPreprocessor param4) throws IOException {
        RenderSystem.assertOnRenderThread();
        int var0 = compileShaderInternal(param0, param1, param2, param3, param4);
        Program var1 = new Program(param0, var0, param1);
        param0.getPrograms().put(param1, var1);
        return var1;
    }

    protected static int compileShaderInternal(Program.Type param0, String param1, InputStream param2, String param3, GlslPreprocessor param4) throws IOException {
        String var0 = TextureUtil.readResourceAsString(param2);
        if (var0 == null) {
            throw new IOException("Could not load program " + param0.getName());
        } else {
            int var1 = GlStateManager.glCreateShader(param0.getGlType());
            GlStateManager.glShaderSource(var1, param4.process(var0));
            GlStateManager.glCompileShader(var1);
            if (GlStateManager.glGetShaderi(var1, 35713) == 0) {
                String var2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(var1, 32768));
                throw new IOException("Couldn't compile " + param0.getName() + " program (" + param3 + ", " + param1 + ") : " + var2);
            } else {
                return var1;
            }
        }
    }

    private static Program createProgram(Program.Type param0, String param1, int param2) {
        return new Program(param0, param2, param1);
    }

    protected int getId() {
        return this.id;
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        VERTEX("vertex", ".vsh", 35633),
        FRAGMENT("fragment", ".fsh", 35632);

        private final String name;
        private final String extension;
        private final int glType;
        private final Map<String, Program> programs = Maps.newHashMap();

        private Type(String param0, String param1, int param2) {
            this.name = param0;
            this.extension = param1;
            this.glType = param2;
        }

        public String getName() {
            return this.name;
        }

        public String getExtension() {
            return this.extension;
        }

        int getGlType() {
            return this.glType;
        }

        public Map<String, Program> getPrograms() {
            return this.programs;
        }
    }
}
