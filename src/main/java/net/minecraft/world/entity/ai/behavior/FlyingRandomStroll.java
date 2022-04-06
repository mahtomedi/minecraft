package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.phys.Vec3;

public class FlyingRandomStroll extends RandomStroll {
    public FlyingRandomStroll(float param0) {
        this(param0, true);
    }

    public FlyingRandomStroll(float param0, boolean param1) {
        super(param0, param1);
    }

    @Override
    protected Vec3 getTargetPos(PathfinderMob param0) {
        Vec3 var0 = param0.getViewVector(0.0F);
        return AirAndWaterRandomPos.getPos(param0, this.maxHorizontalDistance, this.maxVerticalDistance, -2, var0.x, var0.z, (float) (Math.PI / 2));
    }
}
