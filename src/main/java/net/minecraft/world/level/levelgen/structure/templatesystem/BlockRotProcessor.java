package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

public class BlockRotProcessor extends StructureProcessor {
    public static final Codec<BlockRotProcessor> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    TagKey.codec(Registry.BLOCK_REGISTRY).optionalFieldOf("rottable_blocks").forGetter(param0x -> param0x.rottableBlocks),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("integrity").forGetter(param0x -> param0x.integrity)
                )
                .apply(param0, BlockRotProcessor::new)
    );
    private Optional<TagKey<Block>> rottableBlocks;
    private final float integrity;

    public BlockRotProcessor(TagKey<Block> param0, float param1) {
        this(Optional.of(param0), param1);
    }

    public BlockRotProcessor(float param0) {
        this(Optional.empty(), param0);
    }

    private BlockRotProcessor(Optional<TagKey<Block>> param0, float param1) {
        this.integrity = param1;
        this.rottableBlocks = param0;
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
        return (!this.rottableBlocks.isPresent() || param3.state.is(this.rottableBlocks.get())) && !(var0.nextFloat() <= this.integrity) ? null : param4;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
