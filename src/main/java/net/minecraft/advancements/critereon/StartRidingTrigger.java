package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StartRidingTrigger extends SimpleCriterionTrigger<StartRidingTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("started_riding");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public StartRidingTrigger.TriggerInstance createInstance(JsonObject param0, Optional<ContextAwarePredicate> param1, DeserializationContext param2) {
        return new StartRidingTrigger.TriggerInstance(param1);
    }

    public void trigger(ServerPlayer param0) {
        this.trigger(param0, param0x -> true);
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(Optional<ContextAwarePredicate> param0) {
            super(StartRidingTrigger.ID, param0);
        }

        public static StartRidingTrigger.TriggerInstance playerStartsRiding(EntityPredicate.Builder param0) {
            return new StartRidingTrigger.TriggerInstance(EntityPredicate.wrap(param0));
        }
    }
}
