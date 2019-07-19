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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class NetherTravelTrigger implements CriterionTrigger<NetherTravelTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("nether_travel");
    private final Map<PlayerAdvancements, NetherTravelTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance> param1) {
        NetherTravelTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new NetherTravelTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance> param1) {
        NetherTravelTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public NetherTravelTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        LocationPredicate var0 = LocationPredicate.fromJson(param0.get("entered"));
        LocationPredicate var1 = LocationPredicate.fromJson(param0.get("exited"));
        DistancePredicate var2 = DistancePredicate.fromJson(param0.get("distance"));
        return new NetherTravelTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, Vec3 param1) {
        NetherTravelTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0.getLevel(), param1, param0.x, param0.y, param0.z);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerLevel param0, Vec3 param1, double param2, double param3, double param4) {
            List<CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2, param3, param4)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<NetherTravelTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final LocationPredicate entered;
        private final LocationPredicate exited;
        private final DistancePredicate distance;

        public TriggerInstance(LocationPredicate param0, LocationPredicate param1, DistancePredicate param2) {
            super(NetherTravelTrigger.ID);
            this.entered = param0;
            this.exited = param1;
            this.distance = param2;
        }

        public static NetherTravelTrigger.TriggerInstance travelledThroughNether(DistancePredicate param0) {
            return new NetherTravelTrigger.TriggerInstance(LocationPredicate.ANY, LocationPredicate.ANY, param0);
        }

        public boolean matches(ServerLevel param0, Vec3 param1, double param2, double param3, double param4) {
            if (!this.entered.matches(param0, param1.x, param1.y, param1.z)) {
                return false;
            } else if (!this.exited.matches(param0, param2, param3, param4)) {
                return false;
            } else {
                return this.distance.matches(param1.x, param1.y, param1.z, param2, param3, param4);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("entered", this.entered.serializeToJson());
            var0.add("exited", this.exited.serializeToJson());
            var0.add("distance", this.distance.serializeToJson());
            return var0;
        }
    }
}
