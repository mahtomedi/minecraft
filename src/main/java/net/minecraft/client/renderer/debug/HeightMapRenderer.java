package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        LevelAccessor var0 = this.minecraft.level;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        BlockPos var1 = new BlockPos(param2, 0.0, param4);
        Tesselator var2 = Tesselator.getInstance();
        BufferBuilder var3 = var2.getBuilder();
        var3.begin(5, DefaultVertexFormat.POSITION_COLOR);

        for(BlockPos var4 : BlockPos.betweenClosed(var1.offset(-40, 0, -40), var1.offset(40, 0, 40))) {
            int var5 = var0.getHeight(Heightmap.Types.WORLD_SURFACE_WG, var4.getX(), var4.getZ());
            if (var0.getBlockState(var4.offset(0, var5, 0).below()).isAir()) {
                LevelRenderer.addChainedFilledBoxVertices(
                    var3,
                    (double)((float)var4.getX() + 0.25F) - param2,
                    (double)var5 - param3,
                    (double)((float)var4.getZ() + 0.25F) - param4,
                    (double)((float)var4.getX() + 0.75F) - param2,
                    (double)var5 + 0.09375 - param3,
                    (double)((float)var4.getZ() + 0.75F) - param4,
                    0.0F,
                    0.0F,
                    1.0F,
                    0.5F
                );
            } else {
                LevelRenderer.addChainedFilledBoxVertices(
                    var3,
                    (double)((float)var4.getX() + 0.25F) - param2,
                    (double)var5 - param3,
                    (double)((float)var4.getZ() + 0.25F) - param4,
                    (double)((float)var4.getX() + 0.75F) - param2,
                    (double)var5 + 0.09375 - param3,
                    (double)((float)var4.getZ() + 0.75F) - param4,
                    0.0F,
                    1.0F,
                    0.0F,
                    0.5F
                );
            }
        }

        var2.end();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}
