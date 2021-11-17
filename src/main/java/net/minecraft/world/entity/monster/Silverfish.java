package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Silverfish extends Monster {
    @Nullable
    private Silverfish.SilverfishWakeUpFriendsGoal friendsGoal;

    public Silverfish(EntityType<? extends Silverfish> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        this.friendsGoal = new Silverfish.SilverfishWakeUpFriendsGoal(this);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(3, this.friendsGoal);
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new Silverfish.SilverfishMergeWithStoneGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public double getMyRidingOffset() {
        return 0.1;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 0.13F;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            if ((param0 instanceof EntityDamageSource || param0 == DamageSource.MAGIC) && this.friendsGoal != null) {
                this.friendsGoal.notifyHurt();
            }

            return super.hurt(param0, param1);
        }
    }

    @Override
    public void tick() {
        this.yBodyRot = this.getYRot();
        super.tick();
    }

    @Override
    public void setYBodyRot(float param0) {
        this.setYRot(param0);
        super.setYBodyRot(param0);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        return InfestedBlock.isCompatibleHostBlock(param1.getBlockState(param0.below())) ? 10.0F : super.getWalkTargetValue(param0, param1);
    }

    public static boolean checkSilverfishSpawnRules(EntityType<Silverfish> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        if (checkAnyLightMonsterSpawnRules(param0, param1, param2, param3, param4)) {
            Player var0 = param1.getNearestPlayer((double)param3.getX() + 0.5, (double)param3.getY() + 0.5, (double)param3.getZ() + 0.5, 5.0, true);
            return var0 == null;
        } else {
            return false;
        }
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    static class SilverfishMergeWithStoneGoal extends RandomStrollGoal {
        @Nullable
        private Direction selectedDirection;
        private boolean doMerge;

        public SilverfishMergeWithStoneGoal(Silverfish param0) {
            super(param0, 1.0, 10);
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.mob.getTarget() != null) {
                return false;
            } else if (!this.mob.getNavigation().isDone()) {
                return false;
            } else {
                Random var0 = this.mob.getRandom();
                if (this.mob.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && var0.nextInt(reducedTickDelay(10)) == 0) {
                    this.selectedDirection = Direction.getRandom(var0);
                    BlockPos var1 = new BlockPos(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()).relative(this.selectedDirection);
                    BlockState var2 = this.mob.level.getBlockState(var1);
                    if (InfestedBlock.isCompatibleHostBlock(var2)) {
                        this.doMerge = true;
                        return true;
                    }
                }

                this.doMerge = false;
                return super.canUse();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.doMerge ? false : super.canContinueToUse();
        }

        @Override
        public void start() {
            if (!this.doMerge) {
                super.start();
            } else {
                LevelAccessor var0 = this.mob.level;
                BlockPos var1 = new BlockPos(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()).relative(this.selectedDirection);
                BlockState var2 = var0.getBlockState(var1);
                if (InfestedBlock.isCompatibleHostBlock(var2)) {
                    var0.setBlock(var1, InfestedBlock.infestedStateByHost(var2), 3);
                    this.mob.spawnAnim();
                    this.mob.discard();
                }

            }
        }
    }

    static class SilverfishWakeUpFriendsGoal extends Goal {
        private final Silverfish silverfish;
        private int lookForFriends;

        public SilverfishWakeUpFriendsGoal(Silverfish param0) {
            this.silverfish = param0;
        }

        public void notifyHurt() {
            if (this.lookForFriends == 0) {
                this.lookForFriends = this.adjustedTickDelay(20);
            }

        }

        @Override
        public boolean canUse() {
            return this.lookForFriends > 0;
        }

        @Override
        public void tick() {
            --this.lookForFriends;
            if (this.lookForFriends <= 0) {
                Level var0 = this.silverfish.level;
                Random var1 = this.silverfish.getRandom();
                BlockPos var2 = this.silverfish.blockPosition();

                for(int var3 = 0; var3 <= 5 && var3 >= -5; var3 = (var3 <= 0 ? 1 : 0) - var3) {
                    for(int var4 = 0; var4 <= 10 && var4 >= -10; var4 = (var4 <= 0 ? 1 : 0) - var4) {
                        for(int var5 = 0; var5 <= 10 && var5 >= -10; var5 = (var5 <= 0 ? 1 : 0) - var5) {
                            BlockPos var6 = var2.offset(var4, var3, var5);
                            BlockState var7 = var0.getBlockState(var6);
                            Block var8 = var7.getBlock();
                            if (var8 instanceof InfestedBlock) {
                                if (var0.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                                    var0.destroyBlock(var6, true, this.silverfish);
                                } else {
                                    var0.setBlock(var6, ((InfestedBlock)var8).hostStateByInfested(var0.getBlockState(var6)), 3);
                                }

                                if (var1.nextBoolean()) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}
