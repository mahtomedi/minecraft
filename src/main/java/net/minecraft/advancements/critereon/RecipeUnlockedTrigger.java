package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject param0, ContextAwarePredicate param1, DeserializationContext param2) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "recipe"));
        return new RecipeUnlockedTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Recipe<?> param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static RecipeUnlockedTrigger.TriggerInstance unlocked(ResourceLocation param0) {
        return new RecipeUnlockedTrigger.TriggerInstance(ContextAwarePredicate.ANY, param0);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ResourceLocation recipe;

        public TriggerInstance(ContextAwarePredicate param0, ResourceLocation param1) {
            super(RecipeUnlockedTrigger.ID, param0);
            this.recipe = param1;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.addProperty("recipe", this.recipe.toString());
            return var0;
        }

        public boolean matches(Recipe<?> param0) {
            return this.recipe.equals(param0.getId());
        }
    }
}
