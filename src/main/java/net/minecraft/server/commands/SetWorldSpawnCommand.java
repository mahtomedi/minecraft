package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.AngleArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<CommandSourceStack> param0) {
        param0.register(
            Commands.literal("setworldspawn")
                .requires(param0x -> param0x.hasPermission(2))
                .executes(param0x -> setSpawn(param0x.getSource(), new BlockPos(param0x.getSource().getPosition()), 0.0F))
                .then(
                    Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(param0x -> setSpawn(param0x.getSource(), BlockPosArgument.getSpawnablePos(param0x, "pos"), 0.0F))
                        .then(
                            Commands.argument("angle", AngleArgument.angle())
                                .executes(
                                    param0x -> setSpawn(
                                            param0x.getSource(), BlockPosArgument.getSpawnablePos(param0x, "pos"), AngleArgument.getAngle(param0x, "angle")
                                        )
                                )
                        )
                )
        );
    }

    private static int setSpawn(CommandSourceStack param0, BlockPos param1, float param2) {
        param0.getLevel().setDefaultSpawnPos(param1, param2);
        param0.sendSuccess(Component.translatable("commands.setworldspawn.success", param1.getX(), param1.getY(), param1.getZ(), param2), true);
        return 1;
    }
}
