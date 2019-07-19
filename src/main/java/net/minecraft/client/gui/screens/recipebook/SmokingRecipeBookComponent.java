package net.minecraft.client.gui.screens.recipebook;

import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SmokingRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
    @Override
    protected boolean getFilteringCraftable() {
        return this.book.isSmokerFilteringCraftable();
    }

    @Override
    protected void setFilteringCraftable(boolean param0) {
        this.book.setSmokerFilteringCraftable(param0);
    }

    @Override
    protected boolean isGuiOpen() {
        return this.book.isSmokerGuiOpen();
    }

    @Override
    protected void setGuiOpen(boolean param0) {
        this.book.setSmokerGuiOpen(param0);
    }

    @Override
    protected String getRecipeFilterName() {
        return "gui.recipebook.toggleRecipes.smokable";
    }

    @Override
    protected Set<Item> getFuelItems() {
        return AbstractFurnaceBlockEntity.getFuel().keySet();
    }
}
