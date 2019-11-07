package net.minecraft.client.renderer.debug;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CaveDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Map<BlockPos, BlockPos> tunnelsList = Maps.newHashMap();
    private final Map<BlockPos, Float> thicknessMap = Maps.newHashMap();
    private final List<BlockPos> startPoses = Lists.newArrayList();

    public void addTunnel(BlockPos param0, List<BlockPos> param1, List<Float> param2) {
        for(int var0 = 0; var0 < param1.size(); ++var0) {
            this.tunnelsList.put(param1.get(var0), param0);
            this.thicknessMap.put(param1.get(var0), param2.get(var0));
        }

        this.startPoses.add(param0);
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4, long param5) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        BlockPos var0 = new BlockPos(param2, 0.0, param4);
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();
        var2.begin(5, DefaultVertexFormat.POSITION_COLOR);

        for(Entry<BlockPos, BlockPos> var3 : this.tunnelsList.entrySet()) {
            BlockPos var4 = var3.getKey();
            BlockPos var5 = var3.getValue();
            float var6 = (float)(var5.getX() * 128 % 256) / 256.0F;
            float var7 = (float)(var5.getY() * 128 % 256) / 256.0F;
            float var8 = (float)(var5.getZ() * 128 % 256) / 256.0F;
            float var9 = this.thicknessMap.get(var4);
            if (var0.closerThan(var4, 160.0)) {
                LevelRenderer.addChainedFilledBoxVertices(
                    var2,
                    (double)((float)var4.getX() + 0.5F) - param2 - (double)var9,
                    (double)((float)var4.getY() + 0.5F) - param3 - (double)var9,
                    (double)((float)var4.getZ() + 0.5F) - param4 - (double)var9,
                    (double)((float)var4.getX() + 0.5F) - param2 + (double)var9,
                    (double)((float)var4.getY() + 0.5F) - param3 + (double)var9,
                    (double)((float)var4.getZ() + 0.5F) - param4 + (double)var9,
                    var6,
                    var7,
                    var8,
                    0.5F
                );
            }
        }

        for(BlockPos var10 : this.startPoses) {
            if (var0.closerThan(var10, 160.0)) {
                LevelRenderer.addChainedFilledBoxVertices(
                    var2,
                    (double)var10.getX() - param2,
                    (double)var10.getY() - param3,
                    (double)var10.getZ() - param4,
                    (double)((float)var10.getX() + 1.0F) - param2,
                    (double)((float)var10.getY() + 1.0F) - param3,
                    (double)((float)var10.getZ() + 1.0F) - param4,
                    1.0F,
                    1.0F,
                    1.0F,
                    1.0F
                );
            }
        }

        var1.end();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }
}
