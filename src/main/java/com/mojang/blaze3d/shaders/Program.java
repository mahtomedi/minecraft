package com.mojang.blaze3d.shaders;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.TextureUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class Program {
    private final Program.Type type;
    private final String name;
    private final int id;
    private int references;

    private Program(Program.Type param0, int param1, String param2) {
        this.type = param0;
        this.id = param1;
        this.name = param2;
    }

    public void attachToEffect(Effect param0) {
        ++this.references;
        GLX.glAttachShader(param0.getId(), this.id);
    }

    public void close() {
        --this.references;
        if (this.references <= 0) {
            GLX.glDeleteShader(this.id);
            this.type.getPrograms().remove(this.name);
        }

    }

    public String getName() {
        return this.name;
    }

    public static Program compileShader(Program.Type param0, String param1, InputStream param2) throws IOException {
        String var0 = TextureUtil.readResourceAsString(param2);
        if (var0 == null) {
            throw new IOException("Could not load program " + param0.getName());
        } else {
            int var1 = GLX.glCreateShader(param0.getGlType());
            GLX.glShaderSource(var1, var0);
            GLX.glCompileShader(var1);
            if (GLX.glGetShaderi(var1, GLX.GL_COMPILE_STATUS) == 0) {
                String var2 = StringUtils.trim(GLX.glGetShaderInfoLog(var1, 32768));
                throw new IOException("Couldn't compile " + param0.getName() + " program: " + var2);
            } else {
                Program var3 = new Program(param0, var1, param1);
                param0.getPrograms().put(param1, var3);
                return var3;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum Type {
        VERTEX("vertex", ".vsh", GLX.GL_VERTEX_SHADER),
        FRAGMENT("fragment", ".fsh", GLX.GL_FRAGMENT_SHADER);

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

        private int getGlType() {
            return this.glType;
        }

        public Map<String, Program> getPrograms() {
            return this.programs;
        }
    }
}
