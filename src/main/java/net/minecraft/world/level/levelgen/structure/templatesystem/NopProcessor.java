package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public class NopProcessor extends StructureProcessor {
    public static final Codec<NopProcessor> CODEC = Codec.unit(() -> NopProcessor.INSTANCE);
    public static final NopProcessor INSTANCE = new NopProcessor();

    private NopProcessor() {
    }

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
        return param4;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.NOP;
    }
}
