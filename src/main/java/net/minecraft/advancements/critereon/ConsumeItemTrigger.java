package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("consume_item");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ConsumeItemTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        return new ConsumeItemTrigger.TriggerInstance(ItemPredicate.fromJson(param0.get("item")));
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        this.trigger(param0.getAdvancements(), param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ItemPredicate param0) {
            super(ConsumeItemTrigger.ID);
            this.item = param0;
        }

        public static ConsumeItemTrigger.TriggerInstance usedItem() {
            return new ConsumeItemTrigger.TriggerInstance(ItemPredicate.ANY);
        }

        public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike param0) {
            return new ConsumeItemTrigger.TriggerInstance(
                new ItemPredicate(
                    null,
                    param0.asItem(),
                    MinMaxBounds.Ints.ANY,
                    MinMaxBounds.Ints.ANY,
                    EnchantmentPredicate.NONE,
                    EnchantmentPredicate.NONE,
                    null,
                    NbtPredicate.ANY
                )
            );
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
