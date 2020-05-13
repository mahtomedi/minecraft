package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.function.Function;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.NetherBridgePieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class NetherFortressFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final List<Biome.SpawnerData> FORTRESS_ENEMIES = Lists.newArrayList(
        new Biome.SpawnerData(EntityType.BLAZE, 10, 2, 3),
        new Biome.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 5, 4, 4),
        new Biome.SpawnerData(EntityType.WITHER_SKELETON, 8, 5, 5),
        new Biome.SpawnerData(EntityType.SKELETON, 2, 5, 5),
        new Biome.SpawnerData(EntityType.MAGMA_CUBE, 3, 4, 4)
    );

    public NetherFortressFeature(Function<Dynamic<?>, ? extends NoneFeatureConfiguration> param0) {
        super(param0);
    }

    @Override
    protected int getSpacing(ChunkGeneratorSettings param0) {
        return param0.getRareNetherStructureSpacing();
    }

    @Override
    protected int getSeparation(ChunkGeneratorSettings param0) {
        return param0.getRareNetherStructureSeparation();
    }

    @Override
    protected int getRandomSalt(ChunkGeneratorSettings param0) {
        return param0.getRareNetherStructureSalt();
    }

    @Override
    protected boolean isFeatureChunk(
        BiomeManager param0, ChunkGenerator param1, long param2, WorldgenRandom param3, int param4, int param5, Biome param6, ChunkPos param7
    ) {
        return param3.nextInt(6) < 2;
    }

    @Override
    public StructureFeature.StructureStartFactory getStartFactory() {
        return NetherFortressFeature.NetherBridgeStart::new;
    }

    @Override
    public String getFeatureName() {
        return "Fortress";
    }

    @Override
    public int getLookupRange() {
        return 8;
    }

    @Override
    public List<Biome.SpawnerData> getSpecialEnemies() {
        return FORTRESS_ENEMIES;
    }

    public static class NetherBridgeStart extends StructureStart {
        public NetherBridgeStart(StructureFeature<?> param0, int param1, int param2, BoundingBox param3, int param4, long param5) {
            super(param0, param1, param2, param3, param4, param5);
        }

        @Override
        public void generatePieces(ChunkGenerator param0, StructureManager param1, int param2, int param3, Biome param4) {
            NetherBridgePieces.StartPiece var0 = new NetherBridgePieces.StartPiece(this.random, (param2 << 4) + 2, (param3 << 4) + 2);
            this.pieces.add(var0);
            var0.addChildren(var0, this.pieces, this.random);
            List<StructurePiece> var1 = var0.pendingChildren;

            while(!var1.isEmpty()) {
                int var2 = this.random.nextInt(var1.size());
                StructurePiece var3 = var1.remove(var2);
                var3.addChildren(var0, this.pieces, this.random);
            }

            this.calculateBoundingBox();
            this.moveInsideHeights(this.random, 48, 70);
        }
    }
}
