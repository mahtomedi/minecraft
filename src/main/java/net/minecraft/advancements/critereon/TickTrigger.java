package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public class TickTrigger implements CriterionTrigger<TickTrigger.TriggerInstance> {
    public static final ResourceLocation ID = new ResourceLocation("tick");
    private final Map<PlayerAdvancements, TickTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<TickTrigger.TriggerInstance> param1) {
        TickTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new TickTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<TickTrigger.TriggerInstance> param1) {
        TickTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public TickTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        return new TickTrigger.TriggerInstance();
    }

    public void trigger(ServerPlayer param0) {
        TickTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger();
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<TickTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<TickTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<TickTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger() {
            for(CriterionTrigger.Listener<TickTrigger.TriggerInstance> var0 : Lists.newArrayList(this.listeners)) {
                var0.run(this.player);
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance() {
            super(TickTrigger.ID);
        }
    }
}
