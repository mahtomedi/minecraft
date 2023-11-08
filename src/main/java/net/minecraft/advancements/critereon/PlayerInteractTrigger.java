package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
    @Override
    public Codec<PlayerInteractTrigger.TriggerInstance> codec() {
        return PlayerInteractTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, ItemStack param1, Entity param2) {
        LootContext var0 = EntityPredicate.createContext(param0, param2);
        this.trigger(param0, param2x -> param2x.matches(param1, var0));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<PlayerInteractTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PlayerInteractTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(PlayerInteractTrigger.TriggerInstance::item),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "entity").forGetter(PlayerInteractTrigger.TriggerInstance::entity)
                    )
                    .apply(param0, PlayerInteractTrigger.TriggerInstance::new)
        );

        public static Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(
            Optional<ContextAwarePredicate> param0, ItemPredicate.Builder param1, Optional<ContextAwarePredicate> param2
        ) {
            return CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY
                .createCriterion(new PlayerInteractTrigger.TriggerInstance(param0, Optional.of(param1.build()), param2));
        }

        public static Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(ItemPredicate.Builder param0, Optional<ContextAwarePredicate> param1) {
            return itemUsedOnEntity(Optional.empty(), param0, param1);
        }

        public boolean matches(ItemStack param0, LootContext param1) {
            if (this.item.isPresent() && !this.item.get().matches(param0)) {
                return false;
            } else {
                return this.entity.isEmpty() || this.entity.get().matches(param1);
            }
        }

        @Override
        public void validate(CriterionValidator param0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(param0);
            param0.validateEntity(this.entity, ".entity");
        }
    }
}
