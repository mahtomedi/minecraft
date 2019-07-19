package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockStateMatchTest extends RuleTest {
    private final BlockState blockState;
    private final float probability;

    public RandomBlockStateMatchTest(BlockState param0, float param1) {
        this.blockState = param0;
        this.probability = param1;
    }

    public <T> RandomBlockStateMatchTest(Dynamic<T> param0) {
        this(BlockState.deserialize(param0.get("blockstate").orElseEmptyMap()), param0.get("probability").asFloat(1.0F));
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return param0 == this.blockState && param1.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType getType() {
        return RuleTestType.RANDOM_BLOCKSTATE_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("blockstate"),
                    BlockState.serialize(param0, this.blockState).getValue(),
                    param0.createString("probability"),
                    param0.createFloat(this.probability)
                )
            )
        );
    }
}
