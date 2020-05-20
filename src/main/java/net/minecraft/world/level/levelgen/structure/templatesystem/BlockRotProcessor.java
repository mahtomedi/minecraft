package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;

public class BlockRotProcessor extends StructureProcessor {
    public static final Codec<BlockRotProcessor> CODEC = Codec.FLOAT
        .fieldOf("integrity")
        .withDefault(1.0F)
        .xmap(BlockRotProcessor::new, param0 -> param0.integrity)
        .codec();
    private final float integrity;

    public BlockRotProcessor(float param0) {
        this.integrity = param0;
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
        Random var0 = param5.getRandom(param4.pos);
        return !(this.integrity >= 1.0F) && !(var0.nextFloat() <= this.integrity) ? null : param4;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
