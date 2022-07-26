package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ChainedJsonException;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class PostChain implements AutoCloseable {
    private static final String MAIN_RENDER_TARGET = "minecraft:main";
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
        Resource var0 = this.resourceManager.getResourceOrThrow(param1);

        try {
            try (Reader var1 = var0.openAsReader()) {
                JsonObject var2 = GsonHelper.parse(var1);
                if (GsonHelper.isArrayNode(var2, "targets")) {
                    JsonArray var3 = var2.getAsJsonArray("targets");
                    int var4 = 0;

                    for(JsonElement var5 : var3) {
                        try {
                            this.parseTargetNode(var5);
                        } catch (Exception var141) {
                            ChainedJsonException var7 = ChainedJsonException.forException(var141);
                            var7.prependJsonKey("targets[" + var4 + "]");
                            throw var7;
                        }

                        ++var4;
                    }
                }

                if (GsonHelper.isArrayNode(var2, "passes")) {
                    JsonArray var8 = var2.getAsJsonArray("passes");
                    int var9 = 0;

                    for(JsonElement var10 : var8) {
                        try {
                            this.parsePassNode(param0, var10);
                        } catch (Exception var131) {
                            ChainedJsonException var12 = ChainedJsonException.forException(var131);
                            var12.prependJsonKey("passes[" + var9 + "]");
                            throw var12;
                        }

                        ++var9;
                    }
                }
            }

        } catch (Exception var16) {
            ChainedJsonException var14 = ChainedJsonException.forException(var16);
            var14.setFilenameAndFlush(param1.getPath() + " (" + var0.sourcePackId() + ")");
            throw var14;
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
                        boolean var13;
                        String var14;
                        if (var12.endsWith(":depth")) {
                            var13 = true;
                            var14 = var12.substring(0, var12.lastIndexOf(58));
                        } else {
                            var13 = false;
                            var14 = var12;
                        }

                        RenderTarget var17 = this.getRenderTarget(var14);
                        if (var17 == null) {
                            if (var13) {
                                throw new ChainedJsonException("Render target '" + var14 + "' can't be used as depth buffer");
                            }

                            ResourceLocation var18 = new ResourceLocation("textures/effect/" + var14 + ".png");
                            this.resourceManager
                                .getResource(var18)
                                .orElseThrow(() -> new ChainedJsonException("Render target or texture '" + var14 + "' does not exist"));
                            RenderSystem.setShaderTexture(0, var18);
                            param0.bindForSetup(var18);
                            AbstractTexture var19 = param0.getTexture(var18);
                            int var20 = GsonHelper.getAsInt(var10, "width");
                            int var21 = GsonHelper.getAsInt(var10, "height");
                            boolean var22 = GsonHelper.getAsBoolean(var10, "bilinear");
                            if (var22) {
                                RenderSystem.texParameter(3553, 10241, 9729);
                                RenderSystem.texParameter(3553, 10240, 9729);
                            } else {
                                RenderSystem.texParameter(3553, 10241, 9728);
                                RenderSystem.texParameter(3553, 10240, 9728);
                            }

                            var6.addAuxAsset(var11, var19::getId, var20, var21);
                        } else if (var13) {
                            var6.addAuxAsset(var11, var17::getDepthTextureId, var17.width, var17.height);
                        } else {
                            var6.addAuxAsset(var11, var17::getColorTextureId, var17.width, var17.height);
                        }
                    } catch (Exception var26) {
                        ChainedJsonException var24 = ChainedJsonException.forException(var26);
                        var24.prependJsonKey("auxtargets[" + var8 + "]");
                        throw var24;
                    }

                    ++var8;
                }
            }

            JsonArray var25 = GsonHelper.getAsJsonArray(var0, "uniforms", null);
            if (var25 != null) {
                int var26 = 0;

                for(JsonElement var27 : var25) {
                    try {
                        this.parseUniformNode(var27);
                    } catch (Exception var251) {
                        ChainedJsonException var29 = ChainedJsonException.forException(var251);
                        var29.prependJsonKey("uniforms[" + var26 + "]");
                        throw var29;
                    }

                    ++var26;
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
        RenderTarget var0 = new TextureTarget(param1, param2, true, Minecraft.ON_OSX);
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
        this.shaderOrthoMatrix = new Matrix4f().setOrtho(0.0F, (float)this.screenTarget.width, 0.0F, (float)this.screenTarget.height, 0.1F, 1000.0F);
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

    @Nullable
    private RenderTarget getRenderTarget(@Nullable String param0) {
        if (param0 == null) {
            return null;
        } else {
            return param0.equals("minecraft:main") ? this.screenTarget : this.customRenderTargets.get(param0);
        }
    }
}
