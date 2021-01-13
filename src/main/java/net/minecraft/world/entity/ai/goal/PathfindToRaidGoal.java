package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.raid.Raids;
import net.minecraft.world.phys.Vec3;

public class PathfindToRaidGoal<T extends Raider> extends Goal {
    private final T mob;

    public PathfindToRaidGoal(T param0) {
        this.mob = param0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.getTarget() == null
            && !this.mob.isVehicle()
            && this.mob.hasActiveRaid()
            && !this.mob.getCurrentRaid().isOver()
            && !((ServerLevel)this.mob.level).isVillage(this.mob.blockPosition());
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.hasActiveRaid()
            && !this.mob.getCurrentRaid().isOver()
            && this.mob.level instanceof ServerLevel
            && !((ServerLevel)this.mob.level).isVillage(this.mob.blockPosition());
    }

    @Override
    public void tick() {
        if (this.mob.hasActiveRaid()) {
            Raid var0 = this.mob.getCurrentRaid();
            if (this.mob.tickCount % 20 == 0) {
                this.recruitNearby(var0);
            }

            if (!this.mob.isPathFinding()) {
                Vec3 var1 = RandomPos.getPosTowards(this.mob, 15, 4, Vec3.atBottomCenterOf(var0.getCenter()));
                if (var1 != null) {
                    this.mob.getNavigation().moveTo(var1.x, var1.y, var1.z, 1.0);
                }
            }
        }

    }

    private void recruitNearby(Raid param0) {
        if (param0.isActive()) {
            Set<Raider> var0 = Sets.newHashSet();
            List<Raider> var1 = this.mob
                .level
                .getEntitiesOfClass(
                    Raider.class, this.mob.getBoundingBox().inflate(16.0), param1 -> !param1.hasActiveRaid() && Raids.canJoinRaid(param1, param0)
                );
            var0.addAll(var1);

            for(Raider var2 : var0) {
                param0.joinRaid(param0.getGroupsSpawned(), var2, null, true);
            }
        }

    }
}
