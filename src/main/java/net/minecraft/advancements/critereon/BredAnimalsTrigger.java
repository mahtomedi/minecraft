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
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Animal;

public class BredAnimalsTrigger implements CriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("bred_animals");
    private final Map<PlayerAdvancements, BredAnimalsTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> param1) {
        BredAnimalsTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new BredAnimalsTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> param1) {
        BredAnimalsTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate var0 = EntityPredicate.fromJson(param0.get("parent"));
        EntityPredicate var1 = EntityPredicate.fromJson(param0.get("partner"));
        EntityPredicate var2 = EntityPredicate.fromJson(param0.get("child"));
        return new BredAnimalsTrigger.TriggerInstance(var0, var1, var2);
    }

    public void trigger(ServerPlayer param0, Animal param1, @Nullable Animal param2, @Nullable AgableMob param3) {
        BredAnimalsTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0, param1, param2, param3);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0, Animal param1, @Nullable Animal param2, @Nullable AgableMob param3) {
            List<CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0, param1, param2, param3)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate parent;
        private final EntityPredicate partner;
        private final EntityPredicate child;

        public TriggerInstance(EntityPredicate param0, EntityPredicate param1, EntityPredicate param2) {
            super(BredAnimalsTrigger.ID);
            this.parent = param0;
            this.partner = param1;
            this.child = param2;
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
            return new BredAnimalsTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder param0) {
            return new BredAnimalsTrigger.TriggerInstance(param0.build(), EntityPredicate.ANY, EntityPredicate.ANY);
        }

        public boolean matches(ServerPlayer param0, Animal param1, @Nullable Animal param2, @Nullable AgableMob param3) {
            if (!this.child.matches(param0, param3)) {
                return false;
            } else {
                return this.parent.matches(param0, param1) && this.partner.matches(param0, param2)
                    || this.parent.matches(param0, param2) && this.partner.matches(param0, param1);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("parent", this.parent.serializeToJson());
            var0.add("partner", this.partner.serializeToJson());
            var0.add("child", this.child.serializeToJson());
            return var0;
        }
    }
}
