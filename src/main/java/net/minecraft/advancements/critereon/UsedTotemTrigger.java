package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger extends SimpleCriterionTrigger<UsedTotemTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("used_totem");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public UsedTotemTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        return new UsedTotemTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite param0, ItemPredicate param1) {
            super(UsedTotemTrigger.ID, param0);
            this.item = param1;
        }

        public static UsedTotemTrigger.TriggerInstance usedTotem(ItemLike param0) {
            return new UsedTotemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.Builder.item().of(param0).build());
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
