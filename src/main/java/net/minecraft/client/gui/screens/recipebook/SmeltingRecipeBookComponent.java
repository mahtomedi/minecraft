package net.minecraft.client.gui.screens.recipebook;

import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmeltingRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
    private static final Component FILTER_NAME = Component.translatable("gui.recipebook.toggleRecipes.smeltable");

    @Override
    protected Component getRecipeFilterName() {
        return FILTER_NAME;
    }

    @Override
    protected Set<Item> getFuelItems() {
        return AbstractFurnaceBlockEntity.getFuel().keySet();
    }
}
