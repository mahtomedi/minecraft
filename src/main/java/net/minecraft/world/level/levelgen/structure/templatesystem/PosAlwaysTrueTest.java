package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;

public class PosAlwaysTrueTest extends PosRuleTest {
    public static final Codec<PosAlwaysTrueTest> CODEC = Codec.unit(() -> PosAlwaysTrueTest.INSTANCE);
    public static final PosAlwaysTrueTest INSTANCE = new PosAlwaysTrueTest();

    private PosAlwaysTrueTest() {
    }

    @Override
    public boolean test(BlockPos param0, BlockPos param1, BlockPos param2, RandomSource param3) {
        return true;
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.ALWAYS_TRUE_TEST;
    }
}
