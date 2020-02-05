package net.minecraft.world.entity.animal;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.CatLieOnBedGoal;
import net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.OcelotAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Cat extends TamableAnimal {
    private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(Items.COD, Items.SALMON);
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RELAX_STATE_ONE = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Cat.class, EntityDataSerializers.INT);
    public static final Map<Integer, ResourceLocation> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), param0 -> {
        param0.put(0, new ResourceLocation("textures/entity/cat/tabby.png"));
        param0.put(1, new ResourceLocation("textures/entity/cat/black.png"));
        param0.put(2, new ResourceLocation("textures/entity/cat/red.png"));
        param0.put(3, new ResourceLocation("textures/entity/cat/siamese.png"));
        param0.put(4, new ResourceLocation("textures/entity/cat/british_shorthair.png"));
        param0.put(5, new ResourceLocation("textures/entity/cat/calico.png"));
        param0.put(6, new ResourceLocation("textures/entity/cat/persian.png"));
        param0.put(7, new ResourceLocation("textures/entity/cat/ragdoll.png"));
        param0.put(8, new ResourceLocation("textures/entity/cat/white.png"));
        param0.put(9, new ResourceLocation("textures/entity/cat/jellie.png"));
        param0.put(10, new ResourceLocation("textures/entity/cat/all_black.png"));
    });
    private Cat.CatAvoidEntityGoal<Player> avoidPlayersGoal;
    private TemptGoal temptGoal;
    private float lieDownAmount;
    private float lieDownAmountO;
    private float lieDownAmountTail;
    private float lieDownAmountOTail;
    private float relaxStateOneAmount;
    private float relaxStateOneAmountO;

    public Cat(EntityType<? extends Cat> param0, Level param1) {
        super(param0, param1);
    }

    public ResourceLocation getResourceLocation() {
        return TEXTURE_BY_TYPE.getOrDefault(this.getCatType(), TEXTURE_BY_TYPE.get(0));
    }

    @Override
    protected void registerGoals() {
        this.temptGoal = new Cat.CatTemptGoal(this, 0.6, TEMPT_INGREDIENT, true);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new Cat.CatRelaxOnOwnerGoal(this));
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(5, new CatLieOnBedGoal(this, 1.1, 8));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 5.0F, false));
        this.goalSelector.addGoal(7, new CatSitOnBlockGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
        this.goalSelector.addGoal(9, new OcelotAttackGoal(this));
        this.goalSelector.addGoal(10, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>(this, Rabbit.class, false, null));
        this.targetSelector.addGoal(1, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public int getCatType() {
        return this.entityData.get(DATA_TYPE_ID);
    }

    public void setCatType(int param0) {
        if (param0 < 0 || param0 >= 11) {
            param0 = this.random.nextInt(10);
        }

        this.entityData.set(DATA_TYPE_ID, param0);
    }

    public void setLying(boolean param0) {
        this.entityData.set(IS_LYING, param0);
    }

    public boolean isLying() {
        return this.entityData.get(IS_LYING);
    }

    public void setRelaxStateOne(boolean param0) {
        this.entityData.set(RELAX_STATE_ONE, param0);
    }

    public boolean isRelaxStateOne() {
        return this.entityData.get(RELAX_STATE_ONE);
    }

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    public void setCollarColor(DyeColor param0) {
        this.entityData.set(DATA_COLLAR_COLOR, param0.getId());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TYPE_ID, 1);
        this.entityData.define(IS_LYING, false);
        this.entityData.define(RELAX_STATE_ONE, false);
        this.entityData.define(DATA_COLLAR_COLOR, DyeColor.RED.getId());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("CatType", this.getCatType());
        param0.putByte("CollarColor", (byte)this.getCollarColor().getId());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setCatType(param0.getInt("CatType"));
        if (param0.contains("CollarColor", 99)) {
            this.setCollarColor(DyeColor.byId(param0.getInt("CollarColor")));
        }

    }

    @Override
    public void customServerAiStep() {
        if (this.getMoveControl().hasWanted()) {
            double var0 = this.getMoveControl().getSpeedModifier();
            if (var0 == 0.6) {
                this.setPose(Pose.CROUCHING);
                this.setSprinting(false);
            } else if (var0 == 1.33) {
                this.setPose(Pose.STANDING);
                this.setSprinting(true);
            } else {
                this.setPose(Pose.STANDING);
                this.setSprinting(false);
            }
        } else {
            this.setPose(Pose.STANDING);
            this.setSprinting(false);
        }

    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isTame()) {
            if (this.isInLove()) {
                return SoundEvents.CAT_PURR;
            } else {
                return this.random.nextInt(4) == 0 ? SoundEvents.CAT_PURREOW : SoundEvents.CAT_AMBIENT;
            }
        } else {
            return SoundEvents.CAT_STRAY_AMBIENT;
        }
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    public void hiss() {
        this.playSound(SoundEvents.CAT_HISS, this.getSoundVolume(), this.getVoicePitch());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0);
    }

    @Override
    public boolean causeFallDamage(float param0, float param1) {
        return false;
    }

    @Override
    protected void usePlayerItem(Player param0, ItemStack param1) {
        if (this.isFood(param1)) {
            this.playSound(SoundEvents.CAT_EAT, 1.0F, 1.0F);
        }

        super.usePlayerItem(param0, param1);
    }

    private float getAttackDamage() {
        return (float)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        return param0.hurt(DamageSource.mobAttack(this), this.getAttackDamage());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.temptGoal != null && this.temptGoal.isRunning() && !this.isTame() && this.tickCount % 100 == 0) {
            this.playSound(SoundEvents.CAT_BEG_FOR_FOOD, 1.0F, 1.0F);
        }

        this.handleLieDown();
    }

    private void handleLieDown() {
        if ((this.isLying() || this.isRelaxStateOne()) && this.tickCount % 5 == 0) {
            this.playSound(SoundEvents.CAT_PURR, 0.6F + 0.4F * (this.random.nextFloat() - this.random.nextFloat()), 1.0F);
        }

        this.updateLieDownAmount();
        this.updateRelaxStateOneAmount();
    }

    private void updateLieDownAmount() {
        this.lieDownAmountO = this.lieDownAmount;
        this.lieDownAmountOTail = this.lieDownAmountTail;
        if (this.isLying()) {
            this.lieDownAmount = Math.min(1.0F, this.lieDownAmount + 0.15F);
            this.lieDownAmountTail = Math.min(1.0F, this.lieDownAmountTail + 0.08F);
        } else {
            this.lieDownAmount = Math.max(0.0F, this.lieDownAmount - 0.22F);
            this.lieDownAmountTail = Math.max(0.0F, this.lieDownAmountTail - 0.13F);
        }

    }

    private void updateRelaxStateOneAmount() {
        this.relaxStateOneAmountO = this.relaxStateOneAmount;
        if (this.isRelaxStateOne()) {
            this.relaxStateOneAmount = Math.min(1.0F, this.relaxStateOneAmount + 0.1F);
        } else {
            this.relaxStateOneAmount = Math.max(0.0F, this.relaxStateOneAmount - 0.13F);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public float getLieDownAmount(float param0) {
        return Mth.lerp(param0, this.lieDownAmountO, this.lieDownAmount);
    }

    @OnlyIn(Dist.CLIENT)
    public float getLieDownAmountTail(float param0) {
        return Mth.lerp(param0, this.lieDownAmountOTail, this.lieDownAmountTail);
    }

    @OnlyIn(Dist.CLIENT)
    public float getRelaxStateOneAmount(float param0) {
        return Mth.lerp(param0, this.relaxStateOneAmountO, this.relaxStateOneAmount);
    }

    public Cat getBreedOffspring(AgableMob param0) {
        Cat var0 = EntityType.CAT.create(this.level);
        if (param0 instanceof Cat) {
            if (this.random.nextBoolean()) {
                var0.setCatType(this.getCatType());
            } else {
                var0.setCatType(((Cat)param0).getCatType());
            }

            if (this.isTame()) {
                var0.setOwnerUUID(this.getOwnerUUID());
                var0.setTame(true);
                if (this.random.nextBoolean()) {
                    var0.setCollarColor(this.getCollarColor());
                } else {
                    var0.setCollarColor(((Cat)param0).getCollarColor());
                }
            }
        }

        return var0;
    }

    @Override
    public boolean canMate(Animal param0) {
        if (!this.isTame()) {
            return false;
        } else if (!(param0 instanceof Cat)) {
            return false;
        } else {
            Cat var0 = (Cat)param0;
            return var0.isTame() && super.canMate(param0);
        }
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        param3 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        if (param0.getMoonBrightness() > 0.9F) {
            this.setCatType(this.random.nextInt(11));
        } else {
            this.setCatType(this.random.nextInt(10));
        }

        if (Feature.SWAMP_HUT.isInsideFeature(param0, new BlockPos(this))) {
            this.setCatType(10);
            this.setPersistenceRequired();
        }

        return param3;
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        Item var1 = var0.getItem();
        if (var0.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(param0, param1);
        } else if (this.level.isClientSide) {
            return this.isTame() && this.isOwnedBy(param0) || this.isFood(var0);
        } else {
            if (this.isTame()) {
                if (this.isOwnedBy(param0)) {
                    if (!(var1 instanceof DyeItem)) {
                        if (var1.isEdible() && this.isFood(var0) && this.getHealth() < this.getMaxHealth()) {
                            this.usePlayerItem(param0, var0);
                            this.heal((float)var1.getFoodProperties().getNutrition());
                            return true;
                        }

                        boolean var3 = super.mobInteract(param0, param1);
                        if (!var3 || this.isBaby()) {
                            this.setOrderedToSit(!this.isOrderedToSit());
                        }

                        return var3;
                    }

                    DyeColor var2 = ((DyeItem)var1).getDyeColor();
                    if (var2 != this.getCollarColor()) {
                        this.setCollarColor(var2);
                        if (!param0.abilities.instabuild) {
                            var0.shrink(1);
                        }

                        this.setPersistenceRequired();
                        return true;
                    }
                }
            } else if (this.isFood(var0)) {
                this.usePlayerItem(param0, var0);
                if (this.random.nextInt(3) == 0) {
                    this.tame(param0);
                    this.setOrderedToSit(true);
                    this.level.broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level.broadcastEntityEvent(this, (byte)6);
                }

                this.setPersistenceRequired();
                return true;
            }

            boolean var4 = super.mobInteract(param0, param1);
            if (var4) {
                this.setPersistenceRequired();
            }

            return var4;
        }
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return TEMPT_INGREDIENT.test(param0);
    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return param1.height * 0.5F;
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return !this.isTame() && this.tickCount > 2400;
    }

    @Override
    protected void reassessTameGoals() {
        if (this.avoidPlayersGoal == null) {
            this.avoidPlayersGoal = new Cat.CatAvoidEntityGoal<>(this, Player.class, 16.0F, 0.8, 1.33);
        }

        this.goalSelector.removeGoal(this.avoidPlayersGoal);
        if (!this.isTame()) {
            this.goalSelector.addGoal(4, this.avoidPlayersGoal);
        }

    }

    static class CatAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Cat cat;

        public CatAvoidEntityGoal(Cat param0, Class<T> param1, float param2, double param3, double param4) {
            super(param0, param1, param2, param3, param4, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.cat = param0;
        }

        @Override
        public boolean canUse() {
            return !this.cat.isTame() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return !this.cat.isTame() && super.canContinueToUse();
        }
    }

    static class CatRelaxOnOwnerGoal extends Goal {
        private final Cat cat;
        private Player ownerPlayer;
        private BlockPos goalPos;
        private int onBedTicks;

        public CatRelaxOnOwnerGoal(Cat param0) {
            this.cat = param0;
        }

        @Override
        public boolean canUse() {
            if (!this.cat.isTame()) {
                return false;
            } else if (this.cat.isOrderedToSit()) {
                return false;
            } else {
                LivingEntity var0 = this.cat.getOwner();
                if (var0 instanceof Player) {
                    this.ownerPlayer = (Player)var0;
                    if (!var0.isSleeping()) {
                        return false;
                    }

                    if (this.cat.distanceToSqr(this.ownerPlayer) > 100.0) {
                        return false;
                    }

                    BlockPos var1 = new BlockPos(this.ownerPlayer);
                    BlockState var2 = this.cat.level.getBlockState(var1);
                    if (var2.getBlock().is(BlockTags.BEDS)) {
                        Direction var3 = var2.getValue(BedBlock.FACING);
                        this.goalPos = new BlockPos(var1.getX() - var3.getStepX(), var1.getY(), var1.getZ() - var3.getStepZ());
                        return !this.spaceIsOccupied();
                    }
                }

                return false;
            }
        }

        private boolean spaceIsOccupied() {
            for(Cat var1 : this.cat.level.getEntitiesOfClass(Cat.class, new AABB(this.goalPos).inflate(2.0))) {
                if (var1 != this.cat && (var1.isLying() || var1.isRelaxStateOne())) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.cat.isTame()
                && !this.cat.isOrderedToSit()
                && this.ownerPlayer != null
                && this.ownerPlayer.isSleeping()
                && this.goalPos != null
                && !this.spaceIsOccupied();
        }

        @Override
        public void start() {
            if (this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), 1.1F);
            }

        }

        @Override
        public void stop() {
            this.cat.setLying(false);
            float var0 = this.cat.level.getTimeOfDay(1.0F);
            if (this.ownerPlayer.getSleepTimer() >= 100 && (double)var0 > 0.77 && (double)var0 < 0.8 && (double)this.cat.level.getRandom().nextFloat() < 0.7) {
                this.giveMorningGift();
            }

            this.onBedTicks = 0;
            this.cat.setRelaxStateOne(false);
            this.cat.getNavigation().stop();
        }

        private void giveMorningGift() {
            Random var0 = this.cat.getRandom();
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();
            var1.set(this.cat);
            this.cat
                .randomTeleport(
                    (double)(var1.getX() + var0.nextInt(11) - 5),
                    (double)(var1.getY() + var0.nextInt(5) - 2),
                    (double)(var1.getZ() + var0.nextInt(11) - 5),
                    false
                );
            var1.set(this.cat);
            LootTable var2 = this.cat.level.getServer().getLootTables().get(BuiltInLootTables.CAT_MORNING_GIFT);
            LootContext.Builder var3 = new LootContext.Builder((ServerLevel)this.cat.level)
                .withParameter(LootContextParams.BLOCK_POS, var1)
                .withParameter(LootContextParams.THIS_ENTITY, this.cat)
                .withRandom(var0);

            for(ItemStack var5 : var2.getRandomItems(var3.create(LootContextParamSets.GIFT))) {
                this.cat
                    .level
                    .addFreshEntity(
                        new ItemEntity(
                            this.cat.level,
                            (double)((float)var1.getX() - Mth.sin(this.cat.yBodyRot * (float) (Math.PI / 180.0))),
                            (double)var1.getY(),
                            (double)((float)var1.getZ() + Mth.cos(this.cat.yBodyRot * (float) (Math.PI / 180.0))),
                            var5
                        )
                    );
            }

        }

        @Override
        public void tick() {
            if (this.ownerPlayer != null && this.goalPos != null) {
                this.cat.setInSittingPose(false);
                this.cat.getNavigation().moveTo((double)this.goalPos.getX(), (double)this.goalPos.getY(), (double)this.goalPos.getZ(), 1.1F);
                if (this.cat.distanceToSqr(this.ownerPlayer) < 2.5) {
                    ++this.onBedTicks;
                    if (this.onBedTicks > 16) {
                        this.cat.setLying(true);
                        this.cat.setRelaxStateOne(false);
                    } else {
                        this.cat.lookAt(this.ownerPlayer, 45.0F, 45.0F);
                        this.cat.setRelaxStateOne(true);
                    }
                } else {
                    this.cat.setLying(false);
                }
            }

        }
    }

    static class CatTemptGoal extends TemptGoal {
        @Nullable
        private Player selectedPlayer;
        private final Cat cat;

        public CatTemptGoal(Cat param0, double param1, Ingredient param2, boolean param3) {
            super(param0, param1, param2, param3);
            this.cat = param0;
        }

        @Override
        public void tick() {
            super.tick();
            if (this.selectedPlayer == null && this.mob.getRandom().nextInt(600) == 0) {
                this.selectedPlayer = this.player;
            } else if (this.mob.getRandom().nextInt(500) == 0) {
                this.selectedPlayer = null;
            }

        }

        @Override
        protected boolean canScare() {
            return this.selectedPlayer != null && this.selectedPlayer.equals(this.player) ? false : super.canScare();
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.cat.isTame();
        }
    }
}
