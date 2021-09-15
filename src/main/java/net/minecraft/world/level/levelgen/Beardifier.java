package net.minecraft.world.level.levelgen;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.NoiseEffect;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

public class Beardifier implements NoiseChunk.NoiseFiller {
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
    private final ObjectList<StructurePiece> rigids;
    private final ObjectList<JigsawJunction> junctions;
    private final ObjectListIterator<StructurePiece> pieceIterator;
    private final ObjectListIterator<JigsawJunction> junctionIterator;

    protected Beardifier(StructureFeatureManager param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        int var1 = var0.getMinBlockX();
        int var2 = var0.getMinBlockZ();
        this.junctions = new ObjectArrayList<>(32);
        this.rigids = new ObjectArrayList<>(10);

        for(StructureFeature<?> var3 : StructureFeature.NOISE_AFFECTING_FEATURES) {
            param0.startsForFeature(SectionPos.bottomOf(param1), var3).forEach(param3 -> {
                for(StructurePiece var0x : param3.getPieces()) {
                    if (var0x.isCloseToChunk(var0, 12)) {
                        if (var0x instanceof PoolElementStructurePiece var1x) {
                            StructureTemplatePool.Projection var2x = var1x.getElement().getProjection();
                            if (var2x == StructureTemplatePool.Projection.RIGID) {
                                this.rigids.add(var1x);
                            }

                            for(JigsawJunction var3x : var1x.getJunctions()) {
                                int var4x = var3x.getSourceX();
                                int var5x = var3x.getSourceZ();
                                if (var4x > var1 - 12 && var5x > var2 - 12 && var4x < var1 + 15 + 12 && var5x < var2 + 15 + 12) {
                                    this.junctions.add(var3x);
                                }
                            }
                        } else {
                            this.rigids.add(var0x);
                        }
                    }
                }

            });
        }

        this.pieceIterator = this.rigids.iterator();
        this.junctionIterator = this.junctions.iterator();
    }

    @Override
    public double calculateNoise(int param0, int param1, int param2) {
        double var0 = 0.0;

        while(this.pieceIterator.hasNext()) {
            StructurePiece var1 = this.pieceIterator.next();
            BoundingBox var2 = var1.getBoundingBox();
            int var3 = Math.max(0, Math.max(var2.minX() - param0, param0 - var2.maxX()));
            int var4 = param1 - (var2.minY() + (var1 instanceof PoolElementStructurePiece ? ((PoolElementStructurePiece)var1).getGroundLevelDelta() : 0));
            int var5 = Math.max(0, Math.max(var2.minZ() - param2, param2 - var2.maxZ()));
            NoiseEffect var6 = var1.getNoiseEffect();
            if (var6 == NoiseEffect.BURY) {
                var0 += getBuryContribution(var3, var4, var5);
            } else if (var6 == NoiseEffect.BEARD) {
                var0 += getBeardContribution(var3, var4, var5) * 0.8;
            }
        }

        this.pieceIterator.back(this.rigids.size());

        while(this.junctionIterator.hasNext()) {
            JigsawJunction var7 = this.junctionIterator.next();
            int var8 = param0 - var7.getSourceX();
            int var9 = param1 - var7.getSourceGroundY();
            int var10 = param2 - var7.getSourceZ();
            var0 += getBeardContribution(var8, var9, var10) * 0.4;
        }

        this.junctionIterator.back(this.junctions.size());
        return var0;
    }

    private static double getBuryContribution(int param0, int param1, int param2) {
        double var0 = Mth.length(param0, (double)param1 / 2.0, param2);
        return Mth.clampedMap(var0, 0.0, 6.0, 1.0, 0.0);
    }

    private static double getBeardContribution(int param0, int param1, int param2) {
        int var0 = param0 + 12;
        int var1 = param1 + 12;
        int var2 = param2 + 12;
        if (var0 < 0 || var0 >= 24) {
            return 0.0;
        } else if (var1 < 0 || var1 >= 24) {
            return 0.0;
        } else {
            return var2 >= 0 && var2 < 24 ? (double)BEARD_KERNEL[var2 * 24 * 24 + var0 * 24 + var1] : 0.0;
        }
    }

    private static double computeBeardContribution(int param0, int param1, int param2) {
        double var0 = (double)(param0 * param0 + param2 * param2);
        double var1 = (double)param1 + 0.5;
        double var2 = var1 * var1;
        double var3 = Math.pow(Math.E, -(var2 / 16.0 + var0 / 16.0));
        double var4 = -var1 * Mth.fastInvSqrt(var2 / 2.0 + var0 / 2.0) / 2.0;
        return var4 * var3;
    }
}
