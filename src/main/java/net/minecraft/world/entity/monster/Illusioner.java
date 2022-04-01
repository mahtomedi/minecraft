package net.minecraft.world.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Illusioner extends SpellcasterIllager implements RangedAttackMob {
    private static final int NUM_ILLUSIONS = 4;
    private static final int ILLUSION_TRANSITION_TICKS = 3;
    private static final int ILLUSION_SPREAD = 3;
    private int clientSideIllusionTicks;
    private final Vec3[][] clientSideIllusionOffsets;

    public Illusioner(EntityType<? extends Illusioner> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 5;
        this.clientSideIllusionOffsets = new Vec3[2][4];

        for(int var0 = 0; var0 < 4; ++var0) {
            this.clientSideIllusionOffsets[0][var0] = Vec3.ZERO;
            this.clientSideIllusionOffsets[1][var0] = Vec3.ZERO;
        }

    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpellcasterIllager.SpellcasterCastingSpellGoal());
        this.goalSelector.addGoal(4, new Illusioner.IllusionerMirrorSpellGoal());
        this.goalSelector.addGoal(5, new Illusioner.IllusionerBlindnessSpellGoal());
        this.goalSelector.addGoal(6, new RangedBowAttackGoal<>(this, 0.5, 20, 15.0F));
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false).setUnseenMemoryTicks(300));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 18.0).add(Attributes.MAX_HEALTH, 32.0);
    }

    @Override
    public SpawnGroupData finalizeSpawn(
        ServerLevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return this.getBoundingBox().inflate(3.0, 0.0, 3.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level.isClientSide && this.isInvisible()) {
            --this.clientSideIllusionTicks;
            if (this.clientSideIllusionTicks < 0) {
                this.clientSideIllusionTicks = 0;
            }

            if (this.hurtTime == 1 || this.tickCount % 1200 == 0) {
                this.clientSideIllusionTicks = 3;
                float var0 = -6.0F;
                int var1 = 13;

                for(int var2 = 0; var2 < 4; ++var2) {
                    this.clientSideIllusionOffsets[0][var2] = this.clientSideIllusionOffsets[1][var2];
                    this.clientSideIllusionOffsets[1][var2] = new Vec3(
                        (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5,
                        (double)Math.max(0, this.random.nextInt(6) - 4),
                        (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5
                    );
                }

                for(int var3 = 0; var3 < 16; ++var3) {
                    this.level.addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5), this.getRandomY(), this.getZ(0.5), 0.0, 0.0, 0.0);
                }

                this.level.playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
            } else if (this.hurtTime == this.hurtDuration - 1) {
                this.clientSideIllusionTicks = 3;

                for(int var4 = 0; var4 < 4; ++var4) {
                    this.clientSideIllusionOffsets[0][var4] = this.clientSideIllusionOffsets[1][var4];
                    this.clientSideIllusionOffsets[1][var4] = new Vec3(0.0, 0.0, 0.0);
                }
            }
        }

    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    public Vec3[] getIllusionOffsets(float param0) {
        if (this.clientSideIllusionTicks <= 0) {
            return this.clientSideIllusionOffsets[1];
        } else {
            double var0 = (double)(((float)this.clientSideIllusionTicks - param0) / 3.0F);
            var0 = Math.pow(var0, 0.25);
            Vec3[] var1 = new Vec3[4];

            for(int var2 = 0; var2 < 4; ++var2) {
                var1[var2] = this.clientSideIllusionOffsets[1][var2].scale(1.0 - var0).add(this.clientSideIllusionOffsets[0][var2].scale(var0));
            }

            return var1;
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
        return SoundEvents.ILLUSIONER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ILLUSIONER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.ILLUSIONER_HURT;
    }

    @Override
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.ILLUSIONER_CAST_SPELL;
    }

    @Override
    public void applyRaidBuffs(int param0, boolean param1) {
    }

    @Override
    public void performRangedAttack(LivingEntity param0, float param1) {
        ItemStack var0 = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
        AbstractArrow var1 = ProjectileUtil.getMobArrow(this, var0, param1);
        double var2 = param0.getX() - this.getX();
        double var3 = param0.getY(0.3333333333333333) - var1.getY();
        double var4 = param0.getZ() - this.getZ();
        double var5 = Math.sqrt(var2 * var2 + var4 * var4);
        var1.shoot(var2, var3 + var5 * 0.2F, var4, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level.addFreshEntity(var1);
    }

    @Override
    public void performVehicleAttack(float param0) {
        if (this.isPassenger()) {
            ItemStack var0 = this.getProjectile(this.getItemInHand(ProjectileUtil.getWeaponHoldingHand(this, Items.BOW)));
            AbstractArrow var1 = ProjectileUtil.getMobArrow(this, var0, param0);
            Vec3 var2 = this.getRootVehicle().getForward();
            double var3 = var2.x;
            double var4 = var2.y;
            double var5 = var2.z;
            double var6 = Math.sqrt(var3 * var3 + var5 * var5);
            var1.shoot(var3, var4 + var6 * 0.2F, var5, 1.6F, (float)(14 - this.level.getDifficulty().getId() * 4));
            this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.level.addFreshEntity(var1);
        }
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.getCarried() != LivingEntity.Carried.NONE) {
            return AbstractIllager.IllagerArmPose.CROSSBOW_HOLD;
        } else if (this.isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        } else {
            return this.isAggressive() ? AbstractIllager.IllagerArmPose.BOW_AND_ARROW : AbstractIllager.IllagerArmPose.CROSSED;
        }
    }

    class IllusionerBlindnessSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private int lastTargetId;

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else if (Illusioner.this.getTarget() == null) {
                return false;
            } else if (Illusioner.this.getTarget().getId() == this.lastTargetId) {
                return false;
            } else {
                return Illusioner.this.level.getCurrentDifficultyAt(Illusioner.this.blockPosition()).isHarderThan((float)Difficulty.NORMAL.ordinal());
            }
        }

        @Override
        public void start() {
            super.start();
            LivingEntity var0 = Illusioner.this.getTarget();
            if (var0 != null) {
                this.lastTargetId = var0.getId();
            }

        }

        @Override
        protected int getCastingTime() {
            return 20;
        }

        @Override
        protected int getCastingInterval() {
            return 180;
        }

        @Override
        protected void performSpellCasting() {
            Illusioner.this.getTarget().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 400), Illusioner.this);
        }

        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_BLINDNESS;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.BLINDNESS;
        }
    }

    class IllusionerMirrorSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else {
                return !Illusioner.this.hasEffect(MobEffects.INVISIBILITY);
            }
        }

        @Override
        protected int getCastingTime() {
            return 20;
        }

        @Override
        protected int getCastingInterval() {
            return 340;
        }

        @Override
        protected void performSpellCasting() {
            Illusioner.this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 1200));
        }

        @Nullable
        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.ILLUSIONER_PREPARE_MIRROR;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.DISAPPEAR;
        }
    }
}
