package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class EmptyPoolElement extends StructurePoolElement {
    public static final Codec<EmptyPoolElement> CODEC = Codec.unit(() -> EmptyPoolElement.INSTANCE);
    public static final EmptyPoolElement INSTANCE = new EmptyPoolElement();

    private EmptyPoolElement() {
        super(StructureTemplatePool.Projection.TERRAIN_MATCHING);
    }

    @Override
    public Vec3i getSize(StructureTemplateManager param0, Rotation param1) {
        return Vec3i.ZERO;
    }

    @Override
    public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
        StructureTemplateManager param0, BlockPos param1, Rotation param2, RandomSource param3
    ) {
        return Collections.emptyList();
    }

    @Override
    public BoundingBox getBoundingBox(StructureTemplateManager param0, BlockPos param1, Rotation param2) {
        throw new IllegalStateException("Invalid call to EmtyPoolElement.getBoundingBox, filter me!");
    }

    @Override
    public boolean place(
        StructureTemplateManager param0,
        WorldGenLevel param1,
        StructureManager param2,
        ChunkGenerator param3,
        BlockPos param4,
        BlockPos param5,
        Rotation param6,
        BoundingBox param7,
        RandomSource param8,
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
