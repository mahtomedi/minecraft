package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class EndCityFeature extends StructureFeature<NoneFeatureConfiguration> {
    private static final int RANDOM_SALT = 10387313;

    public EndCityFeature(Codec<NoneFeatureConfiguration> param0) {
        super(param0, EndCityFeature::generatePieces);
    }

    @Override
    protected boolean linearSeparation() {
        return false;
    }

    protected boolean isFeatureChunk(
        ChunkGenerator param0, BiomeSource param1, long param2, ChunkPos param3, NoneFeatureConfiguration param4, LevelHeightAccessor param5
    ) {
        return getYPositionForFeature(param3, param0, param5) >= 60;
    }

    private static int getYPositionForFeature(ChunkPos param0, ChunkGenerator param1, LevelHeightAccessor param2) {
        Random var0 = new Random((long)(param0.x + param0.z * 10387313));
        Rotation var1 = Rotation.getRandom(var0);
        int var2 = 5;
        int var3 = 5;
        if (var1 == Rotation.CLOCKWISE_90) {
            var2 = -5;
        } else if (var1 == Rotation.CLOCKWISE_180) {
            var2 = -5;
            var3 = -5;
        } else if (var1 == Rotation.COUNTERCLOCKWISE_90) {
            var3 = -5;
        }

        int var4 = param0.getBlockX(7);
        int var5 = param0.getBlockZ(7);
        int var6 = param1.getFirstOccupiedHeight(var4, var5, Heightmap.Types.WORLD_SURFACE_WG, param2);
        int var7 = param1.getFirstOccupiedHeight(var4, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG, param2);
        int var8 = param1.getFirstOccupiedHeight(var4 + var2, var5, Heightmap.Types.WORLD_SURFACE_WG, param2);
        int var9 = param1.getFirstOccupiedHeight(var4 + var2, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG, param2);
        return Math.min(Math.min(var6, var7), Math.min(var8, var9));
    }

    private static void generatePieces(StructurePiecesBuilder param0x, NoneFeatureConfiguration param1, PieceGenerator.Context param2) {
        Rotation var0 = Rotation.getRandom(param2.random());
        int var1 = getYPositionForFeature(param2.chunkPos(), param2.chunkGenerator(), param2.heightAccessor());
        if (var1 >= 60) {
            BlockPos var2 = param2.chunkPos().getMiddleBlockPosition(var1);
            if (param2.validBiome()
                .test(param2.chunkGenerator().getNoiseBiome(QuartPos.fromBlock(var2.getX()), QuartPos.fromBlock(var2.getY()), QuartPos.fromBlock(var2.getZ())))
                )
             {
                List<StructurePiece> var3 = Lists.newArrayList();
                EndCityPieces.startHouseTower(param2.structureManager(), var2, var0, var3, param2.random());
                var3.forEach(param0x::addPiece);
            }
        }
    }
}
