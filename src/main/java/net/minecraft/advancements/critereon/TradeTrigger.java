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
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.ItemStack;

public class TradeTrigger implements CriterionTrigger<TradeTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("villager_trade");
    private final Map<PlayerAdvancements, TradeTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<TradeTrigger.TriggerInstance> param1) {
        TradeTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new TradeTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<TradeTrigger.TriggerInstance> param1) {
        TradeTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public TradeTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("villager"));
        ItemPredicate var1 = ItemPredicate.fromJson(param0.get("item"));
        return new TradeTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, AbstractVillager param1, ItemStack param2) {
        TradeTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<TradeTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<TradeTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<TradeTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, AbstractVillager param1, ItemStack param2) {
            List<CriterionTrigger.Listener<TradeTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<TradeTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<TradeTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
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
