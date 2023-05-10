package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public KilledByCrossbowTrigger.TriggerInstance createInstance(JsonObject param0, ContextAwarePredicate param1, DeserializationContext param2) {
        ContextAwarePredicate[] var0 = EntityPredicate.fromJsonArray(param0, "victims", param2);
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("unique_entity_types"));
        return new KilledByCrossbowTrigger.TriggerInstance(param1, var0, var1);
    }

    public void trigger(ServerPlayer param0, Collection<Entity> param1) {
        List<LootContext> var0 = Lists.newArrayList();
        Set<EntityType<?>> var1 = Sets.newHashSet();

        for(Entity var2 : param1) {
            var1.add(var2.getType());
            var0.add(EntityPredicate.createContext(param0, var2));
        }

        this.trigger(param0, param2 -> param2.matches(var0, var1.size()));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final ContextAwarePredicate[] victims;
        private final MinMaxBounds.Ints uniqueEntityTypes;

        public TriggerInstance(ContextAwarePredicate param0, ContextAwarePredicate[] param1, MinMaxBounds.Ints param2) {
            super(KilledByCrossbowTrigger.ID, param0);
            this.victims = param1;
            this.uniqueEntityTypes = param2;
        }

        public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(EntityPredicate.Builder... param0) {
            ContextAwarePredicate[] var0 = new ContextAwarePredicate[param0.length];

            for(int var1 = 0; var1 < param0.length; ++var1) {
                EntityPredicate.Builder var2 = param0[var1];
                var0[var1] = EntityPredicate.wrap(var2.build());
            }

            return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, var0, MinMaxBounds.Ints.ANY);
        }

        public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(MinMaxBounds.Ints param0) {
            ContextAwarePredicate[] var0 = new ContextAwarePredicate[0];
            return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, var0, param0);
        }

        public boolean matches(Collection<LootContext> param0, int param1) {
            if (this.victims.length > 0) {
                List<LootContext> var0 = Lists.newArrayList(param0);

                for(ContextAwarePredicate var1 : this.victims) {
                    boolean var2 = false;
                    Iterator<LootContext> var3 = var0.iterator();

                    while(var3.hasNext()) {
                        LootContext var4 = var3.next();
                        if (var1.matches(var4)) {
                            var3.remove();
                            var2 = true;
                            break;
                        }
                    }

                    if (!var2) {
                        return false;
                    }
                }
            }

            return this.uniqueEntityTypes.matches(param1);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext param0) {
            JsonObject var0 = super.serializeToJson(param0);
            var0.add("victims", ContextAwarePredicate.toJson(this.victims, param0));
            var0.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
            return var0;
        }
    }
}
