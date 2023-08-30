package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;

public abstract class AbstractCriterionTriggerInstance implements SimpleCriterionTrigger.SimpleInstance {
    private final Optional<ContextAwarePredicate> player;

    public AbstractCriterionTriggerInstance(Optional<ContextAwarePredicate> param0) {
        this.player = param0;
    }

    @Override
    public Optional<ContextAwarePredicate> playerPredicate() {
        return this.player;
    }

    @Override
    public JsonObject serializeToJson() {
        JsonObject var0 = new JsonObject();
        this.player.ifPresent(param1 -> var0.add("player", param1.toJson()));
        return var0;
    }
}
