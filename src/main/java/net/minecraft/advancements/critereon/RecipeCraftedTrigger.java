package net.minecraft.advancements.critereon;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

public class RecipeCraftedTrigger extends SimpleCriterionTrigger<RecipeCraftedTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("recipe_crafted");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    protected RecipeCraftedTrigger.TriggerInstance createInstance(JsonObject param0, ContextAwarePredicate param1, DeserializationContext param2) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "recipe_id"));
        ItemPredicate[] var1 = ItemPredicate.fromJsonArray(param0.get("ingredients"));
        return new RecipeCraftedTrigger.TriggerInstance(param1, var0, List.of(var1));
    }

    public void trigger(ServerPlayer param0, ResourceLocation param1, List<ItemStack> param2) {
        this.trigger(param0, param2x -> param2x.matches(param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ResourceLocation recipeId;
        private final List<ItemPredicate> predicates;

        public TriggerInstance(ContextAwarePredicate param0, ResourceLocation param1, List<ItemPredicate> param2) {
            super(RecipeCraftedTrigger.ID, param0);
            this.recipeId = param1;
            this.predicates = param2;
        }

        public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation param0, List<ItemPredicate> param1) {
            return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, param0, param1);
        }

        public static RecipeCraftedTrigger.TriggerInstance craftedItem(ResourceLocation param0) {
            return new RecipeCraftedTrigger.TriggerInstance(ContextAwarePredicate.ANY, param0, List.of());
        }

        boolean matches(ResourceLocation param0, List<ItemStack> param1) {
            if (!param0.equals(this.recipeId)) {
                return false;
            } else {
                List<ItemStack> var0 = new ArrayList<>(param1);

                for(ItemPredicate var1 : this.predicates) {
                    boolean var2 = false;
                    Iterator<ItemStack> var3 = var0.iterator();

                    while(var3.hasNext()) {
                        if (var1.matches(var3.next())) {
                            var3.remove();
                            var2 = true;
                            break;
                        }
                    }

                    if (!var2) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.addProperty("recipe_id", this.recipeId.toString());
            if (this.predicates.size() > 0) {
                JsonArray var1 = new JsonArray();

                for(ItemPredicate var2 : this.predicates) {
                    var1.add(var2.serializeToJson());
                }

                var0.add("ingredients", var1);
            }

            return var0;
        }
    }
}
