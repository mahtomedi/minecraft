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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;

public class CuredZombieVillagerTrigger implements CriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");
    private final Map<PlayerAdvancements, CuredZombieVillagerTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> param1) {
        CuredZombieVillagerTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new CuredZombieVillagerTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> param1) {
        CuredZombieVillagerTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("zombie"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("villager"));
        return new CuredZombieVillagerTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, Zombie param1, Villager param2) {
        CuredZombieVillagerTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, Zombie param1, Villager param2) {
            List<CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate zombie;
        private final EntityPredicate villager;

        public TriggerInstance(EntityPredicate param0, EntityPredicate param1) {
            super(CuredZombieVillagerTrigger.ID);
            this.zombie = param0;
            this.villager = param1;
        }

        public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
            return new CuredZombieVillagerTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Zombie param1, Villager param2) {
            if (!this.zombie.matches(param0, param1)) {
                return false;
            } else {
                return this.villager.matches(param0, param2);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("zombie", this.zombie.serializeToJson());
            var0.add("villager", this.villager.serializeToJson());
            return var0;
        }
    }
}
