package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class EmptyPoolElement extends StructurePoolElement {
    public static final Codec<EmptyPoolElement> CODEC = Codec.unit(() -> EmptyPoolElement.INSTANCE);
    public static final EmptyPoolElement INSTANCE = new EmptyPoolElement();

    private EmptyPoolElement() {
        super(StructureTemplatePool.Projection.TERRAIN_MATCHING);
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(StructureManager param0, BlockPos param1, Rotation param2, Random param3) {
        return Collections.emptyList();
    }

    @Override
    public BoundingBox getBoundingBox(StructureManager param0, BlockPos param1, Rotation param2) {
        return BoundingBox.getUnknownBox();
    }

    @Override
    public boolean place(
        StructureManager param0,
        WorldGenLevel param1,
        StructureFeatureManager param2,
        ChunkGenerator param3,
        BlockPos param4,
        BlockPos param5,
        Rotation param6,
        BoundingBox param7,
        Random param8,
        boolean param9
    ) {
        return true;
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return StructurePoolElementType.EMPTY;
    }

    @Override
    public String toString() {
        return "Empty";
    }
}
