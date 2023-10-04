package net.minecraft.world.entity.animal;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Turtle extends Animal {
    private static final EntityDataAccessor<BlockPos> HOME_POS = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<BlockPos> TRAVEL_POS = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> GOING_HOME = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> TRAVELLING = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
    public static final Ingredient FOOD_ITEMS = Ingredient.of(Blocks.SEAGRASS.asItem());
    int layEggCounter;
    public static final Predicate<LivingEntity> BABY_ON_LAND_SELECTOR = param0 -> param0.isBaby() && !param0.isInWater();

    public Turtle(EntityType<? extends Turtle> param0, Level param1) {
        super(param0, param1);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.setPathfindingMalus(BlockPathTypes.DOOR_IRON_CLOSED, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DOOR_WOOD_CLOSED, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DOOR_OPEN, -1.0F);
        this.moveControl = new Turtle.TurtleMoveControl(this);
        this.setMaxUpStep(1.0F);
    }

    public void setHomePos(BlockPos param0) {
        this.entityData.set(HOME_POS, param0);
    }

    BlockPos getHomePos() {
        return this.entityData.get(HOME_POS);
    }

    void setTravelPos(BlockPos param0) {
        this.entityData.set(TRAVEL_POS, param0);
    }

    BlockPos getTravelPos() {
        return this.entityData.get(TRAVEL_POS);
    }

    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    void setHasEgg(boolean param0) {
        this.entityData.set(HAS_EGG, param0);
    }

    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    void setLayingEgg(boolean param0) {
        this.layEggCounter = param0 ? 1 : 0;
        this.entityData.set(LAYING_EGG, param0);
    }

    boolean isGoingHome() {
        return this.entityData.get(GOING_HOME);
    }

    void setGoingHome(boolean param0) {
        this.entityData.set(GOING_HOME, param0);
    }

    boolean isTravelling() {
        return this.entityData.get(TRAVELLING);
    }

    void setTravelling(boolean param0) {
        this.entityData.set(TRAVELLING, param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HOME_POS, BlockPos.ZERO);
        this.entityData.define(HAS_EGG, false);
        this.entityData.define(TRAVEL_POS, BlockPos.ZERO);
        this.entityData.define(GOING_HOME, false);
        this.entityData.define(TRAVELLING, false);
        this.entityData.define(LAYING_EGG, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("HomePosX", this.getHomePos().getX());
        param0.putInt("HomePosY", this.getHomePos().getY());
        param0.putInt("HomePosZ", this.getHomePos().getZ());
        param0.putBoolean("HasEgg", this.hasEgg());
        param0.putInt("TravelPosX", this.getTravelPos().getX());
        param0.putInt("TravelPosY", this.getTravelPos().getY());
        param0.putInt("TravelPosZ", this.getTravelPos().getZ());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        int var0 = param0.getInt("HomePosX");
        int var1 = param0.getInt("HomePosY");
        int var2 = param0.getInt("HomePosZ");
        this.setHomePos(new BlockPos(var0, var1, var2));
        super.readAdditionalSaveData(param0);
        this.setHasEgg(param0.getBoolean("HasEgg"));
        int var3 = param0.getInt("TravelPosX");
        int var4 = param0.getInt("TravelPosY");
        int var5 = param0.getInt("TravelPosZ");
        this.setTravelPos(new BlockPos(var3, var4, var5));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setHomePos(this.blockPosition());
        this.setTravelPos(BlockPos.ZERO);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public static boolean checkTurtleSpawnRules(EntityType<Turtle> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4) {
        return param3.getY() < param1.getSeaLevel() + 4 && TurtleEggBlock.onSand(param1, param3) && isBrightEnoughToSpawn(param1, param3);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new Turtle.TurtlePanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new Turtle.TurtleBreedGoal(this, 1.0));
        this.goalSelector.addGoal(1, new Turtle.TurtleLayEggGoal(this, 1.0));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, FOOD_ITEMS, false));
        this.goalSelector.addGoal(3, new Turtle.TurtleGoToWaterGoal(this, 1.0));
        this.goalSelector.addGoal(4, new Turtle.TurtleGoHomeGoal(this, 1.0));
        this.goalSelector.addGoal(7, new Turtle.TurtleTravelGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(9, new Turtle.TurtleRandomStrollGoal(this, 1.0, 100));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public MobType getMobType() {
        return MobType.WATER;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return !this.isInWater() && this.onGround() && !this.isBaby() ? SoundEvents.TURTLE_AMBIENT_LAND : super.getAmbientSound();
    }

    @Override
    protected void playSwimSound(float param0) {
        super.playSwimSound(param0 * 1.5F);
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.TURTLE_SWIM;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return this.isBaby() ? SoundEvents.TURTLE_HURT_BABY : SoundEvents.TURTLE_HURT;
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? SoundEvents.TURTLE_DEATH_BABY : SoundEvents.TURTLE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        SoundEvent var0 = this.isBaby() ? SoundEvents.TURTLE_SHAMBLE_BABY : SoundEvents.TURTLE_SHAMBLE;
        this.playSound(var0, 0.15F, 1.0F);
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.hasEgg();
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.15F;
    }

    @Override
    public float getScale() {
        return this.isBaby() ? 0.3F : 1.0F;
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        return new Turtle.TurtlePathNavigation(this, param0);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        return EntityType.TURTLE.create(param0);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.is(Blocks.SEAGRASS.asItem());
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        if (!this.isGoingHome() && param1.getFluidState(param0).is(FluidTags.WATER)) {
            return 10.0F;
        } else {
            return TurtleEggBlock.onSand(param1, param0) ? 10.0F : param1.getPathfindingCostFromLightLevels(param0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
            BlockPos var0 = this.blockPosition();
            if (TurtleEggBlock.onSand(this.level(), var0)) {
                this.level().levelEvent(2001, var0, Block.getId(this.level().getBlockState(var0.below())));
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
        }

    }

    @Override
    protected void ageBoundaryReached() {
        super.ageBoundaryReached();
        if (!this.isBaby() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.spawnAtLocation(Items.SCUTE, 1);
        }

    }

    @Override
    public void travel(Vec3 param0) {
        if (this.isControlledByLocalInstance() && this.isInWater()) {
            this.moveRelative(0.1F, param0);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (this.getTarget() == null && (!this.isGoingHome() || !this.getHomePos().closerToCenterThan(this.position(), 20.0))) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(param0);
        }

    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return false;
    }

    @Override
    public void thunderHit(ServerLevel param0, LightningBolt param1) {
        this.hurt(this.damageSources().lightningBolt(), Float.MAX_VALUE);
    }

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity param0, EntityDimensions param1, float param2) {
        return new Vector3f(0.0F, param1.height + (this.isBaby() ? 0.0F : 0.15625F) * param2, -0.25F * param2);
    }

    static class TurtleBreedGoal extends BreedGoal {
        private final Turtle turtle;

        TurtleBreedGoal(Turtle param0, double param1) {
            super(param0, param1);
            this.turtle = param0;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.turtle.hasEgg();
        }

        @Override
        protected void breed() {
            ServerPlayer var0 = this.animal.getLoveCause();
            if (var0 == null && this.partner.getLoveCause() != null) {
                var0 = this.partner.getLoveCause();
            }

            if (var0 != null) {
                var0.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(var0, this.animal, this.partner, null);
            }

            this.turtle.setHasEgg(true);
            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            RandomSource var1 = this.animal.getRandom();
            if (this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), var1.nextInt(7) + 1));
            }

        }
    }

    static class TurtleGoHomeGoal extends Goal {
        private final Turtle turtle;
        private final double speedModifier;
        private boolean stuck;
        private int closeToHomeTryTicks;
        private static final int GIVE_UP_TICKS = 600;

        TurtleGoHomeGoal(Turtle param0, double param1) {
            this.turtle = param0;
            this.speedModifier = param1;
        }

        @Override
        public boolean canUse() {
            if (this.turtle.isBaby()) {
                return false;
            } else if (this.turtle.hasEgg()) {
                return true;
            } else if (this.turtle.getRandom().nextInt(reducedTickDelay(700)) != 0) {
                return false;
            } else {
                return !this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 64.0);
            }
        }

        @Override
        public void start() {
            this.turtle.setGoingHome(true);
            this.stuck = false;
            this.closeToHomeTryTicks = 0;
        }

        @Override
        public void stop() {
            this.turtle.setGoingHome(false);
        }

        @Override
        public boolean canContinueToUse() {
            return !this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 7.0)
                && !this.stuck
                && this.closeToHomeTryTicks <= this.adjustedTickDelay(600);
        }

        @Override
        public void tick() {
            BlockPos var0 = this.turtle.getHomePos();
            boolean var1 = var0.closerToCenterThan(this.turtle.position(), 16.0);
            if (var1) {
                ++this.closeToHomeTryTicks;
            }

            if (this.turtle.getNavigation().isDone()) {
                Vec3 var2 = Vec3.atBottomCenterOf(var0);
                Vec3 var3 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, var2, (float) (Math.PI / 10));
                if (var3 == null) {
                    var3 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, var2, (float) (Math.PI / 2));
                }

                if (var3 != null && !var1 && !this.turtle.level().getBlockState(BlockPos.containing(var3)).is(Blocks.WATER)) {
                    var3 = DefaultRandomPos.getPosTowards(this.turtle, 16, 5, var2, (float) (Math.PI / 2));
                }

                if (var3 == null) {
                    this.stuck = true;
                    return;
                }

                this.turtle.getNavigation().moveTo(var3.x, var3.y, var3.z, this.speedModifier);
            }

        }
    }

    static class TurtleGoToWaterGoal extends MoveToBlockGoal {
        private static final int GIVE_UP_TICKS = 1200;
        private final Turtle turtle;

        TurtleGoToWaterGoal(Turtle param0, double param1) {
            super(param0, param0.isBaby() ? 2.0 : param1, 24);
            this.turtle = param0;
            this.verticalSearchStart = -1;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.turtle.isInWater() && this.tryTicks <= 1200 && this.isValidTarget(this.turtle.level(), this.blockPos);
        }

        @Override
        public boolean canUse() {
            if (this.turtle.isBaby() && !this.turtle.isInWater()) {
                return super.canUse();
            } else {
                return !this.turtle.isGoingHome() && !this.turtle.isInWater() && !this.turtle.hasEgg() ? super.canUse() : false;
            }
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 160 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
            return param0.getBlockState(param1).is(Blocks.WATER);
        }
    }

    static class TurtleLayEggGoal extends MoveToBlockGoal {
        private final Turtle turtle;

        TurtleLayEggGoal(Turtle param0, double param1) {
            super(param0, param1, 16);
            this.turtle = param0;
        }

        @Override
        public boolean canUse() {
            return this.turtle.hasEgg() && this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 9.0) ? super.canUse() : false;
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.turtle.hasEgg() && this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 9.0);
        }

        @Override
        public void tick() {
            super.tick();
            BlockPos var0 = this.turtle.blockPosition();
            if (!this.turtle.isInWater() && this.isReachedTarget()) {
                if (this.turtle.layEggCounter < 1) {
                    this.turtle.setLayingEgg(true);
                } else if (this.turtle.layEggCounter > this.adjustedTickDelay(200)) {
                    Level var1 = this.turtle.level();
                    var1.playSound(null, var0, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + var1.random.nextFloat() * 0.2F);
                    BlockPos var2 = this.blockPos.above();
                    BlockState var3 = Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, Integer.valueOf(this.turtle.random.nextInt(4) + 1));
                    var1.setBlock(var2, var3, 3);
                    var1.gameEvent(GameEvent.BLOCK_PLACE, var2, GameEvent.Context.of(this.turtle, var3));
                    this.turtle.setHasEgg(false);
                    this.turtle.setLayingEgg(false);
                    this.turtle.setInLoveTime(600);
                }

                if (this.turtle.isLayingEgg()) {
                    ++this.turtle.layEggCounter;
                }
            }

        }

        @Override
        protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
            return !param0.isEmptyBlock(param1.above()) ? false : TurtleEggBlock.isSand(param0, param1);
        }
    }

    static class TurtleMoveControl extends MoveControl {
        private final Turtle turtle;

        TurtleMoveControl(Turtle param0) {
            super(param0);
            this.turtle = param0;
        }

        private void updateSpeed() {
            if (this.turtle.isInWater()) {
                this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, 0.005, 0.0));
                if (!this.turtle.getHomePos().closerToCenterThan(this.turtle.position(), 16.0)) {
                    this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.08F));
                }

                if (this.turtle.isBaby()) {
                    this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 3.0F, 0.06F));
                }
            } else if (this.turtle.onGround()) {
                this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0F, 0.06F));
            }

        }

        @Override
        public void tick() {
            this.updateSpeed();
            if (this.operation == MoveControl.Operation.MOVE_TO && !this.turtle.getNavigation().isDone()) {
                double var0 = this.wantedX - this.turtle.getX();
                double var1 = this.wantedY - this.turtle.getY();
                double var2 = this.wantedZ - this.turtle.getZ();
                double var3 = Math.sqrt(var0 * var0 + var1 * var1 + var2 * var2);
                if (var3 < 1.0E-5F) {
                    this.mob.setSpeed(0.0F);
                } else {
                    var1 /= var3;
                    float var4 = (float)(Mth.atan2(var2, var0) * 180.0F / (float)Math.PI) - 90.0F;
                    this.turtle.setYRot(this.rotlerp(this.turtle.getYRot(), var4, 90.0F));
                    this.turtle.yBodyRot = this.turtle.getYRot();
                    float var5 = (float)(this.speedModifier * this.turtle.getAttributeValue(Attributes.MOVEMENT_SPEED));
                    this.turtle.setSpeed(Mth.lerp(0.125F, this.turtle.getSpeed(), var5));
                    this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, (double)this.turtle.getSpeed() * var1 * 0.1, 0.0));
                }
            } else {
                this.turtle.setSpeed(0.0F);
            }
        }
    }

    static class TurtlePanicGoal extends PanicGoal {
        TurtlePanicGoal(Turtle param0, double param1) {
            super(param0, param1);
        }

        @Override
        public boolean canUse() {
            if (!this.shouldPanic()) {
                return false;
            } else {
                BlockPos var0 = this.lookForWater(this.mob.level(), this.mob, 7);
                if (var0 != null) {
                    this.posX = (double)var0.getX();
                    this.posY = (double)var0.getY();
                    this.posZ = (double)var0.getZ();
                    return true;
                } else {
                    return this.findRandomPosition();
                }
            }
        }
    }

    static class TurtlePathNavigation extends AmphibiousPathNavigation {
        TurtlePathNavigation(Turtle param0, Level param1) {
            super(param0, param1);
        }

        @Override
        public boolean isStableDestination(BlockPos param0) {
            Mob var3 = this.mob;
            if (var3 instanceof Turtle var0 && var0.isTravelling()) {
                return this.level.getBlockState(param0).is(Blocks.WATER);
            }

            return !this.level.getBlockState(param0.below()).isAir();
        }
    }

    static class TurtleRandomStrollGoal extends RandomStrollGoal {
        private final Turtle turtle;

        TurtleRandomStrollGoal(Turtle param0, double param1, int param2) {
            super(param0, param1, param2);
            this.turtle = param0;
        }

        @Override
        public boolean canUse() {
            return !this.mob.isInWater() && !this.turtle.isGoingHome() && !this.turtle.hasEgg() ? super.canUse() : false;
        }
    }

    static class TurtleTravelGoal extends Goal {
        private final Turtle turtle;
        private final double speedModifier;
        private boolean stuck;

        TurtleTravelGoal(Turtle param0, double param1) {
            this.turtle = param0;
            this.speedModifier = param1;
        }

        @Override
        public boolean canUse() {
            return !this.turtle.isGoingHome() && !this.turtle.hasEgg() && this.turtle.isInWater();
        }

        @Override
        public void start() {
            int var0 = 512;
            int var1 = 4;
            RandomSource var2 = this.turtle.random;
            int var3 = var2.nextInt(1025) - 512;
            int var4 = var2.nextInt(9) - 4;
            int var5 = var2.nextInt(1025) - 512;
            if ((double)var4 + this.turtle.getY() > (double)(this.turtle.level().getSeaLevel() - 1)) {
                var4 = 0;
            }

            BlockPos var6 = BlockPos.containing((double)var3 + this.turtle.getX(), (double)var4 + this.turtle.getY(), (double)var5 + this.turtle.getZ());
            this.turtle.setTravelPos(var6);
            this.turtle.setTravelling(true);
            this.stuck = false;
        }

        @Override
        public void tick() {
            if (this.turtle.getNavigation().isDone()) {
                Vec3 var0 = Vec3.atBottomCenterOf(this.turtle.getTravelPos());
                Vec3 var1 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, var0, (float) (Math.PI / 10));
                if (var1 == null) {
                    var1 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, var0, (float) (Math.PI / 2));
                }

                if (var1 != null) {
                    int var2 = Mth.floor(var1.x);
                    int var3 = Mth.floor(var1.z);
                    int var4 = 34;
                    if (!this.turtle.level().hasChunksAt(var2 - 34, var3 - 34, var2 + 34, var3 + 34)) {
                        var1 = null;
                    }
                }

                if (var1 == null) {
                    this.stuck = true;
                    return;
                }

                this.turtle.getNavigation().moveTo(var1.x, var1.y, var1.z, this.speedModifier);
            }

        }

        @Override
        public boolean canContinueToUse() {
            return !this.turtle.getNavigation().isDone() && !this.stuck && !this.turtle.isGoingHome() && !this.turtle.isInLove() && !this.turtle.hasEgg();
        }

        @Override
        public void stop() {
            this.turtle.setTravelling(false);
            super.stop();
        }
    }
}
