package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class LinearPosTest extends PosRuleTest {
    public static final Codec<LinearPosTest> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.FLOAT.fieldOf("min_chance").withDefault(0.0F).forGetter(param0x -> param0x.minChance),
                    Codec.FLOAT.fieldOf("max_chance").withDefault(0.0F).forGetter(param0x -> param0x.maxChance),
                    Codec.INT.fieldOf("min_dist").withDefault(0).forGetter(param0x -> param0x.minDist),
                    Codec.INT.fieldOf("max_dist").withDefault(0).forGetter(param0x -> param0x.maxDist)
                )
                .apply(param0, LinearPosTest::new)
    );
    private final float minChance;
    private final float maxChance;
    private final int minDist;
    private final int maxDist;

    public LinearPosTest(float param0, float param1, int param2, int param3) {
        if (param2 >= param3) {
            throw new IllegalArgumentException("Invalid range: [" + param2 + "," + param3 + "]");
        } else {
            this.minChance = param0;
            this.maxChance = param1;
            this.minDist = param2;
            this.maxDist = param3;
        }
    }

    @Override
    public boolean test(BlockPos param0, BlockPos param1, BlockPos param2, Random param3) {
        int var0 = param1.distManhattan(param2);
        float var1 = param3.nextFloat();
        return (double)var1
            <= Mth.clampedLerp((double)this.minChance, (double)this.maxChance, Mth.inverseLerp((double)var0, (double)this.minDist, (double)this.maxDist));
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.LINEAR_POS_TEST;
    }
}
