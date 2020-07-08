package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class OreConfiguration implements FeatureConfiguration {
    public static final Codec<OreConfiguration> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    RuleTest.CODEC.fieldOf("target").forGetter(param0x -> param0x.target),
                    BlockState.CODEC.fieldOf("state").forGetter(param0x -> param0x.state),
                    Codec.intRange(0, 64).fieldOf("size").forGetter(param0x -> param0x.size)
                )
                .apply(param0, OreConfiguration::new)
    );
    public final RuleTest target;
    public final int size;
    public final BlockState state;

    public OreConfiguration(RuleTest param0, BlockState param1, int param2) {
        this.size = param2;
        this.state = param1;
        this.target = param0;
    }

    public static final class Predicates {
        public static final RuleTest NATURAL_STONE = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);
        public static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);
        public static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
    }
}
