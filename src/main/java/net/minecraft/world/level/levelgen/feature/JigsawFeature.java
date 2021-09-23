package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.structures.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;

public class JigsawFeature extends NoiseAffectingStructureFeature<JigsawConfiguration> {
    public JigsawFeature(Codec<JigsawConfiguration> param0, int param1, boolean param2, boolean param3) {
        super(
            param0,
            (param3x, param4, param5) -> {
                BlockPos var0 = new BlockPos(param5.chunkPos().getMinBlockX(), param1, param5.chunkPos().getMinBlockZ());
                Pools.bootstrap();
                JigsawPlacement.addPieces(
                    param5.registryAccess(),
                    param4,
                    PoolElementStructurePiece::new,
                    param5.chunkGenerator(),
                    param5.structureManager(),
                    var0,
                    param3x,
                    param5.random(),
                    param2,
                    param3,
                    param5.heightAccessor(),
                    param5.validBiome()
                );
            }
        );
    }
}
