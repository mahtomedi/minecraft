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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class KilledTrigger implements CriterionTrigger<KilledTrigger.TriggerInstance> {
    private final Map<PlayerAdvancements, KilledTrigger.PlayerListeners> players = Maps.newHashMap();
    private final ResourceLocation id;

    public KilledTrigger(ResourceLocation param0) {
        this.id = param0;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<KilledTrigger.TriggerInstance> param1) {
        KilledTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new KilledTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<KilledTrigger.TriggerInstance> param1) {
        KilledTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public KilledTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        return new KilledTrigger.TriggerInstance(
            this.id, EntityPredicate.fromJson(param0.get("entity")), DamageSourcePredicate.fromJson(param0.get("killing_blow"))
        );
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2) {
        KilledTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<KilledTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<KilledTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<KilledTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, Entity param1, DamageSource param2) {
            List<CriterionTrigger.Listener<KilledTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<KilledTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<KilledTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate entityPredicate;
        private final DamageSourcePredicate killingBlow;

        public TriggerInstance(ResourceLocation param0, EntityPredicate param1, DamageSourcePredicate param2) {
            super(param0);
            this.entityPredicate = param1;
            this.killingBlow = param2;
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, param0.build(), DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity() {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
        }

        public static KilledTrigger.TriggerInstance playerKilledEntity(EntityPredicate.Builder param0, DamageSourcePredicate.Builder param1) {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, param0.build(), param1.build());
        }

        public static KilledTrigger.TriggerInstance entityKilledPlayer() {
            return new KilledTrigger.TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Entity param1, DamageSource param2) {
            return !this.killingBlow.matches(param0, param2) ? false : this.entityPredicate.matches(param0, param1);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("entity", this.entityPredicate.serializeToJson());
            var0.add("killing_blow", this.killingBlow.serializeToJson());
            return var0;
        }
    }
}
