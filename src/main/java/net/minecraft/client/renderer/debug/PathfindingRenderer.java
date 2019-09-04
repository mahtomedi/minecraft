package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Locale;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PathfindingRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private final Map<Integer, Path> pathMap = Maps.newHashMap();
    private final Map<Integer, Float> pathMaxDist = Maps.newHashMap();
    private final Map<Integer, Long> creationMap = Maps.newHashMap();

    public PathfindingRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    public void addPath(int param0, Path param1, float param2) {
        this.pathMap.put(param0, param1);
        this.creationMap.put(param0, Util.getMillis());
        this.pathMaxDist.put(param0, param2);
    }

    @Override
    public void render(long param0) {
        if (!this.pathMap.isEmpty()) {
            long var0 = Util.getMillis();

            for(Integer var1 : this.pathMap.keySet()) {
                Path var2 = this.pathMap.get(var1);
                float var3 = this.pathMaxDist.get(var1);
                renderPath(this.getCamera(), var2, var3, true, true);
            }

            for(Integer var4 : this.creationMap.keySet().toArray(new Integer[0])) {
                if (var0 - this.creationMap.get(var4) > 20000L) {
                    this.pathMap.remove(var4);
                    this.creationMap.remove(var4);
                }
            }

        }
    }

    public static void renderPath(Camera param0, Path param1, float param2, boolean param3, boolean param4) {
        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );
        RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(6.0F);
        doRenderPath(param0, param1, param2, param3, param4);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.popMatrix();
    }

    private static void doRenderPath(Camera param0, Path param1, float param2, boolean param3, boolean param4) {
        renderPathLine(param0, param1);
        double var0 = param0.getPosition().x;
        double var1 = param0.getPosition().y;
        double var2 = param0.getPosition().z;
        BlockPos var3 = param1.getTarget();
        if (distanceToCamera(param0, var3) <= 40.0F) {
            DebugRenderer.renderFilledBox(
                new AABB(
                        (double)((float)var3.getX() + 0.25F),
                        (double)((float)var3.getY() + 0.25F),
                        (double)var3.getZ() + 0.25,
                        (double)((float)var3.getX() + 0.75F),
                        (double)((float)var3.getY() + 0.75F),
                        (double)((float)var3.getZ() + 0.75F)
                    )
                    .move(-var0, -var1, -var2),
                0.0F,
                1.0F,
                0.0F,
                0.5F
            );

            for(int var4 = 0; var4 < param1.getSize(); ++var4) {
                Node var5 = param1.get(var4);
                if (distanceToCamera(param0, var5.asBlockPos()) <= 40.0F) {
                    float var6 = var4 == param1.getIndex() ? 1.0F : 0.0F;
                    float var7 = var4 == param1.getIndex() ? 0.0F : 1.0F;
                    DebugRenderer.renderFilledBox(
                        new AABB(
                                (double)((float)var5.x + 0.5F - param2),
                                (double)((float)var5.y + 0.01F * (float)var4),
                                (double)((float)var5.z + 0.5F - param2),
                                (double)((float)var5.x + 0.5F + param2),
                                (double)((float)var5.y + 0.25F + 0.01F * (float)var4),
                                (double)((float)var5.z + 0.5F + param2)
                            )
                            .move(-var0, -var1, -var2),
                        var6,
                        0.0F,
                        var7,
                        0.5F
                    );
                }
            }
        }

        if (param3) {
            for(Node var8 : param1.getClosedSet()) {
                if (distanceToCamera(param0, var8.asBlockPos()) <= 40.0F) {
                    DebugRenderer.renderFloatingText(String.format("%s", var8.type), (double)var8.x + 0.5, (double)var8.y + 0.75, (double)var8.z + 0.5, -65536);
                    DebugRenderer.renderFloatingText(
                        String.format(Locale.ROOT, "%.2f", var8.costMalus), (double)var8.x + 0.5, (double)var8.y + 0.25, (double)var8.z + 0.5, -65536
                    );
                }
            }

            for(Node var9 : param1.getOpenSet()) {
                if (distanceToCamera(param0, var9.asBlockPos()) <= 40.0F) {
                    DebugRenderer.renderFloatingText(
                        String.format("%s", var9.type), (double)var9.x + 0.5, (double)var9.y + 0.75, (double)var9.z + 0.5, -16776961
                    );
                    DebugRenderer.renderFloatingText(
                        String.format(Locale.ROOT, "%.2f", var9.costMalus), (double)var9.x + 0.5, (double)var9.y + 0.25, (double)var9.z + 0.5, -16776961
                    );
                }
            }
        }

        if (param4) {
            for(int var10 = 0; var10 < param1.getSize(); ++var10) {
                Node var11 = param1.get(var10);
                if (distanceToCamera(param0, var11.asBlockPos()) <= 40.0F) {
                    DebugRenderer.renderFloatingText(String.format("%s", var11.type), (double)var11.x + 0.5, (double)var11.y + 0.75, (double)var11.z + 0.5, -1);
                    DebugRenderer.renderFloatingText(
                        String.format(Locale.ROOT, "%.2f", var11.costMalus), (double)var11.x + 0.5, (double)var11.y + 0.25, (double)var11.z + 0.5, -1
                    );
                }
            }
        }

    }

    public static void renderPathLine(Camera param0, Path param1) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        double var2 = param0.getPosition().x;
        double var3 = param0.getPosition().y;
        double var4 = param0.getPosition().z;
        var1.begin(3, DefaultVertexFormat.POSITION_COLOR);

        for(int var5 = 0; var5 < param1.getSize(); ++var5) {
            Node var6 = param1.get(var5);
            if (!(distanceToCamera(param0, var6.asBlockPos()) > 40.0F)) {
                float var7 = (float)var5 / (float)param1.getSize() * 0.33F;
                int var8 = var5 == 0 ? 0 : Mth.hsvToRgb(var7, 0.9F, 0.9F);
                int var9 = var8 >> 16 & 0xFF;
                int var10 = var8 >> 8 & 0xFF;
                int var11 = var8 & 0xFF;
                var1.vertex((double)var6.x - var2 + 0.5, (double)var6.y - var3 + 0.5, (double)var6.z - var4 + 0.5).color(var9, var10, var11, 255).endVertex();
            }
        }

        var0.end();
    }

    private static float distanceToCamera(Camera param0, BlockPos param1) {
        return (float)(
            Math.abs((double)param1.getX() - param0.getPosition().x)
                + Math.abs((double)param1.getY() - param0.getPosition().y)
                + Math.abs((double)param1.getZ() - param0.getPosition().z)
        );
    }

    private Camera getCamera() {
        return this.minecraft.gameRenderer.getMainCamera();
    }
}
