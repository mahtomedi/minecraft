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
import net.minecraft.world.entity.LivingEntity;

public class EffectsChangedTrigger implements CriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("effects_changed");
    private final Map<PlayerAdvancements, EffectsChangedTrigger.PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> param1) {
        EffectsChangedTrigger.PlayerListeners var0 = this.players.get(param0);
        if (var0 == null) {
            var0 = new EffectsChangedTrigger.PlayerListeners(param0);
            this.players.put(param0, var0);
        }

        var0.addListener(param1);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> param1) {
        EffectsChangedTrigger.PlayerListeners var0 = this.players.get(param0);
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

    public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        MobEffectsPredicate var0 = MobEffectsPredicate.fromJson(param0.get("effects"));
        return new EffectsChangedTrigger.TriggerInstance(var0);
    }

    public void trigger(ServerPlayer param0) {
        EffectsChangedTrigger.PlayerListeners var0 = this.players.get(param0.getAdvancements());
        if (var0 != null) {
            var0.trigger(param0);
        }

    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements param0) {
            this.player = param0;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> param0) {
            this.listeners.add(param0);
        }

        public void removeListener(CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> param0) {
            this.listeners.remove(param0);
        }

        public void trigger(ServerPlayer param0) {
            List<CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance>> var0 = null;

            for(CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> var1 : this.listeners) {
                if (var1.getTriggerInstance().matches(param0)) {
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1);
                }
            }

            if (var0 != null) {
                for(CriterionTrigger.Listener<EffectsChangedTrigger.TriggerInstance> var2 : var0) {
                    var2.run(this.player);
                }
            }

        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final MobEffectsPredicate effects;

        public TriggerInstance(MobEffectsPredicate param0) {
            super(EffectsChangedTrigger.ID);
            this.effects = param0;
        }

        public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate param0) {
            return new EffectsChangedTrigger.TriggerInstance(param0);
        }

        public boolean matches(ServerPlayer param0) {
            return this.effects.matches((LivingEntity)param0);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("effects", this.effects.serializeToJson());
            return var0;
        }
    }
}
