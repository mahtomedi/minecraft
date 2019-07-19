package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.state.BlockState;

public abstract class RuleTest {
    public abstract boolean test(BlockState var1, Random var2);

    public <T> Dynamic<T> serialize(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.mergeInto(
                this.getDynamic(param0).getValue(),
                param0.createString("predicate_type"),
                param0.createString(Registry.RULE_TEST.getKey(this.getType()).toString())
            )
        );
    }

    protected abstract RuleTestType getType();

    protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> var1);
}
