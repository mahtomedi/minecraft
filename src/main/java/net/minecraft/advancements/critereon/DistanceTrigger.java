package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger extends SimpleCriterionTrigger<DistanceTrigger.TriggerInstance> {
    @Override
    public Codec<DistanceTrigger.TriggerInstance> codec() {
        return DistanceTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, Vec3 param1) {
        Vec3 var0 = param0.position();
        this.trigger(param0, param3 -> param3.matches(param0.serverLevel(), param1, var0));
    }

    public static record TriggerInstance(
        Optional<ContextAwarePredicate> player, Optional<LocationPredicate> startPosition, Optional<DistancePredicate> distance
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<DistanceTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(DistanceTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(LocationPredicate.CODEC, "start_position").forGetter(DistanceTrigger.TriggerInstance::startPosition),
                        ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(DistanceTrigger.TriggerInstance::distance)
                    )
                    .apply(param0, DistanceTrigger.TriggerInstance::new)
        );

        public static Criterion<DistanceTrigger.TriggerInstance> fallFromHeight(
            EntityPredicate.Builder param0, DistancePredicate param1, LocationPredicate.Builder param2
        ) {
            return CriteriaTriggers.FALL_FROM_HEIGHT
                .createCriterion(
                    new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(param0)), Optional.of(param2.build()), Optional.of(param1))
                );
        }

        public static Criterion<DistanceTrigger.TriggerInstance> rideEntityInLava(EntityPredicate.Builder param0, DistancePredicate param1) {
            return CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER
                .createCriterion(new DistanceTrigger.TriggerInstance(Optional.of(EntityPredicate.wrap(param0)), Optional.empty(), Optional.of(param1)));
        }

        public static Criterion<DistanceTrigger.TriggerInstance> travelledThroughNether(DistancePredicate param0) {
            return CriteriaTriggers.NETHER_TRAVEL.createCriterion(new DistanceTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(param0)));
        }

        public boolean matches(ServerLevel param0, Vec3 param1, Vec3 param2) {
            if (this.startPosition.isPresent() && !this.startPosition.get().matches(param0, param1.x, param1.y, param1.z)) {
                return false;
            } else {
                return !this.distance.isPresent() || this.distance.get().matches(param1.x, param1.y, param1.z, param2.x, param2.y, param2.z);
            }
        }
    }
}
