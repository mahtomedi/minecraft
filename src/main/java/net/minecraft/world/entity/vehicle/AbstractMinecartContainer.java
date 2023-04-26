package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public abstract class AbstractMinecartContainer extends AbstractMinecart implements ContainerEntity {
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(36, ItemStack.EMPTY);
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    protected AbstractMinecartContainer(EntityType<?> param0, Level param1) {
        super(param0, param1);
    }

    protected AbstractMinecartContainer(EntityType<?> param0, double param1, double param2, double param3, Level param4) {
        super(param0, param4, param1, param2, param3);
    }

    @Override
    public void destroy(DamageSource param0) {
        super.destroy(param0);
        this.chestVehicleDestroyed(param0, this.level(), this);
    }

    @Override
    public ItemStack getItem(int param0) {
        return this.getChestVehicleItem(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        return this.removeChestVehicleItem(param0, param1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return this.removeChestVehicleItemNoUpdate(param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.setChestVehicleItem(param0, param1);
    }

    @Override
    public SlotAccess getSlot(int param0) {
        return this.getChestVehicleSlot(param0);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.isChestVehicleStillValid(param0);
    }

    @Override
    public void remove(Entity.RemovalReason param0) {
        if (!this.level().isClientSide && param0.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }

        super.remove(param0);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag param0) {
        super.addAdditionalSaveData(param0);
        this.addChestVehicleSaveData(param0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag param0) {
        super.readAdditionalSaveData(param0);
        this.readChestVehicleSaveData(param0);
    }

    @Override
    public InteractionResult interact(Player param0, InteractionHand param1) {
        return this.interactWithContainerVehicle(param0);
    }

    @Override
    protected void applyNaturalSlowdown() {
        float var0 = 0.98F;
        if (this.lootTable == null) {
            int var1 = 15 - AbstractContainerMenu.getRedstoneSignalFromContainer(this);
            var0 += (float)var1 * 0.001F;
        }

        if (this.isInWater()) {
            var0 *= 0.95F;
        }

        this.setDeltaMovement(this.getDeltaMovement().multiply((double)var0, 0.0, (double)var0));
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    public void setLootTable(ResourceLocation param0, long param1) {
        this.lootTable = param0;
        this.lootTableSeed = param1;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        if (this.lootTable != null && param2.isSpectator()) {
            return null;
        } else {
            this.unpackChestVehicleLootTable(param1.player);
            return this.createMenu(param0, param1);
        }
    }

    protected abstract AbstractContainerMenu createMenu(int var1, Inventory var2);

    @Nullable
    @Override
    public ResourceLocation getLootTable() {
        return this.lootTable;
    }

    @Override
    public void setLootTable(@Nullable ResourceLocation param0) {
        this.lootTable = param0;
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    @Override
    public void setLootTableSeed(long param0) {
        this.lootTableSeed = param0;
    }

    @Override
    public NonNullList<ItemStack> getItemStacks() {
        return this.itemStacks;
    }

    @Override
    public void clearItemStacks() {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
    }
}
