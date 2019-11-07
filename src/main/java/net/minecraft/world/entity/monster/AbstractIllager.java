package net.minecraft.world.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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

    @OnlyIn(Dist.CLIENT)
    public AbstractIllager.IllagerArmPose getArmPose() {
        return AbstractIllager.IllagerArmPose.CROSSED;
    }

    @OnlyIn(Dist.CLIENT)
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

    public class RaiderOpenDoorGoal extends OpenDoorGoal {
        public RaiderOpenDoorGoal(Raider param1) {
            super(param1, false);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && AbstractIllager.this.hasActiveRaid();
        }
    }
}
