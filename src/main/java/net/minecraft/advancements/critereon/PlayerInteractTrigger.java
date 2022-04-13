package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    protected PlayerInteractTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "entity", param2);
        return new PlayerInteractTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, Entity param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param2);
        this.trigger(param0, param2x -> param2x.matches(param1, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite param0, ItemPredicate param1, EntityPredicate.Composite param2) {
            super(PlayerInteractTrigger.ID, param0);
            this.item = param1;
            this.entity = param2;
        }

        public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(
            EntityPredicate.Composite param0, ItemPredicate.Builder param1, EntityPredicate.Composite param2
        ) {
            return new PlayerInteractTrigger.TriggerInstance(param0, param1.build(), param2);
        }

        public static PlayerInteractTrigger.TriggerInstance itemUsedOnEntity(ItemPredicate.Builder param0, EntityPredicate.Composite param1) {
            return itemUsedOnEntity(EntityPredicate.Composite.ANY, param0, param1);
        }

        public boolean matches(ItemStack param0, LootContext param1) {
            return !this.item.matches(param0) ? false : this.entity.matches(param1);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("item", this.item.serializeToJson());
            var0.add("entity", this.entity.toJson(param0));
            return var0;
        }
    }
}
