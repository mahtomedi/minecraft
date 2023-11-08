package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger extends SimpleCriterionTrigger<LevitationTrigger.TriggerInstance> {
    @Override
    public Codec<LevitationTrigger.TriggerInstance> codec() {
        return LevitationTrigger.TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer param0, Vec3 param1, int param2) {
        this.trigger(param0, param3 -> param3.matches(param0, param1, param2));
    }

    public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<DistancePredicate> distance, MinMaxBounds.Ints duration)
        implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<LevitationTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(LevitationTrigger.TriggerInstance::player),
                        ExtraCodecs.strictOptionalField(DistancePredicate.CODEC, "distance").forGetter(LevitationTrigger.TriggerInstance::distance),
                        ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "duration", MinMaxBounds.Ints.ANY)
                            .forGetter(LevitationTrigger.TriggerInstance::duration)
                    )
                    .apply(param0, LevitationTrigger.TriggerInstance::new)
        );

        public static Criterion<LevitationTrigger.TriggerInstance> levitated(DistancePredicate param0) {
            return CriteriaTriggers.LEVITATION
                .createCriterion(new LevitationTrigger.TriggerInstance(Optional.empty(), Optional.of(param0), MinMaxBounds.Ints.ANY));
        }

        public boolean matches(ServerPlayer param0, Vec3 param1, int param2) {
            if (this.distance.isPresent() && !this.distance.get().matches(param1.x, param1.y, param1.z, param0.getX(), param0.getY(), param0.getZ())) {
                return false;
            } else {
                return this.duration.matches(param2);
            }
        }
    }
}
