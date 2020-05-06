package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveBackToVillageGoal extends RandomStrollGoal {
    public MoveBackToVillageGoal(PathfinderMob param0, double param1, boolean param2) {
        super(param0, param1, 10, param2);
    }

    @Override
    public boolean canUse() {
        ServerLevel var0 = (ServerLevel)this.mob.level;
        BlockPos var1 = this.mob.blockPosition();
        return var0.isVillage(var1) ? false : super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        ServerLevel var0 = (ServerLevel)this.mob.level;
        BlockPos var1 = this.mob.blockPosition();
        SectionPos var2 = SectionPos.of(var1);
        SectionPos var3 = BehaviorUtils.findSectionClosestToVillage(var0, var2, 2);
        return var3 != var2 ? RandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(var3.center())) : null;
    }
}
