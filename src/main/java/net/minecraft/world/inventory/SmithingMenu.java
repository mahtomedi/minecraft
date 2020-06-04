package net.minecraft.world.inventory;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu extends ItemCombinerMenu {
    private final Level level;
    @Nullable
    private UpgradeRecipe selectedRecipe;

    public SmithingMenu(int param0, Inventory param1) {
        this(param0, param1, ContainerLevelAccess.NULL);
    }

    public SmithingMenu(int param0, Inventory param1, ContainerLevelAccess param2) {
        super(MenuType.SMITHING, param0, param1, param2);
        this.level = param1.player.level;
    }

    @Override
    protected boolean isValidBlock(BlockState param0) {
        return param0.is(Blocks.SMITHING_TABLE);
    }

    @Override
    protected boolean mayPickup(Player param0, boolean param1) {
        return this.selectedRecipe != null && this.selectedRecipe.matches(this.inputSlots, this.level);
    }

    @Override
    protected ItemStack onTake(Player param0, ItemStack param1) {
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.access.execute((param0x, param1x) -> param0x.levelEvent(1044, param1x, 0));
        return param1;
    }

    private void shrinkStackInSlot(int param0) {
        ItemStack var0 = this.inputSlots.getItem(param0);
        var0.shrink(1);
        this.inputSlots.setItem(param0, var0);
    }

    @Override
    public void createResult() {
        List<UpgradeRecipe> var0 = this.level.getRecipeManager().getRecipesFor(RecipeType.SMITHING, this.inputSlots, this.level);
        if (var0.isEmpty()) {
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            this.selectedRecipe = var0.get(0);
            ItemStack var1 = this.selectedRecipe.assemble(this.inputSlots);
            this.resultSlots.setItem(0, var1);
        }

    }
}
