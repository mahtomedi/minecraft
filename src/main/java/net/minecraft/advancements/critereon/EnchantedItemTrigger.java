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

public class EnchantedItemTrigger implements CriterionTrigger<EnchantedItemTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("enchanted_item");
    private final Map<PlayerAdvancements, EnchantedItemTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> param1) {
        EnchantedItemTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new EnchantedItemTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> param1) {
        EnchantedItemTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public EnchantedItemTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("levels"));
        return new EnchantedItemTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, ItemStack param1, int param2) {
        EnchantedItemTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1, param2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ItemStack param0, int param1) {
            List<CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<EnchantedItemTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        private final MinMaxBounds.Ints levels;

        public TriggerInstance(ItemPredicate param0, MinMaxBounds.Ints param1) {
            super(EnchantedItemTrigger.ID);
            this.item = param0;
            this.levels = param1;
        }

        public static EnchantedItemTrigger.TriggerInstance enchantedItem() {
            return new EnchantedItemTrigger.TriggerInstance(ItemPredicate.ANY, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ItemStack param0, int param1) {
            if (!this.item.matches(param0)) {
                return false;
            } else {
                return this.levels.matches(param1);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("item", this.item.serializeToJson());
            var0.add("levels", this.levels.serializeToJson());
            return var0;
        }
    }
}
