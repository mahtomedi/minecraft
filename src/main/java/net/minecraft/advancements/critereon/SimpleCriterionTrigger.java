package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

public abstract class SimpleCriterionTrigger<T extends AbstractCriterionTriggerInstance> implements CriterionTrigger<T> {
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

    protected abstract T createInstance(JsonObject var1, ContextAwarePredicate var2, DeserializationContext var3);

    public final T createInstance(JsonObject param0, DeserializationContext param1) {
        ContextAwarePredicate var0 = EntityPredicate.fromJson(param0, "player", param1);
        return this.createInstance(param0, var0, param1);
    }

    protected void trigger(ServerPlayer param0, Predicate<T> param1) {
        PlayerAdvancements var0 = param0.getAdvancements();
        Set<CriterionTrigger.Listener<T>> var1 = this.players.get(var0);
        if (var1 != null && !var1.isEmpty()) {
            LootContext var2 = EntityPredicate.createContext(param0, param0);
            List<CriterionTrigger.Listener<T>> var3 = null;

            for(CriterionTrigger.Listener<T> var4 : var1) {
                T var5 = var4.getTriggerInstance();
                if (param1.test(var5) && var5.getPlayerPredicate().matches(var2)) {
                    if (var3 == null) {
                        var3 = Lists.newArrayList();
                    }

                    var3.add(var4);
                }
            }

            if (var3 != null) {
                for(CriterionTrigger.Listener<T> var6 : var3) {
                    var6.run(var0);
                }
            }

        }
    }
}
