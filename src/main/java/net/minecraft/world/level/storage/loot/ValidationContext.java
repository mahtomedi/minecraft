package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

public class ValidationContext {
    private final Multimap<String, String> problems;
    private final Supplier<String> context;
    private final LootContextParamSet params;
    private final LootDataResolver resolver;
    private final Set<LootDataId<?>> visitedElements;
    @Nullable
    private String contextCache;

    public ValidationContext(LootContextParamSet param0, LootDataResolver param1) {
        this(HashMultimap.create(), () -> "", param0, param1, ImmutableSet.of());
    }

    public ValidationContext(
        Multimap<String, String> param0, Supplier<String> param1, LootContextParamSet param2, LootDataResolver param3, Set<LootDataId<?>> param4
    ) {
        this.problems = param0;
        this.context = param1;
        this.params = param2;
        this.resolver = param3;
        this.visitedElements = param4;
    }

    private String getContext() {
        if (this.contextCache == null) {
            this.contextCache = this.context.get();
        }

        return this.contextCache;
    }

    public void reportProblem(String param0) {
        this.problems.put(this.getContext(), param0);
    }

    public ValidationContext forChild(String param0) {
        return new ValidationContext(this.problems, () -> this.getContext() + param0, this.params, this.resolver, this.visitedElements);
    }

    public ValidationContext enterElement(String param0, LootDataId<?> param1) {
        ImmutableSet<LootDataId<?>> var0 = ImmutableSet.<LootDataId<?>>builder().addAll(this.visitedElements).add(param1).build();
        return new ValidationContext(this.problems, () -> this.getContext() + param0, this.params, this.resolver, var0);
    }

    public boolean hasVisitedElement(LootDataId<?> param0) {
        return this.visitedElements.contains(param0);
    }

    public Multimap<String, String> getProblems() {
        return ImmutableMultimap.copyOf(this.problems);
    }

    public void validateUser(LootContextUser param0) {
        this.params.validateUser(this, param0);
    }

    public LootDataResolver resolver() {
        return this.resolver;
    }

    public ValidationContext setParams(LootContextParamSet param0) {
        return new ValidationContext(this.problems, this.context, param0, this.resolver, this.visitedElements);
    }
}
