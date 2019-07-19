package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;

public abstract class JumpGoal extends Goal {
    public JumpGoal() {
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    protected float rotlerp(float param0, float param1, float param2) {
        float var0 = param1 - param0;

        while(var0 < -180.0F) {
            var0 += 360.0F;
        }

        while(var0 >= 180.0F) {
            var0 -= 360.0F;
        }

        return param0 + param2 * var0;
    }
}
