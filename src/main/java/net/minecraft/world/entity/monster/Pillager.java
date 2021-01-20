package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Pillager extends AbstractIllager implements CrossbowAttackMob {
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Pillager.class, EntityDataSerializers.BOOLEAN);
    private final SimpleContainer inventory = new SimpleContainer(5);

    public Pillager(EntityType<? extends Pillager> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0F));
        this.goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 1.0, 8.0F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35F)
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0)
            .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING_CROSSBOW, false);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem param0) {
        return param0 == Items.CROSSBOW;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isChargingCrossbow() {
        return this.entityData.get(IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean param0) {
        this.entityData.set(IS_CHARGING_CROSSBOW, param0);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        ListTag var0 = new ListTag();

        for(int var1 = 0; var1 < this.inventory.getContainerSize(); ++var1) {
            ItemStack var2 = this.inventory.getItem(var1);
            if (!var2.isEmpty()) {
                var0.add(var2.save(new CompoundTag()));
            }
        }

        param0.put("Inventory", var0);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isChargingCrossbow()) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE;
        } else if (this.isHolding(Items.CROSSBOW)) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        } else {
            return this.isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        ListTag var0 = param0.getList("Inventory", 10);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            ItemStack var2 = ItemStack.of(var0.getCompound(var1));
            if (!var2.isEmpty()) {
                this.inventory.addItem(var2);
            }
        }

        this.setCanPickUpLoot(true);
    }

    @Override
    public float getWalkTargetValue(BlockPos param0, LevelReader param1) {
        BlockState var0 = param1.getBlockState(param0.below());
        return !var0.is(Blocks.GRASS_BLOCK) && !var0.is(Blocks.SAND) ? 0.5F - param1.getBrightness(param0) : 10.0F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.populateDefaultEquipmentSlots(param1);
        this.populateDefaultEquipmentEnchantments(param1);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
    }

    @Override
    protected void enchantSpawnedWeapon(float param0) {
        super.enchantSpawnedWeapon(param0);
        if (this.random.nextInt(300) == 0) {
            ItemStack var0 = this.getMainHandItem();
            if (var0.is(Items.CROSSBOW)) {
                Map<Enchantment, Integer> var1 = EnchantmentHelper.getEnchantments(var0);
                var1.putIfAbsent(Enchantments.PIERCING, 1);
                EnchantmentHelper.setEnchantments(var1, var0);
                this.setItemSlot(EquipmentSlot.MAINHAND, var0);
            }
        }

    }

    @Override
    public boolean isAlliedTo(Entity param0) {
        if (super.isAlliedTo(param0)) {
            return true;
        } else if (param0 instanceof LivingEntity && ((LivingEntity)param0).getMobType() == MobType.ILLAGER) {
            return this.getTeam() == null && param0.getTeam() == null;
        } else {
            return false;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PILLAGER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PILLAGER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.PILLAGER_HURT;
    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        this.performCrossbowAttack(this, 1.6F);
    }

    @Override
    public void shootCrossbowProjectile(LivingEntity param0, ItemStack param1, Projectile param2, float param3) {
        this.shootCrossbowProjectile(this, param0, param2, param3, 1.6F);
    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        ItemStack var0 = param0.getItem();
        if (var0.getItem() instanceof BannerItem) {
            super.pickUpItem(param0);
        } else if (this.wantsItem(var0)) {
            this.onItemPickup(param0);
            ItemStack var1 = this.inventory.addItem(var0);
            if (var1.isEmpty()) {
                param0.discard();
            } else {
                var0.setCount(var1.getCount());
            }
        }

    }

    private boolean wantsItem(ItemStack param0) {
        return this.hasActiveRaid() && param0.is(Items.WHITE_BANNER);
    }

    @Override
    public SlotAccess getSlot(int param0) {
        int var0 = param0 - 300;
        return var0 >= 0 && var0 < this.inventory.getContainerSize() ? SlotAccess.forContainer(this.inventory, var0) : super.getSlot(param0);
    }

    @Override
    public void applyRaidBuffs(int param0, boolean param1) {
        Raid var0 = this.getCurrentRaid();
        boolean var1 = this.random.nextFloat() <= var0.getEnchantOdds();
        if (var1) {
            ItemStack var2 = new ItemStack(Items.CROSSBOW);
            Map<Enchantment, Integer> var3 = Maps.newHashMap();
            if (param0 > var0.getNumGroups(Difficulty.NORMAL)) {
                var3.put(Enchantments.QUICK_CHARGE, 2);
            } else if (param0 > var0.getNumGroups(Difficulty.EASY)) {
                var3.put(Enchantments.QUICK_CHARGE, 1);
            }

            var3.put(Enchantments.MULTISHOT, 1);
            EnchantmentHelper.setEnchantments(var3, var2);
            this.setItemSlot(EquipmentSlot.MAINHAND, var2);
        }

    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }
}
