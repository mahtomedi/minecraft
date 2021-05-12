package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LlamaFollowCaravanGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class Llama extends AbstractChestedHorse implements RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final int VARIANTS = 4;
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Blocks.HAY_BLOCK.asItem());
    private static final EntityDataAccessor<Integer> DATA_STRENGTH_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_SWAG_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VARIANT_ID = SynchedEntityData.defineId(Llama.class, EntityDataSerializers.INT);
    boolean didSpit;
    @Nullable
    private Llama caravanHead;
    @Nullable
    private Llama caravanTail;

    public Llama(EntityType<? extends Llama> param0, Level param1) {
        super(param0, param1);
    }

    public boolean isTraderLlama() {
        return false;
    }

    private void setStrength(int param0) {
        this.entityData.set(DATA_STRENGTH_ID, Math.max(1, Math.min(5, param0)));
    }

    private void setRandomStrength() {
        int var0 = this.random.nextFloat() < 0.04F ? 5 : 3;
        this.setStrength(1 + this.random.nextInt(var0));
    }

    public int getStrength() {
        return this.entityData.get(DATA_STRENGTH_ID);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Variant", this.getVariant());
        param0.putInt("Strength", this.getStrength());
        if (!this.inventory.getItem(1).isEmpty()) {
            param0.put("DecorItem", this.inventory.getItem(1).save(new CompoundTag()));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.setStrength(param0.getInt("Strength"));
        super.readAdditionalSaveData(param0);
        this.setVariant(param0.getInt("Variant"));
        if (param0.contains("DecorItem", 10)) {
            this.inventory.setItem(1, ItemStack.of(param0.getCompound("DecorItem")));
        }

        this.updateContainerEquipment();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new LlamaFollowCaravanGoal(this, 2.1F));
        this.goalSelector.addGoal(3, new RangedAttackGoal(this, 1.25, 40, 20.0F));
        this.goalSelector.addGoal(3, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new Llama.LlamaHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new Llama.LlamaAttackWolfGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBaseChestedHorseAttributes().add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STRENGTH_ID, 0);
        this.entityData.define(DATA_SWAG_ID, -1);
        this.entityData.define(DATA_VARIANT_ID, 0);
    }

    public int getVariant() {
        return Mth.clamp(this.entityData.get(DATA_VARIANT_ID), 0, 3);
    }

    public void setVariant(int param0) {
        this.entityData.set(DATA_VARIANT_ID, param0);
    }

    @Override
    protected int getInventorySize() {
        return this.hasChest() ? 2 + 3 * this.getInventoryColumns() : super.getInventorySize();
    }

    @Override
    public void positionRider(Entity param0) {
        if (this.hasPassenger(param0)) {
            float var0 = Mth.cos(this.yBodyRot * (float) (Math.PI / 180.0));
            float var1 = Mth.sin(this.yBodyRot * (float) (Math.PI / 180.0));
            float var2 = 0.3F;
            param0.setPos(
                this.getX() + (double)(0.3F * var1),
                this.getY() + this.getPassengersRidingOffset() + param0.getMyRidingOffset(),
                this.getZ() - (double)(0.3F * var0)
            );
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return (double)this.getBbHeight() * 0.67;
    }

    @Override
    public boolean canBeControlledByRider() {
        return false;
    }

    @Override
    public boolean isFood(ItemStack param0) {
        return FOOD_ITEMS.test(param0);
    }

    @Override
    protected boolean handleEating(Player param0, ItemStack param1) {
        int var0 = 0;
        int var1 = 0;
        float var2 = 0.0F;
        boolean var3 = false;
        if (param1.is(Items.WHEAT)) {
            var0 = 10;
            var1 = 3;
            var2 = 2.0F;
        } else if (param1.is(Blocks.HAY_BLOCK.asItem())) {
            var0 = 90;
            var1 = 6;
            var2 = 10.0F;
            if (this.isTamed() && this.getAge() == 0 && this.canFallInLove()) {
                var3 = true;
                this.setInLove(param0);
            }
        }

        if (this.getHealth() < this.getMaxHealth() && var2 > 0.0F) {
            this.heal(var2);
            var3 = true;
        }

        if (this.isBaby() && var0 > 0) {
            this.level.addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level.isClientSide) {
                this.ageUp(var0);
            }

            var3 = true;
        }

        if (var1 > 0 && (var3 || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
            var3 = true;
            if (!this.level.isClientSide) {
                this.modifyTemper(var1);
            }
        }

        if (var3) {
            this.gameEvent(GameEvent.MOB_INTERACT, this.eyeBlockPosition());
            if (!this.isSilent()) {
                SoundEvent var4 = this.getEatingSound();
                if (var4 != null) {
                    this.level
                        .playSound(
                            null,
                            this.getX(),
                            this.getY(),
                            this.getZ(),
                            this.getEatingSound(),
                            this.getSoundSource(),
                            1.0F,
                            1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                        );
                }
            }
        }

        return var3;
    }

    @Override
    protected boolean isImmobile() {
        return this.isDeadOrDying() || this.isEating();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setRandomStrength();
        int var0;
        if (param3 instanceof Llama.LlamaGroupData) {
            var0 = ((Llama.LlamaGroupData)param3).variant;
        } else {
            var0 = this.random.nextInt(4);
            param3 = new Llama.LlamaGroupData(var0);
        }

        this.setVariant(var0);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.LLAMA_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(SoundEvents.LLAMA_STEP, 0.15F, 1.0F);
    }

    @Override
    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.LLAMA_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    @Override
    public void makeMad() {
        SoundEvent var0 = this.getAngrySound();
        if (var0 != null) {
            this.playSound(var0, this.getSoundVolume(), this.getVoicePitch());
        }

    }

    @Override
    public int getInventoryColumns() {
        return this.getStrength();
    }

    @Override
    public boolean canWearArmor() {
        return true;
    }

    @Override
    public boolean isWearingArmor() {
        return !this.inventory.getItem(1).isEmpty();
    }

    @Override
    public boolean isArmor(ItemStack param0) {
        return param0.is(ItemTags.CARPETS);
    }

    @Override
    public boolean isSaddleable() {
        return false;
    }

    @Override
    public void containerChanged(Container param0) {
        DyeColor var0 = this.getSwag();
        super.containerChanged(param0);
        DyeColor var1 = this.getSwag();
        if (this.tickCount > 20 && var1 != null && var1 != var0) {
            this.playSound(SoundEvents.LLAMA_SWAG, 0.5F, 1.0F);
        }

    }

    @Override
    protected void updateContainerEquipment() {
        if (!this.level.isClientSide) {
            super.updateContainerEquipment();
            this.setSwag(getDyeColor(this.inventory.getItem(1)));
        }
    }

    private void setSwag(@Nullable DyeColor param0) {
        this.entityData.set(DATA_SWAG_ID, param0 == null ? -1 : param0.getId());
    }

    @Nullable
    private static DyeColor getDyeColor(ItemStack param0) {
        Block var0 = Block.byItem(param0.getItem());
        return var0 instanceof WoolCarpetBlock ? ((WoolCarpetBlock)var0).getColor() : null;
    }

    @Nullable
    public DyeColor getSwag() {
        int var0 = this.entityData.get(DATA_SWAG_ID);
        return var0 == -1 ? null : DyeColor.byId(var0);
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canMate(Animal param0) {
        return param0 != this && param0 instanceof Llama && this.canParent() && ((Llama)param0).canParent();
    }

    public Llama getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        Llama var0 = this.makeBabyLlama();
        this.setOffspringAttributes(param1, var0);
        Llama var1 = (Llama)param1;
        int var2 = this.random.nextInt(Math.max(this.getStrength(), var1.getStrength())) + 1;
        if (this.random.nextFloat() < 0.03F) {
            ++var2;
        }

        var0.setStrength(var2);
        var0.setVariant(this.random.nextBoolean() ? this.getVariant() : var1.getVariant());
        return var0;
    }

    protected Llama makeBabyLlama() {
        return EntityType.LLAMA.create(this.level);
    }

    private void spit(LivingEntity param0) {
        LlamaSpit var0 = new LlamaSpit(this.level, this);
        double var1 = param0.getX() - this.getX();
        double var2 = param0.getY(0.3333333333333333) - var0.getY();
        double var3 = param0.getZ() - this.getZ();
        float var4 = Mth.sqrt(var1 * var1 + var3 * var3) * 0.2F;
        var0.shoot(var1, var2 + (double)var4, var3, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.level
                .playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.LLAMA_SPIT,
                    this.getSoundSource(),
                    1.0F,
                    1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.2F
                );
        }

        this.level.addFreshEntity(var0);
        this.didSpit = true;
    }

    void setDidSpit(boolean param0) {
        this.didSpit = param0;
    }

    @Override
    public boolean causeFallDamage(float param0, float param1, DamageSource param2) {
        int var0 = this.calculateFallDamage(param0, param1);
        if (var0 <= 0) {
            return false;
        } else {
            if (param0 >= 6.0F) {
                this.hurt(param2, (float)var0);
                if (this.isVehicle()) {
                    for(Entity var1 : this.getIndirectPassengers()) {
                        var1.hurt(param2, (float)var0);
                    }
                }
            }

            this.playBlockFallSound();
            return true;
        }
    }

    public void leaveCaravan() {
        if (this.caravanHead != null) {
            this.caravanHead.caravanTail = null;
        }

        this.caravanHead = null;
    }

    public void joinCaravan(Llama param0) {
        this.caravanHead = param0;
        this.caravanHead.caravanTail = this;
    }

    public boolean hasCaravanTail() {
        return this.caravanTail != null;
    }

    public boolean inCaravan() {
        return this.caravanHead != null;
    }

    @Nullable
    public Llama getCaravanHead() {
        return this.caravanHead;
    }

    @Override
    protected double followLeashSpeed() {
        return 2.0;
    }

    @Override
    protected void followMommy() {
        if (!this.inCaravan() && this.isBaby()) {
            super.followMommy();
        }

    }

    @Override
    public boolean canEatGrass() {
        return false;
    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        this.spit(param0);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.75 * (double)this.getEyeHeight(), (double)this.getBbWidth() * 0.5);
    }

    static class LlamaAttackWolfGoal extends NearestAttackableTargetGoal<Wolf> {
        public LlamaAttackWolfGoal(Llama param0) {
            super(param0, Wolf.class, 16, false, true, param0x -> !((Wolf)param0x).isTame());
        }

        @Override
        protected double getFollowDistance() {
            return super.getFollowDistance() * 0.25;
        }
    }

    static class LlamaGroupData extends AgeableMob.AgeableMobGroupData {
        public final int variant;

        LlamaGroupData(int param0) {
            super(true);
            this.variant = param0;
        }
    }

    static class LlamaHurtByTargetGoal extends HurtByTargetGoal {
        public LlamaHurtByTargetGoal(Llama param0) {
            super(param0);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.mob instanceof Llama var0 && var0.didSpit) {
                var0.setDidSpit(false);
                return false;
            }

            return super.canContinueToUse();
        }
    }
}
