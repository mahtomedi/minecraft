package net.minecraft.world.entity.animal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.SharedMonsterAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Panda extends Animal {
    private static final EntityDataAccessor<Integer> UNHAPPY_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SNEEZE_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Byte> MAIN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> HIDDEN_GENE_ID = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(Panda.class, EntityDataSerializers.BYTE);
    private static final TargetingConditions BREED_TARGETING = new TargetingConditions().range(8.0).allowSameTeam().allowInvulnerable();
    private boolean gotBamboo;
    private boolean didBite;
    public int rollCounter;
    private Vec3 rollDelta;
    private float sitAmount;
    private float sitAmountO;
    private float onBackAmount;
    private float onBackAmountO;
    private float rollAmount;
    private float rollAmountO;
    private Panda.PandaLookAtPlayerGoal lookAtPlayerGoal;
    private static final Predicate<ItemEntity> PANDA_ITEMS = param0 -> {
        Item var0 = param0.getItem().getItem();
        return (var0 == Blocks.BAMBOO.asItem() || var0 == Blocks.CAKE.asItem()) && param0.isAlive() && !param0.hasPickUpDelay();
    };

    public Panda(EntityType<? extends Panda> param0, Level param1) {
        super(param0, param1);
        this.moveControl = new Panda.PandaMoveControl(this);
        if (!this.isBaby()) {
            this.setCanPickUpLoot(true);
        }

    }

    @Override
    public boolean canTakeItem(ItemStack param0) {
        EquipmentSlot var0 = Mob.getEquipmentSlotForItem(param0);
        if (!this.getItemBySlot(var0).isEmpty()) {
            return false;
        } else {
            return var0 == EquipmentSlot.MAINHAND && super.canTakeItem(param0);
        }
    }

    public int getUnhappyCounter() {
        return this.entityData.get(UNHAPPY_COUNTER);
    }

    public void setUnhappyCounter(int param0) {
        this.entityData.set(UNHAPPY_COUNTER, param0);
    }

    public boolean isSneezing() {
        return this.getFlag(2);
    }

    public boolean isSitting() {
        return this.getFlag(8);
    }

    public void sit(boolean param0) {
        this.setFlag(8, param0);
    }

    public boolean isOnBack() {
        return this.getFlag(16);
    }

    public void setOnBack(boolean param0) {
        this.setFlag(16, param0);
    }

    public boolean isEating() {
        return this.entityData.get(EAT_COUNTER) > 0;
    }

    public void eat(boolean param0) {
        this.entityData.set(EAT_COUNTER, param0 ? 1 : 0);
    }

    private int getEatCounter() {
        return this.entityData.get(EAT_COUNTER);
    }

    private void setEatCounter(int param0) {
        this.entityData.set(EAT_COUNTER, param0);
    }

    public void sneeze(boolean param0) {
        this.setFlag(2, param0);
        if (!param0) {
            this.setSneezeCounter(0);
        }

    }

    public int getSneezeCounter() {
        return this.entityData.get(SNEEZE_COUNTER);
    }

    public void setSneezeCounter(int param0) {
        this.entityData.set(SNEEZE_COUNTER, param0);
    }

    public Panda.Gene getMainGene() {
        return Panda.Gene.byId(this.entityData.get(MAIN_GENE_ID));
    }

    public void setMainGene(Panda.Gene param0) {
        if (param0.getId() > 6) {
            param0 = Panda.Gene.getRandom(this.random);
        }

        this.entityData.set(MAIN_GENE_ID, (byte)param0.getId());
    }

    public Panda.Gene getHiddenGene() {
        return Panda.Gene.byId(this.entityData.get(HIDDEN_GENE_ID));
    }

    public void setHiddenGene(Panda.Gene param0) {
        if (param0.getId() > 6) {
            param0 = Panda.Gene.getRandom(this.random);
        }

        this.entityData.set(HIDDEN_GENE_ID, (byte)param0.getId());
    }

    public boolean isRolling() {
        return this.getFlag(4);
    }

    public void roll(boolean param0) {
        this.setFlag(4, param0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(UNHAPPY_COUNTER, 0);
        this.entityData.define(SNEEZE_COUNTER, 0);
        this.entityData.define(MAIN_GENE_ID, (byte)0);
        this.entityData.define(HIDDEN_GENE_ID, (byte)0);
        this.entityData.define(DATA_ID_FLAGS, (byte)0);
        this.entityData.define(EAT_COUNTER, 0);
    }

    private boolean getFlag(int param0) {
        return (this.entityData.get(DATA_ID_FLAGS) & param0) != 0;
    }

    private void setFlag(int param0, boolean param1) {
        byte var0 = this.entityData.get(DATA_ID_FLAGS);
        if (param1) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(var0 | param0));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(var0 & ~param0));
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putString("MainGene", this.getMainGene().getName());
        param0.putString("HiddenGene", this.getHiddenGene().getName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setMainGene(Panda.Gene.byName(param0.getString("MainGene")));
        this.setHiddenGene(Panda.Gene.byName(param0.getString("HiddenGene")));
    }

    @Nullable
    @Override
    public AgableMob getBreedOffspring(AgableMob param0) {
        Panda var0 = EntityType.PANDA.create(this.level);
        if (param0 instanceof Panda) {
            var0.setGeneFromParents(this, (Panda)param0);
        }

        var0.setAttributes();
        return var0;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new Panda.PandaPanicGoal(this, 2.0));
        this.goalSelector.addGoal(2, new Panda.PandaBreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new Panda.PandaAttackGoal(this, 1.2F, true));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0, Ingredient.of(Blocks.BAMBOO.asItem()), false));
        this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Player.class, 8.0F, 2.0, 2.0));
        this.goalSelector.addGoal(6, new Panda.PandaAvoidGoal<>(this, Monster.class, 4.0F, 2.0, 2.0));
        this.goalSelector.addGoal(7, new Panda.PandaSitGoal());
        this.goalSelector.addGoal(8, new Panda.PandaLieOnBackGoal(this));
        this.goalSelector.addGoal(8, new Panda.PandaSneezeGoal(this));
        this.lookAtPlayerGoal = new Panda.PandaLookAtPlayerGoal(this, Player.class, 6.0F);
        this.goalSelector.addGoal(9, this.lookAtPlayerGoal);
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(12, new Panda.PandaRollGoal(this));
        this.goalSelector.addGoal(13, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(14, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new Panda.PandaHurtByTargetGoal(this).setAlertOthers(new Class[0]));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.15F);
        this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0);
    }

    public Panda.Gene getVariant() {
        return Panda.Gene.getVariantFromGenes(this.getMainGene(), this.getHiddenGene());
    }

    public boolean isLazy() {
        return this.getVariant() == Panda.Gene.LAZY;
    }

    public boolean isWorried() {
        return this.getVariant() == Panda.Gene.WORRIED;
    }

    public boolean isPlayful() {
        return this.getVariant() == Panda.Gene.PLAYFUL;
    }

    public boolean isWeak() {
        return this.getVariant() == Panda.Gene.WEAK;
    }

    @Override
    public boolean isAggressive() {
        return this.getVariant() == Panda.Gene.AGGRESSIVE;
    }

    @Override
    public boolean canBeLeashed(Player param0) {
        return false;
    }

    @Override
    public boolean doHurtTarget(Entity param0) {
        this.playSound(SoundEvents.PANDA_BITE, 1.0F, 1.0F);
        if (!this.isAggressive()) {
            this.didBite = true;
        }

        return super.doHurtTarget(param0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isWorried()) {
            if (this.level.isThundering() && !this.isInWater()) {
                this.sit(true);
                this.eat(false);
            } else if (!this.isEating()) {
                this.sit(false);
            }
        }

        if (this.getTarget() == null) {
            this.gotBamboo = false;
            this.didBite = false;
        }

        if (this.getUnhappyCounter() > 0) {
            if (this.getTarget() != null) {
                this.lookAt(this.getTarget(), 90.0F, 90.0F);
            }

            if (this.getUnhappyCounter() == 29 || this.getUnhappyCounter() == 14) {
                this.playSound(SoundEvents.PANDA_CANT_BREED, 1.0F, 1.0F);
            }

            this.setUnhappyCounter(this.getUnhappyCounter() - 1);
        }

        if (this.isSneezing()) {
            this.setSneezeCounter(this.getSneezeCounter() + 1);
            if (this.getSneezeCounter() > 20) {
                this.sneeze(false);
                this.afterSneeze();
            } else if (this.getSneezeCounter() == 1) {
                this.playSound(SoundEvents.PANDA_PRE_SNEEZE, 1.0F, 1.0F);
            }
        }

        if (this.isRolling()) {
            this.handleRoll();
        } else {
            this.rollCounter = 0;
        }

        if (this.isSitting()) {
            this.xRot = 0.0F;
        }

        this.updateSitAmount();
        this.handleEating();
        this.updateOnBackAnimation();
        this.updateRollAmount();
    }

    public boolean isScared() {
        return this.isWorried() && this.level.isThundering();
    }

    private void handleEating() {
        if (!this.isEating() && this.isSitting() && !this.isScared() && !this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && this.random.nextInt(80) == 1) {
            this.eat(true);
        } else if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() || !this.isSitting()) {
            this.eat(false);
        }

        if (this.isEating()) {
            this.addEatingParticles();
            if (!this.level.isClientSide && this.getEatCounter() > 80 && this.random.nextInt(20) == 1) {
                if (this.getEatCounter() > 100 && this.isFoodOrCake(this.getItemBySlot(EquipmentSlot.MAINHAND))) {
                    if (!this.level.isClientSide) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    }

                    this.sit(false);
                }

                this.eat(false);
                return;
            }

            this.setEatCounter(this.getEatCounter() + 1);
        }

    }

    private void addEatingParticles() {
        if (this.getEatCounter() % 5 == 0) {
            this.playSound(
                SoundEvents.PANDA_EAT, 0.5F + 0.5F * (float)this.random.nextInt(2), (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F
            );

            for(int var0 = 0; var0 < 6; ++var0) {
                Vec3 var1 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, ((double)this.random.nextFloat() - 0.5) * 0.1);
                var1 = var1.xRot(-this.xRot * (float) (Math.PI / 180.0));
                var1 = var1.yRot(-this.yRot * (float) (Math.PI / 180.0));
                double var2 = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
                Vec3 var3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.8, var2, 1.0 + ((double)this.random.nextFloat() - 0.5) * 0.4);
                var3 = var3.yRot(-this.yBodyRot * (float) (Math.PI / 180.0));
                var3 = var3.add(this.getX(), this.getEyeY() + 1.0, this.getZ());
                this.level
                    .addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, this.getItemBySlot(EquipmentSlot.MAINHAND)),
                        var3.x,
                        var3.y,
                        var3.z,
                        var1.x,
                        var1.y + 0.05,
                        var1.z
                    );
            }
        }

    }

    private void updateSitAmount() {
        this.sitAmountO = this.sitAmount;
        if (this.isSitting()) {
            this.sitAmount = Math.min(1.0F, this.sitAmount + 0.15F);
        } else {
            this.sitAmount = Math.max(0.0F, this.sitAmount - 0.19F);
        }

    }

    private void updateOnBackAnimation() {
        this.onBackAmountO = this.onBackAmount;
        if (this.isOnBack()) {
            this.onBackAmount = Math.min(1.0F, this.onBackAmount + 0.15F);
        } else {
            this.onBackAmount = Math.max(0.0F, this.onBackAmount - 0.19F);
        }

    }

    private void updateRollAmount() {
        this.rollAmountO = this.rollAmount;
        if (this.isRolling()) {
            this.rollAmount = Math.min(1.0F, this.rollAmount + 0.15F);
        } else {
            this.rollAmount = Math.max(0.0F, this.rollAmount - 0.19F);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public float getSitAmount(float param0) {
        return Mth.lerp(param0, this.sitAmountO, this.sitAmount);
    }

    @OnlyIn(Dist.CLIENT)
    public float getLieOnBackAmount(float param0) {
        return Mth.lerp(param0, this.onBackAmountO, this.onBackAmount);
    }

    @OnlyIn(Dist.CLIENT)
    public float getRollAmount(float param0) {
        return Mth.lerp(param0, this.rollAmountO, this.rollAmount);
    }

    private void handleRoll() {
        ++this.rollCounter;
        if (this.rollCounter > 32) {
            this.roll(false);
        } else {
            if (!this.level.isClientSide) {
                Vec3 var0 = this.getDeltaMovement();
                if (this.rollCounter == 1) {
                    float var1 = this.yRot * (float) (Math.PI / 180.0);
                    float var2 = this.isBaby() ? 0.1F : 0.2F;
                    this.rollDelta = new Vec3(var0.x + (double)(-Mth.sin(var1) * var2), 0.0, var0.z + (double)(Mth.cos(var1) * var2));
                    this.setDeltaMovement(this.rollDelta.add(0.0, 0.27, 0.0));
                } else if ((float)this.rollCounter != 7.0F && (float)this.rollCounter != 15.0F && (float)this.rollCounter != 23.0F) {
                    this.setDeltaMovement(this.rollDelta.x, var0.y, this.rollDelta.z);
                } else {
                    this.setDeltaMovement(0.0, this.onGround ? 0.27 : var0.y, 0.0);
                }
            }

        }
    }

    private void afterSneeze() {
        Vec3 var0 = this.getDeltaMovement();
        this.level
            .addParticle(
                ParticleTypes.SNEEZE,
                this.getX() - (double)(this.getBbWidth() + 1.0F) * 0.5 * (double)Mth.sin(this.yBodyRot * (float) (Math.PI / 180.0)),
                this.getEyeY() - 0.1F,
                this.getZ() + (double)(this.getBbWidth() + 1.0F) * 0.5 * (double)Mth.cos(this.yBodyRot * (float) (Math.PI / 180.0)),
                var0.x,
                0.0,
                var0.z
            );
        this.playSound(SoundEvents.PANDA_SNEEZE, 1.0F, 1.0F);

        for(Panda var2 : this.level.getEntitiesOfClass(Panda.class, this.getBoundingBox().inflate(10.0))) {
            if (!var2.isBaby() && var2.onGround && !var2.isInWater() && var2.canPerformAction()) {
                var2.jumpFromGround();
            }
        }

        if (!this.level.isClientSide() && this.random.nextInt(700) == 0 && this.level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.spawnAtLocation(Items.SLIME_BALL);
        }

    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && PANDA_ITEMS.test(param0)) {
            ItemStack var0 = param0.getItem();
            this.setItemSlot(EquipmentSlot.MAINHAND, var0);
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(param0, var0.getCount());
            param0.remove();
        }

    }

    @Override
    public boolean hurt(DamageSource param0, float param1) {
        this.sit(false);
        return super.hurt(param0, param1);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setMainGene(Panda.Gene.getRandom(this.random));
        this.setHiddenGene(Panda.Gene.getRandom(this.random));
        this.setAttributes();
        if (param3 == null) {
            param3 = new AgableMob.AgableMobGroupData();
            ((AgableMob.AgableMobGroupData)param3).setBabySpawnChance(0.2F);
        }

        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public void setGeneFromParents(Panda param0, @Nullable Panda param1) {
        if (param1 == null) {
            if (this.random.nextBoolean()) {
                this.setMainGene(param0.getOneOfGenesRandomly());
                this.setHiddenGene(Panda.Gene.getRandom(this.random));
            } else {
                this.setMainGene(Panda.Gene.getRandom(this.random));
                this.setHiddenGene(param0.getOneOfGenesRandomly());
            }
        } else if (this.random.nextBoolean()) {
            this.setMainGene(param0.getOneOfGenesRandomly());
            this.setHiddenGene(param1.getOneOfGenesRandomly());
        } else {
            this.setMainGene(param1.getOneOfGenesRandomly());
            this.setHiddenGene(param0.getOneOfGenesRandomly());
        }

        if (this.random.nextInt(32) == 0) {
            this.setMainGene(Panda.Gene.getRandom(this.random));
        }

        if (this.random.nextInt(32) == 0) {
            this.setHiddenGene(Panda.Gene.getRandom(this.random));
        }

    }

    private Panda.Gene getOneOfGenesRandomly() {
        return this.random.nextBoolean() ? this.getMainGene() : this.getHiddenGene();
    }

    public void setAttributes() {
        if (this.isWeak()) {
            this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(10.0);
        }

        if (this.isLazy()) {
            this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.07F);
        }

    }

    private void tryToSit() {
        if (!this.isInWater()) {
            this.setZza(0.0F);
            this.getNavigation().stop();
            this.sit(true);
        }

    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (var0.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(param0, param1);
        } else if (this.isScared()) {
            return false;
        } else if (this.isOnBack()) {
            this.setOnBack(false);
            return true;
        } else if (this.isFood(var0)) {
            if (this.getTarget() != null) {
                this.gotBamboo = true;
            }

            if (this.isBaby()) {
                this.usePlayerItem(param0, var0);
                this.ageUp((int)((float)(-this.getAge() / 20) * 0.1F), true);
            } else if (!this.level.isClientSide && this.getAge() == 0 && this.canFallInLove()) {
                this.usePlayerItem(param0, var0);
                this.setInLove(param0);
            } else {
                if (this.level.isClientSide || this.isSitting() || this.isInWater()) {
                    return false;
                }

                this.tryToSit();
                this.eat(true);
                ItemStack var1 = this.getItemBySlot(EquipmentSlot.MAINHAND);
                if (!var1.isEmpty() && !param0.abilities.instabuild) {
                    this.spawnAtLocation(var1);
                }

                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(var0.getItem(), 1));
                this.usePlayerItem(param0, var0);
            }

            param0.swing(param1, true);
            return true;
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAggressive()) {
            return SoundEvents.PANDA_AGGRESSIVE_AMBIENT;
        } else {
            return this.isWorried() ? SoundEvents.PANDA_WORRIED_AMBIENT : SoundEvents.PANDA_AMBIENT;
        }
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.PANDA_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return param0.getItem() == Blocks.BAMBOO.asItem();
    }

    private boolean isFoodOrCake(ItemStack param0) {
        return this.isFood(param0) || param0.getItem() == Blocks.CAKE.asItem();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PANDA_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.PANDA_HURT;
    }

    public boolean canPerformAction() {
        return !this.isOnBack() && !this.isScared() && !this.isEating() && !this.isRolling() && !this.isSitting();
    }

    public static enum Gene {
        NORMAL(0, "normal", false),
        LAZY(1, "lazy", false),
        WORRIED(2, "worried", false),
        PLAYFUL(3, "playful", false),
        BROWN(4, "brown", true),
        WEAK(5, "weak", true),
        AGGRESSIVE(6, "aggressive", false);

        private static final Panda.Gene[] BY_ID = Arrays.stream(values())
            .sorted(Comparator.comparingInt(Panda.Gene::getId))
            .toArray(param0 -> new Panda.Gene[param0]);
        private final int id;
        private final String name;
        private final boolean isRecessive;

        private Gene(int param0, String param1, boolean param2) {
            this.id = param0;
            this.name = param1;
            this.isRecessive = param2;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public boolean isRecessive() {
            return this.isRecessive;
        }

        private static Panda.Gene getVariantFromGenes(Panda.Gene param0, Panda.Gene param1) {
            if (param0.isRecessive()) {
                return param0 == param1 ? param0 : NORMAL;
            } else {
                return param0;
            }
        }

        public static Panda.Gene byId(int param0) {
            if (param0 < 0 || param0 >= BY_ID.length) {
                param0 = 0;
            }

            return BY_ID[param0];
        }

        public static Panda.Gene byName(String param0) {
            for(Panda.Gene var0 : values()) {
                if (var0.name.equals(param0)) {
                    return var0;
                }
            }

            return NORMAL;
        }

        public static Panda.Gene getRandom(Random param0) {
            int var0 = param0.nextInt(16);
            if (var0 == 0) {
                return LAZY;
            } else if (var0 == 1) {
                return WORRIED;
            } else if (var0 == 2) {
                return PLAYFUL;
            } else if (var0 == 4) {
                return AGGRESSIVE;
            } else if (var0 < 9) {
                return WEAK;
            } else {
                return var0 < 11 ? BROWN : NORMAL;
            }
        }
    }

    static class PandaAttackGoal extends MeleeAttackGoal {
        private final Panda panda;

        public PandaAttackGoal(Panda param0, double param1, boolean param2) {
            super(param0, param1, param2);
            this.panda = param0;
        }

        @Override
        public boolean canUse() {
            return this.panda.canPerformAction() && super.canUse();
        }
    }

    static class PandaAvoidGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final Panda panda;

        public PandaAvoidGoal(Panda param0, Class<T> param1, float param2, double param3, double param4) {
            super(param0, param1, param2, param3, param4, EntitySelector.NO_SPECTATORS::test);
            this.panda = param0;
        }

        @Override
        public boolean canUse() {
            return this.panda.isWorried() && this.panda.canPerformAction() && super.canUse();
        }
    }

    class PandaBreedGoal extends BreedGoal {
        private final Panda panda;
        private int unhappyCooldown;

        public PandaBreedGoal(Panda param0, double param1) {
            super(param0, param1);
            this.panda = param0;
        }

        @Override
        public boolean canUse() {
            if (!super.canUse() || this.panda.getUnhappyCounter() != 0) {
                return false;
            } else if (!this.canFindBamboo()) {
                if (this.unhappyCooldown <= this.panda.tickCount) {
                    this.panda.setUnhappyCounter(32);
                    this.unhappyCooldown = this.panda.tickCount + 600;
                    if (this.panda.isEffectiveAi()) {
                        Player var0 = this.level.getNearestPlayer(Panda.BREED_TARGETING, this.panda);
                        this.panda.lookAtPlayerGoal.setTarget(var0);
                    }
                }

                return false;
            } else {
                return true;
            }
        }

        private boolean canFindBamboo() {
            BlockPos var0 = new BlockPos(this.panda);
            BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos();

            for(int var2 = 0; var2 < 3; ++var2) {
                for(int var3 = 0; var3 < 8; ++var3) {
                    for(int var4 = 0; var4 <= var3; var4 = var4 > 0 ? -var4 : 1 - var4) {
                        for(int var5 = var4 < var3 && var4 > -var3 ? var3 : 0; var5 <= var3; var5 = var5 > 0 ? -var5 : 1 - var5) {
                            var1.set(var0).move(var4, var2, var5);
                            if (this.level.getBlockState(var1).getBlock() == Blocks.BAMBOO) {
                                return true;
                            }
                        }
                    }
                }
            }

            return false;
        }
    }

    static class PandaHurtByTargetGoal extends HurtByTargetGoal {
        private final Panda panda;

        public PandaHurtByTargetGoal(Panda param0, Class<?>... param1) {
            super(param0, param1);
            this.panda = param0;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.panda.gotBamboo && !this.panda.didBite) {
                return super.canContinueToUse();
            } else {
                this.panda.setTarget(null);
                return false;
            }
        }

        @Override
        protected void alertOther(Mob param0, LivingEntity param1) {
            if (param0 instanceof Panda && ((Panda)param0).isAggressive()) {
                param0.setTarget(param1);
            }

        }
    }

    static class PandaLieOnBackGoal extends Goal {
        private final Panda panda;
        private int cooldown;

        public PandaLieOnBackGoal(Panda param0) {
            this.panda = param0;
        }

        @Override
        public boolean canUse() {
            return this.cooldown < this.panda.tickCount && this.panda.isLazy() && this.panda.canPerformAction() && this.panda.random.nextInt(400) == 1;
        }

        @Override
        public boolean canContinueToUse() {
            if (!this.panda.isInWater() && (this.panda.isLazy() || this.panda.random.nextInt(600) != 1)) {
                return this.panda.random.nextInt(2000) != 1;
            } else {
                return false;
            }
        }

        @Override
        public void start() {
            this.panda.setOnBack(true);
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            this.panda.setOnBack(false);
            this.cooldown = this.panda.tickCount + 200;
        }
    }

    static class PandaLookAtPlayerGoal extends LookAtPlayerGoal {
        private final Panda panda;

        public PandaLookAtPlayerGoal(Panda param0, Class<? extends LivingEntity> param1, float param2) {
            super(param0, param1, param2);
            this.panda = param0;
        }

        public void setTarget(LivingEntity param0) {
            this.lookAt = param0;
        }

        @Override
        public boolean canUse() {
            if (this.mob.getRandom().nextFloat() >= this.probability) {
                return false;
            } else {
                if (this.lookAt == null) {
                    if (this.lookAtType == Player.class) {
                        this.lookAt = this.mob.level.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
                    } else {
                        this.lookAt = this.mob
                            .level
                            .getNearestLoadedEntity(
                                this.lookAtType,
                                this.lookAtContext,
                                this.mob,
                                this.mob.getX(),
                                this.mob.getEyeY(),
                                this.mob.getZ(),
                                this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance)
                            );
                    }
                }

                return this.panda.canPerformAction() && this.lookAt != null;
            }
        }

        @Override
        public void tick() {
            if (this.lookAt != null) {
                super.tick();
            }

        }
    }

    static class PandaMoveControl extends MoveControl {
        private final Panda panda;

        public PandaMoveControl(Panda param0) {
            super(param0);
            this.panda = param0;
        }

        @Override
        public void tick() {
            if (this.panda.canPerformAction()) {
                super.tick();
            }
        }
    }

    static class PandaPanicGoal extends PanicGoal {
        private final Panda panda;

        public PandaPanicGoal(Panda param0, double param1) {
            super(param0, param1);
            this.panda = param0;
        }

        @Override
        public boolean canUse() {
            if (!this.panda.isOnFire()) {
                return false;
            } else {
                BlockPos var0 = this.lookForWater(this.mob.level, this.mob, 5, 4);
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

        @Override
        public boolean canContinueToUse() {
            if (this.panda.isSitting()) {
                this.panda.getNavigation().stop();
                return false;
            } else {
                return super.canContinueToUse();
            }
        }
    }

    static class PandaRollGoal extends Goal {
        private final Panda panda;

        public PandaRollGoal(Panda param0) {
            this.panda = param0;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK, Goal.Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if ((this.panda.isBaby() || this.panda.isPlayful()) && this.panda.onGround) {
                if (!this.panda.canPerformAction()) {
                    return false;
                } else {
                    float var0 = this.panda.yRot * (float) (Math.PI / 180.0);
                    int var1 = 0;
                    int var2 = 0;
                    float var3 = -Mth.sin(var0);
                    float var4 = Mth.cos(var0);
                    if ((double)Math.abs(var3) > 0.5) {
                        var1 = (int)((float)var1 + var3 / Math.abs(var3));
                    }

                    if ((double)Math.abs(var4) > 0.5) {
                        var2 = (int)((float)var2 + var4 / Math.abs(var4));
                    }

                    if (this.panda.level.getBlockState(new BlockPos(this.panda).offset(var1, -1, var2)).isAir()) {
                        return true;
                    } else if (this.panda.isPlayful() && this.panda.random.nextInt(60) == 1) {
                        return true;
                    } else {
                        return this.panda.random.nextInt(500) == 1;
                    }
                }
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            this.panda.roll(true);
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }
    }

    class PandaSitGoal extends Goal {
        private int cooldown;

        public PandaSitGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.cooldown <= Panda.this.tickCount
                && !Panda.this.isBaby()
                && !Panda.this.isInWater()
                && Panda.this.canPerformAction()
                && Panda.this.getUnhappyCounter() <= 0) {
                List<ItemEntity> var0 = Panda.this.level
                    .getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(6.0, 6.0, 6.0), Panda.PANDA_ITEMS);
                return !var0.isEmpty() || !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (!Panda.this.isInWater() && (Panda.this.isLazy() || Panda.this.random.nextInt(600) != 1)) {
                return Panda.this.random.nextInt(2000) != 1;
            } else {
                return false;
            }
        }

        @Override
        public void tick() {
            if (!Panda.this.isSitting() && !Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.tryToSit();
            }

        }

        @Override
        public void start() {
            List<ItemEntity> var0 = Panda.this.level
                .getEntitiesOfClass(ItemEntity.class, Panda.this.getBoundingBox().inflate(8.0, 8.0, 8.0), Panda.PANDA_ITEMS);
            if (!var0.isEmpty() && Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.getNavigation().moveTo(var0.get(0), 1.2F);
            } else if (!Panda.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                Panda.this.tryToSit();
            }

            this.cooldown = 0;
        }

        @Override
        public void stop() {
            ItemStack var0 = Panda.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!var0.isEmpty()) {
                Panda.this.spawnAtLocation(var0);
                Panda.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                int var1 = Panda.this.isLazy() ? Panda.this.random.nextInt(50) + 10 : Panda.this.random.nextInt(150) + 10;
                this.cooldown = Panda.this.tickCount + var1 * 20;
            }

            Panda.this.sit(false);
        }
    }

    static class PandaSneezeGoal extends Goal {
        private final Panda panda;

        public PandaSneezeGoal(Panda param0) {
            this.panda = param0;
        }

        @Override
        public boolean canUse() {
            if (this.panda.isBaby() && this.panda.canPerformAction()) {
                if (this.panda.isWeak() && this.panda.random.nextInt(500) == 1) {
                    return true;
                } else {
                    return this.panda.random.nextInt(6000) == 1;
                }
            } else {
                return false;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            this.panda.sneeze(true);
        }
    }
}
