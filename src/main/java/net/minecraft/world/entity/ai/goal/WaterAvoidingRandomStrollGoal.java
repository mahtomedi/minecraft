package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class WaterAvoidingRandomStrollGoal extends RandomStrollGoal {
    protected final float probability;

    public WaterAvoidingRandomStrollGoal(PathfinderMob param0, double param1) {
        this(param0, param1, 0.001F);
    }

    public WaterAvoidingRandomStrollGoal(PathfinderMob param0, double param1, float param2) {
        super(param0, param1);
        this.probability = param2;
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        if (this.mob.isInWaterOrBubble()) {
            Vec3 var0 = RandomPos.getLandPos(this.mob, 15, 7);
            return var0 == null ? super.getPosition() : var0;
        } else {
            return this.mob.getRandom().nextFloat() >= this.probability ? RandomPos.getLandPos(this.mob, 10, 7) : super.getPosition();
        }
    }
}
