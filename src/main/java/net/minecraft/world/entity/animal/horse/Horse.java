package net.minecraft.world.entity.animal.horse;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.SoundType;

public class Horse extends AbstractHorse {
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("556E1665-8B10-40C8-8F9D-CF9B1667F295");
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);

    public Horse(EntityType<? extends Horse> param0, Level param1) {
        super(param0, param1);
    }

    @Override
    protected void randomizeAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.generateRandomMaxHealth());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.generateRandomSpeed());
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(this.generateRandomJumpStrength());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putInt("Variant", this.getTypeVariant());
        if (!this.inventory.getItem(1).isEmpty()) {
            param0.put("ArmorItem", this.inventory.getItem(1).save(new CompoundTag()));
        }

    }

    public ItemStack getArmor() {
        return this.getItemBySlot(EquipmentSlot.CHEST);
    }

    private void setArmor(ItemStack param0) {
        this.setItemSlot(EquipmentSlot.CHEST, param0);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setTypeVariant(param0.getInt("Variant"));
        if (param0.contains("ArmorItem", 10)) {
            ItemStack var0 = ItemStack.of(param0.getCompound("ArmorItem"));
            if (!var0.isEmpty() && this.isArmor(var0)) {
                this.inventory.setItem(1, var0);
            }
        }

        this.updateContainerEquipment();
    }

    private void setTypeVariant(int param0) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, param0);
    }

    private int getTypeVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    private void setVariantAndMarkings(Variant param0, Markings param1) {
        this.setTypeVariant(param0.getId() & 0xFF | param1.getId() << 8 & 0xFF00);
    }

    public Variant getVariant() {
        return Variant.byId(this.getTypeVariant() & 0xFF);
    }

    public Markings getMarkings() {
        return Markings.byId((this.getTypeVariant() & 0xFF00) >> 8);
    }

    @Override
    protected void updateContainerEquipment() {
        if (!this.level.isClientSide) {
            super.updateContainerEquipment();
            this.setArmorEquipment(this.inventory.getItem(1));
            this.setDropChance(EquipmentSlot.CHEST, 0.0F);
        }
    }

    private void setArmorEquipment(ItemStack param0) {
        this.setArmor(param0);
        if (!this.level.isClientSide) {
            this.getAttribute(Attributes.ARMOR).removeModifier(ARMOR_MODIFIER_UUID);
            if (this.isArmor(param0)) {
                int var0 = ((HorseArmorItem)param0.getItem()).getProtection();
                if (var0 != 0) {
                    this.getAttribute(Attributes.ARMOR)
                        .addTransientModifier(
                            new AttributeModifier(ARMOR_MODIFIER_UUID, "Horse armor bonus", (double)var0, AttributeModifier.Operation.ADDITION)
                        );
                }
            }
        }

    }

    @Override
    public void containerChanged(Container param0) {
        ItemStack var0 = this.getArmor();
        super.containerChanged(param0);
        ItemStack var1 = this.getArmor();
        if (this.tickCount > 20 && this.isArmor(var1) && var0 != var1) {
            this.playSound(SoundEvents.HORSE_ARMOR, 0.5F, 1.0F);
        }

    }

    @Override
    protected void playGallopSound(SoundType param0) {
        super.playGallopSound(param0);
        if (this.random.nextInt(10) == 0) {
            this.playSound(SoundEvents.HORSE_BREATHE, param0.getVolume() * 0.6F, param0.getPitch());
        }

    }

    @Override
    protected SoundEvent getAmbientSound() {
        super.getAmbientSound();
        return SoundEvents.HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        super.getDeathSound();
        return SoundEvents.HORSE_DEATH;
    }

    @Nullable
    @Override
    protected SoundEvent getEatingSound() {
        return SoundEvents.HORSE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource param0) {
        super.getHurtSound(param0);
        return SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        super.getAngrySound();
        return SoundEvents.HORSE_ANGRY;
    }

    @Override
    public boolean mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        boolean var1 = !var0.isEmpty();
        if (var1 && var0.getItem() instanceof SpawnEggItem) {
            return super.mobInteract(param0, param1);
        } else {
            if (!this.isBaby()) {
                if (this.isTamed() && param0.isSecondaryUseActive()) {
                    this.openInventory(param0);
                    return true;
                }

                if (this.isVehicle()) {
                    return super.mobInteract(param0, param1);
                }
            }

            if (var1) {
                if (this.handleEating(param0, var0)) {
                    if (!param0.abilities.instabuild) {
                        var0.shrink(1);
                    }

                    return true;
                }

                if (var0.interactEnemy(param0, this, param1)) {
                    return true;
                }

                if (!this.isTamed()) {
                    this.makeMad();
                    return true;
                }

                boolean var2 = !this.isBaby() && !this.isSaddled() && var0.getItem() == Items.SADDLE;
                if (this.isArmor(var0) || var2) {
                    this.openInventory(param0);
                    return true;
                }
            }

            if (this.isBaby()) {
                return super.mobInteract(param0, param1);
            } else {
                this.doPlayerRide(param0);
                return true;
            }
        }
    }

    @Override
    public boolean canMate(Animal param0) {
        if (param0 == this) {
            return false;
        } else if (!(param0 instanceof Donkey) && !(param0 instanceof Horse)) {
            return false;
        } else {
            return this.canParent() && ((AbstractHorse)param0).canParent();
        }
    }

    @Override
    public AgableMob getBreedOffspring(AgableMob param0) {
        AbstractHorse var0;
        if (param0 instanceof Donkey) {
            var0 = EntityType.MULE.create(this.level);
        } else {
            Horse var1 = (Horse)param0;
            var0 = EntityType.HORSE.create(this.level);
            int var3 = this.random.nextInt(9);
            Variant var4;
            if (var3 < 4) {
                var4 = this.getVariant();
            } else if (var3 < 8) {
                var4 = var1.getVariant();
            } else {
                var4 = Util.getRandom(Variant.values(), this.random);
            }

            int var7 = this.random.nextInt(5);
            Markings var8;
            if (var7 < 2) {
                var8 = this.getMarkings();
            } else if (var7 < 4) {
                var8 = var1.getMarkings();
            } else {
                var8 = Util.getRandom(Markings.values(), this.random);
            }

            ((Horse)var0).setVariantAndMarkings(var4, var8);
        }

        this.setOffspringAttributes(param0, var0);
        return var0;
    }

    @Override
    public boolean canWearArmor() {
        return true;
    }

    @Override
    public boolean isArmor(ItemStack param0) {
        return param0.getItem() instanceof HorseArmorItem;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(
        LevelAccessor param0, DifficultyInstance param1, MobSpawnType param2, @Nullable SpawnGroupData param3, @Nullable CompoundTag param4
    ) {
        Variant var0;
        if (param3 instanceof Horse.HorseGroupData) {
            var0 = ((Horse.HorseGroupData)param3).variant;
        } else {
            var0 = Util.getRandom(Variant.values(), this.random);
            param3 = new Horse.HorseGroupData(var0);
        }

        this.setVariantAndMarkings(var0, Util.getRandom(Markings.values(), this.random));
        return super.finalizeSpawn(param0, param1, param2, param3, param4);
    }

    public static class HorseGroupData extends AgableMob.AgableMobGroupData {
        public final Variant variant;

        public HorseGroupData(Variant param0) {
            this.variant = param0;
        }
    }
}
