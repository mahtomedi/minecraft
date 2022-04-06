package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TagMatchTest extends RuleTest {
    public static final Codec<TagMatchTest> CODEC = TagKey.codec(Registry.BLOCK_REGISTRY).fieldOf("tag").xmap(TagMatchTest::new, param0 -> param0.tag).codec();
    private final TagKey<Block> tag;

    public TagMatchTest(TagKey<Block> param0) {
        this.tag = param0;
    }

    @Override
    public boolean test(BlockState param0, RandomSource param1) {
        return param0.is(this.tag);
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.TAG_TEST;
    }
}
