package net.minecraft.data.recipes;

import java.util.function.Consumer;
import net.minecraft.advancements.CriterionTriggerInstance;

public interface RecipeBuilder {
    RecipeBuilder unlockedBy(String var1, CriterionTriggerInstance var2);

    RecipeBuilder group(String var1);

    void save(Consumer<FinishedRecipe> var1);
}
