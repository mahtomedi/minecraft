package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
    public FishingRodHookedTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ItemPredicate> var0 = ItemPredicate.fromJson(param0.get("rod"));
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "entity", param2);
        Optional<ItemPredicate> var2 = ItemPredicate.fromJson(param0.get("item"));
        return new FishingRodHookedTrigger.TriggerInstance(param1, var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, FishingHook param2, Collection<ItemStack> param3) {
        LootContext var0 = EntityPredicate.createContext(param0, (Entity)(param2.getHookedIn() != null ? param2.getHookedIn() : param2));
        this.trigger(param0, param3x -> param3x.matches(param1, var0, param3));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> rod;
        private final Optional<ContextAwarePredicate> entity;
        private final Optional<ItemPredicate> item;

        public TriggerInstance(
            Optional<ContextAwarePredicate> param0, Optional<ItemPredicate> param1, Optional<ContextAwarePredicate> param2, Optional<ItemPredicate> param3
        ) {
            super(param0);
            this.rod = param1;
            this.entity = param2;
            this.item = param3;
        }

        public static Criterion<FishingRodHookedTrigger.TriggerInstance> fishedItem(
            Optional<ItemPredicate> param0, Optional<EntityPredicate> param1, Optional<ItemPredicate> param2
        ) {
            return CriteriaTriggers.FISHING_ROD_HOOKED
                .createCriterion(new FishingRodHookedTrigger.TriggerInstance(Optional.empty(), param0, EntityPredicate.wrap(param1), param2));
        }

        public boolean matches(ItemStack param0, LootContext param1, Collection<ItemStack> param2) {
            if (this.rod.isPresent() && !this.rod.get().matches(param0)) {
                return false;
            } else if (this.entity.isPresent() && !this.entity.get().matches(param1)) {
                return false;
            } else {
                if (this.item.isPresent()) {
                    boolean var0 = false;
                    Entity var1 = param1.getParamOrNull(LootContextParams.THIS_ENTITY);
                    if (var1 instanceof ItemEntity var2 && this.item.get().matches(var2.getItem())) {
                        var0 = true;
                    }

                    for(ItemStack var3 : param2) {
                        if (this.item.get().matches(var3)) {
                            var0 = true;
                            break;
                        }
                    }

                    if (!var0) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            this.rod.ifPresent(param1 -> var0.add("rod", param1.serializeToJson()));
            this.entity.ifPresent(param1 -> var0.add("entity", param1.toJson()));
            this.item.ifPresent(param1 -> var0.add("item", param1.serializeToJson()));
            return var0;
        }
    }
}
