package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.LongIterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.FeatureAccess;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureFeature<C extends FeatureConfiguration> extends Feature<C> {
    private static final Logger LOGGER = LogManager.getLogger();

    public StructureFeature(Function<Dynamic<?>, ? extends C> param0) {
        super(param0, false);
    }

    @Override
    public boolean place(LevelAccessor param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, Random param2, BlockPos param3, C param4) {
        if (!param0.getLevelData().isGenerateMapFeatures()) {
            return false;
        } else {
            int var0 = param3.getX() >> 4;
            int var1 = param3.getZ() >> 4;
            int var2 = var0 << 4;
            int var3 = var1 << 4;
            boolean var4 = false;

            for(Long var5 : param0.getChunk(var0, var1).getReferencesForFeature(this.getFeatureName())) {
                ChunkPos var6 = new ChunkPos(var5);
                StructureStart var7 = param0.getChunk(var6.x, var6.z).getStartForFeature(this.getFeatureName());
                if (var7 != null && var7 != StructureStart.INVALID_START) {
                    var7.postProcess(param0, param2, new BoundingBox(var2, var3, var2 + 15, var3 + 15), new ChunkPos(var0, var1));
                    var4 = true;
                }
            }

            return var4;
        }
    }

    protected StructureStart getStructureAt(LevelAccessor param0, BlockPos param1, boolean param2) {
        for(StructureStart var1 : this.dereferenceStructureStarts(param0, param1.getX() >> 4, param1.getZ() >> 4)) {
            if (var1.isValid() && var1.getBoundingBox().isInside(param1)) {
                if (!param2) {
                    return var1;
                }

                for(StructurePiece var2 : var1.getPieces()) {
                    if (var2.getBoundingBox().isInside(param1)) {
                        return var1;
                    }
                }
            }
        }

        return StructureStart.INVALID_START;
    }

    public boolean isInsideBoundingFeature(LevelAccessor param0, BlockPos param1) {
        return this.getStructureAt(param0, param1, false).isValid();
    }

    public boolean isInsideFeature(LevelAccessor param0, BlockPos param1) {
        return this.getStructureAt(param0, param1, true).isValid();
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(
        Level param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, BlockPos param2, int param3, boolean param4
    ) {
        if (!param1.getBiomeSource().canGenerateStructure(this)) {
            return null;
        } else {
            int var0 = param2.getX() >> 4;
            int var1 = param2.getZ() >> 4;
            int var2 = 0;

            for(WorldgenRandom var3 = new WorldgenRandom(); var2 <= param3; ++var2) {
                for(int var4 = -var2; var4 <= var2; ++var4) {
                    boolean var5 = var4 == -var2 || var4 == var2;

                    for(int var6 = -var2; var6 <= var2; ++var6) {
                        boolean var7 = var6 == -var2 || var6 == var2;
                        if (var5 || var7) {
                            ChunkPos var8 = this.getPotentialFeatureChunkFromLocationWithOffset(param1, var3, var0, var1, var4, var6);
                            StructureStart var9 = param0.getChunk(var8.x, var8.z, ChunkStatus.STRUCTURE_STARTS).getStartForFeature(this.getFeatureName());
                            if (var9 != null && var9.isValid()) {
                                if (param4 && var9.canBeReferenced()) {
                                    var9.addReference();
                                    return var9.getLocatePos();
                                }

                                if (!param4) {
                                    return var9.getLocatePos();
                                }
                            }

                            if (var2 == 0) {
                                break;
                            }
                        }
                    }

                    if (var2 == 0) {
                        break;
                    }
                }
            }

            return null;
        }
    }

    private List<StructureStart> dereferenceStructureStarts(LevelAccessor param0, int param1, int param2) {
        List<StructureStart> var0 = Lists.newArrayList();
        ChunkAccess var1 = param0.getChunk(param1, param2, ChunkStatus.STRUCTURE_REFERENCES);
        LongIterator var2 = var1.getReferencesForFeature(this.getFeatureName()).iterator();

        while(var2.hasNext()) {
            long var3 = var2.nextLong();
            FeatureAccess var4 = param0.getChunk(ChunkPos.getX(var3), ChunkPos.getZ(var3), ChunkStatus.STRUCTURE_STARTS);
            StructureStart var5 = var4.getStartForFeature(this.getFeatureName());
            if (var5 != null) {
                var0.add(var5);
            }
        }

        return var0;
    }

    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> param0, Random param1, int param2, int param3, int param4, int param5) {
        return new ChunkPos(param2 + param4, param3 + param5);
    }

    public abstract boolean isFeatureChunk(ChunkGenerator<?> var1, Random var2, int var3, int var4);

    public abstract StructureFeature.StructureStartFactory getStartFactory();

    public abstract String getFeatureName();

    public abstract int getLookupRange();

    public interface StructureStartFactory {
        StructureStart create(StructureFeature<?> var1, int var2, int var3, Biome var4, BoundingBox var5, int var6, long var7);
    }
}
