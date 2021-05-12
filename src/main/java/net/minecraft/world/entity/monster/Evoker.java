package net.minecraft.world.entity.monster;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Evoker extends SpellcasterIllager {
    private Sheep wololoTarget;

    public Evoker(EntityType<? extends Evoker> param0, Level param1) {
        super(param0, param1);
        this.xpReward = 10;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new Evoker.EvokerCastingSpellGoal());
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.6, 1.0));
        this.goalSelector.addGoal(4, new Evoker.EvokerSummonSpellGoal());
        this.goalSelector.addGoal(5, new Evoker.EvokerAttackSpellGoal());
        this.goalSelector.addGoal(6, new Evoker.EvokerWololoSpellGoal());
        this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Raider.class).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false).setUnseenMemoryTicks(300));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, false));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.FOLLOW_RANGE, 12.0).add(Attributes.MAX_HEALTH, 24.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
    }

    @Override
    public SoundEvent getCelebrateSound() {
        return SoundEvents.EVOKER_CELEBRATE;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
    }

    @Override
    public boolean isAlliedTo(Entity param0) {
        if (param0 == null) {
            return false;
        } else if (param0 == this) {
            return true;
        } else if (super.isAlliedTo(param0)) {
            return true;
        } else if (param0 instanceof Vex) {
            return this.isAlliedTo(((Vex)param0).getOwner());
        } else if (param0 instanceof LivingEntity && ((LivingEntity)param0).getMobType() == MobType.ILLAGER) {
            return this.getTeam() == null && param0.getTeam() == null;
        } else {
            return false;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        return SoundEvents.EVOKER_HURT;
    }

    void setWololoTarget(@Nullable Sheep param0) {
        this.wololoTarget = param0;
    }

    @Nullable
    Sheep getWololoTarget() {
        return this.wololoTarget;
    }

    @Override
    protected SoundEvent getCastingSoundEvent() {
        return SoundEvents.EVOKER_CAST_SPELL;
    }

    @Override
    public void applyRaidBuffs(int param0, boolean param1) {
    }

    class EvokerAttackSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        @Override
        protected int getCastingTime() {
            return 40;
        }

        @Override
        protected int getCastingInterval() {
            return 100;
        }

        @Override
        protected void performSpellCasting() {
            LivingEntity var0 = Evoker.this.getTarget();
            double var1 = Math.min(var0.getY(), Evoker.this.getY());
            double var2 = Math.max(var0.getY(), Evoker.this.getY()) + 1.0;
            float var3 = (float)Mth.atan2(var0.getZ() - Evoker.this.getZ(), var0.getX() - Evoker.this.getX());
            if (Evoker.this.distanceToSqr(var0) < 9.0) {
                for(int var4 = 0; var4 < 5; ++var4) {
                    float var5 = var3 + (float)var4 * (float) Math.PI * 0.4F;
                    this.createSpellEntity(
                        Evoker.this.getX() + (double)Mth.cos(var5) * 1.5, Evoker.this.getZ() + (double)Mth.sin(var5) * 1.5, var1, var2, var5, 0
                    );
                }

                for(int var6 = 0; var6 < 8; ++var6) {
                    float var7 = var3 + (float)var6 * (float) Math.PI * 2.0F / 8.0F + (float) (Math.PI * 2.0 / 5.0);
                    this.createSpellEntity(
                        Evoker.this.getX() + (double)Mth.cos(var7) * 2.5, Evoker.this.getZ() + (double)Mth.sin(var7) * 2.5, var1, var2, var7, 3
                    );
                }
            } else {
                for(int var8 = 0; var8 < 16; ++var8) {
                    double var9 = 1.25 * (double)(var8 + 1);
                    int var10 = 1 * var8;
                    this.createSpellEntity(
                        Evoker.this.getX() + (double)Mth.cos(var3) * var9, Evoker.this.getZ() + (double)Mth.sin(var3) * var9, var1, var2, var3, var10
                    );
                }
            }

        }

        private void createSpellEntity(double param0, double param1, double param2, double param3, float param4, int param5) {
            BlockPos var0 = new BlockPos(param0, param3, param1);
            boolean var1 = false;
            double var2 = 0.0;

            do {
                BlockPos var3 = var0.below();
                BlockState var4 = Evoker.this.level.getBlockState(var3);
                if (var4.isFaceSturdy(Evoker.this.level, var3, Direction.UP)) {
                    if (!Evoker.this.level.isEmptyBlock(var0)) {
                        BlockState var5 = Evoker.this.level.getBlockState(var0);
                        VoxelShape var6 = var5.getCollisionShape(Evoker.this.level, var0);
                        if (!var6.isEmpty()) {
                            var2 = var6.max(Direction.Axis.Y);
                        }
                    }

                    var1 = true;
                    break;
                }

                var0 = var0.below();
            } while(var0.getY() >= Mth.floor(param2) - 1);

            if (var1) {
                Evoker.this.level.addFreshEntity(new EvokerFangs(Evoker.this.level, param0, (double)var0.getY() + var2, param1, param4, param5, Evoker.this));
            }

        }

        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_ATTACK;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.FANGS;
        }
    }

    class EvokerCastingSpellGoal extends SpellcasterIllager.SpellcasterCastingSpellGoal {
        @Override
        public void tick() {
            if (Evoker.this.getTarget() != null) {
                Evoker.this.getLookControl().setLookAt(Evoker.this.getTarget(), (float)Evoker.this.getMaxHeadYRot(), (float)Evoker.this.getMaxHeadXRot());
            } else if (Evoker.this.getWololoTarget() != null) {
                Evoker.this.getLookControl().setLookAt(Evoker.this.getWololoTarget(), (float)Evoker.this.getMaxHeadYRot(), (float)Evoker.this.getMaxHeadXRot());
            }

        }
    }

    class EvokerSummonSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private final TargetingConditions vexCountTargeting = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().ignoreInvisibilityTesting();

        @Override
        public boolean canUse() {
            if (!super.canUse()) {
                return false;
            } else {
                int var0 = Evoker.this.level
                    .getNearbyEntities(Vex.class, this.vexCountTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0))
                    .size();
                return Evoker.this.random.nextInt(8) + 1 > var0;
            }
        }

        @Override
        protected int getCastingTime() {
            return 100;
        }

        @Override
        protected int getCastingInterval() {
            return 340;
        }

        @Override
        protected void performSpellCasting() {
            ServerLevel var0 = (ServerLevel)Evoker.this.level;

            for(int var1 = 0; var1 < 3; ++var1) {
                BlockPos var2 = Evoker.this.blockPosition().offset(-2 + Evoker.this.random.nextInt(5), 1, -2 + Evoker.this.random.nextInt(5));
                Vex var3 = EntityType.VEX.create(Evoker.this.level);
                var3.moveTo(var2, 0.0F, 0.0F);
                var3.finalizeSpawn(var0, Evoker.this.level.getCurrentDifficultyAt(var2), MobSpawnType.MOB_SUMMONED, null, null);
                var3.setOwner(Evoker.this);
                var3.setBoundOrigin(var2);
                var3.setLimitedLife(20 * (30 + Evoker.this.random.nextInt(90)));
                var0.addFreshEntityWithPassengers(var3);
            }

        }

        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_SUMMON;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.SUMMON_VEX;
        }
    }

    public class EvokerWololoSpellGoal extends SpellcasterIllager.SpellcasterUseSpellGoal {
        private final TargetingConditions wololoTargeting = TargetingConditions.forNonCombat()
            .range(16.0)
            .selector(param0x -> ((Sheep)param0x).getColor() == DyeColor.BLUE);

        @Override
        public boolean canUse() {
            if (Evoker.this.getTarget() != null) {
                return false;
            } else if (Evoker.this.isCastingSpell()) {
                return false;
            } else if (Evoker.this.tickCount < this.nextAttackTickCount) {
                return false;
            } else if (!Evoker.this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            } else {
                List<Sheep> var0 = Evoker.this.level
                    .getNearbyEntities(Sheep.class, this.wololoTargeting, Evoker.this, Evoker.this.getBoundingBox().inflate(16.0, 4.0, 16.0));
                if (var0.isEmpty()) {
                    return false;
                } else {
                    Evoker.this.setWololoTarget(var0.get(Evoker.this.random.nextInt(var0.size())));
                    return true;
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return Evoker.this.getWololoTarget() != null && this.attackWarmupDelay > 0;
        }

        @Override
        public void stop() {
            super.stop();
            Evoker.this.setWololoTarget(null);
        }

        @Override
        protected void performSpellCasting() {
            Sheep var0 = Evoker.this.getWololoTarget();
            if (var0 != null && var0.isAlive()) {
                var0.setColor(DyeColor.RED);
            }

        }

        @Override
        protected int getCastWarmupTime() {
            return 40;
        }

        @Override
        protected int getCastingTime() {
            return 60;
        }

        @Override
        protected int getCastingInterval() {
            return 140;
        }

        @Override
        protected SoundEvent getSpellPrepareSound() {
            return SoundEvents.EVOKER_PREPARE_WOLOLO;
        }

        @Override
        protected SpellcasterIllager.IllagerSpell getSpell() {
            return SpellcasterIllager.IllagerSpell.WOLOLO;
        }
    }
}
