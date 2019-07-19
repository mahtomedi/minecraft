package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeightMapRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public HeightMapRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        LevelAccessor var1 = this.minecraft.level;
        double var2 = var0.getPosition().x;
        double var3 = var0.getPosition().y;
        double var4 = var0.getPosition().z;
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        GlStateManager.disableTexture();
        BlockPos var5 = new BlockPos(var0.getPosition().x, 0.0, var0.getPosition().z);
        Tesselator var6 = Tesselator.getInstance();
        BufferBuilder var7 = var6.getBuilder();
        var7.begin(5, DefaultVertexFormat.POSITION_COLOR);

        for(BlockPos var8 : BlockPos.betweenClosed(var5.offset(-40, 0, -40), var5.offset(40, 0, 40))) {
            int var9 = var1.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var8.getX(), var8.getZ());
            if (var1.getBlockState(var8.offset(0, var9, 0).below()).isAir()) {
                LevelRenderer.addChainedFilledBoxVertices(
                    var7,
                    (double)((float)var8.getX() + 0.25F) - var2,
                    (double)var9 - var3,
                    (double)((float)var8.getZ() + 0.25F) - var4,
                    (double)((float)var8.getX() + 0.75F) - var2,
                    (double)var9 + 0.09375 - var3,
                    (double)((float)var8.getZ() + 0.75F) - var4,
                    0.0F,
                    0.0F,
                    1.0F,
                    0.5F
                );
            } else {
                LevelRenderer.addChainedFilledBoxVertices(
                    var7,
                    (double)((float)var8.getX() + 0.25F) - var2,
                    (double)var9 - var3,
                    (double)((float)var8.getZ() + 0.25F) - var4,
                    (double)((float)var8.getX() + 0.75F) - var2,
                    (double)var9 + 0.09375 - var3,
                    (double)((float)var8.getZ() + 0.75F) - var4,
                    0.0F,
                    1.0F,
                    0.0F,
                    0.5F
                );
            }
        }

        var6.end();
        GlStateManager.enableTexture();
        GlStateManager.popMatrix();
    }
}
