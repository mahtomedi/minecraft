package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
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
    public boolean place(WorldGenLevel param0, StructureFeatureManager param1, ChunkGenerator param2, Random param3, BlockPos param4, C param5) {
        if (!param1.shouldGenerateFeatures()) {
            return false;
        } else {
            int var0 = param4.getX() >> 4;
            int var1 = param4.getZ() >> 4;
            int var2 = var0 << 4;
            int var3 = var1 << 4;
            return param1.startsForFeature(SectionPos.of(param4), this).map(param8 -> {
                param8.postProcess(param0, param1, param2, param3, new BoundingBox(var2, var3, var2 + 15, var3 + 15), new ChunkPos(var0, var1));
                return null;
            }).count() != 0L;
        }
    }

    protected StructureStart getStructureAt(StructureFeatureManager param0, BlockPos param1, boolean param2) {
        return param0.startsForFeature(SectionPos.of(param1), this)
            .filter(param1x -> param1x.getBoundingBox().isInside(param1))
            .filter(param2x -> !param2 || param2x.getPieces().stream().anyMatch(param1x -> param1x.getBoundingBox().isInside(param1)))
            .findFirst()
            .orElse(StructureStart.INVALID_START);
    }

    public boolean isInsideBoundingFeature(StructureFeatureManager param0, BlockPos param1) {
        return this.getStructureAt(param0, param1, false).isValid();
    }

    public boolean isInsideFeature(StructureFeatureManager param0, BlockPos param1) {
        return this.getStructureAt(param0, param1, true).isValid();
    }

    @Nullable
    public BlockPos getNearestGeneratedFeature(ServerLevel param0, ChunkGenerator param1, BlockPos param2, int param3, boolean param4) {
        if (!param1.canGenerateStructure(this)) {
            return null;
        } else {
            StructureFeatureManager var0 = param0.structureFeatureManager();
            int var1 = this.getSpacing(param1.getSettings());
            int var2 = param2.getX() >> 4;
            int var3 = param2.getZ() >> 4;
            int var4 = 0;

            for(WorldgenRandom var5 = new WorldgenRandom(); var4 <= param3; ++var4) {
                for(int var6 = -var4; var6 <= var4; ++var6) {
                    boolean var7 = var6 == -var4 || var6 == var4;

                    for(int var8 = -var4; var8 <= var4; ++var8) {
                        boolean var9 = var8 == -var4 || var8 == var4;
                        if (var7 || var9) {
                            int var10 = var2 + var1 * var6;
                            int var11 = var3 + var1 * var8;
                            ChunkPos var12 = this.getPotentialFeatureChunk(param1.getSettings(), param0.getSeed(), var5, var10, var11);
                            ChunkAccess var13 = param0.getChunk(var12.x, var12.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart var14 = var0.getStartForFeature(SectionPos.of(var13.getPos(), 0), this, var13);
                            if (var14 != null && var14.isValid()) {
                                if (param4 && var14.canBeReferenced()) {
                                    var14.addReference();
                                    return var14.getLocatePos();
                                }

                                if (!param4) {
                                    return var14.getLocatePos();
                                }
                            }

                            if (var4 == 0) {
                                break;
                            }
                        }
                    }

                    if (var4 == 0) {
                        break;
                    }
                }
            }

            return null;
        }
    }

    protected int getSpacing(ChunkGeneratorSettings param0) {
        return 1;
    }

    protected int getSeparation(ChunkGeneratorSettings param0) {
        return 0;
    }

    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return 0;
    }

    protected boolean linearSeparation() {
        return true;
    }

    public final ChunkPos getPotentialFeatureChunk(ChunkGeneratorSettings param0, long param1, WorldgenRandom param2, int param3, int param4) {
        int var0 = this.getSpacing(param0);
        int var1 = this.getSeparation(param0);
        int var2 = Math.floorDiv(param3, var0);
        int var3 = Math.floorDiv(param4, var0);
        param2.setLargeFeatureWithSalt(param1, var2, var3, this.getRandomSalt(param0));
        int var4;
        int var5;
        if (this.linearSeparation()) {
            var4 = param2.nextInt(var0 - var1);
            var5 = param2.nextInt(var0 - var1);
        } else {
            var4 = (param2.nextInt(var0 - var1) + param2.nextInt(var0 - var1)) / 2;
            var5 = (param2.nextInt(var0 - var1) + param2.nextInt(var0 - var1)) / 2;
        }

        return new ChunkPos(var2 * var0 + var4, var3 * var0 + var5);
    }

    public boolean featureChunk(BiomeManager param0, ChunkGenerator param1, long param2, WorldgenRandom param3, int param4, int param5, Biome param6) {
        ChunkPos var0 = this.getPotentialFeatureChunk(param1.getSettings(), param2, param3, param4, param5);
        return param4 == var0.x
            && param5 == var0.z
            && param1.isBiomeValidStartForStructure(param6, this)
            && this.isFeatureChunk(param0, param1, param2, param3, param4, param5, param6, var0);
    }

    protected boolean isFeatureChunk(
        BiomeManager param0, ChunkGenerator param1, long param2, WorldgenRandom param3, int param4, int param5, Biome param6, ChunkPos param7
    ) {
        return true;
    }

    public abstract StructureFeature.StructureStartFactory getStartFactory();

    public abstract String getFeatureName();

    public abstract int getLookupRange();

    public interface StructureStartFactory {
        StructureStart create(StructureFeature<?> var1, int var2, int var3, BoundingBox var4, int var5, long var6);
    }
}
