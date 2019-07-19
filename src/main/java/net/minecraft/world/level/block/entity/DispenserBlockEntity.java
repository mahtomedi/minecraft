package net.minecraft.world.level.block.entity;

import java.util.Random;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;

public class DispenserBlockEntity extends RandomizableContainerBlockEntity {
    private static final Random RANDOM = new Random();
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    protected DispenserBlockEntity(BlockEntityType<?> param0) {
        super(param0);
    }

    public DispenserBlockEntity() {
        this(BlockEntityType.DISPENSER);
    }

    @Override
    public int getContainerSize() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack var0 : this.items) {
            if (!var0.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public int getRandomSlot() {
        this.unpackLootTable(null);
        int var0 = -1;
        int var1 = 1;

        for(int var2 = 0; var2 < this.items.size(); ++var2) {
            if (!this.items.get(var2).isEmpty() && RANDOM.nextInt(var1++) == 0) {
                var0 = var2;
            }
        }

        return var0;
    }

    public int addItem(ItemStack param0) {
        for(int var0 = 0; var0 < this.items.size(); ++var0) {
            if (this.items.get(var0).isEmpty()) {
                this.setItem(var0, param0);
                return var0;
            }
        }

        return -1;
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.dispenser");
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(param0)) {
            ContainerHelper.loadAllItems(param0, this.items);
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (!this.trySaveLootTable(param0)) {
            ContainerHelper.saveAllItems(param0, this.items);
        }

        return param0;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> param0) {
        this.items = param0;
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new DispenserMenu(param0, param1, this);
    }
}
