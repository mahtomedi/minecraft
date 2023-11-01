package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity implements RandomizableContainer {
    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;

    protected RandomizableContainerBlockEntity(BlockEntityType<?> param0, BlockPos param1, BlockState param2) {
        super(param0, param1, param2);
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
    public boolean isEmpty() {
        this.unpackLootTable(null);
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int param0) {
        this.unpackLootTable(null);
        return this.getItems().get(param0);
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        this.unpackLootTable(null);
        ItemStack var0 = ContainerHelper.removeItem(this.getItems(), param0, param1);
        if (!var0.isEmpty()) {
            this.setChanged();
        }

        return var0;
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        this.unpackLootTable(null);
        return ContainerHelper.takeItem(this.getItems(), param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        this.unpackLootTable(null);
        this.getItems().set(param0, param1);
        if (param1.getCount() > this.getMaxStackSize()) {
            param1.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    @Override
    public boolean stillValid(Player param0) {
        return Container.stillValidBlockEntity(this, param0);
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    protected abstract NonNullList<ItemStack> getItems();

    protected abstract void setItems(NonNullList<ItemStack> var1);

    @Override
    public boolean canOpen(Player param0) {
        return super.canOpen(param0) && (this.lootTable == null || !param0.isSpectator());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int param0, Inventory param1, Player param2) {
        if (this.canOpen(param2)) {
            this.unpackLootTable(param1.player);
            return this.createMenu(param0, param1);
        } else {
            return null;
        }
    }
}
