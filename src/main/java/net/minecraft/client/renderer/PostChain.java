package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.TextureObject;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

@OnlyIn(Dist.CLIENT)
public class PostChain implements AutoCloseable {
    private final RenderTarget screenTarget;
    private final ResourceManager resourceManager;
    private final String name;
    private final List<PostPass> passes = Lists.newArrayList();
    private final Map<String, RenderTarget> customRenderTargets = Maps.newHashMap();
    private final List<RenderTarget> fullSizedTargets = Lists.newArrayList();
    private Matrix4f shaderOrthoMatrix;
    private int screenWidth;
    private int screenHeight;
    private float time;
    private float lastStamp;

    public PostChain(TextureManager param0, ResourceManager param1, RenderTarget param2, ResourceLocation param3) throws IOException, JsonSyntaxException {
        this.resourceManager = param1;
        this.screenTarget = param2;
        this.time = 0.0F;
        this.lastStamp = 0.0F;
        this.screenWidth = param2.viewWidth;
        this.screenHeight = param2.viewHeight;
        this.name = param3.toString();
        this.updateOrthoMatrix();
        this.load(param0, param3);
    }

    private void load(TextureManager param0, ResourceLocation param1) throws IOException, JsonSyntaxException {
        Resource var0 = null;

        try {
            var0 = this.resourceManager.getResource(param1);
            JsonObject var1 = GsonHelper.parse(new InputStreamReader(var0.getInputStream(), StandardCharsets.UTF_8));
            if (GsonHelper.isArrayNode(var1, "targets")) {
                JsonArray var2 = var1.getAsJsonArray("targets");
                int var3 = 0;

                for(JsonElement var4 : var2) {
                    try {
                        this.parseTargetNode(var4);
                    } catch (Exception var17) {
                        ChainedJsonException var6 = ChainedJsonException.forException(var17);
                        var6.prependJsonKey("targets[" + var3 + "]");
                        throw var6;
                    }

                    ++var3;
                }
            }

            if (GsonHelper.isArrayNode(var1, "passes")) {
                JsonArray var7 = var1.getAsJsonArray("passes");
                int var8 = 0;

                for(JsonElement var9 : var7) {
                    try {
                        this.parsePassNode(param0, var9);
                    } catch (Exception var16) {
                        ChainedJsonException var11 = ChainedJsonException.forException(var16);
                        var11.prependJsonKey("passes[" + var8 + "]");
                        throw var11;
                    }

                    ++var8;
                }
            }
        } catch (Exception var18) {
            ChainedJsonException var13 = ChainedJsonException.forException(var18);
            var13.setFilenameAndFlush(param1.getPath());
            throw var13;
        } finally {
            IOUtils.closeQuietly((Closeable)var0);
        }

    }

    private void parseTargetNode(JsonElement param0) throws ChainedJsonException {
        if (GsonHelper.isStringValue(param0)) {
            this.addTempTarget(param0.getAsString(), this.screenWidth, this.screenHeight);
        } else {
            JsonObject var0 = GsonHelper.convertToJsonObject(param0, "target");
            String var1 = GsonHelper.getAsString(var0, "name");
            int var2 = GsonHelper.getAsInt(var0, "width", this.screenWidth);
            int var3 = GsonHelper.getAsInt(var0, "height", this.screenHeight);
            if (this.customRenderTargets.containsKey(var1)) {
                throw new ChainedJsonException(var1 + " is already defined");
            }

            this.addTempTarget(var1, var2, var3);
        }

    }

    private void parsePassNode(TextureManager param0, JsonElement param1) throws IOException {
        JsonObject var0 = GsonHelper.convertToJsonObject(param1, "pass");
        String var1 = GsonHelper.getAsString(var0, "name");
        String var2 = GsonHelper.getAsString(var0, "intarget");
        String var3 = GsonHelper.getAsString(var0, "outtarget");
        RenderTarget var4 = this.getRenderTarget(var2);
        RenderTarget var5 = this.getRenderTarget(var3);
        if (var4 == null) {
            throw new ChainedJsonException("Input target '" + var2 + "' does not exist");
        } else if (var5 == null) {
            throw new ChainedJsonException("Output target '" + var3 + "' does not exist");
        } else {
            PostPass var6 = this.addPass(var1, var4, var5);
            JsonArray var7 = GsonHelper.getAsJsonArray(var0, "auxtargets", null);
            if (var7 != null) {
                int var8 = 0;

                for(JsonElement var9 : var7) {
                    try {
                        JsonObject var10 = GsonHelper.convertToJsonObject(var9, "auxtarget");
                        String var11 = GsonHelper.getAsString(var10, "name");
                        String var12 = GsonHelper.getAsString(var10, "id");
                        RenderTarget var13 = this.getRenderTarget(var12);
                        if (var13 == null) {
                            ResourceLocation var14 = new ResourceLocation("textures/effect/" + var12 + ".png");
                            Resource var15 = null;

                            try {
                                var15 = this.resourceManager.getResource(var14);
                            } catch (FileNotFoundException var29) {
                                throw new ChainedJsonException("Render target or texture '" + var12 + "' does not exist");
                            } finally {
                                IOUtils.closeQuietly((Closeable)var15);
                            }

                            param0.bind(var14);
                            TextureObject var17 = param0.getTexture(var14);
                            int var18 = GsonHelper.getAsInt(var10, "width");
                            int var19 = GsonHelper.getAsInt(var10, "height");
                            boolean var20x = GsonHelper.getAsBoolean(var10, "bilinear");
                            if (var20x) {
                                RenderSystem.texParameter(3553, 10241, 9729);
                                RenderSystem.texParameter(3553, 10240, 9729);
                            } else {
                                RenderSystem.texParameter(3553, 10241, 9728);
                                RenderSystem.texParameter(3553, 10240, 9728);
                            }

                            var6.addAuxAsset(var11, var17.getId(), var18, var19);
                        } else {
                            var6.addAuxAsset(var11, var13, var13.width, var13.height);
                        }
                    } catch (Exception var31) {
                        ChainedJsonException var22 = ChainedJsonException.forException(var31);
                        var22.prependJsonKey("auxtargets[" + var8 + "]");
                        throw var22;
                    }

                    ++var8;
                }
            }

            JsonArray var23 = GsonHelper.getAsJsonArray(var0, "uniforms", null);
            if (var23 != null) {
                int var24 = 0;

                for(JsonElement var25 : var23) {
                    try {
                        this.parseUniformNode(var25);
                    } catch (Exception var28) {
                        ChainedJsonException var27 = ChainedJsonException.forException(var28);
                        var27.prependJsonKey("uniforms[" + var24 + "]");
                        throw var27;
                    }

                    ++var24;
                }
            }

        }
    }

    private void parseUniformNode(JsonElement param0) throws ChainedJsonException {
        JsonObject var0 = GsonHelper.convertToJsonObject(param0, "uniform");
        String var1 = GsonHelper.getAsString(var0, "name");
        Uniform var2 = this.passes.get(this.passes.size() - 1).getEffect().getUniform(var1);
        if (var2 == null) {
            throw new ChainedJsonException("Uniform '" + var1 + "' does not exist");
        } else {
            float[] var3 = new float[4];
            int var4 = 0;

            for(JsonElement var6 : GsonHelper.getAsJsonArray(var0, "values")) {
                try {
                    var3[var4] = GsonHelper.convertToFloat(var6, "value");
                } catch (Exception var12) {
                    ChainedJsonException var8 = ChainedJsonException.forException(var12);
                    var8.prependJsonKey("values[" + var4 + "]");
                    throw var8;
                }

                ++var4;
            }

            switch(var4) {
                case 0:
                default:
                    break;
                case 1:
                    var2.set(var3[0]);
                    break;
                case 2:
                    var2.set(var3[0], var3[1]);
                    break;
                case 3:
                    var2.set(var3[0], var3[1], var3[2]);
                    break;
                case 4:
                    var2.set(var3[0], var3[1], var3[2], var3[3]);
            }

        }
    }

    public RenderTarget getTempTarget(String param0) {
        return this.customRenderTargets.get(param0);
    }

    public void addTempTarget(String param0, int param1, int param2) {
        RenderTarget var0 = new RenderTarget(param1, param2, true, Minecraft.ON_OSX);
        var0.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.customRenderTargets.put(param0, var0);
        if (param1 == this.screenWidth && param2 == this.screenHeight) {
            this.fullSizedTargets.add(var0);
        }

    }

    @Override
    public void close() {
        for(RenderTarget var0 : this.customRenderTargets.values()) {
            var0.destroyBuffers();
        }

        for(PostPass var1 : this.passes) {
            var1.close();
        }

        this.passes.clear();
    }

    public PostPass addPass(String param0, RenderTarget param1, RenderTarget param2) throws IOException {
        PostPass var0 = new PostPass(this.resourceManager, param0, param1, param2);
        this.passes.add(this.passes.size(), var0);
        return var0;
    }

    private void updateOrthoMatrix() {
        this.shaderOrthoMatrix = Matrix4f.orthographic((float)this.screenTarget.width, (float)this.screenTarget.height, 0.1F, 1000.0F);
    }

    public void resize(int param0, int param1) {
        this.screenWidth = this.screenTarget.width;
        this.screenHeight = this.screenTarget.height;
        this.updateOrthoMatrix();

        for(PostPass var0 : this.passes) {
            var0.setOrthoMatrix(this.shaderOrthoMatrix);
        }

        for(RenderTarget var1 : this.fullSizedTargets) {
            var1.resize(param0, param1, Minecraft.ON_OSX);
        }

    }

    public void process(float param0) {
        if (param0 < this.lastStamp) {
            this.time += 1.0F - this.lastStamp;
            this.time += param0;
        } else {
            this.time += param0 - this.lastStamp;
        }

        this.lastStamp = param0;

        while(this.time > 20.0F) {
            this.time -= 20.0F;
        }

        for(PostPass var0 : this.passes) {
            var0.process(this.time / 20.0F);
        }

    }

    public final String getName() {
        return this.name;
    }

    private RenderTarget getRenderTarget(String param0) {
        if (param0 == null) {
            return null;
        } else {
            return param0.equals("minecraft:main") ? this.screenTarget : this.customRenderTargets.get(param0);
        }
    }
}
