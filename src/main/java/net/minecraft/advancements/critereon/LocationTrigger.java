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
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LocationTrigger implements CriterionTrigger<LocationTrigger.TriggerInstance> {
    private final ResourceLocation id;
    private final Map<PlayerAdvancements, LocationTrigger.PlayerListeners> players = Maps.newHashMap();

    public LocationTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<LocationTrigger.TriggerInstance> param1) {
        LocationTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new LocationTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<LocationTrigger.TriggerInstance> param1) {
        LocationTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public LocationTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        LocationPredicate var0 = LocationPredicate.fromJson(param0);
        return new LocationTrigger.TriggerInstance(this.id, var0);
    }

    public void trigger(ServerPlayer param0) {
        LocationTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0.getLevel(), param0.x, param0.y, param0.z);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<LocationTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<LocationTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<LocationTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerLevel param0, double param1, double param2, double param3) {
            List<CriterionTrigger.Listener<LocationTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<LocationTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2, param3)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<LocationTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation param0, LocationPredicate param1) {
            super(param0);
            this.location = param1;
        }

        public static LocationTrigger.TriggerInstance located(LocationPredicate param0) {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, param0);
        }

        public static LocationTrigger.TriggerInstance sleptInBed() {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, LocationPredicate.ANY);
        }

        public static LocationTrigger.TriggerInstance raidWon() {
            return new LocationTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, LocationPredicate.ANY);
        }

        public boolean matches(ServerLevel param0, double param1, double param2, double param3) {
            return this.location.matches(param0, param1, param2, param3);
        }

        @Override
        public JsonElement serializeToJson() {
            return this.location.serializeToJson();
        }
    }
}
