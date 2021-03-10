package net.minecraft.world.inventory;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class ResultSlot extends Slot {
    private final CraftingContainer craftSlots;
    private final Player player;
    private int removeCount;

    public ResultSlot(Player param0, CraftingContainer param1, Container param2, int param3, int param4, int param5) {
        super(param2, param3, param4, param5);
        this.player = param0;
        this.craftSlots = param1;
    }

    @Override
    public boolean mayPlace(ItemStack param0) {
        return false;
    }

    @Override
    public ItemStack remove(int param0) {
        if (this.hasItem()) {
            this.removeCount += Math.min(param0, this.getItem().getCount());
        }

        return super.remove(param0);
    }

    @Override
    protected void onQuickCraft(ItemStack param0, int param1) {
        this.removeCount += param1;
        this.checkTakeAchievements(param0);
    }

    @Override
    protected void onSwapCraft(int param0) {
        this.removeCount += param0;
    }

    @Override
    protected void checkTakeAchievements(ItemStack param0) {
        if (this.removeCount > 0) {
            param0.onCraftedBy(this.player.level, this.player, this.removeCount);
        }

        if (this.container instanceof RecipeHolder) {
            ((RecipeHolder)this.container).awardUsedRecipes(this.player);
        }

        this.removeCount = 0;
    }

    @Override
    public void onTake(Player param0, ItemStack param1) {
        this.checkTakeAchievements(param1);
        NonNullList<ItemStack> var0 = param0.level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this.craftSlots, param0.level);

        for(int var1 = 0; var1 < var0.size(); ++var1) {
            ItemStack var2 = this.craftSlots.getItem(var1);
            ItemStack var3 = var0.get(var1);
            if (!var2.isEmpty()) {
                this.craftSlots.removeItem(var1, 1);
                var2 = this.craftSlots.getItem(var1);
            }

            if (!var3.isEmpty()) {
                if (var2.isEmpty()) {
                    this.craftSlots.setItem(var1, var3);
                } else if (ItemStack.isSame(var2, var3) && ItemStack.tagMatches(var2, var3)) {
                    var3.grow(var2.getCount());
                    this.craftSlots.setItem(var1, var3);
                } else if (!this.player.getInventory().add(var3)) {
                    this.player.drop(var3, false);
                }
            }
        }

    }
}
