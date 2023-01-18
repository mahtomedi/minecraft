package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class RideCommand {
    private static final DynamicCommandExceptionType ERROR_NOT_RIDING = new DynamicCommandExceptionType(
        param0 -> Component.translatable("commands.ride.not_riding", param0)
    );
    private static final Dynamic2CommandExceptionType ERROR_ALREADY_RIDING = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.ride.already_riding", param0, param1)
    );
    private static final Dynamic2CommandExceptionType ERROR_MOUNT_FAILED = new Dynamic2CommandExceptionType(
        (param0, param1) -> Component.translatable("commands.ride.mount.failure.generic", param0, param1)
    );
    private static final SimpleCommandExceptionType ERROR_MOUNTING_PLAYER = new SimpleCommandExceptionType(
        Component.translatable("commands.ride.mount.failure.cant_ride_players")
    );
    private static final SimpleCommandExceptionType ERROR_MOUNTING_LOOP = new SimpleCommandExceptionType(
        Component.translatable("commands.ride.mount.failure.loop")
    );

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("ride")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("target", EntityArgument.entity())
                        .then(
                            Commands.literal("mount")
                                .then(
                                    Commands.argument("vehicle", EntityArgument.entity())
                                        .executes(
                                            param0x -> mount(
                                                    param0x.getSource(),
                                                    EntityArgument.getEntity(param0x, "target"),
                                                    EntityArgument.getEntity(param0x, "vehicle")
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("dismount").executes(param0x -> dismount(param0x.getSource(), EntityArgument.getEntity(param0x, "target"))))
                )
        );
    }

    private static int mount(CommandSourceStack param0, Entity param1, Entity param2) throws CommandSyntaxException {
        Entity var0 = param1.getVehicle();
        if (var0 != null) {
            throw ERROR_ALREADY_RIDING.create(param1.getDisplayName(), var0.getDisplayName());
        } else if (param2.getType() == EntityType.PLAYER) {
            throw ERROR_MOUNTING_PLAYER.create();
        } else if (param1.getSelfAndPassengers().anyMatch(param1x -> param1x == param2)) {
            throw ERROR_MOUNTING_LOOP.create();
        } else if (!param1.startRiding(param2, true)) {
            throw ERROR_MOUNT_FAILED.create(param1.getDisplayName(), param2.getDisplayName());
        } else {
            param0.sendSuccess(Component.translatable("commands.ride.mount.success", param1.getDisplayName(), param2.getDisplayName()), true);
            return 1;
        }
    }

    private static int dismount(CommandSourceStack param0, Entity param1) throws CommandSyntaxException {
        Entity var0 = param1.getVehicle();
        if (var0 == null) {
            throw ERROR_NOT_RIDING.create(param1.getDisplayName());
        } else {
            param1.stopRiding();
            param0.sendSuccess(Component.translatable("commands.ride.dismount.success", param1.getDisplayName(), var0.getDisplayName()), true);
            return 1;
        }
    }
}
