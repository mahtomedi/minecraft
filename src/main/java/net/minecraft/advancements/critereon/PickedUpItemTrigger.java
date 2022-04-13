package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
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

    protected PickedUpItemTrigger.TriggerInstance createInstance(JsonObject param0, EntityPredicate.Composite param1, DeserializationContext param2) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        EntityPredicate.Composite var1 = EntityPredicate.Composite.fromJson(param0, "entity", param2);
        return new PickedUpItemTrigger.TriggerInstance(this.id, param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, @Nullable Entity param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param2);
        this.trigger(param0, param3 -> param3.matches(param0, param1, var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        private final EntityPredicate.Composite entity;

        public TriggerInstance(ResourceLocation param0, EntityPredicate.Composite param1, ItemPredicate param2, EntityPredicate.Composite param3) {
            super(param0, param1);
            this.item = param2;
            this.entity = param3;
        }

        public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByEntity(
            EntityPredicate.Composite param0, ItemPredicate param1, EntityPredicate.Composite param2
        ) {
            return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_ENTITY.getId(), param0, param1, param2);
        }

        public static PickedUpItemTrigger.TriggerInstance thrownItemPickedUpByPlayer(
            EntityPredicate.Composite param0, ItemPredicate param1, EntityPredicate.Composite param2
        ) {
            return new PickedUpItemTrigger.TriggerInstance(CriteriaTriggers.THROWN_ITEM_PICKED_UP_BY_PLAYER.getId(), param0, param1, param2);
        }

        public boolean matches(ServerPlayer param0, ItemStack param1, LootContext param2) {
            if (!this.item.matches(param1)) {
                return false;
            } else {
                return this.entity.matches(param2);
            }
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
