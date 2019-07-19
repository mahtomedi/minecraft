package net.minecraft.world.level.storage.loot;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.function.Supplier;

public class LootTableProblemCollector {
    private final Multimap<String, String> problems;
    private final Supplier<String> context;
    private String contextCache;

    public LootTableProblemCollector() {
        this(HashMultimap.create(), () -> "");
    }

    public LootTableProblemCollector(Multimap<String, String> param0, Supplier<String> param1) {
        this.problems = param0;
        this.context = param1;
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

    public LootTableProblemCollector forChild(String param0) {
        return new LootTableProblemCollector(this.problems, () -> this.getContext() + param0);
    }

    public Multimap<String, String> getProblems() {
        return ImmutableMultimap.copyOf(this.problems);
    }
}
