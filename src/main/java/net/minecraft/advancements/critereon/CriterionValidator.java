package net.minecraft.advancements.critereon;

import java.util.List;
import java.util.Optional;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class CriterionValidator {
    private final ProblemReporter reporter;
    private final LootDataResolver lootData;

    public CriterionValidator(ProblemReporter param0, LootDataResolver param1) {
        this.reporter = param0;
        this.lootData = param1;
    }

    public void validateEntity(Optional<ContextAwarePredicate> param0, String param1) {
        param0.ifPresent(param1x -> this.validateEntity(param1x, param1));
    }

    public void validateEntities(List<ContextAwarePredicate> param0, String param1) {
        this.validate(param0, LootContextParamSets.ADVANCEMENT_ENTITY, param1);
    }

    public void validateEntity(ContextAwarePredicate param0, String param1) {
        this.validate(param0, LootContextParamSets.ADVANCEMENT_ENTITY, param1);
    }

    public void validate(ContextAwarePredicate param0, LootContextParamSet param1, String param2) {
        param0.validate(new ValidationContext(this.reporter.forChild(param2), param1, this.lootData));
    }

    public void validate(List<ContextAwarePredicate> param0, LootContextParamSet param1, String param2) {
        for(int var0 = 0; var0 < param0.size(); ++var0) {
            ContextAwarePredicate var1 = param0.get(var0);
            var1.validate(new ValidationContext(this.reporter.forChild(param2 + "[" + var0 + "]"), param1, this.lootData));
        }

    }
}
