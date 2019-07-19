package net.minecraft.world.entity.projectile;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Arrow extends AbstractArrow {
    private static final EntityDataAccessor<Integer> ID_EFFECT_COLOR = SynchedEntityData.defineId(Arrow.class, EntityDataSerializers.INT);
    private Potion potion = Potions.EMPTY;
    private final Set<MobEffectInstance> effects = Sets.newHashSet();
    private boolean fixedColor;

    public Arrow(EntityType<? extends Arrow> param0, Level param1) {
        super(param0, param1);
    }

    public Arrow(Level param0, double param1, double param2, double param3) {
        super(EntityType.ARROW, param1, param2, param3, param0);
    }

    public Arrow(Level param0, LivingEntity param1) {
        super(EntityType.ARROW, param1, param0);
    }

    public void setEffectsFromItem(ItemStack param0) {
        if (param0.getItem() == Items.TIPPED_ARROW) {
            this.potion = PotionUtils.getPotion(param0);
            Collection<MobEffectInstance> var0 = PotionUtils.getCustomEffects(param0);
            if (!var0.isEmpty()) {
                for(MobEffectInstance var1 : var0) {
                    this.effects.add(new MobEffectInstance(var1));
                }
            }

            int var2 = getCustomColor(param0);
            if (var2 == -1) {
                this.updateColor();
            } else {
                this.setFixedColor(var2);
            }
        } else if (param0.getItem() == Items.ARROW) {
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }

    }

    public static int getCustomColor(ItemStack param0) {
        CompoundTag var0 = param0.getTag();
        return var0 != null && var0.contains("CustomPotionColor", 99) ? var0.getInt("CustomPotionColor") : -1;
    }

    private void updateColor() {
        this.fixedColor = false;
        this.entityData.set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

    public void addEffect(MobEffectInstance param0) {
        this.effects.add(param0);
        this.getEntityData().set(ID_EFFECT_COLOR, PotionUtils.getColor(PotionUtils.getAllEffects(this.potion, this.effects)));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ID_EFFECT_COLOR, -1);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.inGround) {
                if (this.inGroundTime % 5 == 0) {
                    this.makeParticle(1);
                }
            } else {
                this.makeParticle(2);
            }
        } else if (this.inGround && this.inGroundTime != 0 && !this.effects.isEmpty() && this.inGroundTime >= 600) {
            this.level.broadcastEntityEvent(this, (byte)0);
            this.potion = Potions.EMPTY;
            this.effects.clear();
            this.entityData.set(ID_EFFECT_COLOR, -1);
        }

    }

    private void makeParticle(int param0) {
        int var0 = this.getColor();
        if (var0 != -1 && param0 > 0) {
            double var1 = (double)(var0 >> 16 & 0xFF) / 255.0;
            double var2 = (double)(var0 >> 8 & 0xFF) / 255.0;
            double var3 = (double)(var0 >> 0 & 0xFF) / 255.0;

            for(int var4 = 0; var4 < param0; ++var4) {
                this.level
                    .addParticle(
                        ParticleTypes.ENTITY_EFFECT,
                        this.x + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
                        this.y + this.random.nextDouble() * (double)this.getBbHeight(),
                        this.z + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
                        var1,
                        var2,
                        var3
                    );
            }

        }
    }

    public int getColor() {
        return this.entityData.get(ID_EFFECT_COLOR);
    }

    private void setFixedColor(int param0) {
        this.fixedColor = true;
        this.entityData.set(ID_EFFECT_COLOR, param0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        if (this.potion != Potions.EMPTY && this.potion != null) {
            param0.putString("Potion", Registry.POTION.getKey(this.potion).toString());
        }

        if (this.fixedColor) {
            param0.putInt("Color", this.getColor());
        }

        if (!this.effects.isEmpty()) {
            ListTag var0 = new ListTag();

            for(MobEffectInstance var1 : this.effects) {
                var0.add(var1.save(new CompoundTag()));
            }

            param0.put("CustomPotionEffects", var0);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        if (param0.contains("Potion", 8)) {
            this.potion = PotionUtils.getPotion(param0);
        }

        for(MobEffectInstance var0 : PotionUtils.getCustomEffects(param0)) {
            this.addEffect(var0);
        }

        if (param0.contains("Color", 99)) {
            this.setFixedColor(param0.getInt("Color"));
        } else {
            this.updateColor();
        }

    }

    @Override
    protected void doPostHurtEffects(LivingEntity param0) {
        super.doPostHurtEffects(param0);

        for(MobEffectInstance var0 : this.potion.getEffects()) {
            param0.addEffect(
                new MobEffectInstance(var0.getEffect(), Math.max(var0.getDuration() / 8, 1), var0.getAmplifier(), var0.isAmbient(), var0.isVisible())
            );
        }

        if (!this.effects.isEmpty()) {
            for(MobEffectInstance var1 : this.effects) {
                param0.addEffect(var1);
            }
        }

    }

    @Override
    protected ItemStack getPickupItem() {
        if (this.effects.isEmpty() && this.potion == Potions.EMPTY) {
            return new ItemStack(Items.ARROW);
        } else {
            ItemStack var0 = new ItemStack(Items.TIPPED_ARROW);
            PotionUtils.setPotion(var0, this.potion);
            PotionUtils.setCustomEffects(var0, this.effects);
            if (this.fixedColor) {
                var0.getOrCreateTag().putInt("CustomPotionColor", this.getColor());
            }

            return var0;
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleEntityEvent(byte param0) {
        if (param0 == 0) {
            int var0 = this.getColor();
            if (var0 != -1) {
                double var1 = (double)(var0 >> 16 & 0xFF) / 255.0;
                double var2 = (double)(var0 >> 8 & 0xFF) / 255.0;
                double var3 = (double)(var0 >> 0 & 0xFF) / 255.0;

                for(int var4 = 0; var4 < 20; ++var4) {
                    this.level
                        .addParticle(
                            ParticleTypes.ENTITY_EFFECT,
                            this.x + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
                            this.y + this.random.nextDouble() * (double)this.getBbHeight(),
                            this.z + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
                            var1,
                            var2,
                            var3
                        );
                }
            }
        } else {
            super.handleEntityEvent(param0);
        }

    }
}
