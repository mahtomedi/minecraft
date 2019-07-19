package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;

public class LocateHidingPlaceDuringRaid extends LocateHidingPlace {
    public LocateHidingPlaceDuringRaid(int param0, float param1) {
        super(param0, param1, 1);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        Raid var0 = param0.getRaidAt(new BlockPos(param1));
        return super.checkExtraStartConditions(param0, param1) && var0 != null && var0.isActive() && !var0.isVictory() && !var0.isLoss();
    }
}
