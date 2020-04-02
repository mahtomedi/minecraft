package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class StructureFeature<C extends FeatureConfiguration> extends Feature<C> {
    private static final Logger LOGGER = LogManager.getLogger();

    public StructureFeature(Function<Dynamic<?>, ? extends C> param0) {
        super(param0);
    }

    @Override
    public ConfiguredFeature<C, ? extends StructureFeature<C>> configured(C param0) {
        return new ConfiguredFeature<>(this, param0);
    }

    @Override
    public boolean place(
        LevelAccessor param0, StructureFeatureManager param1, ChunkGenerator<? extends ChunkGeneratorSettings> param2, Random param3, BlockPos param4, C param5
    ) {
        if (!param0.getLevelData().isGenerateMapFeatures()) {
            return false;
        } else {
            int var0 = param4.getX() >> 4;
            int var1 = param4.getZ() >> 4;
            int var2 = var0 << 4;
            int var3 = var1 << 4;
            return param1.startsForFeature(SectionPos.of(param4), this, param0).map(param8 -> {
                param8.postProcess(param0, param1, param2, param3, new BoundingBox(var2, var3, var2 + 15, var3 + 15), new ChunkPos(var0, var1));
                return null;
            }).count() != 0L;
        }
    }

    protected StructureStart getStructureAt(LevelAccessor param0, StructureFeatureManager param1, BlockPos param2, boolean param3) {
        return param1.startsForFeature(SectionPos.of(param2), this, param0)
            .filter(param1x -> param1x.getBoundingBox().isInside(param2))
            .filter(param2x -> !param3 || param2x.getPieces().stream().anyMatch(param1x -> param1x.getBoundingBox().isInside(param2)))
            .findFirst()
            .orElse(StructureStart.INVALID_START);
    }

    public boolean isInsideBoundingFeature(LevelAccessor param0, StructureFeatureManager param1, BlockPos param2) {
        return this.getStructureAt(param0, param1, param2, false).isValid();
    }

    public boolean isInsideFeature(LevelAccessor param0, StructureFeatureManager param1, BlockPos param2) {
        return this.getStructureAt(param0, param1, param2, true).isValid();
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(
        ServerLevel param0, ChunkGenerator<? extends ChunkGeneratorSettings> param1, BlockPos param2, int param3, boolean param4
    ) {
        if (!param1.getBiomeSource().canGenerateStructure(this)) {
            return null;
        } else {
            StructureFeatureManager var0 = param0.structureFeatureManager();
            int var1 = param2.getX() >> 4;
            int var2 = param2.getZ() >> 4;
            int var3 = 0;

            for(WorldgenRandom var4 = new WorldgenRandom(); var3 <= param3; ++var3) {
                for(int var5 = -var3; var5 <= var3; ++var5) {
                    boolean var6 = var5 == -var3 || var5 == var3;

                    for(int var7 = -var3; var7 <= var3; ++var7) {
                        boolean var8 = var7 == -var3 || var7 == var3;
                        if (var6 || var8) {
                            ChunkPos var9 = this.getPotentialFeatureChunkFromLocationWithOffset(param1, var4, var1, var2, var5, var7);
                            ChunkAccess var10 = param0.getChunk(var9.x, var9.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart var11 = var0.getStartForFeature(SectionPos.of(var10.getPos(), 0), this, var10);
                            if (var11 != null && var11.isValid()) {
                                if (param4 && var11.canBeReferenced()) {
                                    var11.addReference();
                                    return var11.getLocatePos();
                                }

                                if (!param4) {
                                    return var11.getLocatePos();
                                }
                            }

                            if (var3 == 0) {
                                break;
                            }
                        }
                    }

                    if (var3 == 0) {
                        break;
                    }
                }
            }

            return null;
        }
    }

    protected ChunkPos getPotentialFeatureChunkFromLocationWithOffset(ChunkGenerator<?> param0, Random param1, int param2, int param3, int param4, int param5) {
        return new ChunkPos(param2 + param4, param3 + param5);
    }

    public abstract boolean isFeatureChunk(BiomeManager var1, ChunkGenerator<?> var2, Random var3, int var4, int var5, Biome var6);

    public abstract StructureFeature.StructureStartFactory getStartFactory();

    public abstract String getFeatureName();

    public abstract int getLookupRange();

    public interface StructureStartFactory {
        StructureStart create(StructureFeature<?> var1, int var2, int var3, BoundingBox var4, int var5, long var6);
    }
}
