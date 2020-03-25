package net.minecraft.world.level.levelgen.structure.templatesystem;

import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;

public interface PosRuleTestType extends Deserializer<PosRuleTest> {
    PosRuleTestType ALWAYS_TRUE_TEST = register("always_true", param0 -> PosAlwaysTrueTest.INSTANCE);
    PosRuleTestType LINEAR_POS_TEST = register("linear_pos", LinearPosTest::new);
    PosRuleTestType AXIS_ALIGNED_LINEAR_POS_TEST = register("axis_aligned_linear_pos", AxisAlignedLinearPosTest::new);

    static PosRuleTestType register(String param0, PosRuleTestType param1) {
        return Registry.register(Registry.POS_RULE_TEST, param0, param1);
    }
}
