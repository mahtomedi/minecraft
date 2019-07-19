package net.minecraft.world.entity.animal.horse;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TraderLlama extends Llama {
    private int despawnDelay = 47999;

    public TraderLlama(EntityType<? extends TraderLlama> param0, Level param1) {
        super(param0, param1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isTraderLlama() {
        return true;
    }

    @Override
    protected Llama makeBabyLlama() {
        return EntityType.TRADER_LLAMA.create(this.level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("DespawnDelay", this.despawnDelay);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("DespawnDelay", 99)) {
            this.despawnDelay = param0.getInt("DespawnDelay");
        }

    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        this.targetSelector.addGoal(1, new TraderLlama.TraderLlamaDefendWanderingTraderGoal(this));
    }

    @Override
    protected void doPlayerRide(Player param0) {
        Entity var0 = this.getLeashHolder();
        if (!(var0 instanceof WanderingTrader)) {
            super.doPlayerRide(param0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.maybeDespawn();
        }

    }

    private void maybeDespawn() {
        if (this.canDespawn()) {
            this.despawnDelay = this.isLeashedToWanderingTrader() ? ((WanderingTrader)this.getLeashHolder()).getDespawnDelay() - 1 : this.despawnDelay - 1;
            if (this.despawnDelay <= 0) {
                this.dropLeash(true, false);
                this.remove();
            }

        }
    }

    private boolean canDespawn() {
        return !this.isTamed() && !this.isLeashedToSomethingOtherThanTheWanderingTrader() && !this.hasOnePlayerPassenger();
    }

    private boolean isLeashedToWanderingTrader() {
        return this.getLeashHolder() instanceof WanderingTrader;
    }

    private boolean isLeashedToSomethingOtherThanTheWanderingTrader() {
        return this.isLeashed() && !this.isLeashedToWanderingTrader();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        SpawnGroupData var0 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        if (param2 == MobSpawnType.EVENT) {
            this.setAge(0);
        }

        return var0;
    }

    public class TraderLlamaDefendWanderingTraderGoal extends TargetGoal {
        private final Llama llama;
        private LivingEntity ownerLastHurtBy;
        private int timestamp;

        public TraderLlamaDefendWanderingTraderGoal(Llama param1) {
            super(param1, false);
            this.llama = param1;
            this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.llama.isLeashed()) {
                return false;
            } else {
                Entity var0 = this.llama.getLeashHolder();
                if (!(var0 instanceof WanderingTrader)) {
                    return false;
                } else {
                    WanderingTrader var1 = (WanderingTrader)var0;
                    this.ownerLastHurtBy = var1.getLastHurtByMob();
                    int var2 = var1.getLastHurtByMobTimestamp();
                    return var2 != this.timestamp && this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT);
                }
            }
        }

        @Override
        public void start() {
            this.mob.setTarget(this.ownerLastHurtBy);
            Entity var0 = this.llama.getLeashHolder();
            if (var0 instanceof WanderingTrader) {
                this.timestamp = ((WanderingTrader)var0).getLastHurtByMobTimestamp();
            }

            super.start();
        }
    }
}
