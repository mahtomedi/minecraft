package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ParticleCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(Component.translatable("commands.particle.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("particle")
                .requires(param0x -> param0x.hasPermission(2))
                .then(
                    Commands.argument("name", ParticleArgument.particle())
                        .executes(
                            param0x -> sendParticles(
                                    param0x.getSource(),
                                    ParticleArgument.getParticle(param0x, "name"),
                                    param0x.getSource().getPosition(),
                                    Vec3.ZERO,
                                    0.0F,
                                    0,
                                    false,
                                    param0x.getSource().getServer().getPlayerList().getPlayers()
                                )
                        )
                        .then(
                            Commands.argument("pos", Vec3Argument.vec3())
                                .executes(
                                    param0x -> sendParticles(
                                            param0x.getSource(),
                                            ParticleArgument.getParticle(param0x, "name"),
                                            Vec3Argument.getVec3(param0x, "pos"),
                                            Vec3.ZERO,
                                            0.0F,
                                            0,
                                            false,
                                            param0x.getSource().getServer().getPlayerList().getPlayers()
                                        )
                                )
                                .then(
                                    Commands.argument("delta", Vec3Argument.vec3(false))
                                        .then(
                                            Commands.argument("speed", FloatArgumentType.floatArg(0.0F))
                                                .then(
                                                    Commands.argument("count", IntegerArgumentType.integer(0))
                                                        .executes(
                                                            param0x -> sendParticles(
                                                                    param0x.getSource(),
                                                                    ParticleArgument.getParticle(param0x, "name"),
                                                                    Vec3Argument.getVec3(param0x, "pos"),
                                                                    Vec3Argument.getVec3(param0x, "delta"),
                                                                    FloatArgumentType.getFloat(param0x, "speed"),
                                                                    IntegerArgumentType.getInteger(param0x, "count"),
                                                                    false,
                                                                    param0x.getSource().getServer().getPlayerList().getPlayers()
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("force")
                                                                .executes(
                                                                    param0x -> sendParticles(
                                                                            param0x.getSource(),
                                                                            ParticleArgument.getParticle(param0x, "name"),
                                                                            Vec3Argument.getVec3(param0x, "pos"),
                                                                            Vec3Argument.getVec3(param0x, "delta"),
                                                                            FloatArgumentType.getFloat(param0x, "speed"),
                                                                            IntegerArgumentType.getInteger(param0x, "count"),
                                                                            true,
                                                                            param0x.getSource().getServer().getPlayerList().getPlayers()
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.argument("viewers", EntityArgument.players())
                                                                        .executes(
                                                                            param0x -> sendParticles(
                                                                                    param0x.getSource(),
                                                                                    ParticleArgument.getParticle(param0x, "name"),
                                                                                    Vec3Argument.getVec3(param0x, "pos"),
                                                                                    Vec3Argument.getVec3(param0x, "delta"),
                                                                                    FloatArgumentType.getFloat(param0x, "speed"),
                                                                                    IntegerArgumentType.getInteger(param0x, "count"),
                                                                                    true,
                                                                                    EntityArgument.getPlayers(param0x, "viewers")
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                            Commands.literal("normal")
                                                                .executes(
                                                                    param0x -> sendParticles(
                                                                            param0x.getSource(),
                                                                            ParticleArgument.getParticle(param0x, "name"),
                                                                            Vec3Argument.getVec3(param0x, "pos"),
                                                                            Vec3Argument.getVec3(param0x, "delta"),
                                                                            FloatArgumentType.getFloat(param0x, "speed"),
                                                                            IntegerArgumentType.getInteger(param0x, "count"),
                                                                            false,
                                                                            param0x.getSource().getServer().getPlayerList().getPlayers()
                                                                        )
                                                                )
                                                                .then(
                                                                    Commands.argument("viewers", EntityArgument.players())
                                                                        .executes(
                                                                            param0x -> sendParticles(
                                                                                    param0x.getSource(),
                                                                                    ParticleArgument.getParticle(param0x, "name"),
                                                                                    Vec3Argument.getVec3(param0x, "pos"),
                                                                                    Vec3Argument.getVec3(param0x, "delta"),
                                                                                    FloatArgumentType.getFloat(param0x, "speed"),
                                                                                    IntegerArgumentType.getInteger(param0x, "count"),
                                                                                    false,
                                                                                    EntityArgument.getPlayers(param0x, "viewers")
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

    private static int sendParticles(
        CommandSourceStack param0, ParticleOptions param1, Vec3 param2, Vec3 param3, float param4, int param5, boolean param6, Collection<ServerPlayer> param7
    ) throws CommandSyntaxException {
        int var0 = 0;

        for(ServerPlayer var1 : param7) {
            if (param0.getLevel().sendParticles(var1, param1, param6, param2.x, param2.y, param2.z, param5, param3.x, param3.y, param3.z, (double)param4)) {
                ++var0;
            }
        }

        if (var0 == 0) {
            throw ERROR_FAILED.create();
        } else {
            param0.sendSuccess(Component.translatable("commands.particle.success", Registry.PARTICLE_TYPE.getKey(param1.getType()).toString()), true);
            return var0;
        }
    }
}
