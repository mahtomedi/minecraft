package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class RandomBlockMatchTest extends RuleTest {
    public static final Codec<RandomBlockMatchTest> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(param0x -> param0x.block),
                    Codec.FLOAT.fieldOf("probability").forGetter(param0x -> param0x.probability)
                )
                .apply(param0, RandomBlockMatchTest::new)
    );
    private final Block block;
    private final float probability;

    public RandomBlockMatchTest(Block param0, float param1) {
        this.block = param0;
        this.probability = param1;
    }

    @Override
    public boolean test(BlockState param0, Random param1) {
        return param0.is(this.block) && param1.nextFloat() < this.probability;
    }

    @Override
    protected RuleTestType<?> getType() {
        return RuleTestType.RANDOM_BLOCK_TEST;
    }
}
