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

public class FilledBucketTrigger implements CriterionTrigger<FilledBucketTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("filled_bucket");
    private final Map<PlayerAdvancements, FilledBucketTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance> param1) {
        FilledBucketTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new FilledBucketTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance> param1) {
        FilledBucketTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public FilledBucketTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        ItemPredicate var0 = ItemPredicate.fromJson(param0.get("item"));
        return new FilledBucketTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, ItemStack param1) {
        FilledBucketTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ItemStack param0) {
            List<CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<FilledBucketTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(ItemPredicate param0) {
            super(FilledBucketTrigger.ID);
            this.item = param0;
        }

        public static FilledBucketTrigger.TriggerInstance filledBucket(ItemPredicate param0) {
            return new FilledBucketTrigger.TriggerInstance(param0);
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
