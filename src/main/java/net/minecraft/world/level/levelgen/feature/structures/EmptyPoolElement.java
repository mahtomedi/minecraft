package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class EmptyPoolElement extends StructurePoolElement {
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
        StructureManager param0, LevelAccessor param1, ChunkGenerator<?> param2, BlockPos param3, Rotation param4, BoundingBox param5, Random param6
    ) {
        return true;
    }

    @Override
    public StructurePoolElementType getType() {
        return StructurePoolElementType.EMPTY;
    }

    @Override
    public <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }

    @Override
    public String toString() {
        return "Empty";
    }
}
