package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Vector3f;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class HeightMapRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public HeightMapRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        LevelAccessor var0 = this.minecraft.level;
        RenderSystem.pushMatrix();
        RenderSystem.disableBlend();
        RenderSystem.disableTexture();
        RenderSystem.enableDepthTest();
        BlockPos var1 = new BlockPos(param2, 0.0, param4);
        Tesselator var2 = Tesselator.getInstance();
        BufferBuilder var3 = var2.getBuilder();
        var3.begin(5, DefaultVertexFormat.POSITION_COLOR);

        for(int var4 = -32; var4 <= 32; var4 += 16) {
            for(int var5 = -32; var5 <= 32; var5 += 16) {
                ChunkAccess var6 = var0.getChunk(var1.offset(var4, 0, var5));

                for(Entry<Heightmap.Types, Heightmap> var7 : var6.getHeightmaps()) {
                    Heightmap.Types var8 = var7.getKey();
                    ChunkPos var9 = var6.getPos();
                    Vector3f var10 = this.getColor(var8);

                    for(int var11 = 0; var11 < 16; ++var11) {
                        for(int var12 = 0; var12 < 16; ++var12) {
                            int var13 = var9.x * 16 + var11;
                            int var14 = var9.z * 16 + var12;
                            float var15 = (float)((double)((float)var0.getHeight(var8, var13, var14) + (float)var8.ordinal() * 0.09375F) - param3);
                            LevelRenderer.addChainedFilledBoxVertices(
                                var3,
                                (double)((float)var13 + 0.25F) - param2,
                                (double)var15,
                                (double)((float)var14 + 0.25F) - param4,
                                (double)((float)var13 + 0.75F) - param2,
                                (double)(var15 + 0.09375F),
                                (double)((float)var14 + 0.75F) - param4,
                                var10.x(),
                                var10.y(),
                                var10.z(),
                                1.0F
                            );
                        }
                    }
                }
            }
        }

        var2.end();
        RenderSystem.enableTexture();
        RenderSystem.popMatrix();
    }

    private Vector3f getColor(Heightmap.Types param0) {
        switch(param0) {
            case WORLD_SURFACE_WG:
                return new Vector3f(1.0F, 1.0F, 0.0F);
            case OCEAN_FLOOR_WG:
                return new Vector3f(1.0F, 0.0F, 1.0F);
            case WORLD_SURFACE:
                return new Vector3f(0.0F, 0.7F, 0.0F);
            case OCEAN_FLOOR:
                return new Vector3f(0.0F, 0.0F, 0.5F);
            case MOTION_BLOCKING:
                return new Vector3f(0.0F, 0.3F, 0.3F);
            case MOTION_BLOCKING_NO_LEAVES:
                return new Vector3f(0.0F, 0.5F, 0.5F);
            default:
                return new Vector3f(0.0F, 0.0F, 0.0F);
        }
    }
}
