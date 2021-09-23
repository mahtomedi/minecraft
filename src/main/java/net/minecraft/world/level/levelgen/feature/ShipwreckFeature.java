package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.structure.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckFeature extends StructureFeature<ShipwreckConfiguration> {
    public ShipwreckFeature(Codec<ShipwreckConfiguration> param0) {
        super(param0, ShipwreckFeature::generatePieces);
    }

    private static void generatePieces(StructurePiecesBuilder param0x, ShipwreckConfiguration param1, PieceGenerator.Context param2) {
        Heightmap.Types var0 = param1.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
        if (param2.validBiomeOnTop(var0)) {
            Rotation var1 = Rotation.getRandom(param2.random());
            BlockPos var2 = new BlockPos(param2.chunkPos().getMinBlockX(), 90, param2.chunkPos().getMinBlockZ());
            ShipwreckPieces.addPieces(param2.structureManager(), var2, var1, param0x, param2.random(), param1);
        }
    }
}
