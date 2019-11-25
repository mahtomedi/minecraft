package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
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
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        Camera var0 = this.minecraft.gameRenderer.getMainCamera();
        LevelAccessor var1 = this.minecraft.level;
        DimensionType var2 = var1.getDimension().getType();
        BlockPos var3 = new BlockPos(var0.getPosition().x, 0.0, var0.getPosition().z);
        VertexConsumer var4 = param1.getBuffer(RenderType.lines());
        if (this.postMainBoxes.containsKey(var2)) {
            for(BoundingBox var5 : this.postMainBoxes.get(var2).values()) {
                if (var3.closerThan(var5.getCenter(), 500.0)) {
                    LevelRenderer.renderLineBox(
                        var4,
                        (double)var5.x0 - param2,
                        (double)var5.y0 - param3,
                        (double)var5.z0 - param4,
                        (double)(var5.x1 + 1) - param2,
                        (double)(var5.y1 + 1) - param3,
                        (double)(var5.z1 + 1) - param4,
                        1.0F,
                        1.0F,
                        1.0F,
                        1.0F
                    );
                }
            }
        }

        if (this.postPiecesBoxes.containsKey(var2)) {
            for(Entry<String, BoundingBox> var6 : this.postPiecesBoxes.get(var2).entrySet()) {
                String var7 = var6.getKey();
                BoundingBox var8 = var6.getValue();
                Boolean var9 = this.startPiecesMap.get(var2).get(var7);
                if (var3.closerThan(var8.getCenter(), 500.0)) {
                    if (var9) {
                        LevelRenderer.renderLineBox(
                            var4,
                            (double)var8.x0 - param2,
                            (double)var8.y0 - param3,
                            (double)var8.z0 - param4,
                            (double)(var8.x1 + 1) - param2,
                            (double)(var8.y1 + 1) - param3,
                            (double)(var8.z1 + 1) - param4,
                            0.0F,
                            1.0F,
                            0.0F,
                            1.0F
                        );
                    } else {
                        LevelRenderer.renderLineBox(
                            var4,
                            (double)var8.x0 - param2,
                            (double)var8.y0 - param3,
                            (double)var8.z0 - param4,
                            (double)(var8.x1 + 1) - param2,
                            (double)(var8.y1 + 1) - param3,
                            (double)(var8.z1 + 1) - param4,
                            0.0F,
                            0.0F,
                            1.0F,
                            1.0F
                        );
                    }
                }
            }
        }

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
