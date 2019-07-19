package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class DoNothing extends Behavior<LivingEntity> {
    public DoNothing(int param0, int param1) {
        super(ImmutableMap.of(), param0, param1);
    }

    @Override
    protected boolean canStillUse(ServerLevel param0, LivingEntity param1, long param2) {
        return true;
    }
}
