package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class AlwaysTrueTest extends RuleTest {
    public static final AlwaysTrueTest INSTANCE = new AlwaysTrueTest();

    private AlwaysTrueTest() {
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return true;
    }

    @Override
    protected RuleTestType getType() {
        return RuleTestType.ALWAYS_TRUE_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.emptyMap());
    }
}
