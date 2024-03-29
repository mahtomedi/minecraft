package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SetSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("spawnpoint")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(
                    param0x -> setSpawn(
                            param0x.getSource(),
                            Collections.singleton(param0x.getSource().getPlayerOrException()),
                            BlockPos.containing(param0x.getSource().getPosition()),
                            0.0F
                        )
                )
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .executes(
                            param0x -> setSpawn(
                                    param0x.getSource(),
                                    EntityArgument.getPlayers(param0x, "targets"),
                                    BlockPos.containing(param0x.getSource().getPosition()),
                                    0.0F
                                )
                        )
                        .then(
                            Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(
                                    param0x -> setSpawn(
                                            param0x.getSource(),
                                            EntityArgument.getPlayers(param0x, "targets"),
                                            BlockPosArgument.getSpawnablePos(param0x, "pos"),
                                            0.0F
                                        )
                                )
                                .then(
                                    Commands.argument("angle", AngleArgument.angle())
                                        .executes(
                                            param0x -> setSpawn(
                                                    param0x.getSource(),
                                                    EntityArgument.getPlayers(param0x, "targets"),
                                                    BlockPosArgument.getSpawnablePos(param0x, "pos"),
                                                    AngleArgument.getAngle(param0x, "angle")
                                                )
                                        )
                                )
                        )
                )
        );
    }

    private static int setSpawn(CommandSourceStack param0, Collection<ServerPlayer> param1, BlockPos param2, float param3) {
        ResourceKey<Level> var0 = param0.getLevel().dimension();

        for(ServerPlayer var1 : param1) {
            var1.setRespawnPosition(var0, param2, param3, true, false);
        }

        String var2 = var0.location().toString();
        if (param1.size() == 1) {
            param0.sendSuccess(
                () -> Component.translatable(
                        "commands.spawnpoint.success.single",
                        param2.getX(),
                        param2.getY(),
                        param2.getZ(),
                        param3,
                        var2,
                        param1.iterator().next().getDisplayName()
                    ),
                true
            );
        } else {
            param0.sendSuccess(
                () -> Component.translatable("commands.spawnpoint.success.multiple", param2.getX(), param2.getY(), param2.getZ(), param3, var2, param1.size()),
                true
            );
        }

        return param1.size();
    }
}
