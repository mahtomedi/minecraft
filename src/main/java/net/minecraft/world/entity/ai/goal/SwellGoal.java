package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Items;

public class SwellGoal extends Goal {
    private final Creeper creeper;
    @Nullable
    private LivingEntity target;

    public SwellGoal(Creeper param0) {
        this.creeper = param0;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity var0 = this.creeper.getTarget();
        if (var0 != null && var0.isCrouching() && var0.getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL)) {
            return false;
        } else {
            return this.creeper.getSwellDir() > 0 || var0 != null && this.creeper.distanceToSqr(var0) < 9.0;
        }
    }

    @Override
    public void start() {
        this.creeper.getNavigation().stop();
        this.target = this.creeper.getTarget();
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.creeper.wasPickedUpByPlayer) {
            if (this.target == null) {
                this.creeper.setSwellDir(-1);
                return;
            }

            if (this.target.isCrouching() && this.target.getItemBySlot(EquipmentSlot.HEAD).is(Items.BARREL)) {
                this.creeper.setSwellDir(-1);
                return;
            }

            if (this.creeper.distanceToSqr(this.target) > 49.0) {
                this.creeper.setSwellDir(-1);
                return;
            }

            if (!this.creeper.getSensing().hasLineOfSight(this.target)) {
                this.creeper.setSwellDir(-1);
                return;
            }
        }

        this.creeper.setSwellDir(1);
    }
}
