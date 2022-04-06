package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Beardifier implements DensityFunctions.BeardifierOrMarker {
    public static final int BEARD_KERNEL_RADIUS = 12;
    private static final int BEARD_KERNEL_SIZE = 24;
    private static final float[] BEARD_KERNEL = Util.make(new float[13824], param0 -> {
        for(int var0 = 0; var0 < 24; ++var0) {
            for(int var1 = 0; var1 < 24; ++var1) {
                for(int var2 = 0; var2 < 24; ++var2) {
                    param0[var0 * 24 * 24 + var1 * 24 + var2] = (float)computeBeardContribution(var1 - 12, var2 - 12, var0 - 12);
                }
            }
        }

    });
    private final ObjectListIterator<Beardifier.Rigid> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    public static Beardifier forStructuresInChunk(StructureManager param0, ChunkPos param1) {
        int var0 = param1.getMinBlockX();
        int var1 = param1.getMinBlockZ();
        ObjectList<Beardifier.Rigid> var2 = new ObjectArrayList<>(10);
        ObjectList<JigsawJunction> var3 = new ObjectArrayList<>(32);
        param0.startsForStructure(param1, param0x -> param0x.terrainAdaptation() != TerrainAdjustment.NONE).forEach(param5 -> {
            TerrainAdjustment var0x = param5.getStructure().terrainAdaptation();

            for(StructurePiece var1x : param5.getPieces()) {
                if (var1x.isCloseToChunk(param1, 12)) {
                    if (var1x instanceof PoolElementStructurePiece var2x) {
                        StructureTemplatePool.Projection var3x = var2x.getElement().getProjection();
                        if (var3x == StructureTemplatePool.Projection.RIGID) {
                            var2.add(new Beardifier.Rigid(var2x.getBoundingBox(), var0x, var2x.getGroundLevelDelta()));
                        }

                        for(JigsawJunction var4x : var2x.getJunctions()) {
                            int var5x = var4x.getSourceX();
                            int var6 = var4x.getSourceZ();
                            if (var5x > var0 - 12 && var6 > var1 - 12 && var5x < var0 + 15 + 12 && var6 < var1 + 15 + 12) {
                                var3.add(var4x);
                            }
                        }
                    } else {
                        var2.add(new Beardifier.Rigid(var1x.getBoundingBox(), var0x, 0));
                    }
                }
            }

        });
        return new Beardifier(var2.iterator(), var3.iterator());
    }

    @VisibleForTesting
    public Beardifier(ObjectListIterator<Beardifier.Rigid> param0, ObjectListIterator<JigsawJunction> param1) {
        this.pieceIterator = param0;
        this.junctionIterator = param1;
    }

    @Override
    public double compute(DensityFunction.FunctionContext param0) {
        int var0 = param0.blockX();
        int var1 = param0.blockY();
        int var2 = param0.blockZ();

        double var3;
        double var10001;
        for(var3 = 0.0; this.pieceIterator.hasNext(); var3 += var10001) {
            Beardifier.Rigid var4 = this.pieceIterator.next();
            BoundingBox var5 = var4.box();
            int var6 = var4.groundLevelDelta();
            int var7 = Math.max(0, Math.max(var5.minX() - var0, var0 - var5.maxX()));
            int var8 = Math.max(0, Math.max(var5.minZ() - var2, var2 - var5.maxZ()));
            int var9 = var5.minY() + var6;
            int var10 = var1 - var9;

            int var11 = switch(var4.terrainAdjustment()) {
                case NONE -> 0;
                case BURY, BEARD_THIN -> var10;
                case BEARD_BOX -> Math.max(0, Math.max(var9 - var1, var1 - var5.maxY()));
            };
            switch(var4.terrainAdjustment()) {
                case NONE:
                    var10001 = 0.0;
                    break;
                case BURY:
                    var10001 = getBuryContribution(var7, var11, var8);
                    break;
                case BEARD_THIN:
                case BEARD_BOX:
                    var10001 = getBeardContribution(var7, var11, var8, var10) * 0.8;
                    break;
                default:
                    throw new IncompatibleClassChangeError();
            }
        }

        this.pieceIterator.back(Integer.MAX_VALUE);

        while(this.junctionIterator.hasNext()) {
            JigsawJunction var12 = this.junctionIterator.next();
            int var13 = var0 - var12.getSourceX();
            int var14 = var1 - var12.getSourceGroundY();
            int var15 = var2 - var12.getSourceZ();
            var3 += getBeardContribution(var13, var14, var15, var14) * 0.4;
        }

        this.junctionIterator.back(Integer.MAX_VALUE);
        return var3;
    }

    @Override
    public double minValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    private static double getBuryContribution(int param0, int param1, int param2) {
        double var0 = Mth.length((double)param0, (double)param1 / 2.0, (double)param2);
        return Mth.clampedMap(var0, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getBeardContribution(int param0, int param1, int param2, int param3) {
        int var0 = param0 + 12;
        int var1 = param1 + 12;
        int var2 = param2 + 12;
        if (isInKernelRange(var0) && isInKernelRange(var1) && isInKernelRange(var2)) {
            double var3 = (double)param3 + 0.5;
            double var4 = Mth.lengthSquared((double)param0, var3, (double)param2);
            double var5 = -var3 * Mth.fastInvSqrt(var4 / 2.0) / 2.0;
            return var5 * (double)BEARD_KERNEL[var2 * 24 * 24 + var0 * 24 + var1];
        } else {
            return 0.0;
        }
    }

    private static boolean isInKernelRange(int param0) {
        return param0 >= 0 && param0 < 24;
    }

    private static double computeBeardContribution(int param0, int param1, int param2) {
        return computeBeardContribution(param0, (double)param1 + 0.5, param2);
    }

    private static double computeBeardContribution(int param0, double param1, int param2) {
        double var0 = Mth.lengthSquared((double)param0, param1, (double)param2);
        return Math.pow(Math.E, -var0 / 16.0);
    }

    @VisibleForTesting
    public static record Rigid(BoundingBox box, TerrainAdjustment terrainAdjustment, int groundLevelDelta) {
    }
}
