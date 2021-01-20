package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.phys.Vec3;

public class RandomSwimmingGoal extends RandomStrollGoal {
    public RandomSwimmingGoal(PathfinderMob param0, double param1, int param2) {
        super(param0, param1, param2);
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        return BehaviorUtils.getRandomSwimmablePos(this.mob, 10, 7);
    }
}
