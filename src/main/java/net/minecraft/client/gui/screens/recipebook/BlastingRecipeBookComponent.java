package net.minecraft.client.gui.screens.recipebook;

import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlastingRecipeBookComponent extends AbstractFurnaceRecipeBookComponent {
    @Override
    protected boolean getFilteringCraftable() {
        return this.book.isBlastingFurnaceFilteringCraftable();
    }

    @Override
    protected void setFilteringCraftable(boolean param0) {
        this.book.setBlastingFurnaceFilteringCraftable(param0);
    }

    @Override
    protected boolean isGuiOpen() {
        return this.book.isBlastingFurnaceGuiOpen();
    }

    @Override
    protected void setGuiOpen(boolean param0) {
        this.book.setBlastingFurnaceGuiOpen(param0);
    }

    @Override
    protected Component getRecipeFilterName() {
        return new TranslatableComponent("gui.recipebook.toggleRecipes.blastable");
    }

    @Override
    protected Set<Item> getFuelItems() {
        return AbstractFurnaceBlockEntity.getFuel().keySet();
    }
}
