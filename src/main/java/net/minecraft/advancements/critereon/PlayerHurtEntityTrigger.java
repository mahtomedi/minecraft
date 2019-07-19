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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class PlayerHurtEntityTrigger implements CriterionTrigger<PlayerHurtEntityTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("player_hurt_entity");
    private final Map<PlayerAdvancements, PlayerHurtEntityTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> param1) {
        PlayerHurtEntityTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new PlayerHurtEntityTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> param1) {
        PlayerHurtEntityTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public PlayerHurtEntityTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        DamagePredicate var0 = DamagePredicate.fromJson(param0.get("damage"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("entity"));
        return new PlayerHurtEntityTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, Entity param1, DamageSource param2, float param3, float param4, boolean param5) {
        PlayerHurtEntityTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2, param3, param4, param5);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, Entity param1, DamageSource param2, float param3, float param4, boolean param5) {
            List<CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2, param3, param4, param5)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<PlayerHurtEntityTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamagePredicate damage;
        private final EntityPredicate entity;

        public TriggerInstance(DamagePredicate param0, EntityPredicate param1) {
            super(PlayerHurtEntityTrigger.ID);
            this.damage = param0;
            this.entity = param1;
        }

        public static PlayerHurtEntityTrigger.TriggerInstance playerHurtEntity(DamagePredicate.Builder param0) {
            return new PlayerHurtEntityTrigger.TriggerInstance(param0.build(), EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Entity param1, DamageSource param2, float param3, float param4, boolean param5) {
            if (!this.damage.matches(param0, param2, param3, param4, param5)) {
                return false;
            } else {
                return this.entity.matches(param0, param1);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("damage", this.damage.serializeToJson());
            var0.add("entity", this.entity.serializeToJson());
            return var0;
        }
    }
}
