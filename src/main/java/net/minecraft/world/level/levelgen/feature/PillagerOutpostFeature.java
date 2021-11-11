package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class PillagerOutpostFeature extends JigsawFeature {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1)
    );

    public PillagerOutpostFeature(Codec<JigsawConfiguration> param0) {
        super(param0, 0, true, true);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0, BiomeSource param1, long param2, ChunkPos param3, JigsawConfiguration param4, LevelHeightAccessor param5
    ) {
        int var0 = param3.x >> 4;
        int var1 = param3.z >> 4;
        WorldgenRandom var2 = new WorldgenRandom(new LegacyRandomSource(0L));
        var2.setSeed((long)(var0 ^ var1 << 4) ^ param2);
        var2.nextInt();
        if (var2.nextInt(5) != 0) {
            return false;
        } else {
            return !this.isNearVillage(param0, param2, param3);
        }
    }

    private boolean isNearVillage(ChunkGenerator param0, long param1, ChunkPos param2) {
        StructureFeatureConfiguration var0 = param0.getSettings().getConfig(StructureFeature.VILLAGE);
        if (var0 == null) {
            return false;
        } else {
            int var1 = param2.x;
            int var2 = param2.z;

            for(int var3 = var1 - 10; var3 <= var1 + 10; ++var3) {
                for(int var4 = var2 - 10; var4 <= var2 + 10; ++var4) {
                    ChunkPos var5 = StructureFeature.VILLAGE.getPotentialFeatureChunk(var0, param1, var3, var4);
                    if (var3 == var5.x && var4 == var5.z) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
