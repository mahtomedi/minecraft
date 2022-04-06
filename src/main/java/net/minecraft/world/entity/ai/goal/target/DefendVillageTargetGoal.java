package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class DefendVillageTargetGoal extends TargetGoal {
    private final IronGolem golem;
    @Nullable
    private LivingEntity potentialTarget;
    private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);

    public DefendVillageTargetGoal(IronGolem param0) {
        super(param0, false, true);
        this.golem = param0;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        AABB var0 = this.golem.getBoundingBox().inflate(10.0, 8.0, 10.0);
        List<? extends LivingEntity> var1 = this.golem.level.getNearbyEntities(Villager.class, this.attackTargeting, this.golem, var0);
        List<Player> var2 = this.golem.level.getNearbyPlayers(this.attackTargeting, this.golem, var0);

        for(LivingEntity var3 : var1) {
            Villager var4 = (Villager)var3;

            for(Player var5 : var2) {
                int var6 = var4.getPlayerReputation(var5);
                if (var6 <= -100) {
                    this.potentialTarget = var5;
                }
            }
        }

        if (this.potentialTarget == null) {
            return false;
        } else {
            return !(this.potentialTarget instanceof Player) || !this.potentialTarget.isSpectator() && !((Player)this.potentialTarget).isCreative();
        }
    }

    @Override
    public void start() {
        this.golem.setTarget(this.potentialTarget);
        super.start();
    }
}
