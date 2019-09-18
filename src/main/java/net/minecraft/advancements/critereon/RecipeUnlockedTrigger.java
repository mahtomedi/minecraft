package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeUnlockedTrigger extends SimpleCriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "recipe"));
        return new RecipeUnlockedTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, Recipe<?> param1) {
        this.trigger(param0.getAdvancements(), param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ResourceLocation recipe;

        public TriggerInstance(ResourceLocation param0) {
            super(RecipeUnlockedTrigger.ID);
            this.recipe = param0;
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.addProperty("recipe", this.recipe.toString());
            return var0;
        }

        public boolean matches(Recipe<?> param0) {
            return this.recipe.equals(param0.getId());
        }
    }
}
