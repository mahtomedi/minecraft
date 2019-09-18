package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class FilledBucketTrigger extends SimpleCriterionTrigger<FilledBucketTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("filled_bucket");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public FilledBucketTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        return new FilledBucketTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        this.trigger(param0.getAdvancements(), param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ItemPredicate param0) {
            super(FilledBucketTrigger.ID);
            this.item = param0;
        }

        public static FilledBucketTrigger.TriggerInstance filledBucket(ItemPredicate param0) {
            return new FilledBucketTrigger.TriggerInstance(param0);
        }

        public boolean matches(ItemStack param0) {
            return this.item.matches(param0);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}
