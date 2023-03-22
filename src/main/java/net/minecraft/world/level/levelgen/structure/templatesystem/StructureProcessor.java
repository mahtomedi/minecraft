package net.minecraft.world.level.levelgen.structure.templatesystem;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;

public abstract class StructureProcessor {
    @Nullable
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader param0,
        BlockPos param1,
        BlockPos param2,
        StructureTemplate.StructureBlockInfo param3,
        StructureTemplate.StructureBlockInfo param4,
        StructurePlaceSettings param5
    ) {
        return param4;
    }

    protected abstract StructureProcessorType<?> getType();

    public List<StructureTemplate.StructureBlockInfo> finalizeProcessing(
        LevelAccessor param0,
        BlockPos param1,
        BlockPos param2,
        List<StructureTemplate.StructureBlockInfo> param3,
        List<StructureTemplate.StructureBlockInfo> param4,
        StructurePlaceSettings param5
    ) {
        return param4;
    }
}
