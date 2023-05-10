package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class UsingItemTrigger extends SimpleCriterionTrigger<UsingItemTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("using_item");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public UsingItemTrigger.TriggerInstance createInstance(JsonObject param0, ContextAwarePredicate param1, DeserializationContext param2) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        return new UsingItemTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ContextAwarePredicate param0, ItemPredicate param1) {
            super(UsingItemTrigger.ID, param0);
            this.item = param1;
        }

        public static UsingItemTrigger.TriggerInstance lookingAt(EntityPredicate.Builder param0, ItemPredicate.Builder param1) {
            return new UsingItemTrigger.TriggerInstance(EntityPredicate.wrap(param0.build()), param1.build());
        }

        public boolean matches(ItemStack param0) {
            return this.item.matches(param0);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}
