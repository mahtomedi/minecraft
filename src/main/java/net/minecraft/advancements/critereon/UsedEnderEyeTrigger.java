package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger implements CriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("used_ender_eye");
    private final Map<PlayerAdvancements, UsedEnderEyeTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> param1) {
        UsedEnderEyeTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new UsedEnderEyeTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> param1) {
        UsedEnderEyeTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public UsedEnderEyeTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        MinMaxBounds.Floats var0 = MinMaxBounds.Floats.fromJson(param0.get("distance"));
        return new UsedEnderEyeTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, BlockPos param1) {
        UsedEnderEyeTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            double var1 = param0.x - (double)param1.getX();
            double var2 = param0.z - (double)param1.getZ();
            var0.trigger(var1 * var1 + var2 * var2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(double param0) {
            List<CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<UsedEnderEyeTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Floats level;

        public TriggerInstance(MinMaxBounds.Floats param0) {
            super(UsedEnderEyeTrigger.ID);
            this.level = param0;
        }

        public boolean matches(double param0) {
            return this.level.matchesSqr(param0);
        }
    }
}
