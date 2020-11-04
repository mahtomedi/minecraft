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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BrewingStandBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
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

    public BrewingStandBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BREWING_STAND, param0, param1);
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

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, BrewingStandBlockEntity param3) {
        ItemStack var0 = param3.items.get(4);
        if (param3.fuel <= 0 && var0.is(Items.BLAZE_POWDER)) {
            param3.fuel = 20;
            var0.shrink(1);
            setChanged(param0, param1, param2);
        }

        boolean var1 = isBrewable(param3.items);
        boolean var2 = param3.brewTime > 0;
        ItemStack var3 = param3.items.get(3);
        if (var2) {
            --param3.brewTime;
            boolean var4 = param3.brewTime == 0;
            if (var4 && var1) {
                doBrew(param0, param1, param3.items);
                setChanged(param0, param1, param2);
            } else if (!var1 || !var3.is(param3.ingredient)) {
                param3.brewTime = 0;
                setChanged(param0, param1, param2);
            }
        } else if (var1 && param3.fuel > 0) {
            --param3.fuel;
            param3.brewTime = 400;
            param3.ingredient = var3.getItem();
            setChanged(param0, param1, param2);
        }

        boolean[] var5 = param3.getPotionBits();
        if (!Arrays.equals(var5, param3.lastPotionCount)) {
            param3.lastPotionCount = var5;
            BlockState var6 = param2;
            if (!(param2.getBlock() instanceof BrewingStandBlock)) {
                return;
            }

            for(int var7 = 0; var7 < BrewingStandBlock.HAS_BOTTLE.length; ++var7) {
                var6 = var6.setValue(BrewingStandBlock.HAS_BOTTLE[var7], Boolean.valueOf(var5[var7]));
            }

            param0.setBlock(param1, var6, 2);
        }

    }

    private boolean[] getPotionBits() {
        boolean[] var0 = new boolean[3];

        for(int var1 = 0; var1 < 3; ++var1) {
            if (!this.items.get(var1).isEmpty()) {
                var0[var1] = true;
            }
        }

        return var0;
    }

    private static boolean isBrewable(NonNullList<ItemStack> param0) {
        ItemStack var0 = param0.get(3);
        if (var0.isEmpty()) {
            return false;
        } else if (!PotionBrewing.isIngredient(var0)) {
            return false;
        } else {
            for(int var1 = 0; var1 < 3; ++var1) {
                ItemStack var2 = param0.get(var1);
                if (!var2.isEmpty() && PotionBrewing.hasMix(var2, var0)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static void doBrew(Level param0, BlockPos param1, NonNullList<ItemStack> param2) {
        ItemStack var0 = param2.get(3);

        for(int var1 = 0; var1 < 3; ++var1) {
            param2.set(var1, PotionBrewing.mix(var0, param2.get(var1)));
        }

        var0.shrink(1);
        if (var0.getItem().hasCraftingRemainingItem()) {
            ItemStack var2 = new ItemStack(var0.getItem().getCraftingRemainingItem());
            if (var0.isEmpty()) {
                var0 = var2;
            } else {
                Containers.dropItemStack(param0, (double)param1.getX(), (double)param1.getY(), (double)param1.getZ(), var2);
            }
        }

        param2.set(3, var0);
        param0.levelEvent(1035, param1, 0);
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
        } else if (param0 == 4) {
            return param1.is(Items.BLAZE_POWDER);
        } else {
            return (param1.is(Items.POTION) || param1.is(Items.SPLASH_POTION) || param1.is(Items.LINGERING_POTION) || param1.is(Items.GLASS_BOTTLE))
                && this.getItem(param0).isEmpty();
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
        return param0 == 3 ? param1.is(Items.GLASS_BOTTLE) : true;
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
