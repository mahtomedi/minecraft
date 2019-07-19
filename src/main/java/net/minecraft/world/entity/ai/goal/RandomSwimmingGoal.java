package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;

public class RandomSwimmingGoal extends RandomStrollGoal {
    public RandomSwimmingGoal(PathfinderMob param0, double param1, int param2) {
        super(param0, param1, param2);
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        Vec3 var0 = RandomPos.getPos(this.mob, 10, 7);
        int var1 = 0;

        while(
            var0 != null
                && !this.mob.level.getBlockState(new BlockPos(var0)).isPathfindable(this.mob.level, new BlockPos(var0), PathComputationType.WATER)
                && var1++ < 10
        ) {
            var0 = RandomPos.getPos(this.mob, 10, 7);
        }

        return var0;
    }
}
