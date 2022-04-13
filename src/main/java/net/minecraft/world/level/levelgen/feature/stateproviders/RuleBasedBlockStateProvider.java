package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

public record RuleBasedBlockStateProvider(BlockStateProvider fallback, List<RuleBasedBlockStateProvider.Rule> rules) {
    public static final Codec<RuleBasedBlockStateProvider> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockStateProvider.CODEC.fieldOf("fallback").forGetter(RuleBasedBlockStateProvider::fallback),
                    RuleBasedBlockStateProvider.Rule.CODEC.listOf().fieldOf("rules").forGetter(RuleBasedBlockStateProvider::rules)
                )
                .apply(param0, RuleBasedBlockStateProvider::new)
    );

    public static RuleBasedBlockStateProvider simple(BlockStateProvider param0) {
        return new RuleBasedBlockStateProvider(param0, List.of());
    }

    public static RuleBasedBlockStateProvider simple(Block param0) {
        return simple(BlockStateProvider.simple(param0));
    }

    public BlockState getState(WorldGenLevel param0, RandomSource param1, BlockPos param2) {
        for(RuleBasedBlockStateProvider.Rule var0 : this.rules) {
            if (var0.ifTrue().test(param0, param2)) {
                return var0.then().getState(param1, param2);
            }
        }

        return this.fallback.getState(param1, param2);
    }

    public static record Rule(BlockPredicate ifTrue, BlockStateProvider then) {
        public static final Codec<RuleBasedBlockStateProvider.Rule> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        BlockPredicate.CODEC.fieldOf("if_true").forGetter(RuleBasedBlockStateProvider.Rule::ifTrue),
                        BlockStateProvider.CODEC.fieldOf("then").forGetter(RuleBasedBlockStateProvider.Rule::then)
                    )
                    .apply(param0, RuleBasedBlockStateProvider.Rule::new)
        );
    }
}
