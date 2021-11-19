package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MatchingBlockTagPredicate extends StateTestingPredicate {
    final Tag<Block> tag;
    public static final Codec<MatchingBlockTagPredicate> CODEC = RecordCodecBuilder.create(
        param0 -> stateTestingCodec(param0)
                .and(Tag.codec(() -> SerializationTags.getInstance().getOrEmpty(Registry.BLOCK_REGISTRY)).fieldOf("tag").forGetter(param0x -> param0x.tag))
                .apply(param0, MatchingBlockTagPredicate::new)
    );

    protected MatchingBlockTagPredicate(Vec3i param0, Tag<Block> param1) {
        super(param0);
        this.tag = param1;
    }

    @Override
    protected boolean test(BlockState param0) {
        return param0.is(this.tag);
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.MATCHING_BLOCK_TAG;
    }
}
