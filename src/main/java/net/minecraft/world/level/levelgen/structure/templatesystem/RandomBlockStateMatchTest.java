package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockStateMatchTest extends RuleTest {
    public static final Codec<RandomBlockStateMatchTest> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    BlockState.CODEC.fieldOf("block_state").forGetter(param0x -> param0x.blockState),
                    Codec.FLOAT.fieldOf("probability").forGetter(param0x -> param0x.probability)
                )
                .apply(param0, RandomBlockStateMatchTest::new)
    );
    private final BlockState blockState;
    private final float probability;

    public RandomBlockStateMatchTest(BlockState param0, float param1) {
        this.blockState = param0;
        this.probability = param1;
    }

    @Override
    public boolean test(BlockState param0, RandomSource param1) {
        return param0 == this.blockState && param1.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.RANDOM_BLOCKSTATE_TEST;
    }
}
