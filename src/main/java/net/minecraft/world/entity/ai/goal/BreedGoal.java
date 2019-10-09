package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public class BreedGoal extends Goal {
    private static final TargetingConditions PARTNER_TARGETING = new TargetingConditions().range(8.0).allowInvulnerable().allowSameTeam().allowUnseeable();
    protected final Animal animal;
    private final Class<? extends Animal> partnerClass;
    protected final Level level;
    protected Animal partner;
    private int loveTime;
    private final double speedModifier;

    public BreedGoal(Animal param0, double param1) {
        this(param0, param1, param0.getClass());
    }

    public BreedGoal(Animal param0, double param1, Class<? extends Animal> param2) {
        this.animal = param0;
        this.level = param0.level;
        this.partnerClass = param2;
        this.speedModifier = param1;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.animal.isInLove()) {
            return false;
        } else {
            this.partner = this.getFreePartner();
            return this.partner != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return this.partner.isAlive() && this.partner.isInLove() && this.loveTime < 60;
    }

    @Override
    public void stop() {
        this.partner = null;
        this.loveTime = 0;
    }

    @Override
    public void tick() {
        this.animal.getLookControl().setLookAt(this.partner, 10.0F, (float)this.animal.getMaxHeadXRot());
        this.animal.getNavigation().moveTo(this.partner, this.speedModifier);
        ++this.loveTime;
        if (this.loveTime >= 60 && this.animal.distanceToSqr(this.partner) < 9.0) {
            this.breed();
        }

    }

    @Nullable
    private Animal getFreePartner() {
        List<Animal> var0 = this.level.getNearbyEntities(this.partnerClass, PARTNER_TARGETING, this.animal, this.animal.getBoundingBox().inflate(8.0));
        double var1 = Double.MAX_VALUE;
        Animal var2 = null;

        for(Animal var3 : var0) {
            if (this.animal.canMate(var3) && this.animal.distanceToSqr(var3) < var1) {
                var2 = var3;
                var1 = this.animal.distanceToSqr(var3);
            }
        }

        return var2;
    }

    protected void breed() {
        AgableMob var0 = this.animal.getBreedOffspring(this.partner);
        if (var0 != null) {
            ServerPlayer var1 = this.animal.getLoveCause();
            if (var1 == null && this.partner.getLoveCause() != null) {
                var1 = this.partner.getLoveCause();
            }

            if (var1 != null) {
                var1.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(var1, this.animal, this.partner, var0);
            }

            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            var0.setAge(-24000);
            var0.moveTo(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0F, 0.0F);
            this.level.addFreshEntity(var0);
            this.level.broadcastEntityEvent(this.animal, (byte)18);
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                this.level
                    .addFreshEntity(
                        new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1)
                    );
            }

        }
    }
}
