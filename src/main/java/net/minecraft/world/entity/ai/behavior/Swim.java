package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;

public class Swim extends Behavior<Mob> {
    private final float height;
    private final float chance;

    public Swim(float param0, float param1) {
        super(ImmutableMap.of());
        this.height = param0;
        this.chance = param1;
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, Mob param1) {
        return param1.isInWater() && param1.getFluidHeight(FluidTags.WATER) > (double)this.height || param1.isInLava();
    }

    protected boolean canStillUse(ServerLevel param0, Mob param1, long param2) {
        return this.checkExtraStartConditions(param0, param1);
    }

    protected void tick(ServerLevel param0, Mob param1, long param2) {
        if (param1.getRandom().nextFloat() < this.chance) {
            param1.getJumpControl().jump();
        }

    }
}
