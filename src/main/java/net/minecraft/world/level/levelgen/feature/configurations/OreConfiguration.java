package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class OreConfiguration implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("targets").forGetter(param0x -> param0x.targetStates),
                    Codec.intRange(0, 64).fieldOf("size").forGetter(param0x -> param0x.size),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("discard_chance_on_air_exposure").forGetter(param0x -> param0x.discardChanceOnAirExposure)
                )
                .apply(param0, OreConfiguration::new)
    );
    public final List<OreConfiguration.TargetBlockState> targetStates;
    public final int size;
    public final float discardChanceOnAirExposure;

    public OreConfiguration(List<OreConfiguration.TargetBlockState> param0, int param1, float param2) {
        this.size = param1;
        this.targetStates = param0;
        this.discardChanceOnAirExposure = param2;
    }

    public OreConfiguration(List<OreConfiguration.TargetBlockState> param0, int param1) {
        this(param0, param1, 0.0F);
    }

    public OreConfiguration(RuleTest param0, BlockState param1, int param2, float param3) {
        this(ImmutableList.of(new OreConfiguration.TargetBlockState(param0, param1)), param2, param3);
    }

    public OreConfiguration(RuleTest param0, BlockState param1, int param2) {
        this(ImmutableList.of(new OreConfiguration.TargetBlockState(param0, param1)), param2, 0.0F);
    }

    public static OreConfiguration.TargetBlockState target(RuleTest param0, BlockState param1) {
        return new OreConfiguration.TargetBlockState(param0, param1);
    }

    public static final class Predicates {
        public static final RuleTest NATURAL_STONE = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);
        public static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        public static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        public static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);
        public static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
    }

    public static class TargetBlockState {
        public static final Codec<OreConfiguration.TargetBlockState> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        RuleTest.CODEC.fieldOf("target").forGetter(param0x -> param0x.target),
                        BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state)
                    )
                    .apply(param0, OreConfiguration.TargetBlockState::new)
        );
        public final RuleTest target;
        public final BlockState state;

        private TargetBlockState(RuleTest param0, BlockState param1) {
            this.target = param0;
            this.state = param1;
        }
    }
}
