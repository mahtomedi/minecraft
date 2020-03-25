package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

public class LinearPosTest extends PosRuleTest {
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

    public <T> LinearPosTest(Dynamic<T> param0) {
        this(param0.get("min_chance").asFloat(0.0F), param0.get("max_chance").asFloat(0.0F), param0.get("min_dist").asInt(0), param0.get("max_dist").asInt(0));
    }

    @Override
    public boolean test(BlockPos param0, BlockPos param1, BlockPos param2, Random param3) {
        int var0 = param1.distManhattan(param2);
        float var1 = param3.nextFloat();
        return (double)var1
            <= Mth.clampedLerp((double)this.minChance, (double)this.maxChance, Mth.pct((double)var0, (double)this.minDist, (double)this.maxDist));
    }

    @Override
    protected PosRuleTestType getType() {
        return PosRuleTestType.LINEAR_POS_TEST;
    }

    @Override
    protected <T> Dynamic<T> getDynamic(DynamicOps<T> param0) {
        return new Dynamic<>(
            param0,
            param0.createMap(
                ImmutableMap.of(
                    param0.createString("min_chance"),
                    param0.createFloat(this.minChance),
                    param0.createString("max_chance"),
                    param0.createFloat(this.maxChance),
                    param0.createString("min_dist"),
                    param0.createFloat((float)this.minDist),
                    param0.createString("max_dist"),
                    param0.createFloat((float)this.maxDist)
                )
            )
        );
    }
}
