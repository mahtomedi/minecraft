package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
    @Override
    public Codec<FishingRodHookedTrigger.TriggerInstance> codec() {
        return FishingRodHookedTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, ItemStack param1, FishingHook param2, Collection<ItemStack> param3) {
        LootContext var0 = EntityPredicate.createContext(param0, (Entity)(param2.getHookedIn() != null ? param2.getHookedIn() : param2));
        this.trigger(param0, param3x -> param3x.matches(param1, var0, param3));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<ItemPredicate> rod, Optional<ContextAwarePredicate> entity, Optional<ItemPredicate> item
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<FishingRodHookedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(FishingRodHookedTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "rod").forGetter(FishingRodHookedTrigger.TriggerInstance::rod),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "entity").forGetter(FishingRodHookedTrigger.TriggerInstance::entity),
                        ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(FishingRodHookedTrigger.TriggerInstance::item)
                    )
                    .apply(param0, FishingRodHookedTrigger.TriggerInstance::new)
        );

        public static Criterion<FishingRodHookedTrigger.TriggerInstance> fishedItem(
            Optional<ItemPredicate> param0, Optional<EntityPredicate> param1, Optional<ItemPredicate> param2
        ) {
            return CriteriaTriggers.FISHING_ROD_HOOKED
                .createCriterion(new FishingRodHookedTrigger.TriggerInstance(Optional.empty(), param0, EntityPredicate.wrap(param1), param2));
        }

        public boolean matches(ItemStack param0, LootContext param1, Collection<ItemStack> param2) {
            if (this.rod.isPresent() && !this.rod.get().matches(param0)) {
                return false;
            } else if (this.entity.isPresent() && !this.entity.get().matches(param1)) {
                return false;
            } else {
                if (this.item.isPresent()) {
                    boolean var0 = false;
                    Entity var1 = param1.getParamOrNull(LootContextParams.THIS_ENTITY);
                    if (var1 instanceof ItemEntity var2 && this.item.get().matches(var2.getItem())) {
                        var0 = true;
                    }

                    for(ItemStack var3 : param2) {
                        if (this.item.get().matches(var3)) {
                            var0 = true;
                            break;
                        }
                    }

                    if (!var0) {
                        return false;
                    }
                }

                return true;
            }
        }

        @Override
        public void validate(CriterionValidator param0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(param0);
            param0.validateEntity(this.entity, ".entity");
        }
    }
}
