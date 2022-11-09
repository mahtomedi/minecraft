package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

class MatchingBlocksPredicate extends StateTestingPredicate {
    private final HolderSet<Block> blocks;
    public static final Codec<MatchingBlocksPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> stateTestingCodec(param0)
                .and(RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(param0x -> param0x.blocks))
                .apply(param0, MatchingBlocksPredicate::new)
    );

    public MatchingBlocksPredicate(Vec3i param0, HolderSet<Block> param1) {
        super(param0);
        this.blocks = param1;
    }

    @Override
    protected boolean test(BlockState param0) {
        return param0.is(this.blocks);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCKS;
    }
}
