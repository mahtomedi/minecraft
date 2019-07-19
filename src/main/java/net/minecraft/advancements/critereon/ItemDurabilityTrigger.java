package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger implements CriterionTrigger<ItemDurabilityTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("item_durability_changed");
    private final Map<PlayerAdvancements, ItemDurabilityTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> param1) {
        ItemDurabilityTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new ItemDurabilityTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> param1) {
        ItemDurabilityTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 != null) {
            var0.removeListener(param1);
            if (var0.isEmpty()) {
                this.players.remove(param0);
            }
        }

    }

    @Override
    public void removePlayerListeners(PlayerAdvancements param0) {
        this.players.remove(param0);
    }

    public ItemDurabilityTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("durability"));
        MinMaxBounds.Ints var2 = MinMaxBounds.Ints.fromJson(param0.get("delta"));
        return new ItemDurabilityTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, int param2) {
        ItemDurabilityTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1, param2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ItemStack param0, int param1) {
            List<CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<ItemDurabilityTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        private final MinMaxBounds.Ints durability;
        private final MinMaxBounds.Ints delta;

        public TriggerInstance(ItemPredicate param0, MinMaxBounds.Ints param1, MinMaxBounds.Ints param2) {
            super(ItemDurabilityTrigger.ID);
            this.item = param0;
            this.durability = param1;
            this.delta = param2;
        }

        public static ItemDurabilityTrigger.TriggerInstance changedDurability(ItemPredicate param0, MinMaxBounds.Ints param1) {
            return new ItemDurabilityTrigger.TriggerInstance(param0, param1, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ItemStack param0, int param1) {
            if (!this.item.matches(param0)) {
                return false;
            } else if (!this.durability.matches(param0.getMaxDamage() - param1)) {
                return false;
            } else {
                return this.delta.matches(param0.getDamageValue() - param1);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("item", this.item.serializeToJson());
            var0.add("durability", this.durability.serializeToJson());
            var0.add("delta", this.delta.serializeToJson());
            return var0;
        }
    }
}
