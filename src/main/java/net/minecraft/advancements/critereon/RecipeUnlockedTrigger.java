package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeUnlockedTrigger implements CriterionTrigger<RecipeUnlockedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("recipe_unlocked");
    private final Map<PlayerAdvancements, RecipeUnlockedTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> param1) {
        RecipeUnlockedTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new RecipeUnlockedTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> param1) {
        RecipeUnlockedTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 != null) {
            var0.removeListener(param1);
            if (var0.isEmpty()) {
                this.players.remove(param0);
            }
        }

    }

    @Override
    public void removePlayerListeners(PlayerAdvancements param0) {
        this.players.remove(param0);
    }

    public RecipeUnlockedTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ResourceLocation var0 = new ResourceLocation(GsonHelper.getAsString(param0, "recipe"));
        return new RecipeUnlockedTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, Recipe<?> param1) {
        RecipeUnlockedTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(Recipe<?> param0) {
            List<CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<RecipeUnlockedTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
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
