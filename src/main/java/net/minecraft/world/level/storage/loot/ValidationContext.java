package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ValidationContext {
    private final Multimap<String, String> problems;
    private final Supplier<String> context;
    private final LootContextParamSet params;
    private final Function<ResourceLocation, LootItemCondition> conditionResolver;
    private final Set<ResourceLocation> visitedConditions;
    private final Function<ResourceLocation, LootTable> tableResolver;
    private final Set<ResourceLocation> visitedTables;
    private String contextCache;

    public ValidationContext(LootContextParamSet param0, Function<ResourceLocation, LootItemCondition> param1, Function<ResourceLocation, LootTable> param2) {
        this(HashMultimap.create(), () -> "", param0, param1, ImmutableSet.of(), param2, ImmutableSet.of());
    }

    public ValidationContext(
        Multimap<String, String> param0,
        Supplier<String> param1,
        LootContextParamSet param2,
        Function<ResourceLocation, LootItemCondition> param3,
        Set<ResourceLocation> param4,
        Function<ResourceLocation, LootTable> param5,
        Set<ResourceLocation> param6
    ) {
        this.problems = param0;
        this.context = param1;
        this.params = param2;
        this.conditionResolver = param3;
        this.visitedConditions = param4;
        this.tableResolver = param5;
        this.visitedTables = param6;
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
        return new ValidationContext(
            this.problems,
            () -> this.getContext() + param0,
            this.params,
            this.conditionResolver,
            this.visitedConditions,
            this.tableResolver,
            this.visitedTables
        );
    }

    public ValidationContext enterTable(String param0, ResourceLocation param1) {
        ImmutableSet<ResourceLocation> var0 = ImmutableSet.<ResourceLocation>builder().addAll(this.visitedTables).add(param1).build();
        return new ValidationContext(
            this.problems, () -> this.getContext() + param0, this.params, this.conditionResolver, this.visitedConditions, this.tableResolver, var0
        );
    }

    public ValidationContext enterCondition(String param0, ResourceLocation param1) {
        ImmutableSet<ResourceLocation> var0 = ImmutableSet.<ResourceLocation>builder().addAll(this.visitedConditions).add(param1).build();
        return new ValidationContext(
            this.problems, () -> this.getContext() + param0, this.params, this.conditionResolver, var0, this.tableResolver, this.visitedTables
        );
    }

    public boolean hasVisitedTable(ResourceLocation param0) {
        return this.visitedTables.contains(param0);
    }

    public boolean hasVisitedCondition(ResourceLocation param0) {
        return this.visitedConditions.contains(param0);
    }

    public Multimap<String, String> getProblems() {
        return ImmutableMultimap.copyOf(this.problems);
    }

    public void validateUser(LootContextUser param0) {
        this.params.validateUser(this, param0);
    }

    @Nullable
    public LootTable resolveLootTable(ResourceLocation param0) {
        return this.tableResolver.apply(param0);
    }

    @Nullable
    public LootItemCondition resolveCondition(ResourceLocation param0) {
        return this.conditionResolver.apply(param0);
    }

    public ValidationContext setParams(LootContextParamSet param0) {
        return new ValidationContext(
            this.problems, this.context, param0, this.conditionResolver, this.visitedConditions, this.tableResolver, this.visitedTables
        );
    }
}
