package net.minecraft.world.entity.monster;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FleeSunGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.RestrictSunGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractSkeleton extends Monster implements RangedAttackMob {
    private final RangedBowAttackGoal<AbstractSkeleton> bowGoal = new RangedBowAttackGoal<>(this, 1.0, 20, 15.0F);
    private final MeleeAttackGoal meleeGoal = new MeleeAttackGoal(this, 1.2, false) {
        @Override
        public void stop() {
            super.stop();
            AbstractSkeleton.this.setAggressive(false);
        }

        @Override
        public void start() {
            super.start();
            AbstractSkeleton.this.setAggressive(true);
        }
    };

    protected AbstractSkeleton(EntityType<? extends AbstractSkeleton> param0, Level param1) {
        super(param0, param1);
        this.reassessWeaponGoal();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new RestrictSunGoal(this));
        this.goalSelector.addGoal(3, new FleeSunGoal(this, 1.0));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Wolf.class, 6.0F, 1.0, 1.2));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25);
    }

    @Override
    protected void playStepSound(BlockPos param0, BlockState param1) {
        this.playSound(this.getStepSound(), 0.15F, 1.0F);
    }

    abstract SoundEvent getStepSound();

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    public void aiStep() {
        boolean var0 = this.isSunBurnTick();
        if (var0) {
            ItemStack var1 = this.getItemBySlot(EquipmentSlot.HEAD);
            if (!var1.isEmpty()) {
                if (var1.isDamageableItem()) {
                    var1.setDamageValue(var1.getDamageValue() + this.random.nextInt(2));
                    if (var1.getDamageValue() >= var1.getMaxDamage()) {
                        this.broadcastBreakEvent(EquipmentSlot.HEAD);
                        this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                    }
                }

                var0 = false;
            }

            if (var0) {
                this.setSecondsOnFire(8);
            }
        }

        super.aiStep();
    }

    @Override
    public void rideTick() {
        super.rideTick();
        if (this.getVehicle() instanceof PathfinderMob) {
            PathfinderMob var0 = (PathfinderMob)this.getVehicle();
            this.yBodyRot = var0.yBodyRot;
        }

    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        super.populateDefaultEquipmentSlots(param0);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        param3 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        this.populateDefaultEquipmentSlots(param1);
        this.populateDefaultEquipmentEnchantments(param1);
        this.reassessWeaponGoal();
        this.setCanPickUpLoot(this.random.nextFloat() < 0.55F * param1.getSpecialMultiplier());
        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            LocalDate var0 = LocalDate.now();
            int var1 = var0.get(ChronoField.DAY_OF_MONTH);
            int var2 = var0.get(ChronoField.MONTH_OF_YEAR);
            if (var2 == 10 && var1 == 31 && this.random.nextFloat() < 0.25F) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.armorDropChances[EquipmentSlot.HEAD.getIndex()] = 0.0F;
            }
        }

        return param3;
    }

    public void reassessWeaponGoal() {
        if (this.level != null && !this.level.isClientSide) {
            this.goalSelector.removeGoal(this.meleeGoal);
            this.goalSelector.removeGoal(this.bowGoal);
            ItemStack var0 = this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW));
            if (var0.getItem() == Items.BOW) {
                int var1 = 20;
                if (this.level.getDifficulty() != Difficulty.HARD) {
                    var1 = 40;
                }

                this.bowGoal.setMinAttackInterval(var1);
                this.goalSelector.addGoal(4, this.bowGoal);
            } else {
                this.goalSelector.addGoal(4, this.meleeGoal);
            }

        }
    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        ItemStack var0 = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrow var1 = this.getArrow(var0, param1);
        double var2 = param0.getX() - this.getX();
        double var3 = param0.getY(0.3333333333333333) - var1.getY();
        double var4 = param0.getZ() - this.getZ();
        double var5 = (double)Mth.sqrt(var2 * var2 + var4 * var4);
        var1.shoot(var2, var3 + var5 * 0.2F, var4, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(var1);
    }

    protected AbstractArrow getArrow(ItemStack param0, float param1) {
        return ProjectileUtil.getMobArrow(this, param0, param1);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem param0) {
        return param0 == Items.BOW;
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.reassessWeaponGoal();
    }

    @Override
    public void setItemSlot(EquipmentSlot param0, ItemStack param1) {
        super.setItemSlot(param0, param1);
        if (!this.level.isClientSide) {
            this.reassessWeaponGoal();
        }

    }

    @Override
    protected float getStandingEyeHeight(Pose param0, EntityDimensions param1) {
        return 1.74F;
    }

    @Override
    public double getRidingHeight() {
        return -0.6;
    }
}
