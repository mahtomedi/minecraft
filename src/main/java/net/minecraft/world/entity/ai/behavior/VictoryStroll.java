package net.minecraft.world.entity.ai.behavior;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.raid.Raid;

public class VictoryStroll extends VillageBoundRandomStroll {
    public VictoryStroll(float param0) {
        super(param0);
    }

    protected boolean checkExtraStartConditions(ServerLevel param0, PathfinderMob param1) {
        Raid var0 = param0.getRaidAt(new BlockPos(param1));
        return var0 != null && var0.isVictory() && super.checkExtraStartConditions(param0, param1);
    }
}
