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
import net.minecraft.world.entity.Entity;

public class SummonedEntityTrigger implements CriterionTrigger<SummonedEntityTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("summoned_entity");
    private final Map<PlayerAdvancements, SummonedEntityTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> param1) {
        SummonedEntityTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new SummonedEntityTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> param1) {
        SummonedEntityTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public SummonedEntityTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("entity"));
        return new SummonedEntityTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, Entity param1) {
        SummonedEntityTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, Entity param1) {
            List<CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<SummonedEntityTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate entity;

        public TriggerInstance(EntityPredicate param0) {
            super(SummonedEntityTrigger.ID);
            this.entity = param0;
        }

        public static SummonedEntityTrigger.TriggerInstance summonedEntity(EntityPredicate.Builder param0) {
            return new SummonedEntityTrigger.TriggerInstance(param0.build());
        }

        public boolean matches(ServerPlayer param0, Entity param1) {
            return this.entity.matches(param0, param1);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("entity", this.entity.serializeToJson());
            return var0;
        }
    }
}
