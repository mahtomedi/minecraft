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
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger implements CriterionTrigger<LevitationTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("levitation");
    private final Map<PlayerAdvancements, LevitationTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> param1) {
        LevitationTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new LevitationTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> param1) {
        LevitationTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public LevitationTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        DistancePredicate var0 = DistancePredicate.fromJson(param0.get("distance"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("duration"));
        return new LevitationTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, Vec3 param1, int param2) {
        LevitationTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<LevitationTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, Vec3 param1, int param2) {
            List<CriterionTrigger.Listener<LevitationTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<LevitationTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DistancePredicate distance;
        private final MinMaxBounds.Ints duration;

        public TriggerInstance(DistancePredicate param0, MinMaxBounds.Ints param1) {
            super(LevitationTrigger.ID);
            this.distance = param0;
            this.duration = param1;
        }

        public static LevitationTrigger.TriggerInstance levitated(DistancePredicate param0) {
            return new LevitationTrigger.TriggerInstance(param0, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ServerPlayer param0, Vec3 param1, int param2) {
            if (!this.distance.matches(param1.x, param1.y, param1.z, param0.x, param0.y, param0.z)) {
                return false;
            } else {
                return this.duration.matches(param2);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("distance", this.distance.serializeToJson());
            var0.add("duration", this.duration.serializeToJson());
            return var0;
        }
    }
}
