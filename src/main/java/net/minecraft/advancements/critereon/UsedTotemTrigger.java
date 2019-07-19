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
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger implements CriterionTrigger<UsedTotemTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("used_totem");
    private final Map<PlayerAdvancements, UsedTotemTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> param1) {
        UsedTotemTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new UsedTotemTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> param1) {
        UsedTotemTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public UsedTotemTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        return new UsedTotemTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        UsedTotemTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ItemStack param0) {
            List<CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<UsedTotemTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ItemPredicate param0) {
            super(UsedTotemTrigger.ID);
            this.item = param0;
        }

        public static UsedTotemTrigger.TriggerInstance usedTotem(ItemLike param0) {
            return new UsedTotemTrigger.TriggerInstance(ItemPredicate.Builder.item().of(param0).build());
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
