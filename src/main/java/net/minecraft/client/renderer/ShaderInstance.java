package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Shader;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GlslPreprocessor;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class ShaderInstance implements Shader, AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
    private static ShaderInstance lastAppliedShader;
    private static int lastProgramId = -1;
    private final Map<String, Object> samplerMap = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerLocations = Lists.newArrayList();
    private final List<Uniform> uniforms = Lists.newArrayList();
    private final List<Integer> uniformLocations = Lists.newArrayList();
    private final Map<String, Uniform> uniformMap = Maps.newHashMap();
    private final int programId;
    private final String name;
    private boolean dirty;
    private final BlendMode blend;
    private final List<Integer> attributes;
    private final List<String> attributeNames;
    private final Program vertexProgram;
    private final Program fragmentProgram;
    private final VertexFormat vertexFormat;
    @Nullable
    public final Uniform MODEL_VIEW_MATRIX;
    @Nullable
    public final Uniform PROJECTION_MATRIX;
    @Nullable
    public final Uniform TEXTURE_MATRIX;
    @Nullable
    public final Uniform SCREEN_SIZE;
    @Nullable
    public final Uniform COLOR_MODULATOR;
    @Nullable
    public final Uniform LIGHT0_DIRECTION;
    @Nullable
    public final Uniform LIGHT1_DIRECTION;
    @Nullable
    public final Uniform FOG_START;
    @Nullable
    public final Uniform FOG_END;
    @Nullable
    public final Uniform FOG_COLOR;
    @Nullable
    public final Uniform LINE_WIDTH;
    @Nullable
    public final Uniform GAME_TIME;
    @Nullable
    public final Uniform CHUNK_OFFSET;

    public ShaderInstance(ResourceProvider param0, String param1, VertexFormat param2) throws IOException {
        this.name = param1;
        this.vertexFormat = param2;
        ResourceLocation var0 = new ResourceLocation("shaders/core/" + param1 + ".json");
        Resource var1 = null;

        try {
            var1 = param0.getResource(var0);
            JsonObject var2 = GsonHelper.parse(new InputStreamReader(var1.getInputStream(), StandardCharsets.UTF_8));
            String var3 = GsonHelper.getAsString(var2, "vertex");
            String var4 = GsonHelper.getAsString(var2, "fragment");
            JsonArray var5 = GsonHelper.getAsJsonArray(var2, "samplers", null);
            if (var5 != null) {
                int var6 = 0;

                for(JsonElement var7 : var5) {
                    try {
                        this.parseSamplerNode(var7);
                    } catch (Exception var25) {
                        ChainedJsonException var9 = ChainedJsonException.forException(var25);
                        var9.prependJsonKey("samplers[" + var6 + "]");
                        throw var9;
                    }

                    ++var6;
                }
            }

            JsonArray var10 = GsonHelper.getAsJsonArray(var2, "attributes", null);
            if (var10 != null) {
                int var11 = 0;
                this.attributes = Lists.newArrayListWithCapacity(var10.size());
                this.attributeNames = Lists.newArrayListWithCapacity(var10.size());

                for(JsonElement var12 : var10) {
                    try {
                        this.attributeNames.add(GsonHelper.convertToString(var12, "attribute"));
                    } catch (Exception var241) {
                        ChainedJsonException var14 = ChainedJsonException.forException(var241);
                        var14.prependJsonKey("attributes[" + var11 + "]");
                        throw var14;
                    }

                    ++var11;
                }
            } else {
                this.attributes = null;
                this.attributeNames = null;
            }

            JsonArray var15 = GsonHelper.getAsJsonArray(var2, "uniforms", null);
            if (var15 != null) {
                int var16 = 0;

                for(JsonElement var17 : var15) {
                    try {
                        this.parseUniformNode(var17);
                    } catch (Exception var231) {
                        ChainedJsonException var19 = ChainedJsonException.forException(var231);
                        var19.prependJsonKey("uniforms[" + var16 + "]");
                        throw var19;
                    }

                    ++var16;
                }
            }

            this.blend = parseBlendNode(GsonHelper.getAsJsonObject(var2, "blend", null));
            this.vertexProgram = getOrCreate(param0, Program.Type.VERTEX, var3);
            this.fragmentProgram = getOrCreate(param0, Program.Type.FRAGMENT, var4);
            this.programId = ProgramManager.createProgram();
            if (this.attributeNames != null) {
                int var20 = 0;

                for(String var21 : param2.getElementAttributeNames()) {
                    Uniform.glBindAttribLocation(this.programId, var20, var21);
                    this.attributes.add(var20);
                    ++var20;
                }
            }

            ProgramManager.linkShader(this);
            this.updateLocations();
        } catch (Exception var26) {
            ChainedJsonException var23 = ChainedJsonException.forException(var26);
            var23.setFilenameAndFlush(var0.getPath());
            throw var23;
        } finally {
            IOUtils.closeQuietly((Closeable)var1);
        }

        this.markDirty();
        this.MODEL_VIEW_MATRIX = this.getUniform("ModelViewMat");
        this.PROJECTION_MATRIX = this.getUniform("ProjMat");
        this.TEXTURE_MATRIX = this.getUniform("TextureMat");
        this.SCREEN_SIZE = this.getUniform("ScreenSize");
        this.COLOR_MODULATOR = this.getUniform("ColorModulator");
        this.LIGHT0_DIRECTION = this.getUniform("Light0_Direction");
        this.LIGHT1_DIRECTION = this.getUniform("Light1_Direction");
        this.FOG_START = this.getUniform("FogStart");
        this.FOG_END = this.getUniform("FogEnd");
        this.FOG_COLOR = this.getUniform("FogColor");
        this.LINE_WIDTH = this.getUniform("LineWidth");
        this.GAME_TIME = this.getUniform("GameTime");
        this.CHUNK_OFFSET = this.getUniform("ChunkOffset");
    }

    private static Program getOrCreate(final ResourceProvider param0, Program.Type param1, String param2) throws IOException {
        Program var0 = param1.getPrograms().get(param2);
        Program var5;
        if (var0 == null) {
            String var1 = "shaders/core/" + param2 + param1.getExtension();
            ResourceLocation var2 = new ResourceLocation(var1);
            Resource var3 = param0.getResource(var2);
            final String var4 = FileUtil.getFullResourcePath(var1);

            try {
                var5 = Program.compileShader(param1, param2, var3.getInputStream(), var3.getSourceName(), new GlslPreprocessor() {
                    private final Set<String> importedPaths = Sets.newHashSet();

                    @Override
                    public String applyImport(boolean param0x, String param1) {
                        param1 = FileUtil.normalizeResourcePath((param0 ? var4 : "shaders/include/") + param1);
                        if (!this.importedPaths.add(param1)) {
                            return null;
                        } else {
                            ResourceLocation var0 = new ResourceLocation(param1);

                            try (Resource var1 = param0.getResource(var0)) {
                                return IOUtils.toString(var1.getInputStream(), StandardCharsets.UTF_8);
                            } catch (IOException var18) {
                                ShaderInstance.LOGGER.error("Could not open GLSL import {}: {}", param1, var18.getMessage());
                                return "#error " + var18.getMessage();
                            }
                        }
                    }
                });
            } finally {
                IOUtils.closeQuietly((Closeable)var3);
            }
        } else {
            var5 = var0;
        }

        return var5;
    }

    public static BlendMode parseBlendNode(JsonObject param0) {
        if (param0 == null) {
            return new BlendMode();
        } else {
            int var0 = 32774;
            int var1 = 1;
            int var2 = 0;
            int var3 = 1;
            int var4 = 0;
            boolean var5 = true;
            boolean var6 = false;
            if (GsonHelper.isStringValue(param0, "func")) {
                var0 = BlendMode.stringToBlendFunc(param0.get("func").getAsString());
                if (var0 != 32774) {
                    var5 = false;
                }
            }

            if (GsonHelper.isStringValue(param0, "srcrgb")) {
                var1 = BlendMode.stringToBlendFactor(param0.get("srcrgb").getAsString());
                if (var1 != 1) {
                    var5 = false;
                }
            }

            if (GsonHelper.isStringValue(param0, "dstrgb")) {
                var2 = BlendMode.stringToBlendFactor(param0.get("dstrgb").getAsString());
                if (var2 != 0) {
                    var5 = false;
                }
            }

            if (GsonHelper.isStringValue(param0, "srcalpha")) {
                var3 = BlendMode.stringToBlendFactor(param0.get("srcalpha").getAsString());
                if (var3 != 1) {
                    var5 = false;
                }

                var6 = true;
            }

            if (GsonHelper.isStringValue(param0, "dstalpha")) {
                var4 = BlendMode.stringToBlendFactor(param0.get("dstalpha").getAsString());
                if (var4 != 0) {
                    var5 = false;
                }

                var6 = true;
            }

            if (var5) {
                return new BlendMode();
            } else {
                return var6 ? new BlendMode(var1, var2, var3, var4, var0) : new BlendMode(var1, var2, var0);
            }
        }
    }

    @Override
    public void close() {
        for(Uniform var0 : this.uniforms) {
            var0.close();
        }

        ProgramManager.releaseProgram(this);
    }

    public void clear() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ProgramManager.glUseProgram(0);
        lastProgramId = -1;
        lastAppliedShader = null;
        int var0 = GlStateManager._getActiveTexture();

        for(int var1 = 0; var1 < this.samplerLocations.size(); ++var1) {
            if (this.samplerMap.get(this.samplerNames.get(var1)) != null) {
                GlStateManager._activeTexture(33984 + var1);
                GlStateManager._bindTexture(0);
            }
        }

        GlStateManager._activeTexture(var0);
    }

    public void apply() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        this.dirty = false;
        lastAppliedShader = this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }

        int var0 = GlStateManager._getActiveTexture();

        for(int var1 = 0; var1 < this.samplerLocations.size(); ++var1) {
            String var2 = this.samplerNames.get(var1);
            if (this.samplerMap.get(var2) != null) {
                int var3 = Uniform.glGetUniformLocation(this.programId, var2);
                Uniform.uploadInteger(var3, var1);
                RenderSystem.activeTexture(33984 + var1);
                RenderSystem.enableTexture();
                Object var4 = this.samplerMap.get(var2);
                int var5 = -1;
                if (var4 instanceof RenderTarget) {
                    var5 = ((RenderTarget)var4).getColorTextureId();
                } else if (var4 instanceof AbstractTexture) {
                    var5 = ((AbstractTexture)var4).getId();
                } else if (var4 instanceof Integer) {
                    var5 = (Integer)var4;
                }

                if (var5 != -1) {
                    RenderSystem.bindTexture(var5);
                }
            }
        }

        GlStateManager._activeTexture(var0);

        for(Uniform var6 : this.uniforms) {
            var6.upload();
        }

    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Nullable
    public Uniform getUniform(String param0) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return this.uniformMap.get(param0);
    }

    private void updateLocations() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        IntList var0 = new IntArrayList();

        for(int var1 = 0; var1 < this.samplerNames.size(); ++var1) {
            String var2 = this.samplerNames.get(var1);
            int var3 = Uniform.glGetUniformLocation(this.programId, var2);
            if (var3 == -1) {
                LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.name, var2);
                this.samplerMap.remove(var2);
                var0.add(var1);
            } else {
                this.samplerLocations.add(var3);
            }
        }

        for(int var4 = var0.size() - 1; var4 >= 0; --var4) {
            int var5 = var0.getInt(var4);
            this.samplerNames.remove(var5);
        }

        for(Uniform var6 : this.uniforms) {
            String var7 = var6.getName();
            int var8 = Uniform.glGetUniformLocation(this.programId, var7);
            if (var8 == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, var7);
            } else {
                this.uniformLocations.add(var8);
                var6.setLocation(var8);
                this.uniformMap.put(var7, var6);
            }
        }

    }

    private void parseSamplerNode(JsonElement param0) {
        JsonObject var0 = GsonHelper.convertToJsonObject(param0, "sampler");
        String var1 = GsonHelper.getAsString(var0, "name");
        if (!GsonHelper.isStringValue(var0, "file")) {
            this.samplerMap.put(var1, null);
            this.samplerNames.add(var1);
        } else {
            this.samplerNames.add(var1);
        }
    }

    public void setSampler(String param0, Object param1) {
        this.samplerMap.put(param0, param1);
        this.markDirty();
    }

    private void parseUniformNode(JsonElement param0) throws ChainedJsonException {
        JsonObject var0 = GsonHelper.convertToJsonObject(param0, "uniform");
        String var1 = GsonHelper.getAsString(var0, "name");
        int var2 = Uniform.getTypeFromString(GsonHelper.getAsString(var0, "type"));
        int var3 = GsonHelper.getAsInt(var0, "count");
        float[] var4 = new float[Math.max(var3, 16)];
        JsonArray var5 = GsonHelper.getAsJsonArray(var0, "values");
        if (var5.size() != var3 && var5.size() > 1) {
            throw new ChainedJsonException("Invalid amount of values specified (expected " + var3 + ", found " + var5.size() + ")");
        } else {
            int var6 = 0;

            for(JsonElement var7 : var5) {
                try {
                    var4[var6] = GsonHelper.convertToFloat(var7, "value");
                } catch (Exception var13) {
                    ChainedJsonException var9 = ChainedJsonException.forException(var13);
                    var9.prependJsonKey("values[" + var6 + "]");
                    throw var9;
                }

                ++var6;
            }

            if (var3 > 1 && var5.size() == 1) {
                while(var6 < var3) {
                    var4[var6] = var4[0];
                    ++var6;
                }
            }

            int var10 = var3 > 1 && var3 <= 4 && var2 < 8 ? var3 - 1 : 0;
            Uniform var11 = new Uniform(var1, var2 + var10, var3, this);
            if (var2 <= 3) {
                var11.setSafe((int)var4[0], (int)var4[1], (int)var4[2], (int)var4[3]);
            } else if (var2 <= 7) {
                var11.setSafe(var4[0], var4[1], var4[2], var4[3]);
            } else {
                var11.set(var4);
            }

            this.uniforms.add(var11);
        }
    }

    @Override
    public Program getVertexProgram() {
        return this.vertexProgram;
    }

    @Override
    public Program getFragmentProgram() {
        return this.fragmentProgram;
    }

    @Override
    public void attachToProgram() {
        this.fragmentProgram.attachToShader(this);
        this.vertexProgram.attachToShader(this);
    }

    @Override
    public int getId() {
        return this.programId;
    }
}
