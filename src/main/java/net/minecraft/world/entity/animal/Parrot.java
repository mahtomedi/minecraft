package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowMobGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LandOnOwnersShoulderGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class Parrot extends ShoulderRidingEntity implements VariantHolder<Parrot.Variant>, FlyingAnimal {
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Parrot.class, EntityDataSerializers.INT);
    private static final Predicate<Mob> NOT_PARROT_PREDICATE = new Predicate<Mob>() {
        public boolean test(@Nullable Mob param0) {
            return param0 != null && Parrot.MOB_SOUND_MAP.containsKey(param0.getType());
        }
    };
    private static final Item POISONOUS_FOOD = Items.COOKIE;
    private static final Set<Item> TAME_FOOD = Sets.newHashSet(Items.WHEAT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.BEETROOT_SEEDS);
    static final Map<EntityType<?>, SoundEvent> MOB_SOUND_MAP = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(EntityType.BLAZE, SoundEvents.PARROT_IMITATE_BLAZE);
        param0.put(EntityType.CAVE_SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        param0.put(EntityType.CREEPER, SoundEvents.PARROT_IMITATE_CREEPER);
        param0.put(EntityType.DROWNED, SoundEvents.PARROT_IMITATE_DROWNED);
        param0.put(EntityType.ELDER_GUARDIAN, SoundEvents.PARROT_IMITATE_ELDER_GUARDIAN);
        param0.put(EntityType.ENDER_DRAGON, SoundEvents.PARROT_IMITATE_ENDER_DRAGON);
        param0.put(EntityType.ENDERMITE, SoundEvents.PARROT_IMITATE_ENDERMITE);
        param0.put(EntityType.EVOKER, SoundEvents.PARROT_IMITATE_EVOKER);
        param0.put(EntityType.GHAST, SoundEvents.PARROT_IMITATE_GHAST);
        param0.put(EntityType.GUARDIAN, SoundEvents.PARROT_IMITATE_GUARDIAN);
        param0.put(EntityType.HOGLIN, SoundEvents.PARROT_IMITATE_HOGLIN);
        param0.put(EntityType.HUSK, SoundEvents.PARROT_IMITATE_HUSK);
        param0.put(EntityType.ILLUSIONER, SoundEvents.PARROT_IMITATE_ILLUSIONER);
        param0.put(EntityType.MAGMA_CUBE, SoundEvents.PARROT_IMITATE_MAGMA_CUBE);
        param0.put(EntityType.PHANTOM, SoundEvents.PARROT_IMITATE_PHANTOM);
        param0.put(EntityType.PIGLIN, SoundEvents.PARROT_IMITATE_PIGLIN);
        param0.put(EntityType.PIGLIN_BRUTE, SoundEvents.PARROT_IMITATE_PIGLIN_BRUTE);
        param0.put(EntityType.PILLAGER, SoundEvents.PARROT_IMITATE_PILLAGER);
        param0.put(EntityType.RAVAGER, SoundEvents.PARROT_IMITATE_RAVAGER);
        param0.put(EntityType.SHULKER, SoundEvents.PARROT_IMITATE_SHULKER);
        param0.put(EntityType.SILVERFISH, SoundEvents.PARROT_IMITATE_SILVERFISH);
        param0.put(EntityType.SKELETON, SoundEvents.PARROT_IMITATE_SKELETON);
        param0.put(EntityType.SLIME, SoundEvents.PARROT_IMITATE_SLIME);
        param0.put(EntityType.SPIDER, SoundEvents.PARROT_IMITATE_SPIDER);
        param0.put(EntityType.STRAY, SoundEvents.PARROT_IMITATE_STRAY);
        param0.put(EntityType.VEX, SoundEvents.PARROT_IMITATE_VEX);
        param0.put(EntityType.VINDICATOR, SoundEvents.PARROT_IMITATE_VINDICATOR);
        param0.put(EntityType.WARDEN, SoundEvents.PARROT_IMITATE_WARDEN);
        param0.put(EntityType.WITCH, SoundEvents.PARROT_IMITATE_WITCH);
        param0.put(EntityType.WITHER, SoundEvents.PARROT_IMITATE_WITHER);
        param0.put(EntityType.WITHER_SKELETON, SoundEvents.PARROT_IMITATE_WITHER_SKELETON);
        param0.put(EntityType.ZOGLIN, SoundEvents.PARROT_IMITATE_ZOGLIN);
        param0.put(EntityType.ZOMBIE, SoundEvents.PARROT_IMITATE_ZOMBIE);
        param0.put(EntityType.ZOMBIE_VILLAGER, SoundEvents.PARROT_IMITATE_ZOMBIE_VILLAGER);
    });
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    private float flapping = 1.0F;
    private float nextFlap = 1.0F;
    private boolean partyParrot;
    @Nullable
    private BlockPos jukebox;

    public Parrot(EntityType<? extends Parrot> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new FlyingMoveControl(this, 10, false);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setVariant(Util.getRandom(Parrot.Variant.values(), param0.getRandom()));
        if (param3 == null) {
            param3 = new AgeableMob.AgeableMobGroupData(false);
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.0, 5.0F, 1.0F, true));
        this.goalSelector.addGoal(2, new Parrot.ParrotWanderGoal(this, 1.0));
        this.goalSelector.addGoal(3, new LandOnOwnersShoulderGoal(this));
        this.goalSelector.addGoal(3, new FollowMobGoal(this, 1.0, 3.0F, 7.0F));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.FLYING_SPEED, 0.4F).add(Attributes.MOVEMENT_SPEED, 0.2F);
    }

    @Override
    protected PathNavigation createNavigation(Level param0) {
        FlyingPathNavigation var0 = new FlyingPathNavigation(this, param0);
        var0.setCanOpenDoors(false);
        var0.setCanFloat(true);
        var0.setCanPassDoors(true);
        return var0;
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.6F;
    }

    @Override
    public void aiStep() {
        if (this.jukebox == null || !this.jukebox.closerToCenterThan(this.position(), 3.46) || !this.level.getBlockState(this.jukebox).is(Blocks.JUKEBOX)) {
            this.partyParrot = false;
            this.jukebox = null;
        }

        if (this.level.random.nextInt(400) == 0) {
            imitateNearbyMobs(this.level, this);
        }

        super.aiStep();
        this.calculateFlapping();
    }

    @Override
    public void setRecordPlayingNearby(BlockPos param0, boolean param1) {
        this.jukebox = param0;
        this.partyParrot = param1;
    }

    public boolean isPartyParrot() {
        return this.partyParrot;
    }

    private void calculateFlapping() {
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (float)(!this.onGround && !this.isPassenger() ? 4 : -1) * 0.3F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 1.0F);
        if (!this.onGround && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 var0 = this.getDeltaMovement();
        if (!this.onGround && var0.y < 0.0) {
            this.setDeltaMovement(var0.multiply(1.0, 0.6, 1.0));
        }

        this.flap += this.flapping * 2.0F;
    }

    public static boolean imitateNearbyMobs(Level param0, Entity param1) {
        if (param1.isAlive() && !param1.isSilent() && param0.random.nextInt(2) == 0) {
            List<Mob> var0 = param0.getEntitiesOfClass(Mob.class, param1.getBoundingBox().inflate(20.0), NOT_PARROT_PREDICATE);
            if (!var0.isEmpty()) {
                Mob var1 = var0.get(param0.random.nextInt(var0.size()));
                if (!var1.isSilent()) {
                    SoundEvent var2 = getImitatedSound(var1.getType());
                    param0.playSound(null, param1.getX(), param1.getY(), param1.getZ(), var2, param1.getSoundSource(), 0.7F, getPitch(param0.random));
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (!this.isTame() && TAME_FOOD.contains(var0.getItem())) {
            if (!param0.getAbilities().instabuild) {
                var0.shrink(1);
            }

            if (!this.isSilent()) {
                this.level
                    .playSound(
                        null,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        SoundEvents.PARROT_EAT,
                        this.getSoundSource(),
                        1.0F,
                        1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                    );
            }

            if (!this.level.isClientSide) {
                if (this.random.nextInt(10) == 0) {
                    this.tame(param0);
                    this.level.broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level.broadcastEntityEvent(this, (byte)6);
                }
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else if (var0.is(POISONOUS_FOOD)) {
            if (!param0.getAbilities().instabuild) {
                var0.shrink(1);
            }

            this.addEffect(new MobEffectInstance(MobEffects.POISON, 900));
            if (param0.isCreative() || !this.isInvulnerable()) {
                this.hurt(DamageSource.playerAttack(param0), Float.MAX_VALUE);
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else if (!this.isFlying() && this.isTame() && this.isOwnedBy(param0)) {
            if (!this.level.isClientSide) {
                this.setOrderedToSit(!this.isOrderedToSit());
            }

            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(param0, param1);
        }
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return false;
    }

    public static boolean checkParrotSpawnRules(EntityType<Parrot> param0, LevelAccessor param1, MobSpawnType param2, BlockPos param3, RandomSource param4) {
        return param1.getBlockState(param3.below()).is(BlockTags.PARROTS_SPAWNABLE_ON) && isBrightEnoughToSpawn(param1, param3);
    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        return false;
    }

    @Override
    protected void checkFallDamage(double param0, boolean param1, BlockState param2, BlockPos param3) {
    }

    @Override
    public boolean canMate(Animal param0) {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        return null;
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        return param0.hurt(DamageSource.mobAttack(this), 3.0F);
    }

    @Nullable
    @Override
    public SoundEvent getAmbientSound() {
        return getAmbient(this.level, this.level.random);
    }

    public static SoundEvent getAmbient(Level param0, RandomSource param1) {
        if (param0.getDifficulty() != Difficulty.PEACEFUL && param1.nextInt(1000) == 0) {
            List<EntityType<?>> var0 = Lists.newArrayList(MOB_SOUND_MAP.keySet());
            return getImitatedSound(var0.get(param1.nextInt(var0.size())));
        } else {
            return SoundEvents.PARROT_AMBIENT;
        }
    }

    private static SoundEvent getImitatedSound(EntityType<?> param0) {
        return MOB_SOUND_MAP.getOrDefault(param0, SoundEvents.PARROT_AMBIENT);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.PARROT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PARROT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.PARROT_STEP, 0.15F, 1.0F);
    }

    @Override
    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    @Override
    protected void onFlap() {
        this.playSound(SoundEvents.PARROT_FLY, 0.15F, 1.0F);
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    @Override
    public float getVoicePitch() {
        return getPitch(this.random);
    }

    public static float getPitch(RandomSource param0) {
        return (param0.nextFloat() - param0.nextFloat()) * 0.2F + 1.0F;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    protected void doPush(Entity param0) {
        if (!(param0 instanceof Player)) {
            super.doPush(param0);
        }
    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        if (this.isInvulnerableTo(param0)) {
            return false;
        } else {
            if (!this.level.isClientSide) {
                this.setOrderedToSit(false);
            }

            return super.hurt(param0, param1);
        }
    }

    public Parrot.Variant getVariant() {
        return Parrot.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    public void setVariant(Parrot.Variant param0) {
        this.entityData.set(DATA_VARIANT_ID, param0.id);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_VARIANT_ID, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Variant", this.getVariant().id);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setVariant(Parrot.Variant.byId(param0.getInt("Variant")));
    }

    @Override
    public boolean isFlying() {
        return !this.onGround;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.4F));
    }

    static class ParrotWanderGoal extends WaterAvoidingRandomFlyingGoal {
        public ParrotWanderGoal(PathfinderMob param0, double param1) {
            super(param0, param1);
        }

        @Nullable
        @Override
        protected Vec3 getPosition() {
            Vec3 var0 = null;
            if (this.mob.isInWater()) {
                var0 = LandRandomPos.getPos(this.mob, 15, 15);
            }

            if (this.mob.getRandom().nextFloat() >= this.probability) {
                var0 = this.getTreePos();
            }

            return var0 == null ? super.getPosition() : var0;
        }

        @Nullable
        private Vec3 getTreePos() {
            BlockPos var0 = this.mob.blockPosition();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos var2 = new BlockPos.MutableBlockPos();

            for(BlockPos var4 : BlockPos.betweenClosed(
                Mth.floor(this.mob.getX() - 3.0),
                Mth.floor(this.mob.getY() - 6.0),
                Mth.floor(this.mob.getZ() - 3.0),
                Mth.floor(this.mob.getX() + 3.0),
                Mth.floor(this.mob.getY() + 6.0),
                Mth.floor(this.mob.getZ() + 3.0)
            )) {
                if (!var0.equals(var4)) {
                    BlockState var5 = this.mob.level.getBlockState(var2.setWithOffset(var4, Direction.DOWN));
                    boolean var6 = var5.getBlock() instanceof LeavesBlock || var5.is(BlockTags.LOGS);
                    if (var6 && this.mob.level.isEmptyBlock(var4) && this.mob.level.isEmptyBlock(var1.setWithOffset(var4, Direction.UP))) {
                        return Vec3.atBottomCenterOf(var4);
                    }
                }
            }

            return null;
        }
    }

    public static enum Variant implements StringRepresentable {
        RED_BLUE(0, "red_blue"),
        BLUE(1, "blue"),
        GREEN(2, "green"),
        YELLOW_BLUE(3, "yellow_blue"),
        GRAY(4, "gray");

        public static final Codec<Parrot.Variant> CODEC = StringRepresentable.fromEnum(Parrot.Variant::values);
        private static final IntFunction<Parrot.Variant> BY_ID = ByIdMap.continuous(Parrot.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        final int id;
        private final String name;

        private Variant(int param0, String param1) {
            this.id = param0;
            this.name = param1;
        }

        public int getId() {
            return this.id;
        }

        public static Parrot.Variant byId(int param0) {
            return BY_ID.apply(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
