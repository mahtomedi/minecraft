package net.minecraft.commands.arguments.selector;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class EntitySelector {
    private final int maxResults;
    private final boolean includesEntities;
    private final boolean worldLimited;
    private final Predicate<Entity> predicate;
    private final MinMaxBounds.Floats range;
    private final Function<Vec3, Vec3> position;
    @Nullable
    private final AABB aabb;
    private final BiConsumer<Vec3, List<? extends Entity>> order;
    private final boolean currentEntity;
    @Nullable
    private final String playerName;
    @Nullable
    private final UUID entityUUID;
    @Nullable
    private final EntityType<?> type;
    private final boolean usesSelector;

    public EntitySelector(
        int param0,
        boolean param1,
        boolean param2,
        Predicate<Entity> param3,
        MinMaxBounds.Floats param4,
        Function<Vec3, Vec3> param5,
        @Nullable AABB param6,
        BiConsumer<Vec3, List<? extends Entity>> param7,
        boolean param8,
        @Nullable String param9,
        @Nullable UUID param10,
        @Nullable EntityType<?> param11,
        boolean param12
    ) {
        this.maxResults = param0;
        this.includesEntities = param1;
        this.worldLimited = param2;
        this.predicate = param3;
        this.range = param4;
        this.position = param5;
        this.aabb = param6;
        this.order = param7;
        this.currentEntity = param8;
        this.playerName = param9;
        this.entityUUID = param10;
        this.type = param11;
        this.usesSelector = param12;
    }

    public int getMaxResults() {
        return this.maxResults;
    }

    public boolean includesEntities() {
        return this.includesEntities;
    }

    public boolean isSelfSelector() {
        return this.currentEntity;
    }

    public boolean isWorldLimited() {
        return this.worldLimited;
    }

    private void checkPermissions(CommandSourceStack param0) throws CommandSyntaxException {
        if (this.usesSelector && !param0.hasPermission(2)) {
            throw EntityArgument.ERROR_SELECTORS_NOT_ALLOWED.create();
        }
    }

    public Entity findSingleEntity(CommandSourceStack param0) throws CommandSyntaxException {
        this.checkPermissions(param0);
        List<? extends Entity> var0 = this.findEntities(param0);
        if (var0.isEmpty()) {
            throw EntityArgument.NO_ENTITIES_FOUND.create();
        } else if (var0.size() > 1) {
            throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
        } else {
            return var0.get(0);
        }
    }

    public List<? extends Entity> findEntities(CommandSourceStack param0) throws CommandSyntaxException {
        this.checkPermissions(param0);
        if (!this.includesEntities) {
            return this.findPlayers(param0);
        } else if (this.playerName != null) {
            ServerPlayer var0 = param0.getServer().getPlayerList().getPlayerByName(this.playerName);
            return (List<? extends Entity>)(var0 == null ? Collections.emptyList() : Lists.newArrayList(var0));
        } else if (this.entityUUID != null) {
            for(ServerLevel var1 : param0.getServer().getAllLevels()) {
                Entity var2 = var1.getEntity(this.entityUUID);
                if (var2 != null) {
                    return Lists.newArrayList(var2);
                }
            }

            return Collections.emptyList();
        } else {
            Vec3 var3 = this.position.apply(param0.getPosition());
            Predicate<Entity> var4 = this.getPredicate(var3);
            if (this.currentEntity) {
                return (List<? extends Entity>)(param0.getEntity() != null && var4.test(param0.getEntity())
                    ? Lists.newArrayList(param0.getEntity())
                    : Collections.emptyList());
            } else {
                List<Entity> var5 = Lists.newArrayList();
                if (this.isWorldLimited()) {
                    this.addEntities(var5, param0.getLevel(), var3, var4);
                } else {
                    for(ServerLevel var6 : param0.getServer().getAllLevels()) {
                        this.addEntities(var5, var6, var3, var4);
                    }
                }

                return this.sortAndLimit(var3, var5);
            }
        }
    }

    private void addEntities(List<Entity> param0, ServerLevel param1, Vec3 param2, Predicate<Entity> param3) {
        if (this.aabb != null) {
            param0.addAll(param1.getEntities(this.type, this.aabb.move(param2), param3));
        } else {
            param0.addAll(param1.getEntities(this.type, param3));
        }

    }

    public ServerPlayer findSinglePlayer(CommandSourceStack param0) throws CommandSyntaxException {
        this.checkPermissions(param0);
        List<ServerPlayer> var0 = this.findPlayers(param0);
        if (var0.size() != 1) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        } else {
            return var0.get(0);
        }
    }

    public List<ServerPlayer> findPlayers(CommandSourceStack param0) throws CommandSyntaxException {
        this.checkPermissions(param0);
        if (this.playerName != null) {
            ServerPlayer var0 = param0.getServer().getPlayerList().getPlayerByName(this.playerName);
            return (List<ServerPlayer>)(var0 == null ? Collections.emptyList() : Lists.newArrayList(var0));
        } else if (this.entityUUID != null) {
            ServerPlayer var1 = param0.getServer().getPlayerList().getPlayer(this.entityUUID);
            return (List<ServerPlayer>)(var1 == null ? Collections.emptyList() : Lists.newArrayList(var1));
        } else {
            Vec3 var2 = this.position.apply(param0.getPosition());
            Predicate<Entity> var3 = this.getPredicate(var2);
            if (this.currentEntity) {
                if (param0.getEntity() instanceof ServerPlayer) {
                    ServerPlayer var4 = (ServerPlayer)param0.getEntity();
                    if (var3.test(var4)) {
                        return Lists.newArrayList(var4);
                    }
                }

                return Collections.emptyList();
            } else {
                List<ServerPlayer> var5;
                if (this.isWorldLimited()) {
                    var5 = param0.getLevel().getPlayers(var3::test);
                } else {
                    var5 = Lists.newArrayList();

                    for(ServerPlayer var7 : param0.getServer().getPlayerList().getPlayers()) {
                        if (var3.test(var7)) {
                            var5.add(var7);
                        }
                    }
                }

                return this.sortAndLimit(var2, var5);
            }
        }
    }

    private Predicate<Entity> getPredicate(Vec3 param0) {
        Predicate<Entity> var0 = this.predicate;
        if (this.aabb != null) {
            AABB var1 = this.aabb.move(param0);
            var0 = var0.and(param1 -> var1.intersects(param1.getBoundingBox()));
        }

        if (!this.range.isAny()) {
            var0 = var0.and(param1 -> this.range.matchesSqr(param1.distanceToSqr(param0)));
        }

        return var0;
    }

    private <T extends Entity> List<T> sortAndLimit(Vec3 param0, List<T> param1) {
        if (param1.size() > 1) {
            this.order.accept(param0, param1);
        }

        return param1.subList(0, Math.min(this.maxResults, param1.size()));
    }

    public static Component joinNames(List<? extends Entity> param0) {
        return ComponentUtils.formatList(param0, Entity::getDisplayName);
    }
}
