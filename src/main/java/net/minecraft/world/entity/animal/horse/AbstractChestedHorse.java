package net.minecraft.world.entity.animal.horse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public abstract class AbstractChestedHorse extends AbstractHorse {
    private static final EntityDataAccessor<Boolean> DATA_ID_CHEST = SynchedEntityData.defineId(AbstractChestedHorse.class, EntityDataSerializers.BOOLEAN);

    protected AbstractChestedHorse(EntityType<? extends AbstractChestedHorse> param0, Level param1) {
        super(param0, param1);
        this.canGallop = false;
    }

    @Override
    protected void randomizeAttributes() {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double)this.generateRandomMaxHealth());
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_CHEST, false);
    }

    public static AttributeSupplier.Builder createBaseChestedHorseAttributes() {
        return createBaseHorseAttributes().add(Attributes.MOVEMENT_SPEED, 0.175F).add(Attributes.JUMP_STRENGTH, 0.5);
    }

    public boolean hasChest() {
        return this.entityData.get(DATA_ID_CHEST);
    }

    public void setChest(boolean param0) {
        this.entityData.set(DATA_ID_CHEST, param0);
    }

    @Override
    protected int getInventorySize() {
        return this.hasChest() ? 17 : super.getInventorySize();
    }

    @Override
    public double getPassengersRidingOffset() {
        return super.getPassengersRidingOffset() - 0.25;
    }

    @Override
    protected void dropEquipment() {
        super.dropEquipment();
        if (this.hasChest()) {
            if (!this.level.isClientSide) {
                this.spawnAtLocation(Blocks.CHEST);
            }

            this.setChest(false);
        }

    }

    @Override
    public void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        param0.putBoolean("ChestedHorse", this.hasChest());
        if (this.hasChest()) {
            ListTag var0 = new ListTag();

            for(int var1 = 2; var1 < this.inventory.getContainerSize(); ++var1) {
                ItemStack var2 = this.inventory.getItem(var1);
                if (!var2.isEmpty()) {
                    CompoundTag var3 = new CompoundTag();
                    var3.putByte("Slot", (byte)var1);
                    var2.save(var3);
                    var0.add(var3);
                }
            }

            param0.put("Items", var0);
        }

    }

    @Override
    public void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.setChest(param0.getBoolean("ChestedHorse"));
        if (this.hasChest()) {
            ListTag var0 = param0.getList("Items", 10);
            this.createInventory();

            for(int var1 = 0; var1 < var0.size(); ++var1) {
                CompoundTag var2 = var0.getCompound(var1);
                int var3 = var2.getByte("Slot") & 255;
                if (var3 >= 2 && var3 < this.inventory.getContainerSize()) {
                    this.inventory.setItem(var3, ItemStack.of(var2));
                }
            }
        }

        this.updateContainerEquipment();
    }

    @Override
    public boolean setSlot(int param0, ItemStack param1) {
        if (param0 == 499) {
            if (this.hasChest() && param1.isEmpty()) {
                this.setChest(false);
                this.createInventory();
                return true;
            }

            if (!this.hasChest() && param1.getItem() == Blocks.CHEST.asItem()) {
                this.setChest(true);
                this.createInventory();
                return true;
            }
        }

        return super.setSlot(param0, param1);
    }

    @Override
    public InteractionResult mobInteract(Player param0, InteractionHand param1) {
        ItemStack var0 = param0.getItemInHand(param1);
        if (!this.isBaby()) {
            if (this.isTamed() && param0.isSecondaryUseActive()) {
                this.openInventory(param0);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (this.isVehicle()) {
                return super.mobInteract(param0, param1);
            }
        }

        if (!var0.isEmpty()) {
            if (this.isFood(var0)) {
                return this.fedFood(param0, var0);
            }

            if (!this.isTamed()) {
                this.makeMad();
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (!this.hasChest() && var0.getItem() == Blocks.CHEST.asItem()) {
                this.setChest(true);
                this.playChestEquipsSound();
                if (!param0.abilities.instabuild) {
                    var0.shrink(1);
                }

                this.createInventory();
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }

            if (!this.isBaby() && !this.isSaddled() && var0.getItem() == Items.SADDLE) {
                this.openInventory(param0);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }

        if (this.isBaby()) {
            return super.mobInteract(param0, param1);
        } else {
            this.doPlayerRide(param0);
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }
    }

    protected void playChestEquipsSound() {
        this.playSound(SoundEvents.DONKEY_CHEST, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
    }

    public int getInventoryColumns() {
        return 5;
    }
}
