package net.minecraft.world.entity.monster;

import com.google.common.collect.Maps;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Vindicator extends AbstractIllager {
    private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = param0 -> param0 == Difficulty.NORMAL || param0 == Difficulty.HARD;
    private boolean isJohnny;

    public Vindicator(EntityType<? extends Vindicator> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new Vindicator.VindicatorBreakDoorGoal(this));
        this.goalSelector.addGoal(2, new AbstractIllager.RaiderOpenDoorGoal(this));
        this.goalSelector.addGoal(3, new Raider.HoldGroundAttackGoal(this, 10.0F));
        this.goalSelector.addGoal(4, new Vindicator.VindicatorMeleeAttackGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new Vindicator.VindicatorJohnnyAttackGoal(this));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    @Override
    protected void customServerAiStep() {
        if (!this.isNoAi()) {
            PathNavigation var0 = this.getNavigation();
            if (var0 instanceof GroundPathNavigation) {
                boolean var1 = ((ServerLevel)this.level).isRaided(this.blockPosition());
                ((GroundPathNavigation)var0).setCanOpenDoors(var1);
            }
        }

        super.customServerAiStep();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MOVEMENT_SPEED, 0.35F)
            .add(Attributes.FOLLOW_RANGE, 12.0)
            .add(Attributes.MAX_HEALTH, 24.0)
            .add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.isJohnny) {
            param0.putBoolean("Johnny", true);
        }

    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isAggressive()) {
            return AbstractIllager.IllagerArmPose.ATTACKING;
        } else {
            return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("Johnny", 99)) {
            this.isJohnny = param0.getBoolean("Johnny");
        }

    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.VINDICATOR_CELEBRATE;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        SpawnGroupData var0 = super.finalizeSpawn(param0, param1, param2, param3, param4);
        ((GroundPathNavigation)this.getNavigation()).setCanOpenDoors(true);
        this.populateDefaultEquipmentSlots(param1);
        this.populateDefaultEquipmentEnchantments(param1);
        return var0;
    }

    @Override
    protected void populateDefaultEquipmentSlots(DifficultyInstance param0) {
        if (this.getCurrentRaid() == null) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
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
    public void setCustomName(@Nullable Component param0) {
        super.setCustomName(param0);
        if (!this.isJohnny && param0 != null && param0.getString().equals("Johnny")) {
            this.isJohnny = true;
        }

    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.VINDICATOR_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.VINDICATOR_HURT;
    }

    @Override
    public void applyRaidBuffs(int param0, boolean param1) {
        ItemStack var0 = new ItemStack(Items.IRON_AXE);
        Raid var1 = this.getCurrentRaid();
        int var2 = 1;
        if (param0 > var1.getNumGroups(Difficulty.NORMAL)) {
            var2 = 2;
        }

        boolean var3 = this.random.nextFloat() <= var1.getEnchantOdds();
        if (var3) {
            Map<Enchantment, Integer> var4 = Maps.newHashMap();
            var4.put(Enchantments.SHARPNESS, var2);
            EnchantmentHelper.setEnchantments(var4, var0);
        }

        this.setItemSlot(EquipmentSlot.MAINHAND, var0);
    }

    static class VindicatorBreakDoorGoal extends BreakDoorGoal {
        public VindicatorBreakDoorGoal(Mob param0) {
            super(param0, 6, Vindicator.DOOR_BREAKING_PREDICATE);
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canContinueToUse() {
            Vindicator var0 = (Vindicator)this.mob;
            return var0.hasActiveRaid() && super.canContinueToUse();
        }

        @Override
        public boolean canUse() {
            Vindicator var0 = (Vindicator)this.mob;
            return var0.hasActiveRaid() && var0.random.nextInt(10) == 0 && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }

    static class VindicatorJohnnyAttackGoal extends NearestAttackableTargetGoal<LivingEntity> {
        public VindicatorJohnnyAttackGoal(Vindicator param0) {
            super(param0, LivingEntity.class, 0, true, true, LivingEntity::attackable);
        }

        @Override
        public boolean canUse() {
            return ((Vindicator)this.mob).isJohnny && super.canUse();
        }

        @Override
        public void start() {
            super.start();
            this.mob.setNoActionTime(0);
        }
    }

    class VindicatorMeleeAttackGoal extends MeleeAttackGoal {
        public VindicatorMeleeAttackGoal(Vindicator param0) {
            super(param0, 1.0, false);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity param0) {
            if (this.mob.getVehicle() instanceof Ravager) {
                float var0 = this.mob.getVehicle().getBbWidth() - 0.1F;
                return (double)(var0 * 2.0F * var0 * 2.0F + param0.getBbWidth());
            } else {
                return super.getAttackReachSqr(param0);
            }
        }
    }
}
