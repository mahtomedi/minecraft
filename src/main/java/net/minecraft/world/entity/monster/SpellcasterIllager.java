package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.function.IntFunction;
import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

public abstract class SpellcasterIllager extends AbstractIllager {
    private static final EntityDataAccessor<Byte> DATA_SPELL_CASTING_ID = SynchedEntityData.defineId(SpellcasterIllager.class, EntityDataSerializers.BYTE);
    protected int spellCastingTickCount;
    private SpellcasterIllager.IllagerSpell currentSpell = SpellcasterIllager.IllagerSpell.NONE;

    protected SpellcasterIllager(EntityType<? extends SpellcasterIllager> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL_CASTING_ID, (byte)0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.spellCastingTickCount = param0.getInt("SpellTicks");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("SpellTicks", this.spellCastingTickCount);
    }

    @Override
    public AbstractIllager.IllagerArmPose getArmPose() {
        if (this.isCastingSpell()) {
            return AbstractIllager.IllagerArmPose.SPELLCASTING;
        } else {
            return this.isCelebrating() ? AbstractIllager.IllagerArmPose.CELEBRATING : AbstractIllager.IllagerArmPose.CROSSED;
        }
    }

    public boolean isCastingSpell() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_SPELL_CASTING_ID) > 0;
        } else {
            return this.spellCastingTickCount > 0;
        }
    }

    public void setIsCastingSpell(SpellcasterIllager.IllagerSpell param0) {
        this.currentSpell = param0;
        this.entityData.set(DATA_SPELL_CASTING_ID, (byte)param0.id);
    }

    protected SpellcasterIllager.IllagerSpell getCurrentSpell() {
        return !this.level().isClientSide ? this.currentSpell : SpellcasterIllager.IllagerSpell.byId(this.entityData.get(DATA_SPELL_CASTING_ID));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.spellCastingTickCount > 0) {
            --this.spellCastingTickCount;
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && this.isCastingSpell()) {
            SpellcasterIllager.IllagerSpell var0 = this.getCurrentSpell();
            double var1 = var0.spellColor[0];
            double var2 = var0.spellColor[1];
            double var3 = var0.spellColor[2];
            float var4 = this.yBodyRot * (float) (Math.PI / 180.0) + Mth.cos((float)this.tickCount * 0.6662F) * 0.25F;
            float var5 = Mth.cos(var4);
            float var6 = Mth.sin(var4);
            this.level()
                .addParticle(
                    ParticleTypes.ENTITY_EFFECT, this.getX() + (double)var5 * 0.6, this.getY() + 1.8, this.getZ() + (double)var6 * 0.6, var1, var2, var3
                );
            this.level()
                .addParticle(
                    ParticleTypes.ENTITY_EFFECT, this.getX() - (double)var5 * 0.6, this.getY() + 1.8, this.getZ() - (double)var6 * 0.6, var1, var2, var3
                );
        }

    }

    protected int getSpellCastingTime() {
        return this.spellCastingTickCount;
    }

    protected abstract SoundEvent getCastingSoundEvent();

    protected static enum IllagerSpell {
        NONE(0, 0.0, 0.0, 0.0),
        SUMMON_VEX(1, 0.7, 0.7, 0.8),
        FANGS(2, 0.4, 0.3, 0.35),
        WOLOLO(3, 0.7, 0.5, 0.2),
        DISAPPEAR(4, 0.3, 0.3, 0.8),
        BLINDNESS(5, 0.1, 0.1, 0.2);

        private static final IntFunction<SpellcasterIllager.IllagerSpell> BY_ID = ByIdMap.continuous(
            param0 -> param0.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO
        );
        final int id;
        final double[] spellColor;

        private IllagerSpell(int param0, double param1, double param2, double param3) {
            this.id = param0;
            this.spellColor = new double[]{param1, param2, param3};
        }

        public static SpellcasterIllager.IllagerSpell byId(int param0) {
            return BY_ID.apply(param0);
        }
    }

    protected class SpellcasterCastingSpellGoal extends Goal {
        public SpellcasterCastingSpellGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return SpellcasterIllager.this.getSpellCastingTime() > 0;
        }

        @Override
        public void start() {
            super.start();
            SpellcasterIllager.this.navigation.stop();
        }

        @Override
        public void stop() {
            super.stop();
            SpellcasterIllager.this.setIsCastingSpell(SpellcasterIllager.IllagerSpell.NONE);
        }

        @Override
        public void tick() {
            if (SpellcasterIllager.this.getTarget() != null) {
                SpellcasterIllager.this.getLookControl()
                    .setLookAt(
                        SpellcasterIllager.this.getTarget(), (float)SpellcasterIllager.this.getMaxHeadYRot(), (float)SpellcasterIllager.this.getMaxHeadXRot()
                    );
            }

        }
    }

    protected abstract class SpellcasterUseSpellGoal extends Goal {
        protected int attackWarmupDelay;
        protected int nextAttackTickCount;

        @Override
        public boolean canUse() {
            LivingEntity var0 = SpellcasterIllager.this.getTarget();
            if (var0 == null || !var0.isAlive()) {
                return false;
            } else if (SpellcasterIllager.this.isCastingSpell()) {
                return false;
            } else {
                return SpellcasterIllager.this.tickCount >= this.nextAttackTickCount;
            }
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity var0 = SpellcasterIllager.this.getTarget();
            return var0 != null && var0.isAlive() && this.attackWarmupDelay > 0;
        }

        @Override
        public void start() {
            this.attackWarmupDelay = this.adjustedTickDelay(this.getCastWarmupTime());
            SpellcasterIllager.this.spellCastingTickCount = this.getCastingTime();
            this.nextAttackTickCount = SpellcasterIllager.this.tickCount + this.getCastingInterval();
            SoundEvent var0 = this.getSpellPrepareSound();
            if (var0 != null) {
                SpellcasterIllager.this.playSound(var0, 1.0F, 1.0F);
            }

            SpellcasterIllager.this.setIsCastingSpell(this.getSpell());
        }

        @Override
        public void tick() {
            --this.attackWarmupDelay;
            if (this.attackWarmupDelay == 0) {
                this.performSpellCasting();
                SpellcasterIllager.this.playSound(SpellcasterIllager.this.getCastingSoundEvent(), 1.0F, 1.0F);
            }

        }

        protected abstract void performSpellCasting();

        protected int getCastWarmupTime() {
            return 20;
        }

        protected abstract int getCastingTime();

        protected abstract int getCastingInterval();

        @Nullable
        protected abstract SoundEvent getSpellPrepareSound();

        protected abstract SpellcasterIllager.IllagerSpell getSpell();
    }
}
