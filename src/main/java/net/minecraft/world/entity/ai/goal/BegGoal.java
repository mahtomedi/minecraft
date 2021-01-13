package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class BegGoal extends Goal {
    private final Wolf wolf;
    private Player player;
    private final Level level;
    private final float lookDistance;
    private int lookTime;
    private final TargetingConditions begTargeting;

    public BegGoal(Wolf param0, float param1) {
        this.wolf = param0;
        this.level = param0.level;
        this.lookDistance = param1;
        this.begTargeting = new TargetingConditions().range((double)param1).allowInvulnerable().allowSameTeam().allowNonAttackable();
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.player = this.level.getNearestPlayer(this.begTargeting, this.wolf);
        return this.player == null ? false : this.playerHoldingInteresting(this.player);
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.player.isAlive()) {
            return false;
        } else if (this.wolf.distanceToSqr(this.player) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        } else {
            return this.lookTime > 0 && this.playerHoldingInteresting(this.player);
        }
    }

    @Override
    public void start() {
        this.wolf.setIsInterested(true);
        this.lookTime = 40 + this.wolf.getRandom().nextInt(40);
    }

    @Override
    public void stop() {
        this.wolf.setIsInterested(false);
        this.player = null;
    }

    @Override
    public void tick() {
        this.wolf.getLookControl().setLookAt(this.player.getX(), this.player.getEyeY(), this.player.getZ(), 10.0F, (float)this.wolf.getMaxHeadXRot());
        --this.lookTime;
    }

    private boolean playerHoldingInteresting(Player param0) {
        for(InteractionHand var0 : InteractionHand.values()) {
            ItemStack var1 = param0.getItemInHand(var0);
            if (this.wolf.isTame() && var1.getItem() == Items.BONE) {
                return true;
            }

            if (this.wolf.isFood(var1)) {
                return true;
            }
        }

        return false;
    }
}
