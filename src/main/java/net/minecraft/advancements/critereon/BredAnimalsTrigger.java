package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.loot.LootContext;

public class BredAnimalsTrigger extends SimpleCriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
    @Override
    public Codec<BredAnimalsTrigger.TriggerInstance> codec() {
        return BredAnimalsTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, Animal param1, Animal param2, @Nullable AgeableMob param3) {
        LootContext var0 = EntityPredicate.createContext(param0, param1);
        LootContext var1 = EntityPredicate.createContext(param0, param2);
        LootContext var2 = param3 != null ? EntityPredicate.createContext(param0, param3) : null;
        this.trigger(param0, param3x -> param3x.matches(var0, var1, var2));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player,
        Optional<ContextAwarePredicate> parent,
        Optional<ContextAwarePredicate> partner,
        Optional<ContextAwarePredicate> child
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<BredAnimalsTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(BredAnimalsTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "parent").forGetter(BredAnimalsTrigger.TriggerInstance::parent),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "partner").forGetter(BredAnimalsTrigger.TriggerInstance::partner),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "child").forGetter(BredAnimalsTrigger.TriggerInstance::child)
                    )
                    .apply(param0, BredAnimalsTrigger.TriggerInstance::new)
        );

        public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals() {
            return CriteriaTriggers.BRED_ANIMALS
                .createCriterion(new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        }

        public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(EntityPredicate.Builder param0) {
            return CriteriaTriggers.BRED_ANIMALS
                .createCriterion(
                    new BredAnimalsTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(param0)))
                );
        }

        public static Criterion<BredAnimalsTrigger.TriggerInstance> bredAnimals(
            Optional<EntityPredicate> param0, Optional<EntityPredicate> param1, Optional<EntityPredicate> param2
        ) {
            return CriteriaTriggers.BRED_ANIMALS
                .createCriterion(
                    new BredAnimalsTrigger.TriggerInstance(
                        Optional.empty(), EntityPredicate.wrap(param0), EntityPredicate.wrap(param1), EntityPredicate.wrap(param2)
                    )
                );
        }

        public boolean matches(LootContext param0, LootContext param1, @Nullable LootContext param2) {
            if (!this.child.isPresent() || param2 != null && this.child.get().matches(param2)) {
                return matches(this.parent, param0) && matches(this.partner, param1) || matches(this.parent, param1) && matches(this.partner, param0);
            } else {
                return false;
            }
        }

        private static boolean matches(Optional<ContextAwarePredicate> param0, LootContext param1) {
            return param0.isEmpty() || param0.get().matches(param1);
        }

        @Override
        public void validate(CriterionValidator param0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(param0);
            param0.validateEntity(this.parent, ".parent");
            param0.validateEntity(this.partner, ".partner");
            param0.validateEntity(this.child, ".child");
        }
    }
}
