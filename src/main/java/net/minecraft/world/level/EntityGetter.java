package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableList.Builder;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface EntityGetter {
    List<Entity> getEntities(@Nullable Entity var1, AABB var2, Predicate<? super Entity> var3);

    <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> var1, AABB var2, Predicate<? super T> var3);

    default <T extends Entity> List<T> getEntitiesOfClass(Class<T> param0, AABB param1, Predicate<? super T> param2) {
        return this.getEntities(EntityTypeTest.forClass(param0), param1, param2);
    }

    List<? extends Player> players();

    default List<Entity> getEntities(@Nullable Entity param0, AABB param1) {
        return this.getEntities(param0, param1, EntitySelector.NO_SPECTATORS);
    }

    default boolean isUnobstructed(@Nullable Entity param0, VoxelShape param1) {
        if (param1.isEmpty()) {
            return true;
        } else {
            for(Entity var0 : this.getEntities(param0, param1.bounds())) {
                if (!var0.isRemoved()
                    && var0.blocksBuilding
                    && (param0 == null || !var0.isPassengerOfSameVehicle(param0))
                    && Shapes.joinIsNotEmpty(param1, Shapes.create(var0.getBoundingBox()), BooleanOp.AND)) {
                    return false;
                }
            }

            return true;
        }
    }

    default <T extends Entity> List<T> getEntitiesOfClass(Class<T> param0, AABB param1) {
        return this.getEntitiesOfClass(param0, param1, EntitySelector.NO_SPECTATORS);
    }

    default List<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1) {
        if (param1.getSize() < 1.0E-7) {
            return List.of();
        } else {
            Predicate<Entity> var0 = param0 == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(param0::canCollideWith);
            List<Entity> var1 = this.getEntities(param0, param1.inflate(1.0E-7), var0);
            if (var1.isEmpty()) {
                return List.of();
            } else {
                Builder<VoxelShape> var2 = ImmutableList.builderWithExpectedSize(var1.size());

                for(Entity var3 : var1) {
                    var2.add(Shapes.create(var3.getBoundingBox()));
                }

                return var2.build();
            }
        }
    }

    @Nullable
    default Player getNearestPlayer(double param0, double param1, double param2, double param3, @Nullable Predicate<Entity> param4) {
        double var0 = -1.0;
        Player var1 = null;

        for(Player var2 : this.players()) {
            if (param4 == null || param4.test(var2)) {
                double var3 = var2.distanceToSqr(param0, param1, param2);
                if ((param3 < 0.0 || var3 < param3 * param3) && (var0 == -1.0 || var3 < var0)) {
                    var0 = var3;
                    var1 = var2;
                }
            }
        }

        return var1;
    }

    @Nullable
    default Player getNearestPlayer(Entity param0, double param1) {
        return this.getNearestPlayer(param0.getX(), param0.getY(), param0.getZ(), param1, false);
    }

    @Nullable
    default Player getNearestPlayer(double param0, double param1, double param2, double param3, boolean param4) {
        Predicate<Entity> var0 = param4 ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS;
        return this.getNearestPlayer(param0, param1, param2, param3, var0);
    }

    default boolean hasNearbyAlivePlayer(double param0, double param1, double param2, double param3) {
        for(Player var0 : this.players()) {
            if (EntitySelector.NO_SPECTATORS.test(var0) && EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(var0)) {
                double var1 = var0.distanceToSqr(param0, param1, param2);
                if (param3 < 0.0 || var1 < param3 * param3) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    default Player getNearestPlayer(TargetingConditions param0, LivingEntity param1) {
        return this.getNearestEntity(this.players(), param0, param1, param1.getX(), param1.getY(), param1.getZ());
    }

    @Nullable
    default Player getNearestPlayer(TargetingConditions param0, LivingEntity param1, double param2, double param3, double param4) {
        return this.getNearestEntity(this.players(), param0, param1, param2, param3, param4);
    }

    @Nullable
    default Player getNearestPlayer(TargetingConditions param0, double param1, double param2, double param3) {
        return this.getNearestEntity(this.players(), param0, null, param1, param2, param3);
    }

    @Nullable
    default <T extends LivingEntity> T getNearestEntity(
        Class<? extends T> param0, TargetingConditions param1, @Nullable LivingEntity param2, double param3, double param4, double param5, AABB param6
    ) {
        return this.getNearestEntity(this.getEntitiesOfClass(param0, param6, param0x -> true), param1, param2, param3, param4, param5);
    }

    @Nullable
    default <T extends LivingEntity> T getNearestEntity(
        List<? extends T> param0, TargetingConditions param1, @Nullable LivingEntity param2, double param3, double param4, double param5
    ) {
        double var0 = -1.0;
        T var1 = null;

        for(T var2 : param0) {
            if (param1.test(param2, var2)) {
                double var3 = var2.distanceToSqr(param3, param4, param5);
                if (var0 == -1.0 || var3 < var0) {
                    var0 = var3;
                    var1 = var2;
                }
            }
        }

        return var1;
    }

    default List<Player> getNearbyPlayers(TargetingConditions param0, LivingEntity param1, AABB param2) {
        List<Player> var0 = Lists.newArrayList();

        for(Player var1 : this.players()) {
            if (param2.contains(var1.getX(), var1.getY(), var1.getZ()) && param0.test(param1, var1)) {
                var0.add(var1);
            }
        }

        return var0;
    }

    default <T extends LivingEntity> List<T> getNearbyEntities(Class<T> param0, TargetingConditions param1, LivingEntity param2, AABB param3) {
        List<T> var0 = this.getEntitiesOfClass(param0, param3, param0x -> true);
        List<T> var1 = Lists.newArrayList();

        for(T var2 : var0) {
            if (param1.test(param2, var2)) {
                var1.add(var2);
            }
        }

        return var1;
    }

    @Nullable
    default Player getPlayerByUUID(UUID param0) {
        for(int var0 = 0; var0 < this.players().size(); ++var0) {
            Player var1 = this.players().get(var0);
            if (param0.equals(var1.getUUID())) {
                return var1;
            }
        }

        return null;
    }
}
