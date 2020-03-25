package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class AxisAlignedLinearPosTest extends PosRuleTest {
    private final float minChance;
    private final float maxChance;
    private final int minDist;
    private final int maxDist;
    private final Direction.Axis axis;

    public AxisAlignedLinearPosTest(float param0, float param1, int param2, int param3, Direction.Axis param4) {
        if (param2 >= param3) {
            throw new IllegalArgumentException("Invalid range: [" + param2 + "," + param3 + "]");
        } else {
            this.minChance = param0;
            this.maxChance = param1;
            this.minDist = param2;
            this.maxDist = param3;
            this.axis = param4;
        }
    }

    public <T> AxisAlignedLinearPosTest(Dynamic<T> param0) {
        this(
            param0.get("min_chance").asFloat(0.0F),
            param0.get("max_chance").asFloat(0.0F),
            param0.get("min_dist").asInt(0),
            param0.get("max_dist").asInt(0),
            Direction.Axis.byName(param0.get("axis").asString("y"))
        );
    }

    @Override
    public boolean test(BlockPos param0, BlockPos param1, BlockPos param2, Random param3) {
        Direction var0 = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
        float var1 = (float)Math.abs((param1.getX() - param2.getX()) * var0.getStepX());
        float var2 = (float)Math.abs((param1.getY() - param2.getY()) * var0.getStepY());
        float var3 = (float)Math.abs((param1.getZ() - param2.getZ()) * var0.getStepZ());
        int var4 = (int)(var1 + var2 + var3);
        float var5 = param3.nextFloat();
        return (double)var5
            <= Mth.clampedLerp((double)this.minChance, (double)this.maxChance, Mth.pct((double)var4, (double)this.minDist, (double)this.maxDist));
    }

    @Override
    protected PosRuleTestType getType() {
        return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS_TEST;
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
                    param0.createFloat((float)this.maxDist),
                    param0.createString("axis"),
                    param0.createString(this.axis.getName())
                )
            )
        );
    }
}
