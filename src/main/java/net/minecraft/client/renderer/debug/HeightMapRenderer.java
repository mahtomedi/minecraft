package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class HeightMapRenderer implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CHUNK_DIST = 2;
    private static final float BOX_HEIGHT = 0.09375F;

    public HeightMapRenderer(Minecraft param0) {
        this.minecraft = param0;
    }

    @Override
    public void render(PoseStack param0, MultiBufferSource param1, double param2, double param3, double param4) {
        LevelAccessor var0 = this.minecraft.level;
        VertexConsumer var1 = param1.getBuffer(RenderType.debugFilledBox());
        BlockPos var2 = new BlockPos(param2, 0.0, param4);

        for(int var3 = -2; var3 <= 2; ++var3) {
            for(int var4 = -2; var4 <= 2; ++var4) {
                ChunkAccess var5 = var0.getChunk(var2.offset(var3 * 16, 0, var4 * 16));

                for(Entry<Heightmap.Types, Heightmap> var6 : var5.getHeightmaps()) {
                    Heightmap.Types var7 = var6.getKey();
                    ChunkPos var8 = var5.getPos();
                    Vector3f var9 = this.getColor(var7);

                    for(int var10 = 0; var10 < 16; ++var10) {
                        for(int var11 = 0; var11 < 16; ++var11) {
                            int var12 = SectionPos.sectionToBlockCoord(var8.x, var10);
                            int var13 = SectionPos.sectionToBlockCoord(var8.z, var11);
                            float var14 = (float)((double)((float)var0.getHeight(var7, var12, var13) + (float)var7.ordinal() * 0.09375F) - param3);
                            LevelRenderer.addChainedFilledBoxVertices(
                                param0,
                                var1,
                                (double)((float)var12 + 0.25F) - param2,
                                (double)var14,
                                (double)((float)var13 + 0.25F) - param4,
                                (double)((float)var12 + 0.75F) - param2,
                                (double)(var14 + 0.09375F),
                                (double)((float)var13 + 0.75F) - param4,
                                var9.x(),
                                var9.y(),
                                var9.z(),
                                1.0F
                            );
                        }
                    }
                }
            }
        }

    }

    private Vector3f getColor(Heightmap.Types param0) {
        return switch(param0) {
            case WORLD_SURFACE_WG -> new Vector3f(1.0F, 1.0F, 0.0F);
            case OCEAN_FLOOR_WG -> new Vector3f(1.0F, 0.0F, 1.0F);
            case WORLD_SURFACE -> new Vector3f(0.0F, 0.7F, 0.0F);
            case OCEAN_FLOOR -> new Vector3f(0.0F, 0.0F, 0.5F);
            case MOTION_BLOCKING -> new Vector3f(0.0F, 0.3F, 0.3F);
            case MOTION_BLOCKING_NO_LEAVES -> new Vector3f(0.0F, 0.5F, 0.5F);
        };
    }
}
