package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

public abstract class SimpleCriterionTrigger<T extends CriterionTriggerInstance> implements CriterionTrigger<T> {
    private final Map<PlayerAdvancements, Set<CriterionTrigger.Listener<T>>> players = Maps.newIdentityHashMap();

    @Override
    public final void addPlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<T> param1) {
        this.players.computeIfAbsent(param0, param0x -> Sets.newHashSet()).add(param1);
    }

    @Override
    public final void removePlayerListener(PlayerAdvancements param0, CriterionTrigger.Listener<T> param1) {
        Set<CriterionTrigger.Listener<T>> var0 = this.players.get(param0);
        if (var0 != null) {
            var0.remove(param1);
            if (var0.isEmpty()) {
                this.players.remove(param0);
            }
        }

    }

    @Override
    public final void removePlayerListeners(PlayerAdvancements param0) {
        this.players.remove(param0);
    }

    protected void trigger(PlayerAdvancements param0, Predicate<T> param1) {
        Set<CriterionTrigger.Listener<T>> var0 = this.players.get(param0);
        if (var0 != null) {
            List<CriterionTrigger.Listener<T>> var1 = null;

            for(CriterionTrigger.Listener<T> var2 : var0) {
                if (param1.test(var2.getTriggerInstance())) {
                    if (var1 == null) {
                        var1 = Lists.newArrayList();
                    }

                    var1.add(var2);
                }
            }

            if (var1 != null) {
                for(CriterionTrigger.Listener<T> var3 : var1) {
                    var3.run(param0);
                }
            }

        }
    }

    protected void trigger(PlayerAdvancements param0) {
        Set<CriterionTrigger.Listener<T>> var0 = this.players.get(param0);
        if (var0 != null && !var0.isEmpty()) {
            for(CriterionTrigger.Listener<T> var1 : ImmutableSet.copyOf(var0)) {
                var1.run(param0);
            }
        }

    }
}
