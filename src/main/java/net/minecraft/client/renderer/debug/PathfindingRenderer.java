package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
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
                renderPath(var2, var3, true, true, param2, param3, param4);
            }

            for(Integer var4 : this.creationMap.keySet().toArray(new Integer[0])) {
                if (var0 - this.creationMap.get(var4) > 5000L) {
                    this.pathMap.remove(var4);
                    this.creationMap.remove(var4);
                }
            }

        }
    }

    public static void renderPath(Path param0, float param1, boolean param2, boolean param3, double param4, double param5, double param6) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(6.0F);
        doRenderPath(param0, param1, param2, param3, param4, param5, param6);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private static void doRenderPath(Path param0, float param1, boolean param2, boolean param3, double param4, double param5, double param6) {
        renderPathLine(param0, param4, param5, param6);
        BlockPos var0 = param0.getTarget();
        if (distanceToCamera(var0, param4, param5, param6) <= 80.0F) {
            DebugRenderer.renderFilledBox(
                new AABB(
                        (double)((float)var0.getX() + 0.25F),
                        (double)((float)var0.getY() + 0.25F),
                        (double)var0.getZ() + 0.25,
                        (double)((float)var0.getX() + 0.75F),
                        (double)((float)var0.getY() + 0.75F),
                        (double)((float)var0.getZ() + 0.75F)
                    )
                    .move(-param4, -param5, -param6),
                0.0F,
                1.0F,
                0.0F,
                0.5F
            );

            for(int var1 = 0; var1 < param0.getSize(); ++var1) {
                Node var2 = param0.get(var1);
                if (distanceToCamera(var2.asBlockPos(), param4, param5, param6) <= 80.0F) {
                    float var3 = var1 == param0.getIndex() ? 1.0F : 0.0F;
                    float var4 = var1 == param0.getIndex() ? 0.0F : 1.0F;
                    DebugRenderer.renderFilledBox(
                        new AABB(
                                (double)((float)var2.x + 0.5F - param1),
                                (double)((float)var2.y + 0.01F * (float)var1),
                                (double)((float)var2.z + 0.5F - param1),
                                (double)((float)var2.x + 0.5F + param1),
                                (double)((float)var2.y + 0.25F + 0.01F * (float)var1),
                                (double)((float)var2.z + 0.5F + param1)
                            )
                            .move(-param4, -param5, -param6),
                        var3,
                        0.0F,
                        var4,
                        0.5F
                    );
                }
            }
        }

        if (param2) {
            for(Node var5 : param0.getClosedSet()) {
                if (distanceToCamera(var5.asBlockPos(), param4, param5, param6) <= 80.0F) {
                    DebugRenderer.renderFilledBox(
                        new AABB(
                                (double)((float)var5.x + 0.5F - param1 / 2.0F),
                                (double)((float)var5.y + 0.01F),
                                (double)((float)var5.z + 0.5F - param1 / 2.0F),
                                (double)((float)var5.x + 0.5F + param1 / 2.0F),
                                (double)var5.y + 0.1,
                                (double)((float)var5.z + 0.5F + param1 / 2.0F)
                            )
                            .move(-param4, -param5, -param6),
                        1.0F,
                        0.8F,
                        0.8F,
                        0.5F
                    );
                }
            }

            for(Node var6 : param0.getOpenSet()) {
                if (distanceToCamera(var6.asBlockPos(), param4, param5, param6) <= 80.0F) {
                    DebugRenderer.renderFilledBox(
                        new AABB(
                                (double)((float)var6.x + 0.5F - param1 / 2.0F),
                                (double)((float)var6.y + 0.01F),
                                (double)((float)var6.z + 0.5F - param1 / 2.0F),
                                (double)((float)var6.x + 0.5F + param1 / 2.0F),
                                (double)var6.y + 0.1,
                                (double)((float)var6.z + 0.5F + param1 / 2.0F)
                            )
                            .move(-param4, -param5, -param6),
                        0.8F,
                        1.0F,
                        1.0F,
                        0.5F
                    );
                }
            }
        }

        if (param3) {
            for(int var7 = 0; var7 < param0.getSize(); ++var7) {
                Node var8 = param0.get(var7);
                if (distanceToCamera(var8.asBlockPos(), param4, param5, param6) <= 80.0F) {
                    DebugRenderer.renderFloatingText(
                        String.format("%s", var8.type), (double)var8.x + 0.5, (double)var8.y + 0.75, (double)var8.z + 0.5, -1, 0.02F, true, 0.0F, true
                    );
                    DebugRenderer.renderFloatingText(
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

    public static void renderPathLine(Path param0, double param1, double param2, double param3) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        var1.begin(3, DefaultVertexFormat.POSITION_COLOR);

        for(int var2 = 0; var2 < param0.getSize(); ++var2) {
            Node var3 = param0.get(var2);
            if (!(distanceToCamera(var3.asBlockPos(), param1, param2, param3) > 80.0F)) {
                float var4 = (float)var2 / (float)param0.getSize() * 0.33F;
                int var5 = var2 == 0 ? 0 : Mth.hsvToRgb(var4, 0.9F, 0.9F);
                int var6 = var5 >> 16 & 0xFF;
                int var7 = var5 >> 8 & 0xFF;
                int var8 = var5 & 0xFF;
                var1.vertex((double)var3.x - param1 + 0.5, (double)var3.y - param2 + 0.5, (double)var3.z - param3 + 0.5)
                    .color(var6, var7, var8, 255)
                    .endVertex();
            }
        }

        var0.end();
    }

    private static float distanceToCamera(BlockPos param0, double param1, double param2, double param3) {
        return (float)(Math.abs((double)param0.getX() - param1) + Math.abs((double)param0.getY() - param2) + Math.abs((double)param0.getZ() - param3));
    }
}
