package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class IglooStructure extends Structure {
    public static final Codec<IglooStructure> CODEC = RecordCodecBuilder.create(param0 -> codec(param0).apply(param0, IglooStructure::new));

    public IglooStructure(HolderSet<Biome> param0, Map<MobCategory, StructureSpawnOverride> param1, GenerationStep.Decoration param2, boolean param3) {
        super(param0, param1, param2, param3);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        return onTopOfChunkCenter(param0, Heightmap.Types.WORLD_SURFACE_WG, param1 -> this.generatePieces(param1, param0));
    }

    private void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        ChunkPos var0 = param1.chunkPos();
        WorldgenRandom var1 = param1.random();
        BlockPos var2 = new BlockPos(var0.getMinBlockX(), 90, var0.getMinBlockZ());
        Rotation var3 = Rotation.getRandom(var1);
        IglooPieces.addPieces(param1.structureTemplateManager(), var2, var3, param0, var1);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.IGLOO;
    }
}
