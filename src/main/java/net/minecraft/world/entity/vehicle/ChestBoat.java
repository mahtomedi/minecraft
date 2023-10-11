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
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class ChestBoat extends Boat implements HasCustomInventoryScreen, ContainerEntity {
    private static final int CONTAINER_SIZE = 27;
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    @Nullable
    private ResourceLocation lootTable;
    private long lootTableSeed;

    public ChestBoat(EntityType<? extends Boat> param0, Level param1) {
        super(param0, param1);
    }

    public ChestBoat(Level param0, double param1, double param2, double param3) {
        super(EntityType.CHEST_BOAT, param0);
        this.setPos(param1, param2, param3);
        this.xo = param1;
        this.yo = param2;
        this.zo = param3;
    }

    @Override
    protected float getSinglePassengerXOffset() {
        return 0.15F;
    }

    @Override
    protected int getMaxPassengers() {
        return 1;
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
    public void destroy(DamageSource param0) {
        this.destroy(this.getDropItem());
        this.chestVehicleDestroyed(param0, this.level(), this);
    }

    @Override
    public void remove(Entity.RemovalReason param0) {
        if (!this.level().isClientSide && param0.shouldDestroy()) {
            Containers.dropContents(this.level(), this, this);
        }

        super.remove(param0);
    }

    @Override
    public InteractionResult interact(Player param0, InteractionHand param1) {
        if (this.canAddPassenger(param0) && !param0.isSecondaryUseActive()) {
            return super.interact(param0, param1);
        } else {
            InteractionResult var0 = this.interactWithContainerVehicle(param0);
            if (var0.consumesAction()) {
                this.gameEvent(GameEvent.CONTAINER_OPEN, param0);
                PiglinAi.angerNearbyPiglins(param0, true);
            }

            return var0;
        }
    }

    @Override
    public void openCustomInventoryScreen(Player param0) {
        param0.openMenu(this);
        if (!param0.level().isClientSide) {
            this.gameEvent(GameEvent.CONTAINER_OPEN, param0);
            PiglinAi.angerNearbyPiglins(param0, true);
        }

    }

    @Override
    public Item getDropItem() {
        return switch(this.getVariant()) {
            case SPRUCE -> Items.SPRUCE_CHEST_BOAT;
            case BIRCH -> Items.BIRCH_CHEST_BOAT;
            case JUNGLE -> Items.JUNGLE_CHEST_BOAT;
            case ACACIA -> Items.ACACIA_CHEST_BOAT;
            case CHERRY -> Items.CHERRY_CHEST_BOAT;
            case DARK_OAK -> Items.DARK_OAK_CHEST_BOAT;
            case MANGROVE -> Items.MANGROVE_CHEST_BOAT;
            case BAMBOO -> Items.BAMBOO_CHEST_RAFT;
            default -> Items.OAK_CHEST_BOAT;
        };
    }

    @Override
    public void clearContent() {
        this.clearChestVehicleContent();
    }

    @Override
    public int getContainerSize() {
        return 27;
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

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        if (this.lootTable != null && param2.isSpectator()) {
            return null;
        } else {
            this.unpackLootTable(param1.player);
            return ChestMenu.threeRows(param0, param1, this);
        }
    }

    public void unpackLootTable(@Nullable Player param0) {
        this.unpackChestVehicleLootTable(param0);
    }

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

    @Override
    public void stopOpen(Player param0) {
        this.level().gameEvent(GameEvent.CONTAINER_CLOSE, this.position(), GameEvent.Context.of(param0));
    }
}
