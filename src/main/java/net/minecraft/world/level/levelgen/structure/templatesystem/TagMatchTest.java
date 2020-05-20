package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TagMatchTest extends RuleTest {
    public static final Codec<TagMatchTest> CODEC = Tag.codec(BlockTags::getAllTags).fieldOf("tag").xmap(TagMatchTest::new, param0 -> param0.tag).codec();
    private final Tag<Block> tag;

    public TagMatchTest(Tag<Block> param0) {
        this.tag = param0;
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return param0.is(this.tag);
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.TAG_TEST;
    }
}
