package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<BlockPos, BlockPos> tunnelsList = Maps.newHashMap();
    private final Map<BlockPos, Float> thicknessMap = Maps.newHashMap();
    private final List<BlockPos> startPoses = Lists.newArrayList();

    public CaveDebugRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addTunnel(BlockPos param0, List<BlockPos> param1, List<Float> param2) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            this.tunnelsList.put(param1.get(var0), param0);
            this.thicknessMap.put(param1.get(var0), param2.get(var0));
        }

        this.startPoses.add(param0);
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
        BlockPos var4 = new BlockPos(var0.getPosition().x, 0.0, var0.getPosition().z);
        Tesselator var5 = Tesselator.getInstance();
        BufferBuilder var6 = var5.getBuilder();
        var6.begin(5, DefaultVertexFormat.POSITION_COLOR);

        for(Entry<BlockPos, BlockPos> var7 : this.tunnelsList.entrySet()) {
            BlockPos var8 = var7.getKey();
            BlockPos var9 = var7.getValue();
            float var10 = (float)(var9.getX() * 128 % 256) / 256.0F;
            float var11 = (float)(var9.getY() * 128 % 256) / 256.0F;
            float var12 = (float)(var9.getZ() * 128 % 256) / 256.0F;
            float var13 = this.thicknessMap.get(var8);
            if (var4.closerThan(var8, 160.0)) {
                LevelRenderer.addChainedFilledBoxVertices(
                    var6,
                    (double)((float)var8.getX() + 0.5F) - var1 - (double)var13,
                    (double)((float)var8.getY() + 0.5F) - var2 - (double)var13,
                    (double)((float)var8.getZ() + 0.5F) - var3 - (double)var13,
                    (double)((float)var8.getX() + 0.5F) - var1 + (double)var13,
                    (double)((float)var8.getY() + 0.5F) - var2 + (double)var13,
                    (double)((float)var8.getZ() + 0.5F) - var3 + (double)var13,
                    var10,
                    var11,
                    var12,
                    0.5F
                );
            }
        }

        for(BlockPos var14 : this.startPoses) {
            if (var4.closerThan(var14, 160.0)) {
                LevelRenderer.addChainedFilledBoxVertices(
                    var6,
                    (double)var14.getX() - var1,
                    (double)var14.getY() - var2,
                    (double)var14.getZ() - var3,
                    (double)((float)var14.getX() + 1.0F) - var1,
                    (double)((float)var14.getY() + 1.0F) - var2,
                    (double)((float)var14.getZ() + 1.0F) - var3,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
                );
            }
        }

        var5.end();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}
