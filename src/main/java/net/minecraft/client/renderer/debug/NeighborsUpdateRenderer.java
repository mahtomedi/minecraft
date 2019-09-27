package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class NeighborsUpdateRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

    NeighborsUpdateRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addUpdate(long param0, BlockPos param1) {
        Map<BlockPos, Integer> var0 = this.lastUpdate.get(param0);
        if (var0 == null) {
            var0 = Maps.newHashMap();
            this.lastUpdate.put(param0, var0);
        }

        Integer var1 = var0.get(param1);
        if (var1 == null) {
            var1 = 0;
        }

        var0.put(param1, var1 + 1);
    }

    @Override
    public void render(long param0) {
        long var0 = this.minecraft.level.getGameTime();
        Camera var1 = this.minecraft.gameRenderer.getMainCamera();
        double var2 = var1.getPosition().x;
        double var3 = var1.getPosition().y;
        double var4 = var1.getPosition().z;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.lineWidth(2.0F);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        int var5 = 200;
        double var6 = 0.0025;
        Set<BlockPos> var7 = Sets.newHashSet();
        Map<BlockPos, Integer> var8 = Maps.newHashMap();
        MultiBufferSource.BufferSource var9 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer var10 = var9.getBuffer(RenderType.LINES);
        Iterator<Entry<Long, Map<BlockPos, Integer>>> var11 = this.lastUpdate.entrySet().iterator();

        while(var11.hasNext()) {
            Entry<Long, Map<BlockPos, Integer>> var12 = var11.next();
            Long var13 = var12.getKey();
            Map<BlockPos, Integer> var14 = var12.getValue();
            long var15 = var0 - var13;
            if (var15 > 200L) {
                var11.remove();
            } else {
                for(Entry<BlockPos, Integer> var16 : var14.entrySet()) {
                    BlockPos var17 = var16.getKey();
                    Integer var18 = var16.getValue();
                    if (var7.add(var17)) {
                        AABB var19 = new AABB(BlockPos.ZERO)
                            .inflate(0.002)
                            .deflate(0.0025 * (double)var15)
                            .move((double)var17.getX(), (double)var17.getY(), (double)var17.getZ())
                            .move(-var2, -var3, -var4);
                        LevelRenderer.renderLineBox(var10, var19.minX, var19.minY, var19.minZ, var19.maxX, var19.maxY, var19.maxZ, 1.0F, 1.0F, 1.0F, 1.0F);
                        var8.put(var17, var18);
                    }
                }
            }
        }

        var9.endBatch();

        for(Entry<BlockPos, Integer> var20 : var8.entrySet()) {
            BlockPos var21 = var20.getKey();
            Integer var22 = var20.getValue();
            DebugRenderer.renderFloatingText(String.valueOf(var22), var21.getX(), var21.getY(), var21.getZ(), -1);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
