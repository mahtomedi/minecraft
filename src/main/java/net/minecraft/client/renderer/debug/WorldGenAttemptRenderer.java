package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WorldGenAttemptRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final List<BlockPos> toRender = Lists.newArrayList();
    private final List<Float> scales = Lists.newArrayList();
    private final List<Float> alphas = Lists.newArrayList();
    private final List<Float> reds = Lists.newArrayList();
    private final List<Float> greens = Lists.newArrayList();
    private final List<Float> blues = Lists.newArrayList();

    public WorldGenAttemptRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addPos(BlockPos param0, float param1, float param2, float param3, float param4, float param5) {
        this.toRender.add(param0);
        this.scales.add(param1);
        this.alphas.add(param5);
        this.reds.add(param2);
        this.greens.add(param3);
        this.blues.add(param4);
    }

    @Override
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        double var1 = var0.getPosition().x;
        double var2 = var0.getPosition().y;
        double var3 = var0.getPosition().z;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.disableTexture();
        Tesselator var4 = Tesselator.getInstance();
        BufferBuilder var5 = var4.getBuilder();
        var5.begin(5, DefaultVertexFormat.POSITION_COLOR);

        for(int var6 = 0; var6 < this.toRender.size(); ++var6) {
            BlockPos var7 = this.toRender.get(var6);
            Float var8 = this.scales.get(var6);
            float var9 = var8 / 2.0F;
            LevelRenderer.addChainedFilledBoxVertices(
                var5,
                (double)((float)var7.getX() + 0.5F - var9) - var1,
                (double)((float)var7.getY() + 0.5F - var9) - var2,
                (double)((float)var7.getZ() + 0.5F - var9) - var3,
                (double)((float)var7.getX() + 0.5F + var9) - var1,
                (double)((float)var7.getY() + 0.5F + var9) - var2,
                (double)((float)var7.getZ() + 0.5F + var9) - var3,
                this.reds.get(var6),
                this.greens.get(var6),
                this.blues.get(var6),
                this.alphas.get(var6)
            );
        }

        var4.end();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}
