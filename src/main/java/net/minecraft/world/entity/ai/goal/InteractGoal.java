package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class InteractGoal extends LookAtPlayerGoal {
    public InteractGoal(Mob param0, Class<? extends LivingEntity> param1, float param2) {
        super(param0, param1, param2);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }

    public InteractGoal(Mob param0, Class<? extends LivingEntity> param1, float param2, float param3) {
        super(param0, param1, param2, param3);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));
    }
}
