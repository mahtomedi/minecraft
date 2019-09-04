package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.lineWidth(2.0F);
        RenderSystem.disableTexture();
        RenderSystem.depthMask(false);
        int var5 = 200;
        double var6 = 0.0025;
        Set<BlockPos> var7 = Sets.newHashSet();
        Map<BlockPos, Integer> var8 = Maps.newHashMap();
        Iterator<Entry<Long, Map<BlockPos, Integer>>> var9 = this.lastUpdate.entrySet().iterator();

        while(var9.hasNext()) {
            Entry<Long, Map<BlockPos, Integer>> var10 = var9.next();
            Long var11 = var10.getKey();
            Map<BlockPos, Integer> var12 = var10.getValue();
            long var13 = var0 - var11;
            if (var13 > 200L) {
                var9.remove();
            } else {
                for(Entry<BlockPos, Integer> var14 : var12.entrySet()) {
                    BlockPos var15 = var14.getKey();
                    Integer var16 = var14.getValue();
                    if (var7.add(var15)) {
                        LevelRenderer.renderLineBox(
                            new AABB(BlockPos.ZERO)
                                .inflate(0.002)
                                .deflate(0.0025 * (double)var13)
                                .move((double)var15.getX(), (double)var15.getY(), (double)var15.getZ())
                                .move(-var2, -var3, -var4),
                            1.0F,
                            1.0F,
                            1.0F,
                            1.0F
                        );
                        var8.put(var15, var16);
                    }
                }
            }
        }

        for(Entry<BlockPos, Integer> var17 : var8.entrySet()) {
            BlockPos var18 = var17.getKey();
            Integer var19 = var17.getValue();
            DebugRenderer.renderFloatingText(String.valueOf(var19), var18.getX(), var18.getY(), var18.getZ(), -1);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
