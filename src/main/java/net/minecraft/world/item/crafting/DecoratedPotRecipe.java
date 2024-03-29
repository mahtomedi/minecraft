package net.minecraft.world.item.crafting;

import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;

public class DecoratedPotRecipe extends CustomRecipe {
    public DecoratedPotRecipe(CraftingBookCategory param0) {
        super(param0);
    }

    public boolean matches(CraftingContainer param0, Level param1) {
        if (!this.canCraftInDimensions(param0.getWidth(), param0.getHeight())) {
            return false;
        } else {
            for(int var0 = 0; var0 < param0.getContainerSize(); ++var0) {
                ItemStack var1 = param0.getItem(var0);
                switch(var0) {
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                        if (!var1.is(ItemTags.DECORATED_POT_INGREDIENTS)) {
                            return false;
                        }
                        break;
                    case 2:
                    case 4:
                    case 6:
                    default:
                        if (!var1.is(Items.AIR)) {
                            return false;
                        }
                }
            }

            return true;
        }
    }

    public ItemStack assemble(CraftingContainer param0, RegistryAccess param1) {
        DecoratedPotBlockEntity.Decorations var0 = new DecoratedPotBlockEntity.Decorations(
            param0.getItem(1).getItem(), param0.getItem(3).getItem(), param0.getItem(5).getItem(), param0.getItem(7).getItem()
        );
        return DecoratedPotBlockEntity.createDecoratedPotItem(var0);
    }

    @Override
    public boolean canCraftInDimensions(int param0, int param1) {
        return param0 == 3 && param1 == 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializer.DECORATED_POT_RECIPE;
    }
}
