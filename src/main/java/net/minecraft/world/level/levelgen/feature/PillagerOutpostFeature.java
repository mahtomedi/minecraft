package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BeardedStructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PillagerOutpostPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class PillagerOutpostFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> OUTPOST_ENEMIES = Lists.newArrayList(new Biome.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

    public PillagerOutpostFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return OUTPOST_ENEMIES;
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        int param4,
        int param5,
        Biome param6,
        ChunkPos param7,
        NoneFeatureConfiguration param8
    ) {
        int var0 = param4 >> 4;
        int var1 = param5 >> 4;
        param3.setSeed((long)(var0 ^ var1 << 4) ^ param2);
        param3.nextInt();
        if (param3.nextInt(5) != 0) {
            return false;
        } else {
            for(int var2 = param4 - 10; var2 <= param4 + 10; ++var2) {
                for(int var3 = param5 - 10; var3 <= param5 + 10; ++var3) {
                    ChunkPos var4 = StructureFeature.VILLAGE
                        .getPotentialFeatureChunk(param0.getSettings().getConfig(StructureFeature.VILLAGE), param2, param3, var2, var3);
                    if (var2 == var4.x && var3 == var4.z) {
                        return false;
                    }
                }
            }

            return true;
        }
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return PillagerOutpostFeature.FeatureStart::new;
    }

    public static class FeatureStart extends BeardedStructureStart<NoneFeatureConfiguration> {
        public FeatureStart(StructureFeature<NoneFeatureConfiguration> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4, NoneFeatureConfiguration param5) {
            BlockPos var0 = new BlockPos(param2 * 16, 0, param3 * 16);
            PillagerOutpostPieces.addPieces(param0, param1, var0, this.pieces, this.random);
            this.calculateBoundingBox();
        }
    }
}
