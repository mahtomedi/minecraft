package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;

public class BlockRotProcessor extends StructureProcessor {
    public static final Codec<BlockRotProcessor> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("rottable_blocks").forGetter(param0x -> param0x.rottableBlocks),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("integrity").forGetter(param0x -> param0x.integrity)
                )
                .apply(param0, BlockRotProcessor::new)
    );
    private final Optional<HolderSet<Block>> rottableBlocks;
    private final float integrity;

    public BlockRotProcessor(HolderSet<Block> param0, float param1) {
        this(Optional.of(param0), param1);
    }

    public BlockRotProcessor(float param0) {
        this(Optional.empty(), param0);
    }

    private BlockRotProcessor(Optional<HolderSet<Block>> param0, float param1) {
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
        RandomSource var0 = param5.getRandom(param4.pos);
        return (!this.rottableBlocks.isPresent() || param3.state.is(this.rottableBlocks.get())) && !(var0.nextFloat() <= this.integrity) ? null : param4;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return StructureProcessorType.BLOCK_ROT;
    }
}
