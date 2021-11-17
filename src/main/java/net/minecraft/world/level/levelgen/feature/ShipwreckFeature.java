package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckFeature extends StructureFeature<ShipwreckConfiguration> {
    public ShipwreckFeature(Codec<ShipwreckConfiguration> param0) {
        super(param0, PieceGeneratorSupplier.simple(ShipwreckFeature::checkLocation, ShipwreckFeature::generatePieces));
    }

    private static boolean checkLocation(PieceGeneratorSupplier.Context<ShipwreckConfiguration> param0x) {
        Heightmap.Types var0 = param0x.config().isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
        return param0x.validBiomeOnTop(var0);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, PieceGenerator.Context<ShipwreckConfiguration> param1) {
        Rotation var0 = Rotation.getRandom(param1.random());
        BlockPos var1 = new BlockPos(param1.chunkPos().getMinBlockX(), 90, param1.chunkPos().getMinBlockZ());
        ShipwreckPieces.addPieces(param1.structureManager(), var1, var0, param0x, param1.random(), param1.config());
    }
}
