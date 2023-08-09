package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractCriterionTriggerInstance implements CriterionTriggerInstance {
    private final ResourceLocation criterion;
    private final Optional<ContextAwarePredicate> player;

    public AbstractCriterionTriggerInstance(ResourceLocation param0, Optional<ContextAwarePredicate> param1) {
        this.criterion = param0;
        this.player = param1;
    }

    @Override
    public ResourceLocation getCriterion() {
        return this.criterion;
    }

    protected Optional<ContextAwarePredicate> getPlayerPredicate() {
        return this.player;
    }

    @Override
    public JsonObject serializeToJson() {
        JsonObject var0 = new JsonObject();
        this.player.ifPresent(param1 -> var0.add("player", param1.toJson()));
        return var0;
    }

    @Override
    public String toString() {
        return "AbstractCriterionInstance{criterion=" + this.criterion + "}";
    }
}
