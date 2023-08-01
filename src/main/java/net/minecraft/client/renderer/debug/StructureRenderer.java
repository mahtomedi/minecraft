package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.StructuresDebugPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<ResourceKey<Level>, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
    private final Map<ResourceKey<Level>, Map<String, StructuresDebugPayload.PieceInfo>> postPieces = Maps.newIdentityHashMap();
    private static final int MAX_RENDER_DIST = 500;

    public StructureRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        ResourceKey<Level> var1 = this.minecraft.level.dimension();
        BlockPos var2 = BlockPos.containing(var0.getPosition().x, 0.0, var0.getPosition().z);
        VertexConsumer var3 = param1.getBuffer(RenderType.lines());
        if (this.postMainBoxes.containsKey(var1)) {
            for(BoundingBox var4 : this.postMainBoxes.get(var1).values()) {
                if (var2.closerThan(var4.getCenter(), 500.0)) {
                    LevelRenderer.renderLineBox(
                        param0,
                        var3,
                        (double)var4.minX() - param2,
                        (double)var4.minY() - param3,
                        (double)var4.minZ() - param4,
                        (double)(var4.maxX() + 1) - param2,
                        (double)(var4.maxY() + 1) - param3,
                        (double)(var4.maxZ() + 1) - param4,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F
                    );
                }
            }
        }

        Map<String, StructuresDebugPayload.PieceInfo> var5 = this.postPieces.get(var1);
        if (var5 != null) {
            for(StructuresDebugPayload.PieceInfo var6 : var5.values()) {
                BoundingBox var7 = var6.boundingBox();
                if (var2.closerThan(var7.getCenter(), 500.0)) {
                    if (var6.isStart()) {
                        LevelRenderer.renderLineBox(
                            param0,
                            var3,
                            (double)var7.minX() - param2,
                            (double)var7.minY() - param3,
                            (double)var7.minZ() - param4,
                            (double)(var7.maxX() + 1) - param2,
                            (double)(var7.maxY() + 1) - param3,
                            (double)(var7.maxZ() + 1) - param4,
                            0.0F,
                            1.0F,
                            0.0F,
                            1.0F,
                            0.0F,
                            1.0F,
                            0.0F
                        );
                    } else {
                        LevelRenderer.renderLineBox(
                            param0,
                            var3,
                            (double)var7.minX() - param2,
                            (double)var7.minY() - param3,
                            (double)var7.minZ() - param4,
                            (double)(var7.maxX() + 1) - param2,
                            (double)(var7.maxY() + 1) - param3,
                            (double)(var7.maxZ() + 1) - param4,
                            0.0F,
                            0.0F,
                            1.0F,
                            1.0F,
                            0.0F,
                            0.0F,
                            1.0F
                        );
                    }
                }
            }
        }

    }

    public void addBoundingBox(BoundingBox param0, List<StructuresDebugPayload.PieceInfo> param1, ResourceKey<Level> param2) {
        this.postMainBoxes.computeIfAbsent(param2, param0x -> new HashMap()).put(param0.toString(), param0);
        Map<String, StructuresDebugPayload.PieceInfo> var0 = this.postPieces.computeIfAbsent(param2, param0x -> new HashMap());

        for(StructuresDebugPayload.PieceInfo var1 : param1) {
            var0.put(var1.boundingBox().toString(), var1);
        }

    }

    @Override
    public void clear() {
        this.postMainBoxes.clear();
        this.postPieces.clear();
    }
}
