package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.time.Duration;
import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.lighting.LayerLightSectionStorage;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@OnlyIn(Dist.CLIENT)
public class LightSectionDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
    private static final Duration REFRESH_INTERVAL = Duration.ofMillis(500L);
    private static final int RADIUS = 10;
    private static final Vector4f LIGHT_AND_BLOCKS_COLOR = new Vector4f(1.0F, 1.0F, 0.0F, 0.25F);
    private static final Vector4f LIGHT_ONLY_COLOR = new Vector4f(0.25F, 0.125F, 0.0F, 0.125F);
    private final Minecraft minecraft;
    private final LightLayer lightLayer;
    private Instant lastUpdateTime = Instant.now();
    @Nullable
    private LightSectionDebugRenderer.SectionData data;

    public LightSectionDebugRenderer(Minecraft param0, LightLayer param1) {
        this.minecraft = param0;
        this.lightLayer = param1;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        Instant var0 = Instant.now();
        if (this.data == null || Duration.between(this.lastUpdateTime, var0).compareTo(REFRESH_INTERVAL) > 0) {
            this.lastUpdateTime = var0;
            this.data = new LightSectionDebugRenderer.SectionData(
                this.minecraft.level.getLightEngine(), SectionPos.of(this.minecraft.player.blockPosition()), 10, this.lightLayer
            );
        }

        renderEdges(param0, this.data.lightAndBlocksShape, this.data.minPos, param1, param2, param3, param4, LIGHT_AND_BLOCKS_COLOR);
        renderEdges(param0, this.data.lightShape, this.data.minPos, param1, param2, param3, param4, LIGHT_ONLY_COLOR);
        VertexConsumer var1 = param1.getBuffer(RenderType.debugSectionQuads());
        renderFaces(param0, this.data.lightAndBlocksShape, this.data.minPos, var1, param2, param3, param4, LIGHT_AND_BLOCKS_COLOR);
        renderFaces(param0, this.data.lightShape, this.data.minPos, var1, param2, param3, param4, LIGHT_ONLY_COLOR);
    }

    private static void renderFaces(
        PoseStack param0, DiscreteVoxelShape param1, SectionPos param2, VertexConsumer param3, double param4, double param5, double param6, Vector4f param7
    ) {
        param1.forAllFaces((param7x, param8, param9, param10) -> {
            int var0x = param8 + param2.getX();
            int var1x = param9 + param2.getY();
            int var2x = param10 + param2.getZ();
            renderFace(param0, param3, param7x, param4, param5, param6, var0x, var1x, var2x, param7);
        });
    }

    private static void renderEdges(
        PoseStack param0, DiscreteVoxelShape param1, SectionPos param2, MultiBufferSource param3, double param4, double param5, double param6, Vector4f param7
    ) {
        param1.forAllEdges((param7x, param8, param9, param10, param11, param12) -> {
            int var0x = param7x + param2.getX();
            int var1x = param8 + param2.getY();
            int var2x = param9 + param2.getZ();
            int var3x = param10 + param2.getX();
            int var4x = param11 + param2.getY();
            int var5 = param12 + param2.getZ();
            VertexConsumer var6x = param3.getBuffer(RenderType.debugLineStrip(1.0));
            renderEdge(param0, var6x, param4, param5, param6, var0x, var1x, var2x, var3x, var4x, var5, param7);
        }, true);
    }

    private static void renderFace(
        PoseStack param0,
        VertexConsumer param1,
        Direction param2,
        double param3,
        double param4,
        double param5,
        int param6,
        int param7,
        int param8,
        Vector4f param9
    ) {
        float var0 = (float)((double)SectionPos.sectionToBlockCoord(param6) - param3);
        float var1 = (float)((double)SectionPos.sectionToBlockCoord(param7) - param4);
        float var2 = (float)((double)SectionPos.sectionToBlockCoord(param8) - param5);
        float var3 = var0 + 16.0F;
        float var4 = var1 + 16.0F;
        float var5 = var2 + 16.0F;
        float var6 = param9.x();
        float var7 = param9.y();
        float var8 = param9.z();
        float var9 = param9.w();
        Matrix4f var10 = param0.last().pose();
        switch(param2) {
            case DOWN:
                param1.vertex(var10, var0, var1, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var1, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var1, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var0, var1, var5).color(var6, var7, var8, var9).endVertex();
                break;
            case UP:
                param1.vertex(var10, var0, var4, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var0, var4, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var4, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var4, var2).color(var6, var7, var8, var9).endVertex();
                break;
            case NORTH:
                param1.vertex(var10, var0, var1, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var0, var4, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var4, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var1, var2).color(var6, var7, var8, var9).endVertex();
                break;
            case SOUTH:
                param1.vertex(var10, var0, var1, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var1, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var4, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var0, var4, var5).color(var6, var7, var8, var9).endVertex();
                break;
            case WEST:
                param1.vertex(var10, var0, var1, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var0, var1, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var0, var4, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var0, var4, var2).color(var6, var7, var8, var9).endVertex();
                break;
            case EAST:
                param1.vertex(var10, var3, var1, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var4, var2).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var4, var5).color(var6, var7, var8, var9).endVertex();
                param1.vertex(var10, var3, var1, var5).color(var6, var7, var8, var9).endVertex();
        }

    }

    private static void renderEdge(
        PoseStack param0,
        VertexConsumer param1,
        double param2,
        double param3,
        double param4,
        int param5,
        int param6,
        int param7,
        int param8,
        int param9,
        int param10,
        Vector4f param11
    ) {
        float var0 = (float)((double)SectionPos.sectionToBlockCoord(param5) - param2);
        float var1 = (float)((double)SectionPos.sectionToBlockCoord(param6) - param3);
        float var2 = (float)((double)SectionPos.sectionToBlockCoord(param7) - param4);
        float var3 = (float)((double)SectionPos.sectionToBlockCoord(param8) - param2);
        float var4 = (float)((double)SectionPos.sectionToBlockCoord(param9) - param3);
        float var5 = (float)((double)SectionPos.sectionToBlockCoord(param10) - param4);
        Matrix4f var6 = param0.last().pose();
        param1.vertex(var6, var0, var1, var2).color(param11.x(), param11.y(), param11.z(), 1.0F).endVertex();
        param1.vertex(var6, var3, var4, var5).color(param11.x(), param11.y(), param11.z(), 1.0F).endVertex();
    }

    @OnlyIn(Dist.CLIENT)
    static final class SectionData {
        final DiscreteVoxelShape lightAndBlocksShape;
        final DiscreteVoxelShape lightShape;
        final SectionPos minPos;

        SectionData(LevelLightEngine param0, SectionPos param1, int param2, LightLayer param3) {
            int var0 = param2 * 2 + 1;
            this.lightAndBlocksShape = new BitSetDiscreteVoxelShape(var0, var0, var0);
            this.lightShape = new BitSetDiscreteVoxelShape(var0, var0, var0);

            for(int var1 = 0; var1 < var0; ++var1) {
                for(int var2 = 0; var2 < var0; ++var2) {
                    for(int var3 = 0; var3 < var0; ++var3) {
                        SectionPos var4 = SectionPos.of(param1.x() + var3 - param2, param1.y() + var2 - param2, param1.z() + var1 - param2);
                        LayerLightSectionStorage.SectionType var5 = param0.getDebugSectionType(param3, var4);
                        if (var5 == LayerLightSectionStorage.SectionType.LIGHT_AND_DATA) {
                            this.lightAndBlocksShape.fill(var3, var2, var1);
                            this.lightShape.fill(var3, var2, var1);
                        } else if (var5 == LayerLightSectionStorage.SectionType.LIGHT_ONLY) {
                            this.lightShape.fill(var3, var2, var1);
                        }
                    }
                }
            }

            this.minPos = SectionPos.of(param1.x() - param2, param1.y() - param2, param1.z() - param2);
        }
    }
}
