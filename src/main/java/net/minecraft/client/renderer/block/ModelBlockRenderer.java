package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelBlockRenderer {
    private static final int FACE_CUBIC = 0;
    private static final int FACE_PARTIAL = 1;
    static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors blockColors;
    private static final int CACHE_SIZE = 100;
    static final ThreadLocal<ModelBlockRenderer.Cache> CACHE = ThreadLocal.withInitial(ModelBlockRenderer.Cache::new);

    public ModelBlockRenderer(BlockColors param0) {
        this.blockColors = param0;
    }

    public boolean tesselateBlock(
        BlockAndTintGetter param0,
        BakedModel param1,
        BlockState param2,
        BlockPos param3,
        PoseStack param4,
        VertexConsumer param5,
        boolean param6,
        RandomSource param7,
        long param8,
        int param9
    ) {
        boolean var0 = Minecraft.useAmbientOcclusion() && param2.getLightEmission() == 0 && param1.useAmbientOcclusion();
        Vec3 var1 = param2.getOffset(param0, param3);
        param4.translate(var1.x, var1.y, var1.z);

        try {
            return var0
                ? this.tesselateWithAO(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9)
                : this.tesselateWithoutAO(param0, param1, param2, param3, param4, param5, param6, param7, param8, param9);
        } catch (Throwable var17) {
            CrashReport var3 = CrashReport.forThrowable(var17, "Tesselating block model");
            CrashReportCategory var4 = var3.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(var4, param0, param3, param2);
            var4.setDetail("Using AO", var0);
            throw new ReportedException(var3);
        }
    }

    public boolean tesselateWithAO(
        BlockAndTintGetter param0,
        BakedModel param1,
        BlockState param2,
        BlockPos param3,
        PoseStack param4,
        VertexConsumer param5,
        boolean param6,
        RandomSource param7,
        long param8,
        int param9
    ) {
        boolean var0 = false;
        float[] var1 = new float[DIRECTIONS.length * 2];
        BitSet var2 = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace var3 = new ModelBlockRenderer.AmbientOcclusionFace();
        BlockPos.MutableBlockPos var4 = param3.mutable();

        for(Direction var5 : DIRECTIONS) {
            param7.setSeed(param8);
            List<BakedQuad> var6 = param1.getQuads(param2, var5, param7);
            if (!var6.isEmpty()) {
                var4.setWithOffset(param3, var5);
                if (!param6 || Block.shouldRenderFace(param2, param0, param3, var5, var4)) {
                    this.renderModelFaceAO(param0, param2, param3, param4, param5, var6, var1, var2, var3, param9);
                    var0 = true;
                }
            }
        }

        param7.setSeed(param8);
        List<BakedQuad> var7 = param1.getQuads(param2, null, param7);
        if (!var7.isEmpty()) {
            this.renderModelFaceAO(param0, param2, param3, param4, param5, var7, var1, var2, var3, param9);
            var0 = true;
        }

        return var0;
    }

    public boolean tesselateWithoutAO(
        BlockAndTintGetter param0,
        BakedModel param1,
        BlockState param2,
        BlockPos param3,
        PoseStack param4,
        VertexConsumer param5,
        boolean param6,
        RandomSource param7,
        long param8,
        int param9
    ) {
        boolean var0 = false;
        BitSet var1 = new BitSet(3);
        BlockPos.MutableBlockPos var2 = param3.mutable();

        for(Direction var3 : DIRECTIONS) {
            param7.setSeed(param8);
            List<BakedQuad> var4 = param1.getQuads(param2, var3, param7);
            if (!var4.isEmpty()) {
                var2.setWithOffset(param3, var3);
                if (!param6 || Block.shouldRenderFace(param2, param0, param3, var3, var2)) {
                    int var5 = LevelRenderer.getLightColor(param0, param2, var2);
                    this.renderModelFaceFlat(param0, param2, param3, var5, param9, false, param4, param5, var4, var1);
                    var0 = true;
                }
            }
        }

        param7.setSeed(param8);
        List<BakedQuad> var6 = param1.getQuads(param2, null, param7);
        if (!var6.isEmpty()) {
            this.renderModelFaceFlat(param0, param2, param3, -1, param9, true, param4, param5, var6, var1);
            var0 = true;
        }

        return var0;
    }

    private void renderModelFaceAO(
        BlockAndTintGetter param0,
        BlockState param1,
        BlockPos param2,
        PoseStack param3,
        VertexConsumer param4,
        List<BakedQuad> param5,
        float[] param6,
        BitSet param7,
        ModelBlockRenderer.AmbientOcclusionFace param8,
        int param9
    ) {
        for(BakedQuad var0 : param5) {
            this.calculateShape(param0, param1, param2, var0.getVertices(), var0.getDirection(), param6, param7);
            param8.calculate(param0, param1, param2, var0.getDirection(), param6, param7, var0.isShade());
            this.putQuadData(
                param0,
                param1,
                param2,
                param4,
                param3.last(),
                var0,
                param8.brightness[0],
                param8.brightness[1],
                param8.brightness[2],
                param8.brightness[3],
                param8.lightmap[0],
                param8.lightmap[1],
                param8.lightmap[2],
                param8.lightmap[3],
                param9
            );
        }

    }

    private void putQuadData(
        BlockAndTintGetter param0,
        BlockState param1,
        BlockPos param2,
        VertexConsumer param3,
        PoseStack.Pose param4,
        BakedQuad param5,
        float param6,
        float param7,
        float param8,
        float param9,
        int param10,
        int param11,
        int param12,
        int param13,
        int param14
    ) {
        float var1;
        float var2;
        float var3;
        if (param5.isTinted()) {
            int var0 = this.blockColors.getColor(param1, param0, param2, param5.getTintIndex());
            var1 = (float)(var0 >> 16 & 0xFF) / 255.0F;
            var2 = (float)(var0 >> 8 & 0xFF) / 255.0F;
            var3 = (float)(var0 & 0xFF) / 255.0F;
        } else {
            var1 = 1.0F;
            var2 = 1.0F;
            var3 = 1.0F;
        }

        param3.putBulkData(
            param4, param5, new float[]{param6, param7, param8, param9}, var1, var2, var3, new int[]{param10, param11, param12, param13}, param14, true
        );
    }

    private void calculateShape(
        BlockAndTintGetter param0, BlockState param1, BlockPos param2, int[] param3, Direction param4, @Nullable float[] param5, BitSet param6
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
            int var10 = DIRECTIONS.length;
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
                param6.set(0, var1 == var4 && (var1 < 1.0E-4F || isInteriorOccluded(param0, param1, param2)));
                break;
            case UP:
                param6.set(1, var0 >= 1.0E-4F || var2 >= 1.0E-4F || var3 <= 0.9999F || var5 <= 0.9999F);
                param6.set(0, var1 == var4 && (var4 > 0.9999F || isInteriorOccluded(param0, param1, param2)));
                break;
            case NORTH:
                param6.set(1, var0 >= 1.0E-4F || var1 >= 1.0E-4F || var3 <= 0.9999F || var4 <= 0.9999F);
                param6.set(0, var2 == var5 && (var2 < 1.0E-4F || isInteriorOccluded(param0, param1, param2)));
                break;
            case SOUTH:
                param6.set(1, var0 >= 1.0E-4F || var1 >= 1.0E-4F || var3 <= 0.9999F || var4 <= 0.9999F);
                param6.set(0, var2 == var5 && (var5 > 0.9999F || isInteriorOccluded(param0, param1, param2)));
                break;
            case WEST:
                param6.set(1, var1 >= 1.0E-4F || var2 >= 1.0E-4F || var4 <= 0.9999F || var5 <= 0.9999F);
                param6.set(0, var0 == var3 && (var0 < 1.0E-4F || isInteriorOccluded(param0, param1, param2)));
                break;
            case EAST:
                param6.set(1, var1 >= 1.0E-4F || var2 >= 1.0E-4F || var4 <= 0.9999F || var5 <= 0.9999F);
                param6.set(0, var0 == var3 && (var3 > 0.9999F || isInteriorOccluded(param0, param1, param2)));
        }

    }

    private static boolean isInteriorOccluded(BlockAndTintGetter param0, BlockState param1, BlockPos param2) {
        return param1.canOcclude() && param1.isCollisionShapeFullBlock(param0, param2);
    }

    private void renderModelFaceFlat(
        BlockAndTintGetter param0,
        BlockState param1,
        BlockPos param2,
        int param3,
        int param4,
        boolean param5,
        PoseStack param6,
        VertexConsumer param7,
        List<BakedQuad> param8,
        BitSet param9
    ) {
        for(BakedQuad var0 : param8) {
            if (param5) {
                this.calculateShape(param0, param1, param2, var0.getVertices(), var0.getDirection(), null, param9);
                BlockPos var1 = param9.get(0) ? param2.relative(var0.getDirection()) : param2;
                param3 = LevelRenderer.getLightColor(param0, param1, var1);
            }

            float var2 = param0.getShade(var0.getDirection(), var0.isShade());
            this.putQuadData(param0, param1, param2, param7, param6.last(), var0, var2, var2, var2, var2, param3, param3, param3, param3, param4);
        }

    }

    public void renderModel(
        PoseStack.Pose param0,
        VertexConsumer param1,
        @Nullable BlockState param2,
        BakedModel param3,
        float param4,
        float param5,
        float param6,
        int param7,
        int param8
    ) {
        RandomSource var0 = RandomSource.create();
        long var1 = 42L;

        for(Direction var2 : DIRECTIONS) {
            var0.setSeed(42L);
            renderQuadList(param0, param1, param4, param5, param6, param3.getQuads(param2, var2, var0), param7, param8);
        }

        var0.setSeed(42L);
        renderQuadList(param0, param1, param4, param5, param6, param3.getQuads(param2, null, var0), param7, param8);
    }

    private static void renderQuadList(
        PoseStack.Pose param0, VertexConsumer param1, float param2, float param3, float param4, List<BakedQuad> param5, int param6, int param7
    ) {
        for(BakedQuad var0 : param5) {
            float var1;
            float var2;
            float var3;
            if (var0.isTinted()) {
                var1 = Mth.clamp(param2, 0.0F, 1.0F);
                var2 = Mth.clamp(param3, 0.0F, 1.0F);
                var3 = Mth.clamp(param4, 0.0F, 1.0F);
            } else {
                var1 = 1.0F;
                var2 = 1.0F;
                var3 = 1.0F;
            }

            param1.putBulkData(param0, var0, var1, var2, var3, param6, param7);
        }

    }

    public static void enableCaching() {
        CACHE.get().enable();
    }

    public static void clearCache() {
        CACHE.get().disable();
    }

    @OnlyIn(Dist.CLIENT)
    protected static enum AdjacencyInfo {
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

        final Direction[] corners;
        final boolean doNonCubicWeight;
        final ModelBlockRenderer.SizeInfo[] vert0Weights;
        final ModelBlockRenderer.SizeInfo[] vert1Weights;
        final ModelBlockRenderer.SizeInfo[] vert2Weights;
        final ModelBlockRenderer.SizeInfo[] vert3Weights;
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
        final float[] brightness = new float[4];
        final int[] lightmap = new int[4];

        public AmbientOcclusionFace() {
        }

        public void calculate(BlockAndTintGetter param0, BlockState param1, BlockPos param2, Direction param3, float[] param4, BitSet param5, boolean param6) {
            BlockPos var0 = param5.get(0) ? param2.relative(param3) : param2;
            ModelBlockRenderer.AdjacencyInfo var1 = ModelBlockRenderer.AdjacencyInfo.fromFacing(param3);
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();
            ModelBlockRenderer.Cache var3 = ModelBlockRenderer.CACHE.get();
            var2.setWithOffset(var0, var1.corners[0]);
            BlockState var4 = param0.getBlockState(var2);
            int var5 = var3.getLightColor(var4, param0, var2);
            float var6 = var3.getShadeBrightness(var4, param0, var2);
            var2.setWithOffset(var0, var1.corners[1]);
            BlockState var7 = param0.getBlockState(var2);
            int var8 = var3.getLightColor(var7, param0, var2);
            float var9 = var3.getShadeBrightness(var7, param0, var2);
            var2.setWithOffset(var0, var1.corners[2]);
            BlockState var10 = param0.getBlockState(var2);
            int var11 = var3.getLightColor(var10, param0, var2);
            float var12 = var3.getShadeBrightness(var10, param0, var2);
            var2.setWithOffset(var0, var1.corners[3]);
            BlockState var13 = param0.getBlockState(var2);
            int var14 = var3.getLightColor(var13, param0, var2);
            float var15 = var3.getShadeBrightness(var13, param0, var2);
            BlockState var16 = param0.getBlockState(var2.setWithOffset(var0, var1.corners[0]).move(param3));
            boolean var17 = !var16.isViewBlocking(param0, var2) || var16.getLightBlock(param0, var2) == 0;
            BlockState var18 = param0.getBlockState(var2.setWithOffset(var0, var1.corners[1]).move(param3));
            boolean var19 = !var18.isViewBlocking(param0, var2) || var18.getLightBlock(param0, var2) == 0;
            BlockState var20 = param0.getBlockState(var2.setWithOffset(var0, var1.corners[2]).move(param3));
            boolean var21 = !var20.isViewBlocking(param0, var2) || var20.getLightBlock(param0, var2) == 0;
            BlockState var22 = param0.getBlockState(var2.setWithOffset(var0, var1.corners[3]).move(param3));
            boolean var23 = !var22.isViewBlocking(param0, var2) || var22.getLightBlock(param0, var2) == 0;
            float var27;
            int var28;
            if (!var21 && !var17) {
                var27 = var6;
                var28 = var5;
            } else {
                var2.setWithOffset(var0, var1.corners[0]).move(var1.corners[2]);
                BlockState var24 = param0.getBlockState(var2);
                var27 = var3.getShadeBrightness(var24, param0, var2);
                var28 = var3.getLightColor(var24, param0, var2);
            }

            float var32;
            int var33;
            if (!var23 && !var17) {
                var32 = var6;
                var33 = var5;
            } else {
                var2.setWithOffset(var0, var1.corners[0]).move(var1.corners[3]);
                BlockState var29 = param0.getBlockState(var2);
                var32 = var3.getShadeBrightness(var29, param0, var2);
                var33 = var3.getLightColor(var29, param0, var2);
            }

            float var37;
            int var38;
            if (!var21 && !var19) {
                var37 = var6;
                var38 = var5;
            } else {
                var2.setWithOffset(var0, var1.corners[1]).move(var1.corners[2]);
                BlockState var34 = param0.getBlockState(var2);
                var37 = var3.getShadeBrightness(var34, param0, var2);
                var38 = var3.getLightColor(var34, param0, var2);
            }

            float var42;
            int var43;
            if (!var23 && !var19) {
                var42 = var6;
                var43 = var5;
            } else {
                var2.setWithOffset(var0, var1.corners[1]).move(var1.corners[3]);
                BlockState var39 = param0.getBlockState(var2);
                var42 = var3.getShadeBrightness(var39, param0, var2);
                var43 = var3.getLightColor(var39, param0, var2);
            }

            int var44 = var3.getLightColor(param1, param0, param2);
            var2.setWithOffset(param2, param3);
            BlockState var45 = param0.getBlockState(var2);
            if (param5.get(0) || !var45.isSolidRender(param0, var2)) {
                var44 = var3.getLightColor(var45, param0, var2);
            }

            float var46 = param5.get(0)
                ? var3.getShadeBrightness(param0.getBlockState(var0), param0, var0)
                : var3.getShadeBrightness(param0.getBlockState(param2), param0, param2);
            ModelBlockRenderer.AmbientVertexRemap var47 = ModelBlockRenderer.AmbientVertexRemap.fromFacing(param3);
            if (param5.get(1) && var1.doNonCubicWeight) {
                float var52 = (var15 + var6 + var32 + var46) * 0.25F;
                float var53 = (var12 + var6 + var27 + var46) * 0.25F;
                float var54 = (var12 + var9 + var37 + var46) * 0.25F;
                float var55 = (var15 + var9 + var42 + var46) * 0.25F;
                float var56 = param4[var1.vert0Weights[0].shape] * param4[var1.vert0Weights[1].shape];
                float var57 = param4[var1.vert0Weights[2].shape] * param4[var1.vert0Weights[3].shape];
                float var58 = param4[var1.vert0Weights[4].shape] * param4[var1.vert0Weights[5].shape];
                float var59 = param4[var1.vert0Weights[6].shape] * param4[var1.vert0Weights[7].shape];
                float var60 = param4[var1.vert1Weights[0].shape] * param4[var1.vert1Weights[1].shape];
                float var61 = param4[var1.vert1Weights[2].shape] * param4[var1.vert1Weights[3].shape];
                float var62 = param4[var1.vert1Weights[4].shape] * param4[var1.vert1Weights[5].shape];
                float var63 = param4[var1.vert1Weights[6].shape] * param4[var1.vert1Weights[7].shape];
                float var64 = param4[var1.vert2Weights[0].shape] * param4[var1.vert2Weights[1].shape];
                float var65 = param4[var1.vert2Weights[2].shape] * param4[var1.vert2Weights[3].shape];
                float var66 = param4[var1.vert2Weights[4].shape] * param4[var1.vert2Weights[5].shape];
                float var67 = param4[var1.vert2Weights[6].shape] * param4[var1.vert2Weights[7].shape];
                float var68 = param4[var1.vert3Weights[0].shape] * param4[var1.vert3Weights[1].shape];
                float var69 = param4[var1.vert3Weights[2].shape] * param4[var1.vert3Weights[3].shape];
                float var70 = param4[var1.vert3Weights[4].shape] * param4[var1.vert3Weights[5].shape];
                float var71 = param4[var1.vert3Weights[6].shape] * param4[var1.vert3Weights[7].shape];
                this.brightness[var47.vert0] = var52 * var56 + var53 * var57 + var54 * var58 + var55 * var59;
                this.brightness[var47.vert1] = var52 * var60 + var53 * var61 + var54 * var62 + var55 * var63;
                this.brightness[var47.vert2] = var52 * var64 + var53 * var65 + var54 * var66 + var55 * var67;
                this.brightness[var47.vert3] = var52 * var68 + var53 * var69 + var54 * var70 + var55 * var71;
                int var72 = this.blend(var14, var5, var33, var44);
                int var73 = this.blend(var11, var5, var28, var44);
                int var74 = this.blend(var11, var8, var38, var44);
                int var75 = this.blend(var14, var8, var43, var44);
                this.lightmap[var47.vert0] = this.blend(var72, var73, var74, var75, var56, var57, var58, var59);
                this.lightmap[var47.vert1] = this.blend(var72, var73, var74, var75, var60, var61, var62, var63);
                this.lightmap[var47.vert2] = this.blend(var72, var73, var74, var75, var64, var65, var66, var67);
                this.lightmap[var47.vert3] = this.blend(var72, var73, var74, var75, var68, var69, var70, var71);
            } else {
                float var48 = (var15 + var6 + var32 + var46) * 0.25F;
                float var49 = (var12 + var6 + var27 + var46) * 0.25F;
                float var50 = (var12 + var9 + var37 + var46) * 0.25F;
                float var51 = (var15 + var9 + var42 + var46) * 0.25F;
                this.lightmap[var47.vert0] = this.blend(var14, var5, var33, var44);
                this.lightmap[var47.vert1] = this.blend(var11, var5, var28, var44);
                this.lightmap[var47.vert2] = this.blend(var11, var8, var38, var44);
                this.lightmap[var47.vert3] = this.blend(var14, var8, var43, var44);
                this.brightness[var47.vert0] = var48;
                this.brightness[var47.vert1] = var49;
                this.brightness[var47.vert2] = var50;
                this.brightness[var47.vert3] = var51;
            }

            float var76 = param0.getShade(param3, param6);

            for(int var77 = 0; var77 < this.brightness.length; ++var77) {
                this.brightness[var77] *= var76;
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

        final int vert0;
        final int vert1;
        final int vert2;
        final int vert3;
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

        public int getLightColor(BlockState param0, BlockAndTintGetter param1, BlockPos param2) {
            long var0 = param2.asLong();
            if (this.enabled) {
                int var1 = this.colorCache.get(var0);
                if (var1 != Integer.MAX_VALUE) {
                    return var1;
                }
            }

            int var2 = LevelRenderer.getLightColor(param1, param0, param2);
            if (this.enabled) {
                if (this.colorCache.size() == 100) {
                    this.colorCache.removeFirstInt();
                }

                this.colorCache.put(var0, var2);
            }

            return var2;
        }

        public float getShadeBrightness(BlockState param0, BlockAndTintGetter param1, BlockPos param2) {
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
    protected static enum SizeInfo {
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

        final int shape;

        private SizeInfo(Direction param0, boolean param1) {
            this.shape = param0.get3DDataValue() + (param1 ? ModelBlockRenderer.DIRECTIONS.length : 0);
        }
    }
}
