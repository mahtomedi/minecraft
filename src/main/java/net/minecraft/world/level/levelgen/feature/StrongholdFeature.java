package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StrongholdFeature extends StructureFeature<NoneFeatureConfiguration> {
    private boolean isSpotSelected;
    private ChunkPos[] strongholdPos;
    private final List<StructureStart> discoveredStarts = Lists.newArrayList();
    private long currentSeed;

    public StrongholdFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0, Function<Random, ? extends NoneFeatureConfiguration> param1) {
        super(param0, param1);
    }

    @Override
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        if (this.currentSeed != param1.getSeed()) {
            this.reset();
        }

        if (!this.isSpotSelected) {
            this.generatePositions(param1);
            this.isSpotSelected = true;
        }

        for(ChunkPos var0 : this.strongholdPos) {
            if (param3 == var0.x && param4 == var0.z) {
                return true;
            }
        }

        return false;
    }

    private void reset() {
        this.isSpotSelected = false;
        this.strongholdPos = null;
        this.discoveredStarts.clear();
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return StrongholdFeature.StrongholdStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Stronghold";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    @Nullable
    @Override
    public BlockPos getNearestGeneratedFeature(
        Level param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, BlockPos param2, int param3, boolean param4
    ) {
        if (!param1.getBiomeSource().canGenerateStructure(this)) {
            return null;
        } else {
            if (this.currentSeed != param0.getSeed()) {
                this.reset();
            }

            if (!this.isSpotSelected) {
                this.generatePositions(param1);
                this.isSpotSelected = true;
            }

            BlockPos var0 = null;
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
            double var2 = Double.MAX_VALUE;

            for(ChunkPos var3 : this.strongholdPos) {
                var1.set((var3.x << 4) + 8, 32, (var3.z << 4) + 8);
                double var4 = var1.distSqr(param2);
                if (var0 == null) {
                    var0 = new BlockPos(var1);
                    var2 = var4;
                } else if (var4 < var2) {
                    var0 = new BlockPos(var1);
                    var2 = var4;
                }
            }

            return var0;
        }
    }

    private void generatePositions(ChunkGenerator<?> param0) {
        this.currentSeed = param0.getSeed();
        List<Biome> var0 = Lists.newArrayList();

        for(Biome var1 : Registry.BIOME) {
            if (var1 != null && param0.isBiomeValidStartForStructure(var1, this)) {
                var0.add(var1);
            }
        }

        int var2 = param0.getSettings().getStrongholdsDistance();
        int var3 = param0.getSettings().getStrongholdsCount();
        int var4 = param0.getSettings().getStrongholdsSpread();
        this.strongholdPos = new ChunkPos[var3];
        int var5 = 0;

        for(StructureStart var6 : this.discoveredStarts) {
            if (var5 < this.strongholdPos.length) {
                this.strongholdPos[var5++] = new ChunkPos(var6.getChunkX(), var6.getChunkZ());
            }
        }

        Random var7 = new Random();
        var7.setSeed(param0.getSeed());
        double var8 = var7.nextDouble() * Math.PI * 2.0;
        int var9 = var5;
        if (var5 < this.strongholdPos.length) {
            int var10 = 0;
            int var11 = 0;

            for(int var12 = 0; var12 < this.strongholdPos.length; ++var12) {
                double var13 = (double)(4 * var2 + var2 * var11 * 6) + (var7.nextDouble() - 0.5) * (double)var2 * 2.5;
                int var14 = (int)Math.round(Math.cos(var8) * var13);
                int var15 = (int)Math.round(Math.sin(var8) * var13);
                BlockPos var16 = param0.getBiomeSource().findBiomeHorizontal((var14 << 4) + 8, param0.getSeaLevel(), (var15 << 4) + 8, 112, var0, var7);
                if (var16 != null) {
                    var14 = var16.getX() >> 4;
                    var15 = var16.getZ() >> 4;
                }

                if (var12 >= var9) {
                    this.strongholdPos[var12] = new ChunkPos(var14, var15);
                }

                var8 += (Math.PI * 2) / (double)var4;
                if (++var10 == var4) {
                    ++var11;
                    var10 = 0;
                    var4 += 2 * var4 / (var11 + 1);
                    var4 = Math.min(var4, this.strongholdPos.length - var12);
                    var8 += var7.nextDouble() * Math.PI * 2.0;
                }
            }
        }

    }

    public static class StrongholdStart extends StructureStart {
        public StrongholdStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            int var0 = 0;
            long var1 = param0.getSeed();

            StrongholdPieces.StartPiece var2;
            do {
                this.pieces.clear();
                this.boundingBox = BoundingBox.getUnknownBox();
                this.random.setLargeFeatureSeed(var1 + (long)(var0++), param2, param3);
                StrongholdPieces.resetPieces();
                var2 = new StrongholdPieces.StartPiece(this.random, (param2 << 4) + 2, (param3 << 4) + 2);
                this.pieces.add(var2);
                var2.addChildren(var2, this.pieces, this.random);
                List<StructurePiece> var3 = var2.pendingChildren;

                while(!var3.isEmpty()) {
                    int var4 = this.random.nextInt(var3.size());
                    StructurePiece var5 = var3.remove(var4);
                    var5.addChildren(var2, this.pieces, this.random);
                }

                this.calculateBoundingBox();
                this.moveBelowSeaLevel(param0.getSeaLevel(), this.random, 10);
            } while(this.pieces.isEmpty() || var2.portalRoomPiece == null);

            ((StrongholdFeature)this.getFeature()).discoveredStarts.add(this);
        }
    }
}
