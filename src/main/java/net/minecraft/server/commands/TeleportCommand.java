package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class TeleportCommand {
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(
        Component.translatable("commands.teleport.invalidPosition")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        LiteralCommandNode<CommandSourceStack> var0 = param0.register(
            Commands.literal("teleport")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("location", Vec3Argument.vec3())
                        .executes(
                            param0x -> teleportToPos(
                                    param0x.getSource(),
                                    Collections.singleton(param0x.getSource().getEntityOrException()),
                                    param0x.getSource().getLevel(),
                                    Vec3Argument.getCoordinates(param0x, "location"),
                                    WorldCoordinates.current(),
                                    null
                                )
                        )
                )
                .then(
                    Commands.argument("destination", EntityArgument.entity())
                        .executes(
                            param0x -> teleportToEntity(
                                    param0x.getSource(),
                                    Collections.singleton(param0x.getSource().getEntityOrException()),
                                    EntityArgument.getEntity(param0x, "destination")
                                )
                        )
                )
                .then(
                    Commands.argument("targets", EntityArgument.entities())
                        .then(
                            Commands.argument("location", Vec3Argument.vec3())
                                .executes(
                                    param0x -> teleportToPos(
                                            param0x.getSource(),
                                            EntityArgument.getEntities(param0x, "targets"),
                                            param0x.getSource().getLevel(),
                                            Vec3Argument.getCoordinates(param0x, "location"),
                                            null,
                                            null
                                        )
                                )
                                .then(
                                    Commands.argument("rotation", RotationArgument.rotation())
                                        .executes(
                                            param0x -> teleportToPos(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntities(param0x, "targets"),
                                                    param0x.getSource().getLevel(),
                                                    Vec3Argument.getCoordinates(param0x, "location"),
                                                    RotationArgument.getRotation(param0x, "rotation"),
                                                    null
                                                )
                                        )
                                )
                                .then(
                                    Commands.literal("facing")
                                        .then(
                                            Commands.literal("entity")
                                                .then(
                                                    Commands.argument("facingEntity", EntityArgument.entity())
                                                        .executes(
                                                            param0x -> teleportToPos(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntities(param0x, "targets"),
                                                                    param0x.getSource().getLevel(),
                                                                    Vec3Argument.getCoordinates(param0x, "location"),
                                                                    null,
                                                                    new TeleportCommand.LookAt(
                                                                        EntityArgument.getEntity(param0x, "facingEntity"), EntityAnchorArgument.Anchor.FEET
                                                                    )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.argument("facingAnchor", EntityAnchorArgument.anchor())
                                                                .executes(
                                                                    param0x -> teleportToPos(
                                                                            param0x.getSource(),
                                                                            EntityArgument.getEntities(param0x, "targets"),
                                                                            param0x.getSource().getLevel(),
                                                                            Vec3Argument.getCoordinates(param0x, "location"),
                                                                            null,
                                                                            new TeleportCommand.LookAt(
                                                                                EntityArgument.getEntity(param0x, "facingEntity"),
                                                                                EntityAnchorArgument.getAnchor(param0x, "facingAnchor")
                                                                            )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                        .then(
                                            Commands.argument("facingLocation", Vec3Argument.vec3())
                                                .executes(
                                                    param0x -> teleportToPos(
                                                            param0x.getSource(),
                                                            EntityArgument.getEntities(param0x, "targets"),
                                                            param0x.getSource().getLevel(),
                                                            Vec3Argument.getCoordinates(param0x, "location"),
                                                            null,
                                                            new TeleportCommand.LookAt(Vec3Argument.getVec3(param0x, "facingLocation"))
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                            Commands.argument("destination", EntityArgument.entity())
                                .executes(
                                    param0x -> teleportToEntity(
                                            param0x.getSource(),
                                            EntityArgument.getEntities(param0x, "targets"),
                                            EntityArgument.getEntity(param0x, "destination")
                                        )
                                )
                        )
                )
        );
        param0.register(Commands.literal("tp").requires(param0x -> param0x.hasPermission(2)).redirect(var0));
    }

    private static int teleportToEntity(CommandSourceStack param0, Collection<? extends Entity> param1, Entity param2) throws CommandSyntaxException {
        for(Entity var0 : param1) {
            performTeleport(
                param0,
                var0,
                (ServerLevel)param2.level,
                param2.getX(),
                param2.getY(),
                param2.getZ(),
                EnumSet.noneOf(RelativeMovement.class),
                param2.getYRot(),
                param2.getXRot(),
                null
            );
        }

        if (param1.size() == 1) {
            param0.sendSuccess(
                Component.translatable("commands.teleport.success.entity.single", param1.iterator().next().getDisplayName(), param2.getDisplayName()), true
            );
        } else {
            param0.sendSuccess(Component.translatable("commands.teleport.success.entity.multiple", param1.size(), param2.getDisplayName()), true);
        }

        return param1.size();
    }

    private static int teleportToPos(
        CommandSourceStack param0,
        Collection<? extends Entity> param1,
        ServerLevel param2,
        Coordinates param3,
        @Nullable Coordinates param4,
        @Nullable TeleportCommand.LookAt param5
    ) throws CommandSyntaxException {
        Vec3 var0 = param3.getPosition(param0);
        Vec2 var1 = param4 == null ? null : param4.getRotation(param0);
        Set<RelativeMovement> var2 = EnumSet.noneOf(RelativeMovement.class);
        if (param3.isXRelative()) {
            var2.add(RelativeMovement.X);
        }

        if (param3.isYRelative()) {
            var2.add(RelativeMovement.Y);
        }

        if (param3.isZRelative()) {
            var2.add(RelativeMovement.Z);
        }

        if (param4 == null) {
            var2.add(RelativeMovement.X_ROT);
            var2.add(RelativeMovement.Y_ROT);
        } else {
            if (param4.isXRelative()) {
                var2.add(RelativeMovement.X_ROT);
            }

            if (param4.isYRelative()) {
                var2.add(RelativeMovement.Y_ROT);
            }
        }

        for(Entity var3 : param1) {
            if (param4 == null) {
                performTeleport(param0, var3, param2, var0.x, var0.y, var0.z, var2, var3.getYRot(), var3.getXRot(), param5);
            } else {
                performTeleport(param0, var3, param2, var0.x, var0.y, var0.z, var2, var1.y, var1.x, param5);
            }
        }

        if (param1.size() == 1) {
            param0.sendSuccess(
                Component.translatable(
                    "commands.teleport.success.location.single",
                    param1.iterator().next().getDisplayName(),
                    formatDouble(var0.x),
                    formatDouble(var0.y),
                    formatDouble(var0.z)
                ),
                true
            );
        } else {
            param0.sendSuccess(
                Component.translatable(
                    "commands.teleport.success.location.multiple", param1.size(), formatDouble(var0.x), formatDouble(var0.y), formatDouble(var0.z)
                ),
                true
            );
        }

        return param1.size();
    }

    private static String formatDouble(double param0) {
        return String.format(Locale.ROOT, "%f", param0);
    }

    private static void performTeleport(
        CommandSourceStack param0,
        Entity param1,
        ServerLevel param2,
        double param3,
        double param4,
        double param5,
        Set<RelativeMovement> param6,
        float param7,
        float param8,
        @Nullable TeleportCommand.LookAt param9
    ) throws CommandSyntaxException {
        BlockPos var0 = new BlockPos(param3, param4, param5);
        if (!Level.isInSpawnableBounds(var0)) {
            throw INVALID_POSITION.create();
        } else {
            float var1 = Mth.wrapDegrees(param7);
            float var2 = Mth.wrapDegrees(param8);
            if (param1.teleportTo(param2, param3, param4, param5, param6, var1, var2)) {
                if (param9 != null) {
                    param9.perform(param0, param1);
                }

                if (!(param1 instanceof LivingEntity var3) || !var3.isFallFlying()) {
                    param1.setDeltaMovement(param1.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                    param1.setOnGround(true);
                }

                if (param1 instanceof PathfinderMob var4) {
                    var4.getNavigation().stop();
                }

            }
        }
    }

    static class LookAt {
        private final Vec3 position;
        private final Entity entity;
        private final EntityAnchorArgument.Anchor anchor;

        public LookAt(Entity param0, EntityAnchorArgument.Anchor param1) {
            this.entity = param0;
            this.anchor = param1;
            this.position = param1.apply(param0);
        }

        public LookAt(Vec3 param0) {
            this.entity = null;
            this.position = param0;
            this.anchor = null;
        }

        public void perform(CommandSourceStack param0, Entity param1) {
            if (this.entity != null) {
                if (param1 instanceof ServerPlayer) {
                    ((ServerPlayer)param1).lookAt(param0.getAnchor(), this.entity, this.anchor);
                } else {
                    param1.lookAt(param0.getAnchor(), this.position);
                }
            } else {
                param1.lookAt(param0.getAnchor(), this.position);
            }

        }
    }
}
