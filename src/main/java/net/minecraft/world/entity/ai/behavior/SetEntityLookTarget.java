package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget extends Behavior<LivingEntity> {
    private final Predicate<LivingEntity> predicate;
    private final float maxDistSqr;
    private Optional<LivingEntity> nearestEntityMatchingTest = Optional.empty();

    public SetEntityLookTarget(Tag<EntityType<?>> param0, float param1) {
        this(param1x -> param1x.getType().is(param0), param1);
    }

    public SetEntityLookTarget(MobCategory param0, float param1) {
        this(param1x -> param0.equals(param1x.getType().getCategory()), param1);
    }

    public SetEntityLookTarget(EntityType<?> param0, float param1) {
        this(param1x -> param0.equals(param1x.getType()), param1);
    }

    public SetEntityLookTarget(float param0) {
        this(param0x -> true, param0);
    }

    public SetEntityLookTarget(Predicate<LivingEntity> param0, float param1) {
        super(
            ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET, MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.VALUE_PRESENT
            )
        );
        this.predicate = param0;
        this.maxDistSqr = param1 * param1;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel param0, LivingEntity param1) {
        NearestVisibleLivingEntities var0 = param1.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).get();
        this.nearestEntityMatchingTest = var0.findClosest(this.predicate.and(param1x -> param1x.distanceToSqr(param1) <= (double)this.maxDistSqr));
        return this.nearestEntityMatchingTest.isPresent();
    }

    @Override
    protected void start(ServerLevel param0, LivingEntity param1, long param2) {
        param1.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.nearestEntityMatchingTest.get(), true));
        this.nearestEntityMatchingTest = Optional.empty();
    }
}
