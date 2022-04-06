package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateMatchTest extends RuleTest {
    public static final Codec<BlockStateMatchTest> CODEC = BlockState.CODEC
        .fieldOf("block_state")
        .xmap(BlockStateMatchTest::new, param0 -> param0.blockState)
        .codec();
    private final BlockState blockState;

    public BlockStateMatchTest(BlockState param0) {
        this.blockState = param0;
    }

    @Override
    public boolean test(BlockState param0, RandomSource param1) {
        return param0 == this.blockState;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.BLOCKSTATE_TEST;
    }
}
