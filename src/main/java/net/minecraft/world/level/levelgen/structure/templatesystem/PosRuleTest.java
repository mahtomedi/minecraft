package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

public abstract class PosRuleTest {
    public abstract boolean test(BlockPos var1, BlockPos var2, BlockPos var3, Random var4);

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.mergeInto(
                this.getDynamic(param0).getValue(),
                param0.createString("predicate_type"),
                param0.createString(Registry.POS_RULE_TEST.getKey(this.getType()).toString())
            )
        );
    }

    protected abstract PosRuleTestType getType();

    protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> var1);
}
