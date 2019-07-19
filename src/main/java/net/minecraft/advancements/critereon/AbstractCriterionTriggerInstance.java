package net.minecraft.advancements.critereon;

import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;

public class AbstractCriterionTriggerInstance implements CriterionTriggerInstance {
    private final ResourceLocation criterion;

    public AbstractCriterionTriggerInstance(ResourceLocation param0) {
        this.criterion = param0;
    }

    @Override
    public ResourceLocation getCriterion() {
        return this.criterion;
    }

    @Override
    public String toString() {
        return "AbstractCriterionInstance{criterion=" + this.criterion + '}';
    }
}
