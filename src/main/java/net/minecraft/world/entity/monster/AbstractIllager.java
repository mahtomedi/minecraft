package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;

public abstract class AbstractIllager extends Raider {
    protected AbstractIllager(EntityType<? extends AbstractIllager> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
    }

    @Override
    public MobType getMobType() {
        return MobType.ILLAGER;
    }

    @Override
    public boolean canStealItem() {
        return true;
    }

    public AbstractIllager.IllagerArmPose getArmPose() {
        return this.getCarried() != LivingEntity.Carried.NONE ? AbstractIllager.IllagerArmPose.CROSSBOW_HOLD : AbstractIllager.IllagerArmPose.CROSSED;
    }

    @Override
    public boolean canAttack(LivingEntity param0) {
        return param0 instanceof AbstractVillager && param0.isBaby() ? false : super.canAttack(param0);
    }

    public static enum IllagerArmPose {
        CROSSED,
        ATTACKING,
        SPELLCASTING,
        BOW_AND_ARROW,
        CROSSBOW_HOLD,
        CROSSBOW_CHARGE,
        CELEBRATING,
        NEUTRAL;
    }

    protected class RaiderOpenDoorGoal extends OpenDoorGoal {
        public RaiderOpenDoorGoal(Raider param1) {
            super(param1, false);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && AbstractIllager.this.hasActiveRaid();
        }
    }
}
