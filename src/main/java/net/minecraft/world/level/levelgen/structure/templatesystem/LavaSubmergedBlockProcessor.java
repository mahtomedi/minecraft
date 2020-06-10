package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class LavaSubmergedBlockProcessor extends StructureProcessor {
    public static final Codec<LavaSubmergedBlockProcessor> CODEC = Codec.unit(() -> LavaSubmergedBlockProcessor.INSTANCE);
    public static final LavaSubmergedBlockProcessor INSTANCE = new LavaSubmergedBlockProcessor();

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader param0,
        BlockPos param1,
        BlockPos param2,
        StructureTemplate.StructureBlockInfo param3,
        StructureTemplate.StructureBlockInfo param4,
        StructurePlaceSettings param5
    ) {
        BlockPos var0 = param4.pos;
        boolean var1 = param0.getBlockState(var0).is(Blocks.LAVA);
        return var1 && !Block.isShapeFullBlock(param4.state.getShape(param0, var0))
            ? new StructureTemplate.StructureBlockInfo(var0, Blocks.LAVA.defaultBlockState(), param4.nbt)
            : param4;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.LAVA_SUBMERGED_BLOCK;
    }
}
