package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
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
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Pillager extends AbstractIllager implements CrossbowAttackMob, RangedAttackMob {
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

    @Override
    protected void registerAttributes() {
        super.registerAttributes();
        this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35F);
        this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24.0);
        this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(5.0);
        this.getAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(32.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_CHARGING_CROSSBOW, false);
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
            return this.isAggressive() ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.CROSSED;
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
        Block var0 = param1.getBlockState(param0.below()).getBlock();
        return var0 != Blocks.GRASS_BLOCK && var0 != Blocks.SAND ? 0.5F - param1.getBrightness(param0) : 10.0F;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.populateDefaultEquipmentSlots(param1);
        this.populateDefaultEquipmentEnchantments(param1);
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        ItemStack var0 = new ItemStack(Items.CROSSBOW);
        if (this.random.nextInt(300) == 0) {
            Map<Enchantment, Integer> var1 = Maps.newHashMap();
            var1.put(Enchantments.PIERCING, 1);
            EnchantmentHelper.setEnchantments(var1, var0);
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, var0);
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
        InteractionHand var0 = ProjectileUtil.getWeaponHoldingHand(this, Items.CROSSBOW);
        ItemStack var1 = this.getItemInHand(var0);
        if (this.isHolding(Items.CROSSBOW)) {
            CrossbowItem.performShooting(this.level, this, var0, var1, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        }

        this.noActionTime = 0;
    }

    @Override
    public void shootProjectile(LivingEntity param0, ItemStack param1, Projectile param2, float param3) {
        Entity var0 = (Entity)param2;
        double var1 = param0.x - this.x;
        double var2 = param0.z - this.z;
        double var3 = (double)Mth.sqrt(var1 * var1 + var2 * var2);
        double var4 = param0.getBoundingBox().minY + (double)(param0.getBbHeight() / 3.0F) - var0.y + var3 * 0.2F;
        Vector3f var5 = this.getProjectileShotVector(new Vec3(var1, var4, var2), param3);
        param2.shoot((double)var5.x(), (double)var5.y(), (double)var5.z(), 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.CROSSBOW_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private Vector3f getProjectileShotVector(Vec3 param0, float param1) {
        Vec3 var0 = param0.normalize();
        Vec3 var1 = var0.cross(new Vec3(0.0, 1.0, 0.0));
        if (var1.lengthSqr() <= 1.0E-7) {
            var1 = var0.cross(this.getUpVector(1.0F));
        }

        Quaternion var2 = new Quaternion(new Vector3f(var1), 90.0F, true);
        Vector3f var3 = new Vector3f(var0);
        var3.transform(var2);
        Quaternion var4 = new Quaternion(var3, param1, true);
        Vector3f var5 = new Vector3f(var0);
        var5.transform(var4);
        return var5;
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void pickUpItem(ItemEntity param0) {
        ItemStack var0 = param0.getItem();
        if (var0.getItem() instanceof BannerItem) {
            super.pickUpItem(param0);
        } else {
            Item var1 = var0.getItem();
            if (this.wantsItem(var1)) {
                ItemStack var2 = this.inventory.addItem(var0);
                if (var2.isEmpty()) {
                    param0.remove();
                } else {
                    var0.setCount(var2.getCount());
                }
            }
        }

    }

    private boolean wantsItem(Item param0) {
        return this.hasActiveRaid() && param0 == Items.WHITE_BANNER;
    }

    @Override
    public boolean setSlot(int param0, ItemStack param1) {
        if (super.setSlot(param0, param1)) {
            return true;
        } else {
            int var0 = param0 - 300;
            if (var0 >= 0 && var0 < this.inventory.getContainerSize()) {
                this.inventory.setItem(var0, param1);
                return true;
            } else {
                return false;
            }
        }
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
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() && this.getInventory().isEmpty();
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.PILLAGER_CELEBRATE;
    }

    @Override
    public boolean removeWhenFarAway(double param0) {
        return super.removeWhenFarAway(param0) && this.getInventory().isEmpty();
    }
}
