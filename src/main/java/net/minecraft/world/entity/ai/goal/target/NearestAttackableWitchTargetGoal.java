package net.minecraft.world.entity.ai.goal.target;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raider;

public class NearestAttackableWitchTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private boolean canAttack = true;

    public NearestAttackableWitchTargetGoal(
        Raider param0, Class<T> param1, int param2, boolean param3, boolean param4, @Nullable Predicate<LivingEntity> param5
    ) {
        super(param0, param1, param2, param3, param4, param5);
    }

    public void setCanAttack(boolean param0) {
        this.canAttack = param0;
    }

    @Override
    public boolean canUse() {
        return this.canAttack && super.canUse();
    }
}
