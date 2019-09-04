package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Matrix4f;
import java.io.IOException;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PostPass implements AutoCloseable {
    private final EffectInstance effect;
    public final RenderTarget inTarget;
    public final RenderTarget outTarget;
    private final List<Object> auxAssets = Lists.newArrayList();
    private final List<String> auxNames = Lists.newArrayList();
    private final List<Integer> auxWidths = Lists.newArrayList();
    private final List<Integer> auxHeights = Lists.newArrayList();
    private Matrix4f shaderOrthoMatrix;

    public PostPass(ResourceManager param0, String param1, RenderTarget param2, RenderTarget param3) throws IOException {
        this.effect = new EffectInstance(param0, param1);
        this.inTarget = param2;
        this.outTarget = param3;
    }

    @Override
    public void close() {
        this.effect.close();
    }

    public void addAuxAsset(String param0, Object param1, int param2, int param3) {
        this.auxNames.add(this.auxNames.size(), param0);
        this.auxAssets.add(this.auxAssets.size(), param1);
        this.auxWidths.add(this.auxWidths.size(), param2);
        this.auxHeights.add(this.auxHeights.size(), param3);
    }

    private void prepareState() {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableFog();
        RenderSystem.disableLighting();
        RenderSystem.disableColorMaterial();
        RenderSystem.enableTexture();
        RenderSystem.bindTexture(0);
    }

    public void setOrthoMatrix(Matrix4f param0) {
        this.shaderOrthoMatrix = param0;
    }

    public void process(float param0) {
        this.prepareState();
        this.inTarget.unbindWrite();
        float var0 = (float)this.outTarget.width;
        float var1 = (float)this.outTarget.height;
        RenderSystem.viewport(0, 0, (int)var0, (int)var1);
        this.effect.setSampler("DiffuseSampler", this.inTarget);

        for(int var2 = 0; var2 < this.auxAssets.size(); ++var2) {
            this.effect.setSampler(this.auxNames.get(var2), this.auxAssets.get(var2));
            this.effect.safeGetUniform("AuxSize" + var2).set((float)this.auxWidths.get(var2).intValue(), (float)this.auxHeights.get(var2).intValue());
        }

        this.effect.safeGetUniform("ProjMat").set(this.shaderOrthoMatrix);
        this.effect.safeGetUniform("InSize").set((float)this.inTarget.width, (float)this.inTarget.height);
        this.effect.safeGetUniform("OutSize").set(var0, var1);
        this.effect.safeGetUniform("Time").set(param0);
        Minecraft var3 = Minecraft.getInstance();
        this.effect.safeGetUniform("ScreenSize").set((float)var3.window.getWidth(), (float)var3.window.getHeight());
        this.effect.apply();
        this.outTarget.clear(Minecraft.ON_OSX);
        this.outTarget.bindWrite(false);
        RenderSystem.depthMask(false);
        RenderSystem.colorMask(true, true, true, true);
        Tesselator var4 = Tesselator.getInstance();
        BufferBuilder var5 = var4.getBuilder();
        var5.begin(7, DefaultVertexFormat.POSITION_COLOR);
        var5.vertex(0.0, 0.0, 500.0).color(255, 255, 255, 255).endVertex();
        var5.vertex((double)var0, 0.0, 500.0).color(255, 255, 255, 255).endVertex();
        var5.vertex((double)var0, (double)var1, 500.0).color(255, 255, 255, 255).endVertex();
        var5.vertex(0.0, (double)var1, 500.0).color(255, 255, 255, 255).endVertex();
        var4.end();
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(true, true, true, true);
        this.effect.clear();
        this.outTarget.unbindWrite();
        this.inTarget.unbindRead();

        for(Object var6 : this.auxAssets) {
            if (var6 instanceof RenderTarget) {
                ((RenderTarget)var6).unbindRead();
            }
        }

    }

    public EffectInstance getEffect() {
        return this.effect;
    }
}
