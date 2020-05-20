package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMatchTest extends RuleTest {
    public static final Codec<BlockMatchTest> CODEC = Registry.BLOCK.fieldOf("block").xmap(BlockMatchTest::new, param0 -> param0.block).codec();
    private final Block block;

    public BlockMatchTest(Block param0) {
        this.block = param0;
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return param0.is(this.block);
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.BLOCK_TEST;
    }
}
