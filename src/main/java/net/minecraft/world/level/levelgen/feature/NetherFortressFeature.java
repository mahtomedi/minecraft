package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final WeightedRandomList<MobSpawnSettings.SpawnerData> FORTRESS_ENEMIES = WeightedRandomList.create(
        new MobSpawnSettings.SpawnerData(EntityType.BLAZE, 10, 2, 3),
        new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
        new MobSpawnSettings.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
        new MobSpawnSettings.SpawnerData(EntityType.SKELETON, 2, 5, 5),
        new MobSpawnSettings.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
    );

    public NetherFortressFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0);
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0,
        BiomeSource param1,
        long param2,
        WorldgenRandom param3,
        ChunkPos param4,
        ChunkPos param5,
        NoneFeatureConfiguration param6,
        LevelHeightAccessor param7
    ) {
        return param3.nextInt(5) < 2;
    }

    @Override
    public StructureFeature.StructureStartFactory<NoneFeatureConfiguration> getStartFactory() {
        return NetherFortressFeature.NetherBridgeStart::new;
    }

    @Override
    public WeightedRandomList<MobSpawnSettings.SpawnerData> getSpecialEnemies() {
        return FORTRESS_ENEMIES;
    }

    public static class NetherBridgeStart extends StructureStart<NoneFeatureConfiguration> {
        public NetherBridgeStart(StructureFeature<NoneFeatureConfiguration> param0, ChunkPos param1, int param2, long param3) {
            super(param0, param1, param2, param3);
        }

        public void generatePieces(
            RegistryAccess param0,
            ChunkGenerator param1,
            StructureManager param2,
            ChunkPos param3,
            NoneFeatureConfiguration param4,
            LevelHeightAccessor param5,
            Predicate<Biome> param6
        ) {
            if (param6.test(
                param1.getNoiseBiome(QuartPos.fromBlock(param3.getMiddleBlockX()), QuartPos.fromBlock(64), QuartPos.fromBlock(param3.getMiddleBlockZ()))
            )) {
                NetherBridgePieces.StartPiece var0 = new NetherBridgePieces.StartPiece(this.random, param3.getBlockX(2), param3.getBlockZ(2));
                this.addPiece(var0);
                var0.addChildren(var0, this, this.random);
                List<StructurePiece> var1 = var0.pendingChildren;

                while(!var1.isEmpty()) {
                    int var2 = this.random.nextInt(var1.size());
                    StructurePiece var3 = var1.remove(var2);
                    var3.addChildren(var0, this, this.random);
                }

                this.moveInsideHeights(this.random, 48, 70);
            }
        }
    }
}
