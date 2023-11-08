package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
    @Override
    public Codec<KilledByCrossbowTrigger.TriggerInstance> codec() {
        return KilledByCrossbowTrigger.TriggerInstance.CODEC;
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

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, MinMaxBounds.Ints uniqueEntityTypes)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<KilledByCrossbowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(KilledByCrossbowTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC.listOf(), "victims", List.of())
                            .forGetter(KilledByCrossbowTrigger.TriggerInstance::victims),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "unique_entity_types", MinMaxBounds.Ints.ANY)
                            .forGetter(KilledByCrossbowTrigger.TriggerInstance::uniqueEntityTypes)
                    )
                    .apply(param0, KilledByCrossbowTrigger.TriggerInstance::new)
        );

        public static Criterion<KilledByCrossbowTrigger.TriggerInstance> crossbowKilled(EntityPredicate.Builder... param0) {
            return CriteriaTriggers.KILLED_BY_CROSSBOW
                .createCriterion(new KilledByCrossbowTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(param0), MinMaxBounds.Ints.ANY));
        }

        public static Criterion<KilledByCrossbowTrigger.TriggerInstance> crossbowKilled(MinMaxBounds.Ints param0) {
            return CriteriaTriggers.KILLED_BY_CROSSBOW.createCriterion(new KilledByCrossbowTrigger.TriggerInstance(Optional.empty(), List.of(), param0));
        }

        public boolean matches(Collection<LootContext> param0, int param1) {
            if (!this.victims.isEmpty()) {
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
        public void validate(CriterionValidator param0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(param0);
            param0.validateEntities(this.victims, ".victims");
        }
    }
}
