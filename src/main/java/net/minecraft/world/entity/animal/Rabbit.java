package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.ClimbOnTopOfPowderSnowGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class Rabbit extends Animal implements VariantHolder<Rabbit.Variant> {
    public static final double STROLL_SPEED_MOD = 0.6;
    public static final double BREED_SPEED_MOD = 0.8;
    public static final double FOLLOW_SPEED_MOD = 1.0;
    public static final double FLEE_SPEED_MOD = 2.2;
    public static final double ATTACK_SPEED_MOD = 1.4;
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Rabbit.class, EntityDataSerializers.INT);
    private static final ResourceLocation KILLER_BUNNY = new ResourceLocation("killer_bunny");
    public static final int EVIL_ATTACK_POWER = 8;
    public static final int EVIL_ARMOR_VALUE = 8;
    private static final int MORE_CARROTS_DELAY = 40;
    private int jumpTicks;
    private int jumpDuration;
    private boolean wasOnGround;
    private int jumpDelayTicks;
    int moreCarrotTicks;

    public Rabbit(EntityType<? extends Rabbit> param0, Level param1) {
        super(param0, param1);
        this.jumpControl = new Rabbit.RabbitJumpControl(this);
        this.moveControl = new Rabbit.RabbitMoveControl(this);
        this.setSpeedModifier(0.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ClimbOnTopOfPowderSnowGoal(this, this.level));
        this.goalSelector.addGoal(1, new Rabbit.RabbitPanicGoal(this, 2.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, Ingredient.of(Items.CARROT, Items.GOLDEN_CARROT, Blocks.DANDELION), false));
        this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Player.class, 8.0F, 2.2, 2.2));
        this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Wolf.class, 10.0F, 2.2, 2.2));
        this.goalSelector.addGoal(4, new Rabbit.RabbitAvoidEntityGoal<>(this, Monster.class, 4.0F, 2.2, 2.2));
        this.goalSelector.addGoal(5, new Rabbit.RaidGardenGoal(this));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10.0F));
    }

    @Override
    protected float getJumpPower() {
        if (!this.horizontalCollision && (!this.moveControl.hasWanted() || !(this.moveControl.getWantedY() > this.getY() + 0.5))) {
            Path var0 = this.navigation.getPath();
            if (var0 != null && !var0.isDone()) {
                Vec3 var1 = var0.getNextEntityPos(this);
                if (var1.y > this.getY() + 0.5) {
                    return 0.5F;
                }
            }

            return this.moveControl.getSpeedModifier() <= 0.6 ? 0.2F : 0.3F;
        } else {
            return 0.5F;
        }
    }

    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        double var0 = this.moveControl.getSpeedModifier();
        if (var0 > 0.0) {
            double var1 = this.getDeltaMovement().horizontalDistanceSqr();
            if (var1 < 0.01) {
                this.moveRelative(0.1F, new Vec3(0.0, 0.0, 1.0));
            }
        }

        if (!this.level.isClientSide) {
            this.level.broadcastEntityEvent(this, (byte)1);
        }

    }

    public float getJumpCompletion(float param0) {
        return this.jumpDuration == 0 ? 0.0F : ((float)this.jumpTicks + param0) / (float)this.jumpDuration;
    }

    public void setSpeedModifier(double param0) {
        this.getNavigation().setSpeedModifier(param0);
        this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), param0);
    }

    @Override
    public void setJumping(boolean param0) {
        super.setJumping(param0);
        if (param0) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
        }

    }

    public void startJumping() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, Rabbit.Variant.BROWN.id);
    }

    @Override
    public void customServerAiStep() {
        if (this.jumpDelayTicks > 0) {
            --this.jumpDelayTicks;
        }

        if (this.moreCarrotTicks > 0) {
            this.moreCarrotTicks -= this.random.nextInt(3);
            if (this.moreCarrotTicks < 0) {
                this.moreCarrotTicks = 0;
            }
        }

        if (this.onGround) {
            if (!this.wasOnGround) {
                this.setJumping(false);
                this.checkLandingDelay();
            }

            if (this.getVariant() == Rabbit.Variant.EVIL && this.jumpDelayTicks == 0) {
                LivingEntity var0 = this.getTarget();
                if (var0 != null && this.distanceToSqr(var0) < 16.0) {
                    this.facePoint(var0.getX(), var0.getZ());
                    this.moveControl.setWantedPosition(var0.getX(), var0.getY(), var0.getZ(), this.moveControl.getSpeedModifier());
                    this.startJumping();
                    this.wasOnGround = true;
                }
            }

            Rabbit.RabbitJumpControl var1 = (Rabbit.RabbitJumpControl)this.jumpControl;
            if (!var1.wantJump()) {
                if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
                    Path var2 = this.navigation.getPath();
                    Vec3 var3 = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
                    if (var2 != null && !var2.isDone()) {
                        var3 = var2.getNextEntityPos(this);
                    }

                    this.facePoint(var3.x, var3.z);
                    this.startJumping();
                }
            } else if (!var1.canJump()) {
                this.enableJumpControl();
            }
        }

        this.wasOnGround = this.onGround;
    }

    @Override
    public boolean canSpawnSprintParticle() {
        return false;
    }

    private void facePoint(double param0, double param1) {
        this.setYRot((float)(Mth.atan2(param1 - this.getZ(), param0 - this.getX()) * 180.0F / (float)Math.PI) - 90.0F);
    }

    private void enableJumpControl() {
        ((Rabbit.RabbitJumpControl)this.jumpControl).setCanJump(true);
    }

    private void disableJumpControl() {
        ((Rabbit.RabbitJumpControl)this.jumpControl).setCanJump(false);
    }

    private void setLandingDelay() {
        if (this.moveControl.getSpeedModifier() < 2.2) {
            this.jumpDelayTicks = 10;
        } else {
            this.jumpDelayTicks = 1;
        }

    }

    private void checkLandingDelay() {
        this.setLandingDelay();
        this.disableJumpControl();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }

    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0).add(Attributes.MOVEMENT_SPEED, 0.3F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("RabbitType", this.getVariant().id);
        param0.putInt("MoreCarrotTicks", this.moreCarrotTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setVariant(Rabbit.Variant.byId(param0.getInt("RabbitType")));
        this.moreCarrotTicks = param0.getInt("MoreCarrotTicks");
    }

    protected SoundEvent getJumpSound() {
        return SoundEvents.RABBIT_JUMP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.RABBIT_DEATH;
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        if (this.getVariant() == Rabbit.Variant.EVIL) {
            this.playSound(SoundEvents.RABBIT_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            return param0.hurt(DamageSource.mobAttack(this), 8.0F);
        } else {
            return param0.hurt(DamageSource.mobAttack(this), 3.0F);
        }
    }

    @Override
    public SoundSource getSoundSource() {
        return this.getVariant() == Rabbit.Variant.EVIL ? SoundSource.HOSTILE : SoundSource.NEUTRAL;
    }

    private static boolean isTemptingItem(ItemStack param0) {
        return param0.is(Items.CARROT) || param0.is(Items.GOLDEN_CARROT) || param0.is(Blocks.DANDELION.asItem());
    }

    @Nullable
    public Rabbit getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        Rabbit var0 = EntityType.RABBIT.create(param0);
        if (var0 != null) {
            Rabbit.Variant var1;
            var1 = getRandomRabbitVariant(param0, this.blockPosition());
            label16:
            if (this.random.nextInt(20) != 0) {
                if (param1 instanceof Rabbit var2 && this.random.nextBoolean()) {
                    var1 = var2.getVariant();
                    break label16;
                }

                var1 = this.getVariant();
            }

            var0.setVariant(var1);
        }

        return var0;
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return isTemptingItem(param0);
    }

    public Rabbit.Variant getVariant() {
        return Rabbit.Variant.byId(this.entityData.get(DATA_TYPE_ID));
    }

    public void setVariant(Rabbit.Variant param0) {
        if (param0 == Rabbit.Variant.EVIL) {
            this.getAttribute(Attributes.ARMOR).setBaseValue(8.0);
            this.goalSelector.addGoal(4, new Rabbit.EvilRabbitAttackGoal(this));
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
            this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Wolf.class, true));
            if (!this.hasCustomName()) {
                this.setCustomName(Component.translatable(Util.makeDescriptionId("entity", KILLER_BUNNY)));
            }
        }

        this.entityData.set(DATA_TYPE_ID, param0.id);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        Rabbit.Variant var0 = getRandomRabbitVariant(param0, this.blockPosition());
        if (param3 instanceof Rabbit.RabbitGroupData) {
            var0 = ((Rabbit.RabbitGroupData)param3).variant;
        } else {
            param3 = new Rabbit.RabbitGroupData(var0);
        }

        this.setVariant(var0);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    private static Rabbit.Variant getRandomRabbitVariant(LevelAccessor param0, BlockPos param1) {
        Holder<Biome> var0 = param0.getBiome(param1);
        int var1 = param0.getRandom().nextInt(100);
        if (var0.value().getPrecipitation() == Biome.Precipitation.SNOW) {
            return var1 < 80 ? Rabbit.Variant.WHITE : Rabbit.Variant.WHITE_SPLOTCHED;
        } else if (var0.is(BiomeTags.ONLY_ALLOWS_SNOW_AND_GOLD_RABBITS)) {
            return Rabbit.Variant.GOLD;
        } else {
            return var1 < 50 ? Rabbit.Variant.BROWN : (var1 < 90 ? Rabbit.Variant.SALT : Rabbit.Variant.BLACK);
        }
    }

    public static boolean checkRabbitSpawnRules(EntityType<Rabbit> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4) {
        return param1.getBlockState(param3.below()).is(BlockTags.RABBITS_SPAWNABLE_ON) && isBrightEnoughToSpawn(param1, param3);
    }

    boolean wantsMoreFood() {
        return this.moreCarrotTicks <= 0;
    }

    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 1) {
            this.spawnSprintParticle();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleEntityEvent(param0);
        }

    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.6F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    static class EvilRabbitAttackGoal extends MeleeAttackGoal {
        public EvilRabbitAttackGoal(Rabbit param0) {
            super(param0, 1.4, true);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity param0) {
            return (double)(4.0F + param0.getBbWidth());
        }
    }

    static class RabbitAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Rabbit rabbit;

        public RabbitAvoidEntityGoal(Rabbit param0, Class<T> param1, float param2, double param3, double param4) {
            super(param0, param1, param2, param3, param4);
            this.rabbit = param0;
        }

        @Override
        public boolean canUse() {
            return this.rabbit.getVariant() != Rabbit.Variant.EVIL && super.canUse();
        }
    }

    public static class RabbitGroupData extends AgeableMob.AgeableMobGroupData {
        public final Rabbit.Variant variant;

        public RabbitGroupData(Rabbit.Variant param0) {
            super(1.0F);
            this.variant = param0;
        }
    }

    public static class RabbitJumpControl extends JumpControl {
        private final Rabbit rabbit;
        private boolean canJump;

        public RabbitJumpControl(Rabbit param0) {
            super(param0);
            this.rabbit = param0;
        }

        public boolean wantJump() {
            return this.jump;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean param0) {
            this.canJump = param0;
        }

        @Override
        public void tick() {
            if (this.jump) {
                this.rabbit.startJumping();
                this.jump = false;
            }

        }
    }

    static class RabbitMoveControl extends MoveControl {
        private final Rabbit rabbit;
        private double nextJumpSpeed;

        public RabbitMoveControl(Rabbit param0) {
            super(param0);
            this.rabbit = param0;
        }

        @Override
        public void tick() {
            if (this.rabbit.onGround && !this.rabbit.jumping && !((Rabbit.RabbitJumpControl)this.rabbit.jumpControl).wantJump()) {
                this.rabbit.setSpeedModifier(0.0);
            } else if (this.hasWanted()) {
                this.rabbit.setSpeedModifier(this.nextJumpSpeed);
            }

            super.tick();
        }

        @Override
        public void setWantedPosition(double param0, double param1, double param2, double param3) {
            if (this.rabbit.isInWater()) {
                param3 = 1.5;
            }

            super.setWantedPosition(param0, param1, param2, param3);
            if (param3 > 0.0) {
                this.nextJumpSpeed = param3;
            }

        }
    }

    static class RabbitPanicGoal extends PanicGoal {
        private final Rabbit rabbit;

        public RabbitPanicGoal(Rabbit param0, double param1) {
            super(param0, param1);
            this.rabbit = param0;
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.setSpeedModifier(this.speedModifier);
        }
    }

    static class RaidGardenGoal extends MoveToBlockGoal {
        private final Rabbit rabbit;
        private boolean wantsToRaid;
        private boolean canRaid;

        public RaidGardenGoal(Rabbit param0) {
            super(param0, 0.7F, 16);
            this.rabbit = param0;
        }

        @Override
        public boolean canUse() {
            if (this.nextStartTick <= 0) {
                if (!this.rabbit.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                    return false;
                }

                this.canRaid = false;
                this.wantsToRaid = this.rabbit.wantsMoreFood();
            }

            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canRaid && super.canContinueToUse();
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit
                .getLookControl()
                .setLookAt(
                    (double)this.blockPos.getX() + 0.5,
                    (double)(this.blockPos.getY() + 1),
                    (double)this.blockPos.getZ() + 0.5,
                    10.0F,
                    (float)this.rabbit.getMaxHeadXRot()
                );
            if (this.isReachedTarget()) {
                Level var0 = this.rabbit.level;
                BlockPos var1 = this.blockPos.above();
                BlockState var2 = var0.getBlockState(var1);
                Block var3 = var2.getBlock();
                if (this.canRaid && var3 instanceof CarrotBlock) {
                    int var4 = var2.getValue(CarrotBlock.AGE);
                    if (var4 == 0) {
                        var0.setBlock(var1, Blocks.AIR.defaultBlockState(), 2);
                        var0.destroyBlock(var1, true, this.rabbit);
                    } else {
                        var0.setBlock(var1, var2.setValue(CarrotBlock.AGE, Integer.valueOf(var4 - 1)), 2);
                        var0.levelEvent(2001, var1, Block.getId(var2));
                    }

                    this.rabbit.moreCarrotTicks = 40;
                }

                this.canRaid = false;
                this.nextStartTick = 10;
            }

        }

        @Override
        protected boolean isValidTarget(LevelReader param0, BlockPos param1) {
            BlockState var0 = param0.getBlockState(param1);
            if (var0.is(Blocks.FARMLAND) && this.wantsToRaid && !this.canRaid) {
                var0 = param0.getBlockState(param1.above());
                if (var0.getBlock() instanceof CarrotBlock && ((CarrotBlock)var0.getBlock()).isMaxAge(var0)) {
                    this.canRaid = true;
                    return true;
                }
            }

            return false;
        }
    }

    public static enum Variant implements StringRepresentable {
        BROWN(0, "brown"),
        WHITE(1, "white"),
        BLACK(2, "black"),
        WHITE_SPLOTCHED(3, "white_splotched"),
        GOLD(4, "gold"),
        SALT(5, "salt"),
        EVIL(99, "evil");

        private static final IntFunction<Rabbit.Variant> BY_ID = ByIdMap.sparse(Rabbit.Variant::id, values(), BROWN);
        public static final Codec<Rabbit.Variant> CODEC = StringRepresentable.fromEnum(Rabbit.Variant::values);
        final int id;
        private final String name;

        private Variant(int param0, String param1) {
            this.id = param0;
            this.name = param1;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public int id() {
            return this.id;
        }

        public static Rabbit.Variant byId(int param0) {
            return BY_ID.apply(param0);
        }
    }
}
