package net.minecraft.world.inventory;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

public interface RecipeHolder {
    void setRecipeUsed(@Nullable Recipe<?> var1);

    @Nullable
    Recipe<?> getRecipeUsed();

    default void awardUsedRecipes(Player param0, List<ItemStack> param1) {
        Recipe<?> var0 = this.getRecipeUsed();
        if (var0 != null) {
            param0.triggerRecipeCrafted(var0, param1);
            if (!var0.isSpecial()) {
                param0.awardRecipes(Collections.singleton(var0));
                this.setRecipeUsed(null);
            }
        }

    }

    default boolean setRecipeUsed(Level param0, ServerPlayer param1, Recipe<?> param2) {
        if (!param2.isSpecial() && param0.getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING) && !param1.getRecipeBook().contains(param2)) {
            return false;
        } else {
            this.setRecipeUsed(param2);
            return true;
        }
    }
}
