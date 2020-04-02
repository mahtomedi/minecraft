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
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostFeature extends RandomScatteredFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> OUTPOST_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

    public PillagerOutpostFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
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
    public boolean isFeatureChunk(BiomeManager param0, ChunkGenerator<?> param1, Random param2, int param3, int param4, Biome param5) {
        ChunkPos var0 = this.getPotentialFeatureChunkFromLocationWithOffset(param1, param2, param3, param4, 0, 0);
        if (param3 == var0.x && param4 == var0.z) {
            int var1 = param3 >> 4;
            int var2 = param4 >> 4;
            param2.setSeed((long)(var1 ^ var2 << 4) ^ param1.getSeed());
            param2.nextInt();
            if (param2.nextInt(5) != 0) {
                return false;
            }

            if (param1.isBiomeValidStartForStructure(param5, this)) {
                for(int var3 = param3 - 10; var3 <= param3 + 10; ++var3) {
                    for(int var4 = param4 - 10; var4 <= param4 + 10; ++var4) {
                        if (Feature.VILLAGE
                            .isFeatureChunk(param0, param1, param2, var3, var4, param0.getBiome(new BlockPos((var3 << 4) + 9, 0, (var4 << 4) + 9)))) {
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
        public FeatureStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator<?> param0, StructureManager param1, int param2, int param3, Biome param4) {
            BlockPos var0 = new BlockPos(param2 * 16, 90, param3 * 16);
            PillagerOutpostPieces.addPieces(param0, param1, var0, this.pieces, this.random);
            this.calculateBoundingBox();
        }
    }
}
