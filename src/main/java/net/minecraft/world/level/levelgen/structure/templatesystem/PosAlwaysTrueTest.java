package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;

public class PosAlwaysTrueTest extends PosRuleTest {
    public static final PosAlwaysTrueTest INSTANCE = new PosAlwaysTrueTest();

    private PosAlwaysTrueTest() {
    }

    @Override
    public boolean test(BlockPos param0, BlockPos param1, BlockPos param2, Random param3) {
        return true;
    }

    @Override
    protected PosRuleTestType getType() {
        return PosRuleTestType.ALWAYS_TRUE_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }
}
