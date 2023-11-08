package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
    @Override
    public Codec<ChanneledLightningTrigger.TriggerInstance> codec() {
        return ChanneledLightningTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, Collection<? extends Entity> param1) {
        List<LootContext> var0 = param1.stream().map(param1x -> EntityPredicate.createContext(param0, param1x)).collect(Collectors.toList());
        this.trigger(param0, param1x -> param1x.matches(var0));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ChanneledLightningTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player")
                            .forGetter(ChanneledLightningTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC.listOf(), "victims", List.of())
                            .forGetter(ChanneledLightningTrigger.TriggerInstance::victims)
                    )
                    .apply(param0, ChanneledLightningTrigger.TriggerInstance::new)
        );

        public static Criterion<ChanneledLightningTrigger.TriggerInstance> channeledLightning(EntityPredicate.Builder... param0) {
            return CriteriaTriggers.CHANNELED_LIGHTNING
                .createCriterion(new ChanneledLightningTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(param0)));
        }

        public boolean matches(Collection<? extends LootContext> param0) {
            for(ContextAwarePredicate var0 : this.victims) {
                boolean var1 = false;

                for(LootContext var2 : param0) {
                    if (var0.matches(var2)) {
                        var1 = true;
                        break;
                    }
                }

                if (!var1) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public void validate(CriterionValidator param0) {
            SimpleCriterionTrigger.SimpleInstance.super.validate(param0);
            param0.validateEntities(this.victims, ".victims");
        }
    }
}
