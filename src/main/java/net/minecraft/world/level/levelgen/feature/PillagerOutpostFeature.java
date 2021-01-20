package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

public class PillagerOutpostFeature extends JigsawFeature {
    private static final List<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = ImmutableList.of(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1));

    public PillagerOutpostFeature(Codec<JigsawConfiguration> param0) {
        super(param0, 0, true, true);
    }

    @Override
    public List<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
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
        JigsawConfiguration param8
    ) {
        int var0 = SectionPos.blockToSectionCoord(param4);
        int var1 = SectionPos.blockToSectionCoord(param5);
        param3.setSeed((long)(var0 ^ var1 << 4) ^ param2);
        param3.nextInt();
        if (param3.nextInt(5) != 0) {
            return false;
        } else {
            return !this.isNearVillage(param0, param2, param3, param4, param5);
        }
    }

    private boolean isNearVillage(ChunkGenerator param0, long param1, WorldgenRandom param2, int param3, int param4) {
        StructureFeatureConfiguration var0 = param0.getSettings().getConfig(StructureFeature.VILLAGE);
        if (var0 == null) {
            return false;
        } else {
            for(int var1 = param3 - 10; var1 <= param3 + 10; ++var1) {
                for(int var2 = param4 - 10; var2 <= param4 + 10; ++var2) {
                    ChunkPos var3 = StructureFeature.VILLAGE.getPotentialFeatureChunk(var0, param1, param2, var1, var2);
                    if (var1 == var3.x && var2 == var3.z) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}
