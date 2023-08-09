package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PickedUpItemTrigger extends SimpleCriterionTrigger<PickedUpItemTrigger.TriggerInstance> {
    private final ResourceLocation id;

    public PickedUpItemTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    protected PickedUpItemTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        Optional<ItemPredicate> var0 = ItemPredicate.fromJson(param0.get("item"));
        Optional<ContextAwarePredicate> var1 = EntityPredicate.fromJson(param0, "entity", param2);
        return new PickedUpItemTrigger.TriggerInstance(this.id, param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, @Nullable Entity param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param2);
        this.trigger(param0, param3 -> param3.matches(param0, param1, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final Optional<ItemPredicate> item;
        private final Optional<ContextAwarePredicate> entity;

        public TriggerInstance(
            ResourceLocation param0, Optional<ContextAwarePredicate> param1, Optional<ItemPredicate> param2, Optional<ContextAwarePredicate> param3
        ) {
            super(param0, param1);
            this.item = param2;
            this.entity = param3;
        }

        public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByEntity(
            ContextAwarePredicate param0, Optional<ItemPredicate> param1, Optional<ContextAwarePredicate> param2
        ) {
            return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), Optional.of(param0), param1, param2);
        }

        public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByPlayer(
            Optional<ContextAwarePredicate> param0, Optional<ItemPredicate> param1, Optional<ContextAwarePredicate> param2
        ) {
            return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), param0, param1, param2);
        }

        public boolean matches(ServerPlayer param0, ItemStack param1, LootContext param2) {
            if (this.item.isPresent() && !this.item.get().matches(param1)) {
                return false;
            } else {
                return !this.entity.isPresent() || this.entity.get().matches(param2);
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
