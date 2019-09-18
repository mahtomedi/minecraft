package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.Random;
import net.minecraft.Util;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity> extends BlockEntityRenderer<T> {
    private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final FloatBuffer MODELVIEW = MemoryTracker.createFloatBuffer(16);
    private static final FloatBuffer PROJECTION = MemoryTracker.createFloatBuffer(16);
    private final FloatBuffer buffer = MemoryTracker.createFloatBuffer(16);

    public void render(T param0, double param1, double param2, double param3, float param4, int param5, RenderType param6) {
        RenderSystem.disableLighting();
        RANDOM.setSeed(31100L);
        RenderSystem.getMatrix(2982, MODELVIEW);
        RenderSystem.getMatrix(2983, PROJECTION);
        double var0 = param1 * param1 + param2 * param2 + param3 * param3;
        int var1 = this.getPasses(var0);
        float var2 = this.getOffset();
        boolean var3 = false;

        for(int var4 = 0; var4 < var1; ++var4) {
            RenderSystem.pushMatrix();
            float var5 = 2.0F / (float)(18 - var4);
            if (var4 == 0) {
                this.bindTexture(END_SKY_LOCATION);
                var5 = 0.15F;
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }

            if (var4 >= 1) {
                this.bindTexture(END_PORTAL_LOCATION);
                var3 = true;
                FogRenderer.resetFogColor(true);
            }

            if (var4 == 1) {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            }

            RenderSystem.texGenMode(GlStateManager.TexGen.S, 9216);
            RenderSystem.texGenMode(GlStateManager.TexGen.T, 9216);
            RenderSystem.texGenMode(GlStateManager.TexGen.R, 9216);
            RenderSystem.texGenParam(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
            RenderSystem.texGenParam(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
            RenderSystem.texGenParam(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
            RenderSystem.enableTexGen(GlStateManager.TexGen.S);
            RenderSystem.enableTexGen(GlStateManager.TexGen.T);
            RenderSystem.enableTexGen(GlStateManager.TexGen.R);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.5F, 0.5F, 0.0F);
            RenderSystem.scalef(0.5F, 0.5F, 1.0F);
            float var6 = (float)(var4 + 1);
            RenderSystem.translatef(17.0F / var6, (2.0F + var6 / 1.5F) * ((float)(Util.getMillis() % 800000L) / 800000.0F), 0.0F);
            RenderSystem.rotatef((var6 * var6 * 4321.0F + var6 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.scalef(4.5F - var6 / 4.0F, 4.5F - var6 / 4.0F, 1.0F);
            RenderSystem.multMatrix(PROJECTION);
            RenderSystem.multMatrix(MODELVIEW);
            Tesselator var7 = Tesselator.getInstance();
            BufferBuilder var8 = var7.getBuilder();
            var8.begin(7, DefaultVertexFormat.POSITION_COLOR);
            float var9 = (RANDOM.nextFloat() * 0.5F + 0.1F) * var5;
            float var10 = (RANDOM.nextFloat() * 0.5F + 0.4F) * var5;
            float var11 = (RANDOM.nextFloat() * 0.5F + 0.5F) * var5;
            if (param0.shouldRenderFace(Direction.SOUTH)) {
                var8.vertex(param1, param2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2 + 1.0, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1, param2 + 1.0, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
            }

            if (param0.shouldRenderFace(Direction.NORTH)) {
                var8.vertex(param1, param2 + 1.0, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2 + 1.0, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1, param2, param3).color(var9, var10, var11, 1.0F).endVertex();
            }

            if (param0.shouldRenderFace(Direction.EAST)) {
                var8.vertex(param1 + 1.0, param2 + 1.0, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2 + 1.0, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2, param3).color(var9, var10, var11, 1.0F).endVertex();
            }

            if (param0.shouldRenderFace(Direction.WEST)) {
                var8.vertex(param1, param2, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1, param2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1, param2 + 1.0, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1, param2 + 1.0, param3).color(var9, var10, var11, 1.0F).endVertex();
            }

            if (param0.shouldRenderFace(Direction.DOWN)) {
                var8.vertex(param1, param2, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1, param2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
            }

            if (param0.shouldRenderFace(Direction.UP)) {
                var8.vertex(param1, param2 + (double)var2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2 + (double)var2, param3 + 1.0).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1 + 1.0, param2 + (double)var2, param3).color(var9, var10, var11, 1.0F).endVertex();
                var8.vertex(param1, param2 + (double)var2, param3).color(var9, var10, var11, 1.0F).endVertex();
            }

            var7.end();
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
            this.bindTexture(END_SKY_LOCATION);
        }

        RenderSystem.disableBlend();
        RenderSystem.disableTexGen(GlStateManager.TexGen.S);
        RenderSystem.disableTexGen(GlStateManager.TexGen.T);
        RenderSystem.disableTexGen(GlStateManager.TexGen.R);
        RenderSystem.enableLighting();
        if (var3) {
            FogRenderer.resetFogColor(false);
        }

    }

    protected int getPasses(double param0) {
        int var0;
        if (param0 > 36864.0) {
            var0 = 1;
        } else if (param0 > 25600.0) {
            var0 = 3;
        } else if (param0 > 16384.0) {
            var0 = 5;
        } else if (param0 > 9216.0) {
            var0 = 7;
        } else if (param0 > 4096.0) {
            var0 = 9;
        } else if (param0 > 1024.0) {
            var0 = 11;
        } else if (param0 > 576.0) {
            var0 = 13;
        } else if (param0 > 256.0) {
            var0 = 14;
        } else {
            var0 = 15;
        }

        return var0;
    }

    protected float getOffset() {
        return 0.75F;
    }

    private FloatBuffer getBuffer(float param0, float param1, float param2, float param3) {
        ((Buffer)this.buffer).clear();
        this.buffer.put(param0).put(param1).put(param2).put(param3);
        ((Buffer)this.buffer).flip();
        return this.buffer;
    }
}
