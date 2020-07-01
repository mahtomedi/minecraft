package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;

public class BlastFurnaceMenu extends AbstractFurnaceMenu {
    public BlastFurnaceMenu(int param0, Inventory param1) {
        super(MenuType.BLAST_FURNACE, RecipeType.BLASTING, RecipeBookType.BLAST_FURNACE, param0, param1);
    }

    public BlastFurnaceMenu(int param0, Inventory param1, Container param2, ContainerData param3) {
        super(MenuType.BLAST_FURNACE, RecipeType.BLASTING, RecipeBookType.BLAST_FURNACE, param0, param1, param2, param3);
    }
}
