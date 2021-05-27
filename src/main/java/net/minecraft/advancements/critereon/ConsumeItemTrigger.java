package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger extends SimpleCriterionTrigger<ConsumeItemTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("consume_item");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public ConsumeItemTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        return new ConsumeItemTrigger.TriggerInstance(param1, ItemPredicate.fromJson(param0.get("item")));
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        this.trigger(param0, param1x -> param1x.matches(param1));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite param0, ItemPredicate param1) {
            super(ConsumeItemTrigger.ID, param0);
            this.item = param1;
        }

        public static ConsumeItemTrigger.TriggerInstance usedItem() {
            return new ConsumeItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.ANY);
        }

        public static ConsumeItemTrigger.TriggerInstance usedItem(ItemPredicate param0) {
            return new ConsumeItemTrigger.TriggerInstance(EntityPredicate.Composite.ANY, param0);
        }

        public static ConsumeItemTrigger.TriggerInstance usedItem(ItemLike param0) {
            return new ConsumeItemTrigger.TriggerInstance(
                EntityPredicate.Composite.ANY,
                new ItemPredicate(
                    null,
                    ImmutableSet.of(param0.asItem()),
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
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("item", this.item.serializeToJson());
            return var0;
        }
    }
}
