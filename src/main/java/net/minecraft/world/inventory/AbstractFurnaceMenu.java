package net.minecraft.world.inventory;

import net.minecraft.recipebook.ServerPlaceSmeltingRecipe;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class AbstractFurnaceMenu extends RecipeBookMenu<Container> {
    private final Container container;
    private final ContainerData data;
    protected final Level level;
    private final RecipeType<? extends AbstractCookingRecipe> recipeType;

    protected AbstractFurnaceMenu(MenuType<?> param0, RecipeType<? extends AbstractCookingRecipe> param1, int param2, Inventory param3) {
        this(param0, param1, param2, param3, new SimpleContainer(3), new SimpleContainerData(4));
    }

    protected AbstractFurnaceMenu(
        MenuType<?> param0, RecipeType<? extends AbstractCookingRecipe> param1, int param2, Inventory param3, Container param4, ContainerData param5
    ) {
        super(param0, param2);
        this.recipeType = param1;
        checkContainerSize(param4, 3);
        checkContainerDataCount(param5, 4);
        this.container = param4;
        this.data = param5;
        this.level = param3.player.level;
        this.addSlot(new Slot(param4, 0, 56, 17));
        this.addSlot(new FurnaceFuelSlot(this, param4, 1, 56, 53));
        this.addSlot(new FurnaceResultSlot(param3.player, param4, 2, 116, 35));

        for(int var0 = 0; var0 < 3; ++var0) {
            for(int var1 = 0; var1 < 9; ++var1) {
                this.addSlot(new Slot(param3, var1 + var0 * 9 + 9, 8 + var1 * 18, 84 + var0 * 18));
            }
        }

        for(int var2 = 0; var2 < 9; ++var2) {
            this.addSlot(new Slot(param3, var2, 8 + var2 * 18, 142));
        }

        this.addDataSlots(param5);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedContents param0) {
        if (this.container instanceof StackedContentsCompatible) {
            ((StackedContentsCompatible)this.container).fillStackedContents(param0);
        }

    }

    @Override
    public void clearCraftingContent() {
        this.container.clearContent();
    }

    @Override
    public void handlePlacement(boolean param0, Recipe<?> param1, ServerPlayer param2) {
        new ServerPlaceSmeltingRecipe<>(this).recipeClicked(param2, param1, param0);
    }

    @Override
    public boolean recipeMatches(Recipe<? super Container> param0) {
        return param0.matches(this.container, this.level);
    }

    @Override
    public int getResultSlotIndex() {
        return 2;
    }

    @Override
    public int getGridWidth() {
        return 1;
    }

    @Override
    public int getGridHeight() {
        return 1;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public boolean stillValid(Player param0) {
        return this.container.stillValid(param0);
    }

    @Override
    public ItemStack quickMoveStack(Player param0, int param1) {
        ItemStack var0 = ItemStack.EMPTY;
        Slot var1 = this.slots.get(param1);
        if (var1 != null && var1.hasItem()) {
            ItemStack var2 = var1.getItem();
            var0 = var2.copy();
            if (param1 == 2) {
                if (!this.moveItemStackTo(var2, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                var1.onQuickCraft(var2, var0);
            } else if (param1 != 1 && param1 != 0) {
                if (this.canSmelt(var2)) {
                    if (!this.moveItemStackTo(var2, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.isFuel(var2)) {
                    if (!this.moveItemStackTo(var2, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= 3 && param1 < 30) {
                    if (!this.moveItemStackTo(var2, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (param1 >= 30 && param1 < 39 && !this.moveItemStackTo(var2, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(var2, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (var2.isEmpty()) {
                var1.set(ItemStack.EMPTY);
            } else {
                var1.setChanged();
            }

            if (var2.getCount() == var0.getCount()) {
                return ItemStack.EMPTY;
            }

            var1.onTake(param0, var2);
        }

        return var0;
    }

    protected boolean canSmelt(ItemStack param0) {
        return this.level.getRecipeManager().getRecipeFor(this.recipeType, new SimpleContainer(param0), this.level).isPresent();
    }

    protected boolean isFuel(ItemStack param0) {
        return AbstractFurnaceBlockEntity.isFuel(param0);
    }

    @OnlyIn(Dist.CLIENT)
    public int getBurnProgress() {
        int var0 = this.data.get(2);
        int var1 = this.data.get(3);
        return var1 != 0 && var0 != 0 ? var0 * 24 / var1 : 0;
    }

    @OnlyIn(Dist.CLIENT)
    public int getLitProgress() {
        int var0 = this.data.get(1);
        if (var0 == 0) {
            var0 = 200;
        }

        return this.data.get(0) * 13 / var0;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLit() {
        return this.data.get(0) > 0;
    }
}
