package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostFeature extends RandomScatteredFeature<PillagerOutpostConfiguration> {
    private static final List<Biome.SpawnerData> OUTPOST_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

    public PillagerOutpostFeature(Function<Dynamic<?>, ? extends PillagerOutpostConfiguration> param0) {
        super(param0);
    }

    @Override
    public String getFeatureName() {
        return "Pillager_Outpost";
    }

    @Override
    public int getLookupRange() {
        return 3;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return OUTPOST_ENEMIES;
    }

    @Override
    public boolean isFeatureChunk(ChunkGenerator<?> param0, Random param1, int param2, int param3) {
        ChunkPos var0 = this.getPotentialFeatureChunkFromLocationWithOffset(param0, param1, param2, param3, 0, 0);
        if (param2 == var0.x && param3 == var0.z) {
            int var1 = param2 >> 4;
            int var2 = param3 >> 4;
            param1.setSeed((long)(var1 ^ var2 << 4) ^ param0.getSeed());
            param1.nextInt();
            if (param1.nextInt(5) != 0) {
                return false;
            }

            Biome var3 = param0.getBiomeSource().getBiome(new BlockPos((param2 << 4) + 9, 0, (param3 << 4) + 9));
            if (param0.isBiomeValidStartForStructure(var3, Feature.PILLAGER_OUTPOST)) {
                for(int var4 = param2 - 10; var4 <= param2 + 10; ++var4) {
                    for(int var5 = param3 - 10; var5 <= param3 + 10; ++var5) {
                        if (Feature.VILLAGE.isFeatureChunk(param0, param1, var4, var5)) {
                            return false;
                        }
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return PillagerOutpostFeature.FeatureStart::new;
    }

    @Override
    protected int getRandomSalt() {
        return 165745296;
    }

    public static class FeatureStart extends BeardedStructureStart {
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, Biome param3, BoundingBox param4, int param5, long param6) {
            super(param0, param1, param2, param3, param4, param5, param6);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            BlockPos var0 = new BlockPos(param2 * 16, 90, param3 * 16);
            PillagerOutpostPieces.addPieces(param0, param1, var0, this.pieces, this.random);
            this.calculateBoundingBox();
        }
    }
}
