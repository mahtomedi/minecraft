package net.minecraft.world.entity.animal.horse;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.VariantHolder;
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
import net.minecraft.world.entity.ai.goal.TemptGoal;
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
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Llama extends AbstractChestedHorse implements VariantHolder<Llama.Variant>, RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
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

    private void setRandomStrength(RandomSource param0) {
        int var0 = param0.nextFloat() < 0.04F ? 5 : 3;
        this.setStrength(1 + param0.nextInt(var0));
    }

    public int getStrength() {
        return this.entityData.get(DATA_STRENGTH_ID);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Variant", this.getVariant().id);
        param0.putInt("Strength", this.getStrength());
        if (!this.inventory.getItem(1).isEmpty()) {
            param0.put("DecorItem", this.inventory.getItem(1).save(new CompoundTag()));
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        this.setStrength(param0.getInt("Strength"));
        super.readAdditionalSaveData(param0);
        this.setVariant(Llama.Variant.byId(param0.getInt("Variant")));
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
        this.goalSelector.addGoal(5, new TemptGoal(this, 1.25, Ingredient.of(Items.HAY_BLOCK), false));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
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

    public Llama.Variant getVariant() {
        return Llama.Variant.byId(this.entityData.get(DATA_VARIANT_ID));
    }

    public void setVariant(Llama.Variant param0) {
        this.entityData.set(DATA_VARIANT_ID, param0.id);
    }

    @Override
    protected int getInventorySize() {
        return this.hasChest() ? 2 + 3 * this.getInventoryColumns() : super.getInventorySize();
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
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide) {
                this.ageUp(var0);
            }

            var3 = true;
        }

        if (var1 > 0 && (var3 || !this.isTamed()) && this.getTemper() < this.getMaxTemper()) {
            var3 = true;
            if (!this.level().isClientSide) {
                this.modifyTemper(var1);
            }
        }

        if (var3 && !this.isSilent()) {
            SoundEvent var4 = this.getEatingSound();
            if (var4 != null) {
                this.level()
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

        return var3;
    }

    @Override
    public boolean isImmobile() {
        return this.isDeadOrDying() || this.isEating();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        RandomSource var0 = param0.getRandom();
        this.setRandomStrength(var0);
        Llama.Variant var1;
        if (param3 instanceof Llama.LlamaGroupData) {
            var1 = ((Llama.LlamaGroupData)param3).variant;
        } else {
            var1 = Util.getRandom(Llama.Variant.values(), var0);
            param3 = new Llama.LlamaGroupData(var1);
        }

        this.setVariant(var1);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
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
        return param0.is(ItemTags.WOOL_CARPETS);
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
        if (!this.level().isClientSide) {
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

    @Nullable
    public Llama getBreedOffspring(ServerLevel param0, AgeableMob param1) {
        Llama var0 = this.makeNewLlama();
        if (var0 != null) {
            this.setOffspringAttributes(param1, var0);
            Llama var1 = (Llama)param1;
            int var2 = this.random.nextInt(Math.max(this.getStrength(), var1.getStrength())) + 1;
            if (this.random.nextFloat() < 0.03F) {
                ++var2;
            }

            var0.setStrength(var2);
            var0.setVariant(this.random.nextBoolean() ? this.getVariant() : var1.getVariant());
        }

        return var0;
    }

    @Nullable
    protected Llama makeNewLlama() {
        return EntityType.LLAMA.create(this.level());
    }

    private void spit(LivingEntity param0) {
        LlamaSpit var0 = new LlamaSpit(this.level(), this);
        double var1 = param0.getX() - this.getX();
        double var2 = param0.getY(0.3333333333333333) - var0.getY();
        double var3 = param0.getZ() - this.getZ();
        double var4 = Math.sqrt(var1 * var1 + var3 * var3) * 0.2F;
        var0.shoot(var1, var2 + var4, var3, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.level()
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

        this.level().addFreshEntity(var0);
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

    @Override
    protected Vector3f getPassengerAttachmentPoint(Entity param0, EntityDimensions param1, float param2) {
        return new Vector3f(0.0F, param1.height - (this.isBaby() ? 0.8125F : 0.5F) * param2, -0.3F * param2);
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
        public final Llama.Variant variant;

        LlamaGroupData(Llama.Variant param0) {
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
            Mob var2 = this.mob;
            if (var2 instanceof Llama var0 && var0.didSpit) {
                var0.setDidSpit(false);
                return false;
            }

            return super.canContinueToUse();
        }
    }

    public static enum Variant implements StringRepresentable {
        CREAMY(0, "creamy"),
        WHITE(1, "white"),
        BROWN(2, "brown"),
        GRAY(3, "gray");

        public static final Codec<Llama.Variant> CODEC = StringRepresentable.fromEnum(Llama.Variant::values);
        private static final IntFunction<Llama.Variant> BY_ID = ByIdMap.continuous(Llama.Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
        final int id;
        private final String name;

        private Variant(int param0, String param1) {
            this.id = param0;
            this.name = param1;
        }

        public int getId() {
            return this.id;
        }

        public static Llama.Variant byId(int param0) {
            return BY_ID.apply(param0);
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
