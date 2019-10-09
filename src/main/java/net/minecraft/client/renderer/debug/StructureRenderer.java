package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class StructureRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<DimensionType, Map<String, BoundingBox>> postMainBoxes = Maps.newIdentityHashMap();
    private final Map<DimensionType, Map<String, BoundingBox>> postPiecesBoxes = Maps.newIdentityHashMap();
    private final Map<DimensionType, Map<String, Boolean>> startPiecesMap = Maps.newIdentityHashMap();

    public StructureRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(long param0) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        LevelAccessor var1 = this.minecraft.level;
        DimensionType var2 = var1.getDimension().getType();
        double var3 = var0.getPosition().x;
        double var4 = var0.getPosition().y;
        double var5 = var0.getPosition().z;
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableTexture();
        RenderSystem.disableDepthTest();
        BlockPos var6 = new BlockPos(var0.getPosition().x, 0.0, var0.getPosition().z);
        MultiBufferSource.BufferSource var7 = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer var8 = var7.getBuffer(RenderType.lines());
        if (this.postMainBoxes.containsKey(var2)) {
            for(BoundingBox var9 : this.postMainBoxes.get(var2).values()) {
                if (var6.closerThan(var9.getCenter(), 500.0)) {
                    LevelRenderer.renderLineBox(
                        var8,
                        (double)var9.x0 - var3,
                        (double)var9.y0 - var4,
                        (double)var9.z0 - var5,
                        (double)(var9.x1 + 1) - var3,
                        (double)(var9.y1 + 1) - var4,
                        (double)(var9.z1 + 1) - var5,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F
                    );
                }
            }
        }

        if (this.postPiecesBoxes.containsKey(var2)) {
            for(Entry<String, BoundingBox> var10 : this.postPiecesBoxes.get(var2).entrySet()) {
                String var11 = var10.getKey();
                BoundingBox var12 = var10.getValue();
                Boolean var13 = this.startPiecesMap.get(var2).get(var11);
                if (var6.closerThan(var12.getCenter(), 500.0)) {
                    if (var13) {
                        LevelRenderer.renderLineBox(
                            var8,
                            (double)var12.x0 - var3,
                            (double)var12.y0 - var4,
                            (double)var12.z0 - var5,
                            (double)(var12.x1 + 1) - var3,
                            (double)(var12.y1 + 1) - var4,
                            (double)(var12.z1 + 1) - var5,
                            0.0F,
                            1.0F,
                            0.0F,
                            1.0F
                        );
                    } else {
                        LevelRenderer.renderLineBox(
                            var8,
                            (double)var12.x0 - var3,
                            (double)var12.y0 - var4,
                            (double)var12.z0 - var5,
                            (double)(var12.x1 + 1) - var3,
                            (double)(var12.y1 + 1) - var4,
                            (double)(var12.z1 + 1) - var5,
                            0.0F,
                            0.0F,
                            1.0F,
                            1.0F
                        );
                    }
                }
            }
        }

        var7.endBatch();
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }

    public void addBoundingBox(BoundingBox param0, List<BoundingBox> param1, List<Boolean> param2, DimensionType param3) {
        if (!this.postMainBoxes.containsKey(param3)) {
            this.postMainBoxes.put(param3, Maps.newHashMap());
        }

        if (!this.postPiecesBoxes.containsKey(param3)) {
            this.postPiecesBoxes.put(param3, Maps.newHashMap());
            this.startPiecesMap.put(param3, Maps.newHashMap());
        }

        this.postMainBoxes.get(param3).put(param0.toString(), param0);

        for(int var0 = 0; var0 < param1.size(); ++var0) {
            BoundingBox var1 = param1.get(var0);
            Boolean var2 = param2.get(var0);
            this.postPiecesBoxes.get(param3).put(var1.toString(), var1);
            this.startPiecesMap.get(param3).put(var1.toString(), var2);
        }

    }

    @Override
    public void clear() {
        this.postMainBoxes.clear();
        this.postPiecesBoxes.clear();
        this.startPiecesMap.clear();
    }
}
