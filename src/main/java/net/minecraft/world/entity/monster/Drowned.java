package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class Drowned extends Zombie implements RangedAttackMob {
    public static final float NAUTILUS_SHELL_CHANCE = 0.03F;
    boolean searchingForLand;
    protected final WaterBoundPathNavigation waterNavigation;
    protected final GroundPathNavigation groundNavigation;

    public Drowned(EntityType<? extends Drowned> param0, Level param1) {
        super(param0, param1);
        this.maxUpStep = 1.0F;
        this.moveControl = new Drowned.DrownedMoveControl(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.waterNavigation = new WaterBoundPathNavigation(this, param1);
        this.groundNavigation = new GroundPathNavigation(this, param1);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new Drowned.DrownedGoToWaterGoal(this, 1.0));
        this.goalSelector.addGoal(2, new Drowned.DrownedTridentAttackGoal(this, 1.0, 40, 10.0F));
        this.goalSelector.addGoal(2, new Drowned.DrownedAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new Drowned.DrownedGoToBeachGoal(this, 1.0));
        this.goalSelector.addGoal(6, new Drowned.DrownedSwimUpGoal(this, 1.0, this.level.getSeaLevel()));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Drowned.class).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::okTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Axolotl.class, true, false));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        param3 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        if (this.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty() && this.random.nextFloat() < 0.03F) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.NAUTILUS_SHELL));
            this.handDropChances[EquipmentSlot.OFFHAND.getIndex()] = 2.0F;
        }

        return param3;
    }

    public static boolean checkDrownedSpawnRules(EntityType<Drowned> param0, ServerLevelAccessor param1, MobSpawnType param2, BlockPos param3, Random param4) {
        Optional<ResourceKey<Biome>> var0 = param1.getBiomeName(param3);
        boolean var1 = param1.getDifficulty() != Difficulty.PEACEFUL
            && isDarkEnoughToSpawn(param1, param3, param4)
            && (param2 == MobSpawnType.SPAWNER || param1.getFluidState(param3).is(FluidTags.WATER));
        if (!Objects.equals(var0, Optional.of(Biomes.RIVER)) && !Objects.equals(var0, Optional.of(Biomes.FROZEN_RIVER))) {
            return param4.nextInt(40) == 0 && isDeepEnoughToSpawn(param1, param3) && var1;
        } else {
            return param4.nextInt(15) == 0 && var1;
        }
    }

    private static boolean isDeepEnoughToSpawn(LevelAccessor param0, BlockPos param1) {
        return param1.getY() < param0.getSeaLevel() - 5;
    }

    @Override
    protected boolean supportsBreakDoorGoal() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.DROWNED_AMBIENT_WATER : SoundEvents.DROWNED_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isInWater() ? SoundEvents.DROWNED_HURT_WATER : SoundEvents.DROWNED_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isInWater() ? SoundEvents.DROWNED_DEATH_WATER : SoundEvents.DROWNED_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.DROWNED_STEP;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.DROWNED_SWIM;
    }

    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        if ((double)this.random.nextFloat() > 0.9) {
            int var0 = this.random.nextInt(16);
            if (var0 < 10) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.FISHING_ROD));
            }
        }

    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack param0, ItemStack param1) {
        if (param1.is(Items.NAUTILUS_SHELL)) {
            return false;
        } else if (param1.is(Items.TRIDENT)) {
            if (param0.is(Items.TRIDENT)) {
                return param0.getDamageValue() < param1.getDamageValue();
            } else {
                return false;
            }
        } else {
            return param0.is(Items.TRIDENT) ? true : super.canReplaceCurrentItem(param0, param1);
        }
    }

    @Override
    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader param0) {
        return param0.isUnobstructed(this);
    }

    public boolean okTarget(@Nullable LivingEntity param0) {
        if (param0 != null) {
            return !this.level.isDay() || param0.isInWater();
        } else {
            return false;
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return !this.isSwimming();
    }

    boolean wantsToSwim() {
        if (this.searchingForLand) {
            return true;
        } else {
            LivingEntity var0 = this.getTarget();
            return var0 != null && var0.isInWater();
        }
    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
            this.moveRelative(0.01F, param0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
        } else {
            super.travel(param0);
        }

    }

    @Override
    public void updateSwimming() {
        if (!this.level.isClientSide) {
            if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
                this.navigation = this.waterNavigation;
                this.setSwimming(true);
            } else {
                this.navigation = this.groundNavigation;
                this.setSwimming(false);
            }
        }

    }

    protected boolean closeToNextPos() {
        Path var0 = this.getNavigation().getPath();
        if (var0 != null) {
            BlockPos var1 = var0.getTarget();
            if (var1 != null) {
                double var2 = this.distanceToSqr((double)var1.getX(), (double)var1.getY(), (double)var1.getZ());
                if (var2 < 4.0) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        ThrownTrident var0 = new ThrownTrident(this.level, this, new ItemStack(Items.TRIDENT));
        double var1 = param0.getX() - this.getX();
        double var2 = param0.getY(0.3333333333333333) - var0.getY();
        double var3 = param0.getZ() - this.getZ();
        double var4 = Math.sqrt(var1 * var1 + var3 * var3);
        var0.shoot(var1, var2 + var4 * 0.2F, var3, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.DROWNED_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(var0);
    }

    public void setSearchingForLand(boolean param0) {
        this.searchingForLand = param0;
    }

    static class DrownedAttackGoal extends ZombieAttackGoal {
        private final Drowned drowned;

        public DrownedAttackGoal(Drowned param0, double param1, boolean param2) {
            super(param0, param1, param2);
            this.drowned = param0;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.drowned.okTarget(this.drowned.getTarget());
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.drowned.okTarget(this.drowned.getTarget());
        }
    }

    static class DrownedGoToBeachGoal extends MoveToBlockGoal {
        private final Drowned drowned;

        public DrownedGoToBeachGoal(Drowned param0, double param1) {
            super(param0, param1, 8, 2);
            this.drowned = param0;
        }

        @Override
        public boolean canUse() {
            return super.canUse()
                && !this.drowned.level.isDay()
                && this.drowned.isInWater()
                && this.drowned.getY() >= (double)(this.drowned.level.getSeaLevel() - 3);
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse();
        }

        @Override
        protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
            BlockPos var0 = param1.above();
            return param0.isEmptyBlock(var0) && param0.isEmptyBlock(var0.above())
                ? param0.getBlockState(param1).entityCanStandOn(param0, param1, this.drowned)
                : false;
        }

        @Override
        public void start() {
            this.drowned.setSearchingForLand(false);
            this.drowned.navigation = this.drowned.groundNavigation;
            super.start();
        }

        @Override
        public void stop() {
            super.stop();
        }
    }

    static class DrownedGoToWaterGoal extends Goal {
        private final PathfinderMob mob;
        private double wantedX;
        private double wantedY;
        private double wantedZ;
        private final double speedModifier;
        private final Level level;

        public DrownedGoToWaterGoal(PathfinderMob param0, double param1) {
            this.mob = param0;
            this.speedModifier = param1;
            this.level = param0.level;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (!this.level.isDay()) {
                return false;
            } else if (this.mob.isInWater()) {
                return false;
            } else {
                Vec3 var0 = this.getWaterPos();
                if (var0 == null) {
                    return false;
                } else {
                    this.wantedX = var0.x;
                    this.wantedY = var0.y;
                    this.wantedZ = var0.z;
                    return true;
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }

        @Nullable
        private Vec3 getWaterPos() {
            Random var0 = this.mob.getRandom();
            BlockPos var1 = this.mob.blockPosition();

            for(int var2 = 0; var2 < 10; ++var2) {
                BlockPos var3 = var1.offset(var0.nextInt(20) - 10, 2 - var0.nextInt(8), var0.nextInt(20) - 10);
                if (this.level.getBlockState(var3).is(Blocks.WATER)) {
                    return Vec3.atBottomCenterOf(var3);
                }
            }

            return null;
        }
    }

    static class DrownedMoveControl extends MoveControl {
        private final Drowned drowned;

        public DrownedMoveControl(Drowned param0) {
            super(param0);
            this.drowned = param0;
        }

        @Override
        public void tick() {
            LivingEntity var0 = this.drowned.getTarget();
            if (this.drowned.wantsToSwim() && this.drowned.isInWater()) {
                if (var0 != null && var0.getY() > this.drowned.getY() || this.drowned.searchingForLand) {
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0, 0.002, 0.0));
                }

                if (this.operation != MoveControl.Operation.MOVE_TO || this.drowned.getNavigation().isDone()) {
                    this.drowned.setSpeed(0.0F);
                    return;
                }

                double var1 = this.wantedX - this.drowned.getX();
                double var2 = this.wantedY - this.drowned.getY();
                double var3 = this.wantedZ - this.drowned.getZ();
                double var4 = Math.sqrt(var1 * var1 + var2 * var2 + var3 * var3);
                var2 /= var4;
                float var5 = (float)(Mth.atan2(var3, var1) * 180.0F / (float)Math.PI) - 90.0F;
                this.drowned.setYRot(this.rotlerp(this.drowned.getYRot(), var5, 90.0F));
                this.drowned.yBodyRot = this.drowned.getYRot();
                float var6 = (float)(this.speedModifier * this.drowned.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float var7 = Mth.lerp(0.125F, this.drowned.getSpeed(), var6);
                this.drowned.setSpeed(var7);
                this.drowned
                    .setDeltaMovement(this.drowned.getDeltaMovement().add((double)var7 * var1 * 0.005, (double)var7 * var2 * 0.1, (double)var7 * var3 * 0.005));
            } else {
                if (!this.drowned.onGround) {
                    this.drowned.setDeltaMovement(this.drowned.getDeltaMovement().add(0.0, -0.008, 0.0));
                }

                super.tick();
            }

        }
    }

    static class DrownedSwimUpGoal extends Goal {
        private final Drowned drowned;
        private final double speedModifier;
        private final int seaLevel;
        private boolean stuck;

        public DrownedSwimUpGoal(Drowned param0, double param1, int param2) {
            this.drowned = param0;
            this.speedModifier = param1;
            this.seaLevel = param2;
        }

        @Override
        public boolean canUse() {
            return !this.drowned.level.isDay() && this.drowned.isInWater() && this.drowned.getY() < (double)(this.seaLevel - 2);
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse() && !this.stuck;
        }

        @Override
        public void tick() {
            if (this.drowned.getY() < (double)(this.seaLevel - 1) && (this.drowned.getNavigation().isDone() || this.drowned.closeToNextPos())) {
                Vec3 var0 = DefaultRandomPos.getPosTowards(
                    this.drowned, 4, 8, new Vec3(this.drowned.getX(), (double)(this.seaLevel - 1), this.drowned.getZ()), (float) (Math.PI / 2)
                );
                if (var0 == null) {
                    this.stuck = true;
                    return;
                }

                this.drowned.getNavigation().moveTo(var0.x, var0.y, var0.z, this.speedModifier);
            }

        }

        @Override
        public void start() {
            this.drowned.setSearchingForLand(true);
            this.stuck = false;
        }

        @Override
        public void stop() {
            this.drowned.setSearchingForLand(false);
        }
    }

    static class DrownedTridentAttackGoal extends RangedAttackGoal {
        private final Drowned drowned;

        public DrownedTridentAttackGoal(RangedAttackMob param0, double param1, int param2, float param3) {
            super(param0, param1, param2, param3);
            this.drowned = (Drowned)param0;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && this.drowned.getMainHandItem().is(Items.TRIDENT);
        }

        @Override
        public void start() {
            super.start();
            this.drowned.setAggressive(true);
            this.drowned.startUsingItem(InteractionHand.MAIN_HAND);
        }

        @Override
        public void stop() {
            super.stop();
            this.drowned.stopUsingItem();
            this.drowned.setAggressive(false);
        }
    }
}
