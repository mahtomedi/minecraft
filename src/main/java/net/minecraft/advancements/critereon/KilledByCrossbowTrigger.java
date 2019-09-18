package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public KilledByCrossbowTrigger.TriggerInstance createInstance(JsonObject param0, JsonDeserializationContext param1) {
        EntityPredicate[] var0 = EntityPredicate.fromJsonArray(param0.get("victims"));
        MinMaxBounds.Ints var1 = MinMaxBounds.Ints.fromJson(param0.get("unique_entity_types"));
        return new KilledByCrossbowTrigger.TriggerInstance(var0, var1);
    }

    public void trigger(ServerPlayer param0, Collection<Entity> param1, int param2) {
        this.trigger(param0.getAdvancements(), param3 -> param3.matches(param0, param1, param2));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final EntityPredicate[] victims;
        private final MinMaxBounds.Ints uniqueEntityTypes;

        public TriggerInstance(EntityPredicate[] param0, MinMaxBounds.Ints param1) {
            super(KilledByCrossbowTrigger.ID);
            this.victims = param0;
            this.uniqueEntityTypes = param1;
        }

        public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(EntityPredicate.Builder... param0) {
            EntityPredicate[] var0 = new EntityPredicate[param0.length];

            for(int var1 = 0; var1 < param0.length; ++var1) {
                EntityPredicate.Builder var2 = param0[var1];
                var0[var1] = var2.build();
            }

            return new KilledByCrossbowTrigger.TriggerInstance(var0, MinMaxBounds.Ints.ANY);
        }

        public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(MinMaxBounds.Ints param0) {
            EntityPredicate[] var0 = new EntityPredicate[0];
            return new KilledByCrossbowTrigger.TriggerInstance(var0, param0);
        }

        public boolean matches(ServerPlayer param0, Collection<Entity> param1, int param2) {
            if (this.victims.length > 0) {
                List<Entity> var0 = Lists.newArrayList(param1);

                for(EntityPredicate var1 : this.victims) {
                    boolean var2 = false;
                    Iterator<Entity> var3 = var0.iterator();

                    while(var3.hasNext()) {
                        Entity var4 = var3.next();
                        if (var1.matches(param0, var4)) {
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

            if (this.uniqueEntityTypes == MinMaxBounds.Ints.ANY) {
                return true;
            } else {
                Set<EntityType<?>> var5 = Sets.newHashSet();

                for(Entity var6 : param1) {
                    var5.add(var6.getType());
                }

                return this.uniqueEntityTypes.matches(var5.size()) && this.uniqueEntityTypes.matches(param2);
            }
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject var0 = new JsonObject();
            var0.add("victims", EntityPredicate.serializeArrayToJson(this.victims));
            var0.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
            return var0;
        }
    }
}
