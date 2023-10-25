package net.minecraft.world.entity.projectile;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SpectralArrow extends AbstractArrow {
    private static final ItemStack DEFAULT_ARROW_STACK = new ItemStack(Items.SPECTRAL_ARROW);
    private int duration = 200;

    public SpectralArrow(EntityType<? extends SpectralArrow> param0, Level param1) {
        super(param0, param1, DEFAULT_ARROW_STACK);
    }

    public SpectralArrow(Level param0, LivingEntity param1, ItemStack param2) {
        super(EntityType.SPECTRAL_ARROW, param1, param0, param2);
    }

    public SpectralArrow(Level param0, double param1, double param2, double param3, ItemStack param4) {
        super(EntityType.SPECTRAL_ARROW, param1, param2, param3, param0, param4);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide && !this.inGround) {
            this.level().addParticle(ParticleTypes.INSTANT_EFFECT, this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }

    }

    @Override
    protected void doPostHurtEffects(LivingEntity param0) {
        super.doPostHurtEffects(param0);
        MobEffectInstance var0 = new MobEffectInstance(MobEffects.GLOWING, this.duration, 0);
        param0.addEffect(var0, this.getEffectSource());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("Duration")) {
            this.duration = param0.getInt("Duration");
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Duration", this.duration);
    }
}
