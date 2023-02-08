package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PathfindingRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Map<Integer, Path> pathMap = Maps.newHashMap();
    private final Map<Integer, Float> pathMaxDist = Maps.newHashMap();
    private final Map<Integer, Long> creationMap = Maps.newHashMap();
    private static final long TIMEOUT = 5000L;
    private static final float MAX_RENDER_DIST = 80.0F;
    private static final boolean SHOW_OPEN_CLOSED = true;
    private static final boolean SHOW_OPEN_CLOSED_COST_MALUS = false;
    private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_TEXT = false;
    private static final boolean SHOW_OPEN_CLOSED_NODE_TYPE_WITH_BOX = true;
    private static final boolean SHOW_GROUND_LABELS = true;
    private static final float TEXT_SCALE = 0.02F;

    public void addPath(int param0, Path param1, float param2) {
        this.pathMap.put(param0, param1);
        this.creationMap.put(param0, Util.getMillis());
        this.pathMaxDist.put(param0, param2);
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        if (!this.pathMap.isEmpty()) {
            long var0 = Util.getMillis();

            for(Integer var1 : this.pathMap.keySet()) {
                Path var2 = this.pathMap.get(var1);
                float var3 = this.pathMaxDist.get(var1);
                renderPath(param0, param1, var2, var3, true, true, param2, param3, param4);
            }

            for(Integer var4 : this.creationMap.keySet().toArray(new Integer[0])) {
                if (var0 - this.creationMap.get(var4) > 5000L) {
                    this.pathMap.remove(var4);
                    this.creationMap.remove(var4);
                }
            }

        }
    }

    public static void renderPath(
        PoseStack param0, MultiBufferSource param1, Path param2, float param3, boolean param4, boolean param5, double param6, double param7, double param8
    ) {
        renderPathLine(param0, param1.getBuffer(RenderType.debugLineStrip(6.0)), param2, param6, param7, param8);
        BlockPos var0 = param2.getTarget();
        if (distanceToCamera(var0, param6, param7, param8) <= 80.0F) {
            DebugRenderer.renderFilledBox(
                param0,
                param1,
                new AABB(
                        (double)((float)var0.getX() + 0.25F),
                        (double)((float)var0.getY() + 0.25F),
                        (double)var0.getZ() + 0.25,
                        (double)((float)var0.getX() + 0.75F),
                        (double)((float)var0.getY() + 0.75F),
                        (double)((float)var0.getZ() + 0.75F)
                    )
                    .move(-param6, -param7, -param8),
                0.0F,
                1.0F,
                0.0F,
                0.5F
            );

            for(int var1 = 0; var1 < param2.getNodeCount(); ++var1) {
                Node var2 = param2.getNode(var1);
                if (distanceToCamera(var2.asBlockPos(), param6, param7, param8) <= 80.0F) {
                    float var3 = var1 == param2.getNextNodeIndex() ? 1.0F : 0.0F;
                    float var4 = var1 == param2.getNextNodeIndex() ? 0.0F : 1.0F;
                    DebugRenderer.renderFilledBox(
                        param0,
                        param1,
                        new AABB(
                                (double)((float)var2.x + 0.5F - param3),
                                (double)((float)var2.y + 0.01F * (float)var1),
                                (double)((float)var2.z + 0.5F - param3),
                                (double)((float)var2.x + 0.5F + param3),
                                (double)((float)var2.y + 0.25F + 0.01F * (float)var1),
                                (double)((float)var2.z + 0.5F + param3)
                            )
                            .move(-param6, -param7, -param8),
                        var3,
                        0.0F,
                        var4,
                        0.5F
                    );
                }
            }
        }

        if (param4) {
            for(Node var5 : param2.getClosedSet()) {
                if (distanceToCamera(var5.asBlockPos(), param6, param7, param8) <= 80.0F) {
                    DebugRenderer.renderFilledBox(
                        param0,
                        param1,
                        new AABB(
                                (double)((float)var5.x + 0.5F - param3 / 2.0F),
                                (double)((float)var5.y + 0.01F),
                                (double)((float)var5.z + 0.5F - param3 / 2.0F),
                                (double)((float)var5.x + 0.5F + param3 / 2.0F),
                                (double)var5.y + 0.1,
                                (double)((float)var5.z + 0.5F + param3 / 2.0F)
                            )
                            .move(-param6, -param7, -param8),
                        1.0F,
                        0.8F,
                        0.8F,
                        0.5F
                    );
                }
            }

            for(Node var6 : param2.getOpenSet()) {
                if (distanceToCamera(var6.asBlockPos(), param6, param7, param8) <= 80.0F) {
                    DebugRenderer.renderFilledBox(
                        param0,
                        param1,
                        new AABB(
                                (double)((float)var6.x + 0.5F - param3 / 2.0F),
                                (double)((float)var6.y + 0.01F),
                                (double)((float)var6.z + 0.5F - param3 / 2.0F),
                                (double)((float)var6.x + 0.5F + param3 / 2.0F),
                                (double)var6.y + 0.1,
                                (double)((float)var6.z + 0.5F + param3 / 2.0F)
                            )
                            .move(-param6, -param7, -param8),
                        0.8F,
                        1.0F,
                        1.0F,
                        0.5F
                    );
                }
            }
        }

        if (param5) {
            for(int var7 = 0; var7 < param2.getNodeCount(); ++var7) {
                Node var8 = param2.getNode(var7);
                if (distanceToCamera(var8.asBlockPos(), param6, param7, param8) <= 80.0F) {
                    DebugRenderer.renderFloatingText(
                        param0,
                        param1,
                        String.valueOf(var8.type),
                        (double)var8.x + 0.5,
                        (double)var8.y + 0.75,
                        (double)var8.z + 0.5,
                        -1,
                        0.02F,
                        true,
                        0.0F,
                        true
                    );
                    DebugRenderer.renderFloatingText(
                        param0,
                        param1,
                        String.format(Locale.ROOT, "%.2f", var8.costMalus),
                        (double)var8.x + 0.5,
                        (double)var8.y + 0.25,
                        (double)var8.z + 0.5,
                        -1,
                        0.02F,
                        true,
                        0.0F,
                        true
                    );
                }
            }
        }

    }

    public static void renderPathLine(PoseStack param0, VertexConsumer param1, Path param2, double param3, double param4, double param5) {
        for(int var0 = 0; var0 < param2.getNodeCount(); ++var0) {
            Node var1 = param2.getNode(var0);
            if (!(distanceToCamera(var1.asBlockPos(), param3, param4, param5) > 80.0F)) {
                float var2 = (float)var0 / (float)param2.getNodeCount() * 0.33F;
                int var3 = var0 == 0 ? 0 : Mth.hsvToRgb(var2, 0.9F, 0.9F);
                int var4 = var3 >> 16 & 0xFF;
                int var5 = var3 >> 8 & 0xFF;
                int var6 = var3 & 0xFF;
                param1.vertex(
                        param0.last().pose(),
                        (float)((double)var1.x - param3 + 0.5),
                        (float)((double)var1.y - param4 + 0.5),
                        (float)((double)var1.z - param5 + 0.5)
                    )
                    .color(var4, var5, var6, 255)
                    .endVertex();
            }
        }

    }

    private static float distanceToCamera(BlockPos param0, double param1, double param2, double param3) {
        return (float)(Math.abs((double)param0.getX() - param1) + Math.abs((double)param0.getY() - param2) + Math.abs((double)param0.getZ() - param3));
    }
}
