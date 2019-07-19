package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class TagMatchTest extends RuleTest {
    private final Tag<Block> tag;

    public TagMatchTest(Tag<Block> param0) {
        this.tag = param0;
    }

    public <T> TagMatchTest(Dynamic<T> param0) {
        this(BlockTags.getAllTags().getTag(new ResourceLocation(param0.get("tag").asString(""))));
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return param0.is(this.tag);
    }

    @Override
    protected RuleTestType getType() {
        return RuleTestType.TAG_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createMap(ImmutableMap.of(param0.createString("tag"), param0.createString(this.tag.getId().toString()))));
    }
}
