package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

public class ShipwreckStructure extends Structure {
    public static final Codec<ShipwreckStructure> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(settingsCodec(param0), Codec.BOOL.fieldOf("is_beached").forGetter(param0x -> param0x.isBeached))
                .apply(param0, ShipwreckStructure::new)
    );
    public final boolean isBeached;

    public ShipwreckStructure(Structure.StructureSettings param0, boolean param1) {
        super(param0);
        this.isBeached = param1;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext param0) {
        Heightmap.Types var0 = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
        return onTopOfChunkCenter(param0, var0, param1 -> this.generatePieces(param1, param0));
    }

    private void generatePieces(StructurePiecesBuilder param0, Structure.GenerationContext param1) {
        Rotation var0 = Rotation.getRandom(param1.random());
        BlockPos var1 = new BlockPos(param1.chunkPos().getMinBlockX(), 90, param1.chunkPos().getMinBlockZ());
        ShipwreckPieces.addPieces(param1.structureTemplateManager(), var1, var0, param0, param1.random(), this.isBeached);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.SHIPWRECK;
    }
}
