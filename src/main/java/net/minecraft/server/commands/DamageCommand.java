package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class DamageCommand {
    private static final SimpleCommandExceptionType ERROR_INVULNERABLE = new SimpleCommandExceptionType(Component.translatable("commands.damage.invulnerable"));

    public static void register(CommandDispatcher<CommandSourceStack> param0, CommandBuildContext param1) {
        param0.register(
            Commands.literal("damage")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("target", EntityArgument.entity())
                        .then(
                            Commands.argument("amount", FloatArgumentType.floatArg(0.0F))
                                .executes(
                                    param0x -> damage(
                                            param0x.getSource(),
                                            EntityArgument.getEntity(param0x, "target"),
                                            FloatArgumentType.getFloat(param0x, "amount"),
                                            param0x.getSource().getLevel().damageSources().generic()
                                        )
                                )
                                .then(
                                    ((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument(
                                                    "damageType", ResourceArgument.resource(param1, Registries.DAMAGE_TYPE)
                                                )
                                                .executes(
                                                    param0x -> damage(
                                                            (CommandSourceStack)param0x.getSource(),
                                                            EntityArgument.getEntity(param0x, "target"),
                                                            FloatArgumentType.getFloat(param0x, "amount"),
                                                            new DamageSource(ResourceArgument.getResource(param0x, "damageType", Registries.DAMAGE_TYPE))
                                                        )
                                                ))
                                            .then(
                                                Commands.literal("at")
                                                    .then(
                                                        Commands.argument("location", Vec3Argument.vec3())
                                                            .executes(
                                                                param0x -> damage(
                                                                        param0x.getSource(),
                                                                        EntityArgument.getEntity(param0x, "target"),
                                                                        FloatArgumentType.getFloat(param0x, "amount"),
                                                                        new DamageSource(
                                                                            ResourceArgument.getResource(param0x, "damageType", Registries.DAMAGE_TYPE),
                                                                            Vec3Argument.getVec3(param0x, "location")
                                                                        )
                                                                    )
                                                            )
                                                    )
                                            ))
                                        .then(
                                            Commands.literal("by")
                                                .then(
                                                    Commands.argument("entity", EntityArgument.entity())
                                                        .executes(
                                                            param0x -> damage(
                                                                    param0x.getSource(),
                                                                    EntityArgument.getEntity(param0x, "target"),
                                                                    FloatArgumentType.getFloat(param0x, "amount"),
                                                                    new DamageSource(
                                                                        ResourceArgument.getResource(param0x, "damageType", Registries.DAMAGE_TYPE),
                                                                        EntityArgument.getEntity(param0x, "entity")
                                                                    )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("from")
                                                                .then(
                                                                    Commands.argument("cause", EntityArgument.entity())
                                                                        .executes(
                                                                            param0x -> damage(
                                                                                    param0x.getSource(),
                                                                                    EntityArgument.getEntity(param0x, "target"),
                                                                                    FloatArgumentType.getFloat(param0x, "amount"),
                                                                                    new DamageSource(
                                                                                        ResourceArgument.getResource(
                                                                                            param0x, "damageType", Registries.DAMAGE_TYPE
                                                                                        ),
                                                                                        EntityArgument.getEntity(param0x, "entity"),
                                                                                        EntityArgument.getEntity(param0x, "cause")
                                                                                    )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int damage(CommandSourceStack param0, Entity param1, float param2, DamageSource param3) throws CommandSyntaxException {
        if (param1.hurt(param3, param2)) {
            param0.sendSuccess(Component.translatable("commands.damage.success", param2, param1.getDisplayName()), true);
            return 1;
        } else {
            throw ERROR_INVULNERABLE.create();
        }
    }
}
