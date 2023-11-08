package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
    private final ProblemReporter reporter;
    private final LootContextParamSet params;
    private final LootDataResolver resolver;
    private final Set<LootDataId<?>> visitedElements;

    public ValidationContext(ProblemReporter param0, LootContextParamSet param1, LootDataResolver param2) {
        this(param0, param1, param2, Set.of());
    }

    private ValidationContext(ProblemReporter param0, LootContextParamSet param1, LootDataResolver param2, Set<LootDataId<?>> param3) {
        this.reporter = param0;
        this.params = param1;
        this.resolver = param2;
        this.visitedElements = param3;
    }

    public ValidationContext forChild(String param0) {
        return new ValidationContext(this.reporter.forChild(param0), this.params, this.resolver, this.visitedElements);
    }

    public ValidationContext enterElement(String param0, LootDataId<?> param1) {
        ImmutableSet<LootDataId<?>> var0 = ImmutableSet.<LootDataId<?>>builder().addAll(this.visitedElements).add(param1).build();
        return new ValidationContext(this.reporter.forChild(param0), this.params, this.resolver, var0);
    }

    public boolean hasVisitedElement(LootDataId<?> param0) {
        return this.visitedElements.contains(param0);
    }

    public void reportProblem(String param0) {
        this.reporter.report(param0);
    }

    public void validateUser(LootContextUser param0) {
        this.params.validateUser(this, param0);
    }

    public LootDataResolver resolver() {
        return this.resolver;
    }

    public ValidationContext setParams(LootContextParamSet param0) {
        return new ValidationContext(this.reporter, param0, this.resolver, this.visitedElements);
    }
}
