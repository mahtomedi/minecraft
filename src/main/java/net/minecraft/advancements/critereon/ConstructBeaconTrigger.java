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
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class ConstructBeaconTrigger implements CriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("construct_beacon");
    private final Map<PlayerAdvancements, ConstructBeaconTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> param1) {
        ConstructBeaconTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new ConstructBeaconTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> param1) {
        ConstructBeaconTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        MinMaxBounds.Ints var0 = MinMaxBounds.Ints.fromJson(param0.get("level"));
        return new ConstructBeaconTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, BeaconBlockEntity param1) {
        ConstructBeaconTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(BeaconBlockEntity param0) {
            List<CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<ConstructBeaconTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints level;

        public TriggerInstance(MinMaxBounds.Ints param0) {
            super(ConstructBeaconTrigger.ID);
            this.level = param0;
        }

        public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints param0) {
            return new ConstructBeaconTrigger.TriggerInstance(param0);
        }

        public boolean matches(BeaconBlockEntity param0) {
            return this.level.matches(param0.getLevels());
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("level", this.level.serializeToJson());
            return var0;
        }
    }
}
