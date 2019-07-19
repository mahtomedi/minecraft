package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public class OwnerHurtByTargetGoal extends TargetGoal {
    private final TamableAnimal tameAnimal;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public OwnerHurtByTargetGoal(TamableAnimal param0) {
        super(param0, false);
        this.tameAnimal = param0;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.tameAnimal.isTame() && !this.tameAnimal.isSitting()) {
            LivingEntity var0 = this.tameAnimal.getOwner();
            if (var0 == null) {
                return false;
            } else {
                this.ownerLastHurtBy = var0.getLastHurtByMob();
                int var1 = var0.getLastHurtByMobTimestamp();
                return var1 != this.timestamp
                    && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT)
                    && this.tameAnimal.wantsToAttack(this.ownerLastHurtBy, var0);
            }
        } else {
            return false;
        }
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity var0 = this.tameAnimal.getOwner();
        if (var0 != null) {
            this.timestamp = var0.getLastHurtByMobTimestamp();
        }

        super.start();
    }
}
