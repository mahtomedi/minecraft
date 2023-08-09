package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("villager_trade");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TradeTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ContextAwarePredicate> var0 = EntityPredicate.fromJson(param0, "villager", param2);
        Optional<ItemPredicate> var1 = ItemPredicate.fromJson(param0.get("item"));
        return new TradeTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, AbstractVillager param1, ItemStack param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param2x -> param2x.matches(var0, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ContextAwarePredicate> villager;
        private final Optional<ItemPredicate> item;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, Optional<ContextAwarePredicate> param1, Optional<ItemPredicate> param2) {
            super(TradeTrigger.ID, param0);
            this.villager = param1;
            this.item = param2;
        }

        public static TradeTrigger.TriggerInstance tradedWithVillager() {
            return new TradeTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static TradeTrigger.TriggerInstance tradedWithVillager(EntityPredicate.Builder param0) {
            return new TradeTrigger.TriggerInstance(EntityPredicate.wrap(param0), Optional.empty(), Optional.empty());
        }

        public boolean matches(LootContext param0, ItemStack param1) {
            if (this.villager.isPresent() && !this.villager.get().matches(param0)) {
                return false;
            } else {
                return !this.item.isPresent() || this.item.get().matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.item.ifPresent(param1 -> var0.add("item", param1.serializeToJson()));
            this.villager.ifPresent(param1 -> var0.add("villager", param1.toJson()));
            return var0;
        }
    }
}
