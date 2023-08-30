package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeHolder;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
    public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "recipe"));
        return new RecipeUnlockedTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, RecipeHolder<?> param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static Criterion<RecipeUnlockedTrigger.TriggerInstance> unlocked(ResourceLocation param0) {
        return CriteriaTriggers.RECIPE_UNLOCKED.createCriterion(new RecipeUnlockedTrigger.TriggerInstance(Optional.empty(), param0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ResourceLocation recipe;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, ResourceLocation param1) {
            super(param0);
            this.recipe = param1;
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            var0.addProperty("recipe", this.recipe.toString());
            return var0;
        }

        public boolean matches(RecipeHolder<?> param0) {
            return this.recipe.equals(param0.id());
        }
    }
}
