package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("villager_trade");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TradeTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("villager"));
        ItemPredicate var1 = ItemPredicate.fromJson(param0.get("item"));
        return new TradeTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, AbstractVillager param1, ItemStack param2) {
        this.trigger(param0.getAdvancements(), param3 -> param3.matches(param0, param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate villager;
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate param0, ItemPredicate param1) {
            super(TradeTrigger.ID);
            this.villager = param0;
            this.item = param1;
        }

        public static TradeTrigger.TriggerInstance tradedWithVillager() {
            return new TradeTrigger.TriggerInstance(EntityPredicate.ANY, ItemPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, AbstractVillager param1, ItemStack param2) {
            if (!this.villager.matches(param0, param1)) {
                return false;
            } else {
                return this.item.matches(param2);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("item", this.item.serializeToJson());
            var0.add("villager", this.villager.serializeToJson());
            return var0;
        }
    }
}
