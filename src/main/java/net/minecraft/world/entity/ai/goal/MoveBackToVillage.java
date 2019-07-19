package net.minecraft.world.entity.ai.goal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.phys.Vec3;

public class MoveBackToVillage extends RandomStrollGoal {
    public MoveBackToVillage(PathfinderMob param0, double param1) {
        super(param0, param1, 10);
    }

    @Override
    public boolean canUse() {
        ServerLevel var0 = (ServerLevel)this.mob.level;
        BlockPos var1 = new BlockPos(this.mob);
        return var0.isVillage(var1) ? false : super.canUse();
    }

    @Nullable
    @Override
    protected Vec3 getPosition() {
        ServerLevel var0 = (ServerLevel)this.mob.level;
        BlockPos var1 = new BlockPos(this.mob);
        SectionPos var2 = SectionPos.of(var1);
        SectionPos var3 = BehaviorUtils.findSectionClosestToVillage(var0, var2, 2);
        if (var3 != var2) {
            BlockPos var4 = var3.center();
            return RandomPos.getPosTowards(this.mob, 10, 7, new Vec3((double)var4.getX(), (double)var4.getY(), (double)var4.getZ()));
        } else {
            return null;
        }
    }
}
