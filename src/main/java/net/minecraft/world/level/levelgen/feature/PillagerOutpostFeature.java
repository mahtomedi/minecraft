package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class PillagerOutpostFeature extends JigsawFeature {
    public static final WeightedRandomList<MobSpawnSettings.SpawnerData> OUTPOST_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1)
    );

    public PillagerOutpostFeature(Codec<JigsawConfiguration> param0) {
        super(param0, 0, true, true, PillagerOutpostFeature::checkLocation);
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<JigsawConfiguration> param0x) {
        int var0 = param0x.chunkPos().x >> 4;
        int var1 = param0x.chunkPos().z >> 4;
        WorldgenRandom var2 = new WorldgenRandom(new LegacyRandomSource(0L));
        var2.setSeed((long)(var0 ^ var1 << 4) ^ param0x.seed());
        var2.nextInt();
        if (var2.nextInt(5) != 0) {
            return false;
        } else {
            return !isNearVillage(param0x.chunkGenerator(), param0x.seed(), param0x.chunkPos());
        }
    }

    private static boolean isNearVillage(ChunkGenerator param0, long param1, ChunkPos param2) {
        StructurePlacement var0 = param0.getSettings().getConfig(StructureFeature.VILLAGE);
        if (var0 != null) {
            int var1 = param2.x;
            int var2 = param2.z;

            for(int var3 = var1 - 10; var3 <= var1 + 10; ++var3) {
                for(int var4 = var2 - 10; var4 <= var2 + 10; ++var4) {
                    if (var0.isFeatureChunk(param0, var3, var4)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
