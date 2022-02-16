package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.NoiseAffectingStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;

public class JigsawFeature extends NoiseAffectingStructureFeature<JigsawConfiguration> {
    public JigsawFeature(
        Codec<JigsawConfiguration> param0, int param1, boolean param2, boolean param3, Predicate<PieceGeneratorSupplier.Context<JigsawConfiguration>> param4
    ) {
        super(param0, param4x -> {
            if (!param4.test(param4x)) {
                return Optional.empty();
            } else {
                BlockPos var0 = new BlockPos(param4x.chunkPos().getMinBlockX(), param1, param4x.chunkPos().getMinBlockZ());
                Pools.bootstrap();
                return JigsawPlacement.addPieces(param4x, PoolElementStructurePiece::new, var0, param2, param3);
            }
        });
    }
}
