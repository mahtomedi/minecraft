package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    protected PlayerInteractTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ItemPredicate> var0 = ItemPredicate.fromJson(param0.get("item"));
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "entity", param2);
        return new PlayerInteractTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, Entity param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param2);
        this.trigger(param0, param2x -> param2x.matches(param1, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> item;
        private final Optional<ContextAwarePredicate> entity;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ItemPredicate> param1, Optional<ContextAwarePredicate> param2) {
            super(PlayerInteractTrigger.ID, param0);
            this.item = param1;
            this.entity = param2;
        }

        public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(
            Optional<ContextAwarePredicate> param0, ItemPredicate.Builder param1, Optional<ContextAwarePredicate> param2
        ) {
            return new PlayerInteractTrigger.TriggerInstance(param0, param1.build(), param2);
        }

        public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ItemPredicate.Builder param0, Optional<ContextAwarePredicate> param1) {
            return itemUsedOnEntity(Optional.empty(), param0, param1);
        }

        public boolean matches(ItemStack param0, LootContext param1) {
            if (this.item.isPresent() && !this.item.get().matches(param0)) {
                return false;
            } else {
                return this.entity.isEmpty() || this.entity.get().matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.item.ifPresent(param1 -> var0.add("item", param1.serializeToJson()));
            this.entity.ifPresent(param1 -> var0.add("entity", param1.toJson()));
            return var0;
        }
    }
}
