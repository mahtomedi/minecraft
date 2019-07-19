package net.minecraft.world.level.storage.loot.parameters;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import net.minecraft.world.level.storage.loot.LootContextUser;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;

public class LootContextParamSet {
    private final Set<LootContextParam<?>> required;
    private final Set<LootContextParam<?>> all;

    private LootContextParamSet(Set<LootContextParam<?>> param0, Set<LootContextParam<?>> param1) {
        this.required = ImmutableSet.copyOf(param0);
        this.all = ImmutableSet.copyOf(Sets.union(param0, param1));
    }

    public Set<LootContextParam<?>> getRequired() {
        return this.required;
    }

    public Set<LootContextParam<?>> getAllowed() {
        return this.all;
    }

    @Override
    public String toString() {
        return "[" + Joiner.on(", ").join(this.all.stream().map(param0 -> (this.required.contains(param0) ? "!" : "") + param0.getName()).iterator()) + "]";
    }

    public void validateUser(LootTableProblemCollector param0, LootContextUser param1) {
        Set<LootContextParam<?>> var0 = param1.getReferencedContextParams();
        Set<LootContextParam<?>> var1 = Sets.difference(var0, this.all);
        if (!var1.isEmpty()) {
            param0.reportProblem("Parameters " + var1 + " are not provided in this context");
        }

    }

    public static class Builder {
        private final Set<LootContextParam<?>> required = Sets.newIdentityHashSet();
        private final Set<LootContextParam<?>> optional = Sets.newIdentityHashSet();

        public LootContextParamSet.Builder required(LootContextParam<?> param0) {
            if (this.optional.contains(param0)) {
                throw new IllegalArgumentException("Parameter " + param0.getName() + " is already optional");
            } else {
                this.required.add(param0);
                return this;
            }
        }

        public LootContextParamSet.Builder optional(LootContextParam<?> param0) {
            if (this.required.contains(param0)) {
                throw new IllegalArgumentException("Parameter " + param0.getName() + " is already required");
            } else {
                this.optional.add(param0);
                return this;
            }
        }

        public LootContextParamSet build() {
            return new LootContextParamSet(this.required, this.optional);
        }
    }
}
