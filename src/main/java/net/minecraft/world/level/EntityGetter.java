package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface EntityGetter {
    List<Entity> getEntities(@Nullable Entity var1, AABB var2, @Nullable Predicate<? super Entity> var3);

    <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> var1, AABB var2, @Nullable Predicate<? super T> var3);

    default <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> param0, AABB param1, @Nullable Predicate<? super T> param2) {
        return this.getEntitiesOfClass(param0, param1, param2);
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
                if (!var0.removed
                    && var0.blocksBuilding
                    && (param0 == null || !var0.isPassengerOfSameVehicle(param0))
                    && Shapes.joinIsNotEmpty(param1, Shapes.create(var0.getBoundingBox()), BooleanOp.AND)) {
                    return false;
                }
            }

            return true;
        }
    }

    default <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> param0, AABB param1) {
        return this.getEntitiesOfClass(param0, param1, EntitySelector.NO_SPECTATORS);
    }

    default <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> param0, AABB param1) {
        return this.getLoadedEntitiesOfClass(param0, param1, EntitySelector.NO_SPECTATORS);
    }

    default Stream<VoxelShape> getEntityCollisions(@Nullable Entity param0, AABB param1, Predicate<Entity> param2) {
        if (param1.getSize() < 1.0E-7) {
            return Stream.empty();
        } else {
            AABB var0 = param1.inflate(1.0E-7);
            return this.getEntities(param0, var0)
                .stream()
                .filter(param2)
                .filter(param1x -> param0 == null || !param0.isPassengerOfSameVehicle(param1x))
                .flatMap(param1x -> Stream.of(param1x.getCollideBox(), param0 == null ? null : param0.getCollideAgainstBox(param1x)))
                .filter(Objects::nonNull)
                .filter(var0::intersects)
                .map(Shapes::create);
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
        return this.getNearestEntity(this.getEntitiesOfClass(param0, param6, null), param1, param2, param3, param4, param5);
    }

    @Nullable
    default <T extends LivingEntity> T getNearestLoadedEntity(
        Class<? extends T> param0, TargetingConditions param1, @Nullable LivingEntity param2, double param3, double param4, double param5, AABB param6
    ) {
        return this.getNearestEntity(this.getLoadedEntitiesOfClass(param0, param6, null), param1, param2, param3, param4, param5);
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

    default <T extends LivingEntity> List<T> getNearbyEntities(Class<? extends T> param0, TargetingConditions param1, LivingEntity param2, AABB param3) {
        List<T> var0 = this.getEntitiesOfClass(param0, param3, null);
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
