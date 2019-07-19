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

public class EntityHurtPlayerTrigger implements CriterionTrigger<EntityHurtPlayerTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("entity_hurt_player");
    private final Map<PlayerAdvancements, EntityHurtPlayerTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> param1) {
        EntityHurtPlayerTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new EntityHurtPlayerTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> param1) {
        EntityHurtPlayerTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public EntityHurtPlayerTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        DamagePredicate var0 = DamagePredicate.fromJson(param0.get("damage"));
        return new EntityHurtPlayerTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
        EntityHurtPlayerTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2, param3, param4);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
            List<CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2, param3, param4)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<EntityHurtPlayerTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final DamagePredicate damage;

        public TriggerInstance(DamagePredicate param0) {
            super(EntityHurtPlayerTrigger.ID);
            this.damage = param0;
        }

        public static EntityHurtPlayerTrigger.TriggerInstance entityHurtPlayer(DamagePredicate.Builder param0) {
            return new EntityHurtPlayerTrigger.TriggerInstance(param0.build());
        }

        public boolean matches(ServerPlayer param0, DamageSource param1, float param2, float param3, boolean param4) {
            return this.damage.matches(param0, param1, param2, param3, param4);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("damage", this.damage.serializeToJson());
            return var0;
        }
    }
}
