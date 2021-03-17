package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.shaders.BlendMode;
import com.mojang.blaze3d.shaders.Effect;
import com.mojang.blaze3d.shaders.EffectProgram;
import com.mojang.blaze3d.shaders.Program;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class EffectInstance implements Effect, AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final AbstractUniform DUMMY_UNIFORM = new AbstractUniform();
    private static EffectInstance lastAppliedEffect;
    private static int lastProgramId = -1;
    private final Map<String, IntSupplier> samplerMap = Maps.newHashMap();
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
    private final EffectProgram vertexProgram;
    private final EffectProgram fragmentProgram;

    public EffectInstance(ResourceManager param0, String param1) throws IOException {
        ResourceLocation var0 = new ResourceLocation("shaders/program/" + param1 + ".json");
        this.name = param1;
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
                    } catch (Exception var241) {
                        ChainedJsonException var9 = ChainedJsonException.forException(var241);
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
                    } catch (Exception var23) {
                        ChainedJsonException var14 = ChainedJsonException.forException(var23);
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
                    } catch (Exception var221) {
                        ChainedJsonException var19 = ChainedJsonException.forException(var221);
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
            ProgramManager.linkShader(this);
            this.updateLocations();
            if (this.attributeNames != null) {
                for(String var20 : this.attributeNames) {
                    int var21 = Uniform.glGetAttribLocation(this.programId, var20);
                    this.attributes.add(var21);
                }
            }
        } catch (Exception var251) {
            String var23;
            if (var1 != null) {
                var23 = " (" + var1.getSourceName() + ")";
            } else {
                var23 = "";
            }

            ChainedJsonException var25 = ChainedJsonException.forException(var251);
            var25.setFilenameAndFlush(var0.getPath() + var23);
            throw var25;
        } finally {
            IOUtils.closeQuietly((Closeable)var1);
        }

        this.markDirty();
    }

    public static EffectProgram getOrCreate(ResourceManager param0, Program.Type param1, String param2) throws IOException {
        Program var0 = param1.getPrograms().get(param2);
        if (var0 != null && !(var0 instanceof EffectProgram)) {
            throw new InvalidClassException("Program is not of type EffectProgram");
        } else {
            EffectProgram var3;
            if (var0 == null) {
                ResourceLocation var1 = new ResourceLocation("shaders/program/" + param2 + param1.getExtension());
                Resource var2 = param0.getResource(var1);

                try {
                    var3 = EffectProgram.compileShader(param1, param2, var2.getInputStream(), var2.getSourceName());
                } finally {
                    IOUtils.closeQuietly((Closeable)var2);
                }
            } else {
                var3 = (EffectProgram)var0;
            }

            return var3;
        }
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
        lastAppliedEffect = null;

        for(int var0 = 0; var0 < this.samplerLocations.size(); ++var0) {
            if (this.samplerMap.get(this.samplerNames.get(var0)) != null) {
                GlStateManager._activeTexture(33984 + var0);
                GlStateManager._disableTexture();
                GlStateManager._bindTexture(0);
            }
        }

    }

    public void apply() {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        this.dirty = false;
        lastAppliedEffect = this;
        this.blend.apply();
        if (this.programId != lastProgramId) {
            ProgramManager.glUseProgram(this.programId);
            lastProgramId = this.programId;
        }

        for(int var0 = 0; var0 < this.samplerLocations.size(); ++var0) {
            String var1 = this.samplerNames.get(var0);
            IntSupplier var2 = this.samplerMap.get(var1);
            if (var2 != null) {
                RenderSystem.activeTexture(33984 + var0);
                RenderSystem.enableTexture();
                int var3 = var2.getAsInt();
                if (var3 != -1) {
                    RenderSystem.bindTexture(var3);
                    Uniform.uploadInteger(this.samplerLocations.get(var0), var0);
                }
            }
        }

        for(Uniform var4 : this.uniforms) {
            var4.upload();
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

    public AbstractUniform safeGetUniform(String param0) {
        RenderSystem.assertThread(RenderSystem::isOnGameThread);
        Uniform var0 = this.getUniform(param0);
        return (AbstractUniform)(var0 == null ? DUMMY_UNIFORM : var0);
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
            this.samplerNames.remove(var0.getInt(var4));
        }

        for(Uniform var5 : this.uniforms) {
            String var6 = var5.getName();
            int var7 = Uniform.glGetUniformLocation(this.programId, var6);
            if (var7 == -1) {
                LOGGER.warn("Shader {} could not find uniform named {} in the specified shader program.", this.name, var6);
            } else {
                this.uniformLocations.add(var7);
                var5.setLocation(var7);
                this.uniformMap.put(var6, var5);
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

    public void setSampler(String param0, IntSupplier param1) {
        if (this.samplerMap.containsKey(param0)) {
            this.samplerMap.remove(param0);
        }

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
        this.fragmentProgram.attachToEffect(this);
        this.vertexProgram.attachToEffect(this);
    }

    @Override
    public int getId() {
        return this.programId;
    }
}
