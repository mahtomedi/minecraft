package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
    public ChanneledLightningTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        List<ContextAwarePredicate> var0 = EntityPredicate.fromJsonArray(param0, "victims", param2);
        return new ChanneledLightningTrigger.TriggerInstance(param1, var0);
    }

    public void trigger(ServerPlayer param0, Collection<? extends Entity> param1) {
        List<LootContext> var0 = param1.stream().map(param1x -> EntityPredicate.createContext(param0, param1x)).collect(Collectors.toList());
        this.trigger(param0, param1x -> param1x.matches(var0));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final List<ContextAwarePredicate> victims;

        public TriggerInstance(Optional<ContextAwarePredicate> param0, List<ContextAwarePredicate> param1) {
            super(param0);
            this.victims = param1;
        }

        public static Criterion<ChanneledLightningTrigger.TriggerInstance> channeledLightning(EntityPredicate.Builder... param0) {
            return CriteriaTriggers.CHANNELED_LIGHTNING
                .createCriterion(new ChanneledLightningTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(param0)));
        }

        public boolean matches(Collection<? extends LootContext> param0) {
            for(ContextAwarePredicate var0 : this.victims) {
                boolean var1 = false;

                for(LootContext var2 : param0) {
                    if (var0.matches(var2)) {
                        var1 = true;
                        break;
                    }
                }

                if (!var1) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public JsonObject serializeToJson() {
            JsonObject var0 = super.serializeToJson();
            var0.add("victims", ContextAwarePredicate.toJson(this.victims));
            return var0;
        }
    }
}
