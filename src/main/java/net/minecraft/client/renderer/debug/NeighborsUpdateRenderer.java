package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        long var0 = this.minecraft.level.getGameTime();
        int var1 = 200;
        double var2 = 0.0025;
        Set<BlockPos> var3 = Sets.newHashSet();
        Map<BlockPos, Integer> var4 = Maps.newHashMap();
        VertexConsumer var5 = param1.getBuffer(RenderType.lines());
        Iterator<Entry<Long, Map<BlockPos, Integer>>> var6 = this.lastUpdate.entrySet().iterator();

        while(var6.hasNext()) {
            Entry<Long, Map<BlockPos, Integer>> var7 = var6.next();
            Long var8 = var7.getKey();
            Map<BlockPos, Integer> var9 = var7.getValue();
            long var10 = var0 - var8;
            if (var10 > 200L) {
                var6.remove();
            } else {
                for(Entry<BlockPos, Integer> var11 : var9.entrySet()) {
                    BlockPos var12 = var11.getKey();
                    Integer var13 = var11.getValue();
                    if (var3.add(var12)) {
                        AABB var14 = new AABB(BlockPos.ZERO)
                            .inflate(0.002)
                            .deflate(0.0025 * (double)var10)
                            .move((double)var12.getX(), (double)var12.getY(), (double)var12.getZ())
                            .move(-param2, -param3, -param4);
                        LevelRenderer.renderLineBox(
                            param0, var5, var14.minX, var14.minY, var14.minZ, var14.maxX, var14.maxY, var14.maxZ, 1.0F, 1.0F, 1.0F, 1.0F
                        );
                        var4.put(var12, var13);
                    }
                }
            }
        }

        for(Entry<BlockPos, Integer> var15 : var4.entrySet()) {
            BlockPos var16 = var15.getKey();
            Integer var17 = var15.getValue();
            DebugRenderer.renderFloatingText(String.valueOf(var17), var16.getX(), var16.getY(), var16.getZ(), -1);
        }

    }
}
