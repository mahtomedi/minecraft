package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class EndCityStructure extends Structure {
    public static final Codec<EndCityStructure> CODEC = RecordCodecBuilder.create(param0 -> codec(param0).apply(param0, EndCityStructure::new));
    private static final int RANDOM_SALT = 10387313;

    public EndCityStructure(HolderSet<Biome> param0, Map<MobCategory, StructureSpawnOverride> param1, GenerationStep.Decoration param2, boolean param3) {
        super(param0, param1, param2, param3);
    }

    private static int getYPositionForFeature(ChunkPos param0, ChunkGenerator param1, LevelHeightAccessor param2, RandomState param3) {
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
        int var6 = param1.getFirstOccupiedHeight(var4, var5, Heightmap.Types.WORLD_SURFACE_WG, param2, param3);
        int var7 = param1.getFirstOccupiedHeight(var4, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG, param2, param3);
        int var8 = param1.getFirstOccupiedHeight(var4 + var2, var5, Heightmap.Types.WORLD_SURFACE_WG, param2, param3);
        int var9 = param1.getFirstOccupiedHeight(var4 + var2, var5 + var3, Heightmap.Types.WORLD_SURFACE_WG, param2, param3);
        return Math.min(Math.min(var6, var7), Math.min(var8, var9));
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        int var0 = getYPositionForFeature(param0.chunkPos(), param0.chunkGenerator(), param0.heightAccessor(), param0.randomState());
        if (var0 < 60) {
            return Optional.empty();
        } else {
            BlockPos var1 = param0.chunkPos().getMiddleBlockPosition(var0);
            return Optional.of(new Structure.GenerationStub(var1, param2 -> this.generatePieces(param2, var1, param0)));
        }
    }

    private void generatePieces(StructurePiecesBuilder param0, BlockPos param1, Structure.GenerationContext param2) {
        Rotation var0 = Rotation.getRandom(param2.random());
        List<StructurePiece> var1 = Lists.newArrayList();
        EndCityPieces.startHouseTower(param2.structureTemplateManager(), param1, var0, var1, param2.random());
        var1.forEach(param0::addPiece);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.END_CITY;
    }
}
