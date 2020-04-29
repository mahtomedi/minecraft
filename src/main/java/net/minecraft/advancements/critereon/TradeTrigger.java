package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class TradeTrigger extends SimpleCriterionTrigger<TradeTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("villager_trade");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public TradeTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        EntityPredicate.Composite var0 = EntityPredicate.Composite.fromJson(param0, "villager", param2);
        ItemPredicate var1 = ItemPredicate.fromJson(param0.get("item"));
        return new TradeTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, AbstractVillager param1, ItemStack param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        this.trigger(param0, param2x -> param2x.matches(var0, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite villager;
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite param0, EntityPredicate.Composite param1, ItemPredicate param2) {
            super(TradeTrigger.ID, param0);
            this.villager = param1;
            this.item = param2;
        }

        public static TradeTrigger.TriggerInstance tradedWithVillager() {
            return new TradeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.ANY, ItemPredicate.ANY);
        }

        public boolean matches(LootContext param0, ItemStack param1) {
            if (!this.villager.matches(param0)) {
                return false;
            } else {
                return this.item.matches(param1);
            }
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("item", this.item.serializeToJson());
            var0.add("villager", this.villager.toJson(param0));
            return var0;
        }
    }
}
