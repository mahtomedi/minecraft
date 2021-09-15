package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class PillagerOutpostFeature extends JigsawFeature {
    private static final WeightedRandomList<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1)
    );

    public PillagerOutpostFeature(Codec<JigsawConfiguration> param0) {
        super(param0, 0, true, true);
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return OUTPOST_ENEMIES;
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        ChunkPos param5,
        JigsawConfiguration param6,
        LevelHeightAccessor param7
    ) {
        int var0 = param4.x >> 4;
        int var1 = param4.z >> 4;
        param3.setSeed((long)(var0 ^ var1 << 4) ^ param2);
        param3.nextInt();
        if (param3.nextInt(5) != 0) {
            return false;
        } else {
            return !this.isNearVillage(param0, param2, param3, param4);
        }
    }

    private boolean isNearVillage(ChunkGenerator param0, long param1, WorldgenRandom param2, ChunkPos param3) {
        StructureFeatureConfiguration var0 = param0.getSettings().getConfig(StructureFeature.VILLAGE);
        if (var0 == null) {
            return false;
        } else {
            int var1 = param3.x;
            int var2 = param3.z;

            for(int var3 = var1 - 10; var3 <= var1 + 10; ++var3) {
                for(int var4 = var2 - 10; var4 <= var2 + 10; ++var4) {
                    ChunkPos var5 = StructureFeature.VILLAGE.getPotentialFeatureChunk(var0, param1, param2, var3, var4);
                    if (var3 == var5.x && var4 == var5.z) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
