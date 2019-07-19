package net.minecraft.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;

public interface RuleTestType extends Deserializer<RuleTest> {
    RuleTestType ALWAYS_TRUE_TEST = register("always_true", param0 -> AlwaysTrueTest.INSTANCE);
    RuleTestType BLOCK_TEST = register("block_match", BlockMatchTest::new);
    RuleTestType BLOCKSTATE_TEST = register("blockstate_match", BlockStateMatchTest::new);
    RuleTestType TAG_TEST = register("tag_match", TagMatchTest::new);
    RuleTestType RANDOM_BLOCK_TEST = register("random_block_match", RandomBlockMatchTest::new);
    RuleTestType RANDOM_BLOCKSTATE_TEST = register("random_blockstate_match", RandomBlockStateMatchTest::new);

    static RuleTestType register(String param0, RuleTestType param1) {
        return Registry.register(Registry.RULE_TEST, param0, param1);
    }
}
