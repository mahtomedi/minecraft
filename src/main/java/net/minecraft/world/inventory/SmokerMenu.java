package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;

public class SmokerMenu extends AbstractFurnaceMenu {
    public SmokerMenu(int param0, Inventory param1) {
        super(MenuType.SMOKER, RecipeType.SMOKING, param0, param1);
    }

    public SmokerMenu(int param0, Inventory param1, Container param2, ContainerData param3) {
        super(MenuType.SMOKER, RecipeType.SMOKING, param0, param1, param2, param3);
    }
}
