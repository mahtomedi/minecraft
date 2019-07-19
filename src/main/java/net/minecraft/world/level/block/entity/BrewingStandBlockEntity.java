package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, TickableBlockEntity {
    private static final int[] SLOTS_FOR_UP = new int[]{3};
    private static final int[] SLOTS_FOR_DOWN = new int[]{0, 1, 2, 3};
    private static final int[] SLOTS_FOR_SIDES = new int[]{0, 1, 2, 4};
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int brewTime;
    private boolean[] lastPotionCount;
    private Item ingredient;
    private int fuel;
    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int param0) {
            switch(param0) {
                case 0:
                    return BrewingStandBlockEntity.this.brewTime;
                case 1:
                    return BrewingStandBlockEntity.this.fuel;
                default:
                    return 0;
            }
        }

        @Override
        public void set(int param0, int param1) {
            switch(param0) {
                case 0:
                    BrewingStandBlockEntity.this.brewTime = param1;
                    break;
                case 1:
                    BrewingStandBlockEntity.this.fuel = param1;
            }

        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public BrewingStandBlockEntity() {
        super(BlockEntityType.BREWING_STAND);
    }

    @Override
    protected Component getDefaultName() {
        return new TranslatableComponent("container.brewing");
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
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

    @Override
    public void tick() {
        ItemStack var0 = this.items.get(4);
        if (this.fuel <= 0 && var0.getItem() == Items.BLAZE_POWDER) {
            this.fuel = 20;
            var0.shrink(1);
            this.setChanged();
        }

        boolean var1 = this.isBrewable();
        boolean var2 = this.brewTime > 0;
        ItemStack var3 = this.items.get(3);
        if (var2) {
            --this.brewTime;
            boolean var4 = this.brewTime == 0;
            if (var4 && var1) {
                this.doBrew();
                this.setChanged();
            } else if (!var1) {
                this.brewTime = 0;
                this.setChanged();
            } else if (this.ingredient != var3.getItem()) {
                this.brewTime = 0;
                this.setChanged();
            }
        } else if (var1 && this.fuel > 0) {
            --this.fuel;
            this.brewTime = 400;
            this.ingredient = var3.getItem();
            this.setChanged();
        }

        if (!this.level.isClientSide) {
            boolean[] var5 = this.getPotionBits();
            if (!Arrays.equals(var5, this.lastPotionCount)) {
                this.lastPotionCount = var5;
                BlockState var6 = this.level.getBlockState(this.getBlockPos());
                if (!(var6.getBlock() instanceof BrewingStandBlock)) {
                    return;
                }

                for(int var7 = 0; var7 < BrewingStandBlock.HAS_BOTTLE.length; ++var7) {
                    var6 = var6.setValue(BrewingStandBlock.HAS_BOTTLE[var7], Boolean.valueOf(var5[var7]));
                }

                this.level.setBlock(this.worldPosition, var6, 2);
            }
        }

    }

    public boolean[] getPotionBits() {
        boolean[] var0 = new boolean[3];

        for(int var1 = 0; var1 < 3; ++var1) {
            if (!this.items.get(var1).isEmpty()) {
                var0[var1] = true;
            }
        }

        return var0;
    }

    private boolean isBrewable() {
        ItemStack var0 = this.items.get(3);
        if (var0.isEmpty()) {
            return false;
        } else if (!PotionBrewing.isIngredient(var0)) {
            return false;
        } else {
            for(int var1 = 0; var1 < 3; ++var1) {
                ItemStack var2 = this.items.get(var1);
                if (!var2.isEmpty() && PotionBrewing.hasMix(var2, var0)) {
                    return true;
                }
            }

            return false;
        }
    }

    private void doBrew() {
        ItemStack var0 = this.items.get(3);

        for(int var1 = 0; var1 < 3; ++var1) {
            this.items.set(var1, PotionBrewing.mix(var0, this.items.get(var1)));
        }

        var0.shrink(1);
        BlockPos var2 = this.getBlockPos();
        if (var0.getItem().hasCraftingRemainingItem()) {
            ItemStack var3 = new ItemStack(var0.getItem().getCraftingRemainingItem());
            if (var0.isEmpty()) {
                var0 = var3;
            } else if (!this.level.isClientSide) {
                Containers.dropItemStack(this.level, (double)var2.getX(), (double)var2.getY(), (double)var2.getZ(), var3);
            }
        }

        this.items.set(3, var0);
        this.level.levelEvent(1035, var2, 0);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(param0, this.items);
        this.brewTime = param0.getShort("BrewTime");
        this.fuel = param0.getByte("Fuel");
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putShort("BrewTime", (short)this.brewTime);
        ContainerHelper.saveAllItems(param0, this.items);
        param0.putByte("Fuel", (byte)this.fuel);
        return param0;
    }

    @Override
    public ItemStack getItem(int param0) {
        return param0 >= 0 && param0 < this.items.size() ? this.items.get(param0) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int param0, int param1) {
        return ContainerHelper.removeItem(this.items, param0, param1);
    }

    @Override
    public ItemStack removeItemNoUpdate(int param0) {
        return ContainerHelper.takeItem(this.items, param0);
    }

    @Override
    public void setItem(int param0, ItemStack param1) {
        if (param0 >= 0 && param0 < this.items.size()) {
            this.items.set(param0, param1);
        }

    }

    @Override
    public boolean stillValid(Player param0) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(
                param0.distanceToSqr((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5, (double)this.worldPosition.getZ() + 0.5)
                    > 64.0
            );
        }
    }

    @Override
    public boolean canPlaceItem(int param0, ItemStack param1) {
        if (param0 == 3) {
            return PotionBrewing.isIngredient(param1);
        } else {
            Item var0 = param1.getItem();
            if (param0 == 4) {
                return var0 == Items.BLAZE_POWDER;
            } else {
                return (var0 == Items.POTION || var0 == Items.SPLASH_POTION || var0 == Items.LINGERING_POTION || var0 == Items.GLASS_BOTTLE)
                    && this.getItem(param0).isEmpty();
            }
        }
    }

    @Override
    public int[] getSlotsForFace(Direction param0) {
        if (param0 == Direction.UP) {
            return SLOTS_FOR_UP;
        } else {
            return param0 == Direction.DOWN ? SLOTS_FOR_DOWN : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int param0, ItemStack param1, @Nullable Direction param2) {
        return this.canPlaceItem(param0, param1);
    }

    @Override
    public boolean canTakeItemThroughFace(int param0, ItemStack param1, Direction param2) {
        if (param0 == 3) {
            return param1.getItem() == Items.GLASS_BOTTLE;
        } else {
            return true;
        }
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected AbstractContainerMenu createMenu(int param0, Inventory param1) {
        return new BrewingStandMenu(param0, param1, this, this.dataAccess);
    }
}
