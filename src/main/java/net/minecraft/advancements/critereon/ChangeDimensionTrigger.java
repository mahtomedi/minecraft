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
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.dimension.DimensionType;

public class ChangeDimensionTrigger implements CriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("changed_dimension");
    private final Map<PlayerAdvancements, ChangeDimensionTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> param1) {
        ChangeDimensionTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new ChangeDimensionTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> param1) {
        ChangeDimensionTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        DimensionType var0 = param0.has("from") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(param0, "from"))) : null;
        DimensionType var1 = param0.has("to") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(param0, "to"))) : null;
        return new ChangeDimensionTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, DimensionType param1, DimensionType param2) {
        ChangeDimensionTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param1, param2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(DimensionType param0, DimensionType param1) {
            List<CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        @Nullable
        private final DimensionType from;
        @Nullable
        private final DimensionType to;

        public TriggerInstance(@Nullable DimensionType param0, @Nullable DimensionType param1) {
            super(ChangeDimensionTrigger.ID);
            this.from = param0;
            this.to = param1;
        }

        public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(DimensionType param0) {
            return new ChangeDimensionTrigger.TriggerInstance(null, param0);
        }

        public boolean matches(DimensionType param0, DimensionType param1) {
            if (this.from != null && this.from != param0) {
                return false;
            } else {
                return this.to == null || this.to == param1;
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            if (this.from != null) {
                var0.addProperty("from", DimensionType.getName(this.from).toString());
            }

            if (this.to != null) {
                var0.addProperty("to", DimensionType.getName(this.to).toString());
            }

            return var0;
        }
    }
}
