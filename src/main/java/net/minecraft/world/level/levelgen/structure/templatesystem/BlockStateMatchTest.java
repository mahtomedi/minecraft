package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.world.level.block.state.BlockState;

public class BlockStateMatchTest extends RuleTest {
    private final BlockState blockState;

    public BlockStateMatchTest(BlockState param0) {
        this.blockState = param0;
    }

    public <T> BlockStateMatchTest(Dynamic<T> param0) {
        this(BlockState.deserialize(param0.get("blockstate").orElseEmptyMap()));
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return param0 == this.blockState;
    }

    @Override
    protected RuleTestType getType() {
        return RuleTestType.BLOCKSTATE_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0, param0.createMap(ImmutableMap.of(param0.createString("blockstate"), BlockState.serialize(param0, this.blockState).getValue()))
        );
    }
}
