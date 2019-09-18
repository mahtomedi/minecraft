package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBlockRenderer {
    private final BlockColors blockColors;
    private static final ThreadLocal<ModelBlockRenderer.Cache> CACHE = ThreadLocal.withInitial(() -> new ModelBlockRenderer.Cache());

    public ModelBlockRenderer(BlockColors param0) {
        this.blockColors = param0;
    }

    public boolean tesselateBlock(
        BlockAndBiomeGetter param0, BakedModel param1, BlockState param2, BlockPos param3, BufferBuilder param4, boolean param5, Random param6, long param7
    ) {
        boolean var0 = Minecraft.useAmbientOcclusion() && param2.getLightEmission() == 0 && param1.useAmbientOcclusion();

        try {
            return var0
                ? this.tesselateWithAO(param0, param1, param2, param3, param4, param5, param6, param7)
                : this.tesselateWithoutAO(param0, param1, param2, param3, param4, param5, param6, param7);
        } catch (Throwable var14) {
            CrashReport var2 = CrashReport.forThrowable(var14, "Tesselating block model");
            CrashReportCategory var3 = var2.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(var3, param3, param2);
            var3.setDetail("Using AO", var0);
            throw new ReportedException(var2);
        }
    }

    public boolean tesselateWithAO(
        BlockAndBiomeGetter param0, BakedModel param1, BlockState param2, BlockPos param3, BufferBuilder param4, boolean param5, Random param6, long param7
    ) {
        boolean var0 = false;
        float[] var1 = new float[Direction.values().length * 2];
        BitSet var2 = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace var3 = new ModelBlockRenderer.AmbientOcclusionFace();

        for(Direction var4 : Direction.values()) {
            param6.setSeed(param7);
            List<BakedQuad> var5 = param1.getQuads(param2, var4, param6);
            if (!var5.isEmpty() && (!param5 || Block.shouldRenderFace(param2, param0, param3, var4))) {
                this.renderModelFaceAO(param0, param2, param3, param4, var5, var1, var2, var3);
                var0 = true;
            }
        }

        param6.setSeed(param7);
        List<BakedQuad> var6 = param1.getQuads(param2, null, param6);
        if (!var6.isEmpty()) {
            this.renderModelFaceAO(param0, param2, param3, param4, var6, var1, var2, var3);
            var0 = true;
        }

        return var0;
    }

    public boolean tesselateWithoutAO(
        BlockAndBiomeGetter param0, BakedModel param1, BlockState param2, BlockPos param3, BufferBuilder param4, boolean param5, Random param6, long param7
    ) {
        boolean var0 = false;
        BitSet var1 = new BitSet(3);

        for(Direction var2 : Direction.values()) {
            param6.setSeed(param7);
            List<BakedQuad> var3 = param1.getQuads(param2, var2, param6);
            if (!var3.isEmpty() && (!param5 || Block.shouldRenderFace(param2, param0, param3, var2))) {
                int var4 = param0.getLightColor(param2, param3.relative(var2));
                this.renderModelFaceFlat(param0, param2, param3, var4, false, param4, var3, var1);
                var0 = true;
            }
        }

        param6.setSeed(param7);
        List<BakedQuad> var5 = param1.getQuads(param2, null, param6);
        if (!var5.isEmpty()) {
            this.renderModelFaceFlat(param0, param2, param3, -1, true, param4, var5, var1);
            var0 = true;
        }

        return var0;
    }

    private void renderModelFaceAO(
        BlockAndBiomeGetter param0,
        BlockState param1,
        BlockPos param2,
        BufferBuilder param3,
        List<BakedQuad> param4,
        float[] param5,
        BitSet param6,
        ModelBlockRenderer.AmbientOcclusionFace param7
    ) {
        Vec3 var0 = param1.getOffset(param0, param2);
        double var1 = (double)param2.getX() + var0.x;
        double var2 = (double)param2.getY() + var0.y;
        double var3 = (double)param2.getZ() + var0.z;
        int var4 = 0;

        for(int var5 = param4.size(); var4 < var5; ++var4) {
            BakedQuad var6 = param4.get(var4);
            this.calculateShape(param0, param1, param2, var6.getVertices(), var6.getDirection(), param5, param6);
            param7.calculate(param0, param1, param2, var6.getDirection(), param5, param6);
            param3.putBulkData(var6.getVertices());
            param3.faceTex2(param7.lightmap[0], param7.lightmap[1], param7.lightmap[2], param7.lightmap[3]);
            if (var6.isTinted()) {
                int var7 = this.blockColors.getColor(param1, param0, param2, var6.getTintIndex());
                float var8 = (float)(var7 >> 16 & 0xFF) / 255.0F;
                float var9 = (float)(var7 >> 8 & 0xFF) / 255.0F;
                float var10 = (float)(var7 & 0xFF) / 255.0F;
                param3.faceTint(param7.brightness[0] * var8, param7.brightness[0] * var9, param7.brightness[0] * var10, 4);
                param3.faceTint(param7.brightness[1] * var8, param7.brightness[1] * var9, param7.brightness[1] * var10, 3);
                param3.faceTint(param7.brightness[2] * var8, param7.brightness[2] * var9, param7.brightness[2] * var10, 2);
                param3.faceTint(param7.brightness[3] * var8, param7.brightness[3] * var9, param7.brightness[3] * var10, 1);
            } else {
                param3.faceTint(param7.brightness[0], param7.brightness[0], param7.brightness[0], 4);
                param3.faceTint(param7.brightness[1], param7.brightness[1], param7.brightness[1], 3);
                param3.faceTint(param7.brightness[2], param7.brightness[2], param7.brightness[2], 2);
                param3.faceTint(param7.brightness[3], param7.brightness[3], param7.brightness[3], 1);
            }

            param3.postProcessFacePosition(var1, var2, var3);
        }

    }

    private void calculateShape(
        BlockAndBiomeGetter param0, BlockState param1, BlockPos param2, int[] param3, Direction param4, @Nullable float[] param5, BitSet param6
    ) {
        float var0 = 32.0F;
        float var1 = 32.0F;
        float var2 = 32.0F;
        float var3 = -32.0F;
        float var4 = -32.0F;
        float var5 = -32.0F;

        for(int var6 = 0; var6 < 4; ++var6) {
            float var7 = Float.intBitsToFloat(param3[var6 * 8]);
            float var8 = Float.intBitsToFloat(param3[var6 * 8 + 1]);
            float var9 = Float.intBitsToFloat(param3[var6 * 8 + 2]);
            var0 = Math.min(var0, var7);
            var1 = Math.min(var1, var8);
            var2 = Math.min(var2, var9);
            var3 = Math.max(var3, var7);
            var4 = Math.max(var4, var8);
            var5 = Math.max(var5, var9);
        }

        if (param5 != null) {
            param5[Direction.WEST.get3DDataValue()] = var0;
            param5[Direction.EAST.get3DDataValue()] = var3;
            param5[Direction.DOWN.get3DDataValue()] = var1;
            param5[Direction.UP.get3DDataValue()] = var4;
            param5[Direction.NORTH.get3DDataValue()] = var2;
            param5[Direction.SOUTH.get3DDataValue()] = var5;
            int var10 = Direction.values().length;
            param5[Direction.WEST.get3DDataValue() + var10] = 1.0F - var0;
            param5[Direction.EAST.get3DDataValue() + var10] = 1.0F - var3;
            param5[Direction.DOWN.get3DDataValue() + var10] = 1.0F - var1;
            param5[Direction.UP.get3DDataValue() + var10] = 1.0F - var4;
            param5[Direction.NORTH.get3DDataValue() + var10] = 1.0F - var2;
            param5[Direction.SOUTH.get3DDataValue() + var10] = 1.0F - var5;
        }

        float var11 = 1.0E-4F;
        float var12 = 0.9999F;
        switch(param4) {
            case DOWN:
                param6.set(1, var0 >= 1.0E-4F || var2 >= 1.0E-4F || var3 <= 0.9999F || var5 <= 0.9999F);
                param6.set(0, var1 == var4 && (var1 < 1.0E-4F || param1.isCollisionShapeFullBlock(param0, param2)));
                break;
            case UP:
                param6.set(1, var0 >= 1.0E-4F || var2 >= 1.0E-4F || var3 <= 0.9999F || var5 <= 0.9999F);
                param6.set(0, var1 == var4 && (var4 > 0.9999F || param1.isCollisionShapeFullBlock(param0, param2)));
                break;
            case NORTH:
                param6.set(1, var0 >= 1.0E-4F || var1 >= 1.0E-4F || var3 <= 0.9999F || var4 <= 0.9999F);
                param6.set(0, var2 == var5 && (var2 < 1.0E-4F || param1.isCollisionShapeFullBlock(param0, param2)));
                break;
            case SOUTH:
                param6.set(1, var0 >= 1.0E-4F || var1 >= 1.0E-4F || var3 <= 0.9999F || var4 <= 0.9999F);
                param6.set(0, var2 == var5 && (var5 > 0.9999F || param1.isCollisionShapeFullBlock(param0, param2)));
                break;
            case WEST:
                param6.set(1, var1 >= 1.0E-4F || var2 >= 1.0E-4F || var4 <= 0.9999F || var5 <= 0.9999F);
                param6.set(0, var0 == var3 && (var0 < 1.0E-4F || param1.isCollisionShapeFullBlock(param0, param2)));
                break;
            case EAST:
                param6.set(1, var1 >= 1.0E-4F || var2 >= 1.0E-4F || var4 <= 0.9999F || var5 <= 0.9999F);
                param6.set(0, var0 == var3 && (var3 > 0.9999F || param1.isCollisionShapeFullBlock(param0, param2)));
        }

    }

    private void renderModelFaceFlat(
        BlockAndBiomeGetter param0, BlockState param1, BlockPos param2, int param3, boolean param4, BufferBuilder param5, List<BakedQuad> param6, BitSet param7
    ) {
        Vec3 var0 = param1.getOffset(param0, param2);
        double var1 = (double)param2.getX() + var0.x;
        double var2 = (double)param2.getY() + var0.y;
        double var3 = (double)param2.getZ() + var0.z;
        int var4 = 0;

        for(int var5 = param6.size(); var4 < var5; ++var4) {
            BakedQuad var6 = param6.get(var4);
            if (param4) {
                this.calculateShape(param0, param1, param2, var6.getVertices(), var6.getDirection(), null, param7);
                BlockPos var7 = param7.get(0) ? param2.relative(var6.getDirection()) : param2;
                param3 = param0.getLightColor(param1, var7);
            }

            param5.putBulkData(var6.getVertices());
            param5.faceTex2(param3, param3, param3, param3);
            if (var6.isTinted()) {
                int var8 = this.blockColors.getColor(param1, param0, param2, var6.getTintIndex());
                float var9 = (float)(var8 >> 16 & 0xFF) / 255.0F;
                float var10 = (float)(var8 >> 8 & 0xFF) / 255.0F;
                float var11 = (float)(var8 & 0xFF) / 255.0F;
                param5.faceTint(var9, var10, var11, 4);
                param5.faceTint(var9, var10, var11, 3);
                param5.faceTint(var9, var10, var11, 2);
                param5.faceTint(var9, var10, var11, 1);
            }

            param5.postProcessFacePosition(var1, var2, var3);
        }

    }

    public void renderModel(BakedModel param0, float param1, float param2, float param3, float param4) {
        this.renderModel(null, param0, param1, param2, param3, param4);
    }

    public void renderModel(@Nullable BlockState param0, BakedModel param1, float param2, float param3, float param4, float param5) {
        Random var0 = new Random();
        long var1 = 42L;

        for(Direction var2 : Direction.values()) {
            var0.setSeed(42L);
            this.renderQuadList(param2, param3, param4, param5, param1.getQuads(param0, var2, var0));
        }

        var0.setSeed(42L);
        this.renderQuadList(param2, param3, param4, param5, param1.getQuads(param0, null, var0));
    }

    public void renderSingleBlock(BakedModel param0, BlockState param1, float param2, boolean param3) {
        RenderSystem.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
        int var0 = this.blockColors.getColor(param1, null, null, 0);
        float var1 = (float)(var0 >> 16 & 0xFF) / 255.0F;
        float var2 = (float)(var0 >> 8 & 0xFF) / 255.0F;
        float var3 = (float)(var0 & 0xFF) / 255.0F;
        if (!param3) {
            RenderSystem.color4f(param2, param2, param2, 1.0F);
        }

        this.renderModel(param1, param0, param2, var1, var2, var3);
    }

    private void renderQuadList(float param0, float param1, float param2, float param3, List<BakedQuad> param4) {
        Tesselator var0 = Tesselator.getInstance();
        BufferBuilder var1 = var0.getBuilder();
        int var2 = 0;

        for(int var3 = param4.size(); var2 < var3; ++var2) {
            BakedQuad var4 = param4.get(var2);
            var1.begin(7, DefaultVertexFormat.BLOCK);
            var1.putBulkData(var4.getVertices());
            if (var4.isTinted()) {
                var1.fixupQuadColor(param1 * param0, param2 * param0, param3 * param0);
            } else {
                var1.fixupQuadColor(param0, param0, param0);
            }

            Vec3i var5 = var4.getDirection().getNormal();
            var1.postNormal((float)var5.getX(), (float)var5.getY(), (float)var5.getZ());
            var0.end();
        }

    }

    public static void enableCaching() {
        CACHE.get().enable();
    }

    public static void clearCache() {
        CACHE.get().disable();
    }

    @OnlyIn(Dist.CLIENT)
    public static enum AdjacencyInfo {
        DOWN(
            new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH},
            0.5F,
            true,
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.SOUTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.SOUTH
            }
        ),
        UP(
            new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH},
            1.0F,
            true,
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.SOUTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.SOUTH
            }
        ),
        NORTH(
            new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST},
            0.8F,
            true,
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_WEST
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_EAST
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_EAST
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_WEST
            }
        ),
        SOUTH(
            new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP},
            0.8F,
            true,
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.WEST
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_WEST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.WEST,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.WEST
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.EAST
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_EAST,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.EAST,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.EAST
            }
        ),
        WEST(
            new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH},
            0.6F,
            true,
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.SOUTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.SOUTH
            }
        ),
        EAST(
            new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH},
            0.6F,
            true,
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.SOUTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.DOWN,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.NORTH,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_NORTH,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.NORTH
            },
            new ModelBlockRenderer.SizeInfo[]{
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.SOUTH,
                ModelBlockRenderer.SizeInfo.FLIP_UP,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
                ModelBlockRenderer.SizeInfo.UP,
                ModelBlockRenderer.SizeInfo.SOUTH
            }
        );

        private final Direction[] corners;
        private final boolean doNonCubicWeight;
        private final ModelBlockRenderer.SizeInfo[] vert0Weights;
        private final ModelBlockRenderer.SizeInfo[] vert1Weights;
        private final ModelBlockRenderer.SizeInfo[] vert2Weights;
        private final ModelBlockRenderer.SizeInfo[] vert3Weights;
        private static final ModelBlockRenderer.AdjacencyInfo[] BY_FACING = Util.make(new ModelBlockRenderer.AdjacencyInfo[6], param0 -> {
            param0[Direction.DOWN.get3DDataValue()] = DOWN;
            param0[Direction.UP.get3DDataValue()] = UP;
            param0[Direction.NORTH.get3DDataValue()] = NORTH;
            param0[Direction.SOUTH.get3DDataValue()] = SOUTH;
            param0[Direction.WEST.get3DDataValue()] = WEST;
            param0[Direction.EAST.get3DDataValue()] = EAST;
        });

        private AdjacencyInfo(
            Direction[] param0,
            float param1,
            boolean param2,
            ModelBlockRenderer.SizeInfo[] param3,
            ModelBlockRenderer.SizeInfo[] param4,
            ModelBlockRenderer.SizeInfo[] param5,
            ModelBlockRenderer.SizeInfo[] param6
        ) {
            this.corners = param0;
            this.doNonCubicWeight = param2;
            this.vert0Weights = param3;
            this.vert1Weights = param4;
            this.vert2Weights = param5;
            this.vert3Weights = param6;
        }

        public static ModelBlockRenderer.AdjacencyInfo fromFacing(Direction param0) {
            return BY_FACING[param0.get3DDataValue()];
        }
    }

    @OnlyIn(Dist.CLIENT)
    class AmbientOcclusionFace {
        private final float[] brightness = new float[4];
        private final int[] lightmap = new int[4];

        public AmbientOcclusionFace() {
        }

        public void calculate(BlockAndBiomeGetter param0, BlockState param1, BlockPos param2, Direction param3, float[] param4, BitSet param5) {
            BlockPos var0 = param5.get(0) ? param2.relative(param3) : param2;
            ModelBlockRenderer.AdjacencyInfo var1 = ModelBlockRenderer.AdjacencyInfo.fromFacing(param3);
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
            ModelBlockRenderer.Cache var3 = ModelBlockRenderer.CACHE.get();
            var2.set(var0).move(var1.corners[0]);
            BlockState var4 = param0.getBlockState(var2);
            int var5 = var3.getLightColor(var4, param0, var2);
            float var6 = var3.getShadeBrightness(var4, param0, var2);
            var2.set(var0).move(var1.corners[1]);
            BlockState var7 = param0.getBlockState(var2);
            int var8 = var3.getLightColor(var7, param0, var2);
            float var9 = var3.getShadeBrightness(var7, param0, var2);
            var2.set(var0).move(var1.corners[2]);
            BlockState var10 = param0.getBlockState(var2);
            int var11 = var3.getLightColor(var10, param0, var2);
            float var12 = var3.getShadeBrightness(var10, param0, var2);
            var2.set(var0).move(var1.corners[3]);
            BlockState var13 = param0.getBlockState(var2);
            int var14 = var3.getLightColor(var13, param0, var2);
            float var15 = var3.getShadeBrightness(var13, param0, var2);
            var2.set(var0).move(var1.corners[0]).move(param3);
            boolean var16 = param0.getBlockState(var2).getLightBlock(param0, var2) == 0;
            var2.set(var0).move(var1.corners[1]).move(param3);
            boolean var17 = param0.getBlockState(var2).getLightBlock(param0, var2) == 0;
            var2.set(var0).move(var1.corners[2]).move(param3);
            boolean var18 = param0.getBlockState(var2).getLightBlock(param0, var2) == 0;
            var2.set(var0).move(var1.corners[3]).move(param3);
            boolean var19 = param0.getBlockState(var2).getLightBlock(param0, var2) == 0;
            float var23;
            int var24;
            if (!var18 && !var16) {
                var23 = var6;
                var24 = var5;
            } else {
                var2.set(var0).move(var1.corners[0]).move(var1.corners[2]);
                BlockState var20 = param0.getBlockState(var2);
                var23 = var3.getShadeBrightness(var20, param0, var2);
                var24 = var3.getLightColor(var20, param0, var2);
            }

            float var28;
            int var29;
            if (!var19 && !var16) {
                var28 = var6;
                var29 = var5;
            } else {
                var2.set(var0).move(var1.corners[0]).move(var1.corners[3]);
                BlockState var25 = param0.getBlockState(var2);
                var28 = var3.getShadeBrightness(var25, param0, var2);
                var29 = var3.getLightColor(var25, param0, var2);
            }

            float var33;
            int var34;
            if (!var18 && !var17) {
                var33 = var6;
                var34 = var5;
            } else {
                var2.set(var0).move(var1.corners[1]).move(var1.corners[2]);
                BlockState var30 = param0.getBlockState(var2);
                var33 = var3.getShadeBrightness(var30, param0, var2);
                var34 = var3.getLightColor(var30, param0, var2);
            }

            float var38;
            int var39;
            if (!var19 && !var17) {
                var38 = var6;
                var39 = var5;
            } else {
                var2.set(var0).move(var1.corners[1]).move(var1.corners[3]);
                BlockState var35 = param0.getBlockState(var2);
                var38 = var3.getShadeBrightness(var35, param0, var2);
                var39 = var3.getLightColor(var35, param0, var2);
            }

            int var40 = var3.getLightColor(param1, param0, param2);
            var2.set(param2).move(param3);
            BlockState var41 = param0.getBlockState(var2);
            if (param5.get(0) || !var41.isSolidRender(param0, var2)) {
                var40 = var3.getLightColor(var41, param0, var2);
            }

            float var42 = param5.get(0)
                ? var3.getShadeBrightness(param0.getBlockState(var0), param0, var0)
                : var3.getShadeBrightness(param0.getBlockState(param2), param0, param2);
            ModelBlockRenderer.AmbientVertexRemap var43 = ModelBlockRenderer.AmbientVertexRemap.fromFacing(param3);
            if (param5.get(1) && var1.doNonCubicWeight) {
                float var48 = (var15 + var6 + var28 + var42) * 0.25F;
                float var49 = (var12 + var6 + var23 + var42) * 0.25F;
                float var50 = (var12 + var9 + var33 + var42) * 0.25F;
                float var51 = (var15 + var9 + var38 + var42) * 0.25F;
                float var52 = param4[var1.vert0Weights[0].shape] * param4[var1.vert0Weights[1].shape];
                float var53 = param4[var1.vert0Weights[2].shape] * param4[var1.vert0Weights[3].shape];
                float var54 = param4[var1.vert0Weights[4].shape] * param4[var1.vert0Weights[5].shape];
                float var55 = param4[var1.vert0Weights[6].shape] * param4[var1.vert0Weights[7].shape];
                float var56 = param4[var1.vert1Weights[0].shape] * param4[var1.vert1Weights[1].shape];
                float var57 = param4[var1.vert1Weights[2].shape] * param4[var1.vert1Weights[3].shape];
                float var58 = param4[var1.vert1Weights[4].shape] * param4[var1.vert1Weights[5].shape];
                float var59 = param4[var1.vert1Weights[6].shape] * param4[var1.vert1Weights[7].shape];
                float var60 = param4[var1.vert2Weights[0].shape] * param4[var1.vert2Weights[1].shape];
                float var61 = param4[var1.vert2Weights[2].shape] * param4[var1.vert2Weights[3].shape];
                float var62 = param4[var1.vert2Weights[4].shape] * param4[var1.vert2Weights[5].shape];
                float var63 = param4[var1.vert2Weights[6].shape] * param4[var1.vert2Weights[7].shape];
                float var64 = param4[var1.vert3Weights[0].shape] * param4[var1.vert3Weights[1].shape];
                float var65 = param4[var1.vert3Weights[2].shape] * param4[var1.vert3Weights[3].shape];
                float var66 = param4[var1.vert3Weights[4].shape] * param4[var1.vert3Weights[5].shape];
                float var67 = param4[var1.vert3Weights[6].shape] * param4[var1.vert3Weights[7].shape];
                this.brightness[var43.vert0] = var48 * var52 + var49 * var53 + var50 * var54 + var51 * var55;
                this.brightness[var43.vert1] = var48 * var56 + var49 * var57 + var50 * var58 + var51 * var59;
                this.brightness[var43.vert2] = var48 * var60 + var49 * var61 + var50 * var62 + var51 * var63;
                this.brightness[var43.vert3] = var48 * var64 + var49 * var65 + var50 * var66 + var51 * var67;
                int var68 = this.blend(var14, var5, var29, var40);
                int var69 = this.blend(var11, var5, var24, var40);
                int var70 = this.blend(var11, var8, var34, var40);
                int var71 = this.blend(var14, var8, var39, var40);
                this.lightmap[var43.vert0] = this.blend(var68, var69, var70, var71, var52, var53, var54, var55);
                this.lightmap[var43.vert1] = this.blend(var68, var69, var70, var71, var56, var57, var58, var59);
                this.lightmap[var43.vert2] = this.blend(var68, var69, var70, var71, var60, var61, var62, var63);
                this.lightmap[var43.vert3] = this.blend(var68, var69, var70, var71, var64, var65, var66, var67);
            } else {
                float var44 = (var15 + var6 + var28 + var42) * 0.25F;
                float var45 = (var12 + var6 + var23 + var42) * 0.25F;
                float var46 = (var12 + var9 + var33 + var42) * 0.25F;
                float var47 = (var15 + var9 + var38 + var42) * 0.25F;
                this.lightmap[var43.vert0] = this.blend(var14, var5, var29, var40);
                this.lightmap[var43.vert1] = this.blend(var11, var5, var24, var40);
                this.lightmap[var43.vert2] = this.blend(var11, var8, var34, var40);
                this.lightmap[var43.vert3] = this.blend(var14, var8, var39, var40);
                this.brightness[var43.vert0] = var44;
                this.brightness[var43.vert1] = var45;
                this.brightness[var43.vert2] = var46;
                this.brightness[var43.vert3] = var47;
            }

        }

        private int blend(int param0, int param1, int param2, int param3) {
            if (param0 == 0) {
                param0 = param3;
            }

            if (param1 == 0) {
                param1 = param3;
            }

            if (param2 == 0) {
                param2 = param3;
            }

            return param0 + param1 + param2 + param3 >> 2 & 16711935;
        }

        private int blend(int param0, int param1, int param2, int param3, float param4, float param5, float param6, float param7) {
            int var0 = (int)(
                    (float)(param0 >> 16 & 0xFF) * param4
                        + (float)(param1 >> 16 & 0xFF) * param5
                        + (float)(param2 >> 16 & 0xFF) * param6
                        + (float)(param3 >> 16 & 0xFF) * param7
                )
                & 0xFF;
            int var1 = (int)(
                    (float)(param0 & 0xFF) * param4 + (float)(param1 & 0xFF) * param5 + (float)(param2 & 0xFF) * param6 + (float)(param3 & 0xFF) * param7
                )
                & 0xFF;
            return var0 << 16 | var1;
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        private final int vert0;
        private final int vert1;
        private final int vert2;
        private final int vert3;
        private static final ModelBlockRenderer.AmbientVertexRemap[] BY_FACING = Util.make(new ModelBlockRenderer.AmbientVertexRemap[6], param0 -> {
            param0[Direction.DOWN.get3DDataValue()] = DOWN;
            param0[Direction.UP.get3DDataValue()] = UP;
            param0[Direction.NORTH.get3DDataValue()] = NORTH;
            param0[Direction.SOUTH.get3DDataValue()] = SOUTH;
            param0[Direction.WEST.get3DDataValue()] = WEST;
            param0[Direction.EAST.get3DDataValue()] = EAST;
        });

        private AmbientVertexRemap(int param0, int param1, int param2, int param3) {
            this.vert0 = param0;
            this.vert1 = param1;
            this.vert2 = param2;
            this.vert3 = param3;
        }

        public static ModelBlockRenderer.AmbientVertexRemap fromFacing(Direction param0) {
            return BY_FACING[param0.get3DDataValue()];
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class Cache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap var0 = new Long2IntLinkedOpenHashMap(100, 0.25F) {
                @Override
                protected void rehash(int param0) {
                }
            };
            var0.defaultReturnValue(Integer.MAX_VALUE);
            return var0;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap var0 = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
                @Override
                protected void rehash(int param0) {
                }
            };
            var0.defaultReturnValue(Float.NaN);
            return var0;
        });

        private Cache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.colorCache.clear();
            this.brightnessCache.clear();
        }

        public int getLightColor(BlockState param0, BlockAndBiomeGetter param1, BlockPos param2) {
            long var0 = param2.asLong();
            if (this.enabled) {
                int var1 = this.colorCache.get(var0);
                if (var1 != Integer.MAX_VALUE) {
                    return var1;
                }
            }

            int var2 = param1.getLightColor(param0, param2);
            if (this.enabled) {
                if (this.colorCache.size() == 100) {
                    this.colorCache.removeFirstInt();
                }

                this.colorCache.put(var0, var2);
            }

            return var2;
        }

        public float getShadeBrightness(BlockState param0, BlockAndBiomeGetter param1, BlockPos param2) {
            long var0 = param2.asLong();
            if (this.enabled) {
                float var1 = this.brightnessCache.get(var0);
                if (!Float.isNaN(var1)) {
                    return var1;
                }
            }

            float var2 = param0.getShadeBrightness(param1, param2);
            if (this.enabled) {
                if (this.brightnessCache.size() == 100) {
                    this.brightnessCache.removeFirstFloat();
                }

                this.brightnessCache.put(var0, var2);
            }

            return var2;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static enum SizeInfo {
        DOWN(Direction.DOWN, false),
        UP(Direction.UP, false),
        NORTH(Direction.NORTH, false),
        SOUTH(Direction.SOUTH, false),
        WEST(Direction.WEST, false),
        EAST(Direction.EAST, false),
        FLIP_DOWN(Direction.DOWN, true),
        FLIP_UP(Direction.UP, true),
        FLIP_NORTH(Direction.NORTH, true),
        FLIP_SOUTH(Direction.SOUTH, true),
        FLIP_WEST(Direction.WEST, true),
        FLIP_EAST(Direction.EAST, true);

        private final int shape;

        private SizeInfo(Direction param0, boolean param1) {
            this.shape = param0.get3DDataValue() + (param1 ? Direction.values().length : 0);
        }
    }
}
