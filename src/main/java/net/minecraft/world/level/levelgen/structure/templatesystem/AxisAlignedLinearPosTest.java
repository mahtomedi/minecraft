package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class AxisAlignedLinearPosTest extends PosRuleTest {
    public static final Codec<AxisAlignedLinearPosTest> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.FLOAT.fieldOf("min_chance").orElse(0.0F).forGetter(param0x -> param0x.minChance),
                    Codec.FLOAT.fieldOf("max_chance").orElse(0.0F).forGetter(param0x -> param0x.maxChance),
                    Codec.INT.fieldOf("min_dist").orElse(0).forGetter(param0x -> param0x.minDist),
                    Codec.INT.fieldOf("max_dist").orElse(0).forGetter(param0x -> param0x.maxDist),
                    Direction.Axis.CODEC.fieldOf("axis").orElse(Direction.Axis.Y).forGetter(param0x -> param0x.axis)
                )
                .apply(param0, AxisAlignedLinearPosTest::new)
    );
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

    @Override
    public boolean test(BlockPos param0, BlockPos param1, BlockPos param2, Random param3) {
        Direction var0 = Direction.get(Direction.AxisDirection.POSITIVE, this.axis);
        float var1 = (float)Math.abs((param1.getX() - param2.getX()) * var0.getStepX());
        float var2 = (float)Math.abs((param1.getY() - param2.getY()) * var0.getStepY());
        float var3 = (float)Math.abs((param1.getZ() - param2.getZ()) * var0.getStepZ());
        int var4 = (int)(var1 + var2 + var3);
        float var5 = param3.nextFloat();
        return var5 <= Mth.clampedLerp(this.minChance, this.maxChance, Mth.inverseLerp((float)var4, (float)this.minDist, (float)this.maxDist));
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.AXIS_ALIGNED_LINEAR_POS_TEST;
    }
}
