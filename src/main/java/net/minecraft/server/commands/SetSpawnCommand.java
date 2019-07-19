package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;

public class SetSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("spawnpoint")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(
                    param0x -> setSpawn(
                            param0x.getSource(),
                            Collections.singleton(param0x.getSource().getPlayerOrException()),
                            new BlockPos(param0x.getSource().getPosition())
                        )
                )
                .then(
                    Commands.argument("targets", EntityArgument.players())
                        .executes(
                            param0x -> setSpawn(
                                    param0x.getSource(), EntityArgument.getPlayers(param0x, "targets"), new BlockPos(param0x.getSource().getPosition())
                                )
                        )
                        .then(
                            Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(
                                    param0x -> setSpawn(
                                            param0x.getSource(),
                                            EntityArgument.getPlayers(param0x, "targets"),
                                            BlockPosArgument.getOrLoadBlockPos(param0x, "pos")
                                        )
                                )
                        )
                )
        );
    }

    private static int setSpawn(CommandSourceStack param0, Collection<ServerPlayer> param1, BlockPos param2) {
        for(ServerPlayer var0 : param1) {
            var0.setRespawnPosition(param2, true);
        }

        if (param1.size() == 1) {
            param0.sendSuccess(
                new TranslatableComponent(
                    "commands.spawnpoint.success.single", param2.getX(), param2.getY(), param2.getZ(), param1.iterator().next().getDisplayName()
                ),
                true
            );
        } else {
            param0.sendSuccess(
                new TranslatableComponent("commands.spawnpoint.success.multiple", param2.getX(), param2.getY(), param2.getZ(), param1.size()), true
            );
        }

        return param1.size();
    }
}
