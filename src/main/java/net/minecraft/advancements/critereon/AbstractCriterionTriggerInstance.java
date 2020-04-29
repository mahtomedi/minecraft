package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractCriterionTriggerInstance implements CriterionTriggerInstance {
    private final ResourceLocation criterion;
    private final EntityPredicate.Composite player;

    public AbstractCriterionTriggerInstance(ResourceLocation param0, EntityPredicate.Composite param1) {
        this.criterion = param0;
        this.player = param1;
    }

    @Override
    public ResourceLocation getCriterion() {
        return this.criterion;
    }

    protected EntityPredicate.Composite getPlayerPredicate() {
        return this.player;
    }

    @Override
    public JsonObject serializeToJson(SerializationContext param0) {
        JsonObject var0 = new JsonObject();
        var0.add("player", this.player.toJson(param0));
        return var0;
    }

    @Override
    public String toString() {
        return "AbstractCriterionInstance{criterion=" + this.criterion + '}';
    }
}
