package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockMatchTest extends RuleTest {
    private final Block block;
    private final float probability;

    public RandomBlockMatchTest(Block param0, float param1) {
        this.block = param0;
        this.probability = param1;
    }

    public <T> RandomBlockMatchTest(Dynamic<T> param0) {
        this(Registry.BLOCK.get(new ResourceLocation(param0.get("block").asString(""))), param0.get("probability").asFloat(1.0F));
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return param0.is(this.block) && param1.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType getType() {
        return RuleTestType.RANDOM_BLOCK_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("block"),
                    param0.createString(Registry.BLOCK.getKey(this.block).toString()),
                    param0.createString("probability"),
                    param0.createFloat(this.probability)
                )
            )
        );
    }
}
